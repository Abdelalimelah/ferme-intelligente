import { useState, useEffect, useCallback, useRef } from 'react';
import { useSensorSocket } from '../../hooks/useSensorSocket';
import { getCapteurs, createCapteur, updateCapteur, deleteCapteur } from '../../api/capteurApi';
import { getParcelles } from '../../api/parcelleApi';
import DataTable from '../../components/ui/DataTable';
import Modal from '../../components/ui/Modal';
import Button from '../../components/ui/Button';
import Input from '../../components/ui/Input';
import Select from '../../components/ui/Select';
import Badge from '../../components/ui/Badge';
import LoadingSpinner from '../../components/ui/LoadingSpinner';
import EmptyState from '../../components/ui/EmptyState';
import {
  Plus, Pencil, Trash2, List, Activity, Radio,
  Thermometer, Droplets, FlaskConical, Wifi
} from 'lucide-react';

const typeIcons = {
  'Température': Thermometer,
  'Temperature': Thermometer,
  'Humidité': Droplets,
  'Humidite': Droplets,
  'pH': FlaskConical,
};

const typeOptions = [
  { value: 'Temperature', label: 'Température' },
  { value: 'Humidite', label: 'Humidité' },
  { value: 'pH', label: 'pH' },
];

const uniteByType = { Temperature: '°C', Humidite: '%', pH: 'pH' };

const emptyForm = { type: 'Temperature', unite: '°C', statut: 'ACTIF', dateInstallation: '', parcelleId: '' };

const LIVE_POLL = 5000;

export default function ManageSensors() {
  const [tab, setTab] = useState('liste');
  const [capteurs, setCapteurs] = useState([]);
  const [parcelles, setParcelles] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  // CRUD modal state
  const [modalOpen, setModalOpen] = useState(false);
  const [editId, setEditId] = useState(null);
  const [form, setForm] = useState(emptyForm);
  const [formError, setFormError] = useState('');

  // Delete confirmation state
  const [deleteTarget, setDeleteTarget] = useState(null);

  const cancelledRef = useRef(false);

  const load = useCallback(() => {
    async function attempt(retry) {
      setLoading(true);
      try {
        const [c, p] = await Promise.all([getCapteurs(), getParcelles()]);
        if (cancelledRef.current) return;
        setCapteurs(c.data);
        setParcelles(p.data);
        setError('');
      } catch {
        if (cancelledRef.current) return;
        if (retry) {
          setTimeout(() => { if (!cancelledRef.current) attempt(false); }, 1500);
          return;
        }
        setError('Erreur lors du chargement des capteurs');
      } finally {
        if (!cancelledRef.current) setLoading(false);
      }
    }
    attempt(true);
  }, []);

  useEffect(() => {
    cancelledRef.current = false;
    load();
    return () => { cancelledRef.current = true; };
  }, [load]);

  // WebSocket: merge live readings into sensor list
  const handleLiveReading = useCallback((reading) => {
    setCapteurs(prev => prev.map(c =>
      c.id === reading.capteurId
        ? { ...c, derniereValeur: reading.valeur, derniereLecture: new Date().toISOString() }
        : c
    ));
  }, []);

  const { connected: wsConnected } = useSensorSocket(
    tab === 'live' ? handleLiveReading : () => {},
  );

  // Polling fallback (only when WebSocket is NOT connected and we are on live tab)
  useEffect(() => {
    if (tab !== 'live' || wsConnected) return;
    const interval = setInterval(() => {
      getCapteurs().then(res => setCapteurs(res.data)).catch(() => {});
    }, LIVE_POLL);
    return () => clearInterval(interval);
  }, [tab, wsConnected]);

  const openCreate = () => { setForm(emptyForm); setEditId(null); setFormError(''); setModalOpen(true); };
  const openEdit = (c) => {
    setForm({
      type: c.type, unite: c.unite || '', statut: c.statut || 'ACTIF',
      dateInstallation: c.dateInstallation || '', parcelleId: c.parcelleId,
    });
    setEditId(c.id); setFormError(''); setModalOpen(true);
  };

  const handleTypeChange = (newType) => {
    setForm(f => ({ ...f, type: newType, unite: uniteByType[newType] || f.unite }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setFormError('');
    if (!form.parcelleId) { setFormError('Veuillez sélectionner une parcelle'); return; }
    try {
      const payload = {
        type: form.type, unite: form.unite, statut: form.statut,
        dateInstallation: form.dateInstallation || null,
        parcelleId: Number(form.parcelleId),
      };
      if (editId) await updateCapteur(editId, payload);
      else await createCapteur(payload);
      setModalOpen(false);
      load();
    } catch (err) {
      setFormError(err.response?.data?.message || 'Erreur lors de l\'enregistrement');
    }
  };

  const confirmDelete = async () => {
    if (!deleteTarget) return;
    try {
      await deleteCapteur(deleteTarget.id);
      setDeleteTarget(null);
      load();
    } catch {
      setError('Erreur lors de la suppression');
      setDeleteTarget(null);
    }
  };

  const columns = [
    {
      header: 'Type',
      accessor: 'type',
      render: row => {
        const Icon = typeIcons[row.type] || Radio;
        return (
          <div className="flex items-center gap-2">
            <Icon className="w-4 h-4 text-sage-dark" />
            <span className="font-medium text-bark">{row.type}</span>
          </div>
        );
      },
    },
    { header: 'Unité', accessor: row => row.unite || '-' },
    { header: 'Parcelle', accessor: 'parcelleNom' },
    { header: 'Statut', accessor: 'statut', render: row => <Badge value={row.statut} /> },
    {
      header: 'Dernière valeur',
      accessor: row => row.derniereValeur != null ? `${row.derniereValeur.toFixed(1)} ${row.unite || ''}` : '-',
    },
    {
      header: 'Dernière mise à jour',
      accessor: row => row.derniereLecture ? new Date(row.derniereLecture).toLocaleString('fr-FR') : '-',
    },
  ];

  if (loading) return <LoadingSpinner />;

  return (
    <div>
      {/* Tabs */}
      <div className="flex gap-1 bg-parchment p-1 rounded-xl mb-5 w-fit">
        <button
          onClick={() => setTab('liste')}
          className={`flex items-center gap-2 px-4 py-2 rounded-lg text-sm font-medium transition-colors cursor-pointer ${
            tab === 'liste' ? 'bg-warm-white text-bark shadow-sm' : 'text-stone hover:text-bark'
          }`}
        >
          <List className="w-4 h-4" />Liste
        </button>
        <button
          onClick={() => setTab('live')}
          className={`flex items-center gap-2 px-4 py-2 rounded-lg text-sm font-medium transition-colors cursor-pointer ${
            tab === 'live' ? 'bg-warm-white text-bark shadow-sm' : 'text-stone hover:text-bark'
          }`}
        >
          <Activity className="w-4 h-4" />Temps réel
        </button>
      </div>

      {error && <div className="bg-terracotta-lt text-terracotta text-sm px-4 py-2.5 rounded-xl mb-4">{error}</div>}

      {tab === 'liste' ? (
        <>
          <div className="flex justify-end mb-4">
            <Button onClick={openCreate}><Plus className="w-4 h-4 mr-1.5 inline" />Ajouter un capteur</Button>
          </div>
          <DataTable
            columns={columns}
            data={capteurs}
            searchPlaceholder="Rechercher un capteur..."
            actions={row => (
              <div className="flex gap-1.5 justify-end">
                <button onClick={() => openEdit(row)} className="p-1.5 rounded-lg hover:bg-sage-mist text-stone hover:text-sage-dark cursor-pointer" title="Modifier"><Pencil className="w-4 h-4" /></button>
                <button onClick={() => setDeleteTarget(row)} className="p-1.5 rounded-lg hover:bg-terracotta-lt text-stone hover:text-terracotta cursor-pointer" title="Supprimer"><Trash2 className="w-4 h-4" /></button>
              </div>
            )}
          />
        </>
      ) : (
        <LiveView capteurs={capteurs} wsConnected={wsConnected} />
      )}

      {/* Create / Edit Modal */}
      <Modal isOpen={modalOpen} onClose={() => setModalOpen(false)} title={editId ? 'Modifier le capteur' : 'Nouveau capteur'}>
        <form onSubmit={handleSubmit} className="space-y-4">
          <Select
            label="Type de capteur"
            value={form.type}
            onChange={e => handleTypeChange(e.target.value)}
            options={typeOptions}
          />
          <div className="grid grid-cols-2 gap-3">
            <Input label="Unité" value={form.unite} onChange={e => setForm({ ...form, unite: e.target.value })} required />
            <Select
              label="Statut"
              value={form.statut}
              onChange={e => setForm({ ...form, statut: e.target.value })}
              options={[{ value: 'ACTIF', label: 'Actif' }, { value: 'INACTIF', label: 'Inactif' }]}
            />
          </div>
          <Select
            label="Parcelle"
            value={form.parcelleId}
            onChange={e => setForm({ ...form, parcelleId: e.target.value })}
            options={[{ value: '', label: 'Sélectionner...' }, ...parcelles.map(p => ({ value: p.id, label: p.nom }))]}
          />
          <Input
            label="Date d'installation"
            type="date"
            value={form.dateInstallation}
            onChange={e => setForm({ ...form, dateInstallation: e.target.value })}
          />
          {formError && <p className="text-sm text-terracotta">{formError}</p>}
          <div className="flex justify-end gap-2 pt-2">
            <Button variant="secondary" type="button" onClick={() => setModalOpen(false)}>Annuler</Button>
            <Button type="submit">{editId ? 'Modifier' : 'Créer'}</Button>
          </div>
        </form>
      </Modal>

      {/* Delete Confirmation Modal */}
      <Modal isOpen={!!deleteTarget} onClose={() => setDeleteTarget(null)} title="Confirmer la suppression">
        <div className="space-y-4">
          <p className="text-sm text-charcoal">
            Voulez-vous vraiment supprimer le capteur <span className="font-semibold text-bark">{deleteTarget?.type}</span> de
            la parcelle <span className="font-semibold text-bark">{deleteTarget?.parcelleNom}</span> ?
            Toutes ses données seront également supprimées.
          </p>
          <div className="flex justify-end gap-2">
            <Button variant="secondary" onClick={() => setDeleteTarget(null)}>Annuler</Button>
            <Button variant="danger" onClick={confirmDelete}>Supprimer</Button>
          </div>
        </div>
      </Modal>
    </div>
  );
}

function LiveView({ capteurs, wsConnected }) {
  if (capteurs.length === 0) return <EmptyState message="Aucun capteur trouvé" icon={Radio} />;

  return (
    <div>
      <div className="flex items-center gap-2 mb-4 text-sm">
        {wsConnected ? (
          <>
            <span className="w-2 h-2 rounded-full bg-sage animate-pulse" />
            <span className="text-sage-dark font-medium">Temps réel — WebSocket connecté</span>
          </>
        ) : (
          <>
            <Wifi className="w-4 h-4 animate-pulse text-stone" />
            <span className="text-stone">Polling toutes les 5s (WebSocket non disponible)</span>
          </>
        )}
      </div>
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {capteurs.map(c => {
          const Icon = typeIcons[c.type] || Radio;
          const isActive = c.statut === 'ACTIF';
          return (
            <div key={c.id} className={`bg-warm-white border border-parchment rounded-2xl p-5 transition-all ${!isActive ? 'opacity-60' : ''}`}>
              <div className="flex items-center justify-between mb-3">
                <div className="flex items-center gap-2.5">
                  <div className={`p-2 rounded-xl ${isActive ? 'bg-sage-mist' : 'bg-terracotta-lt'}`}>
                    <Icon className={`w-5 h-5 ${isActive ? 'text-sage-dark' : 'text-terracotta'}`} />
                  </div>
                  <div>
                    <p className="text-sm font-medium text-bark">{c.type}</p>
                    <p className="text-xs text-stone">{c.parcelleNom}</p>
                  </div>
                </div>
                <span className={`w-2.5 h-2.5 rounded-full ${isActive ? 'bg-sage animate-pulse' : 'bg-terracotta'}`} />
              </div>
              <div className="mt-2">
                <p className="font-heading text-3xl font-semibold text-bark">
                  {c.derniereValeur != null ? c.derniereValeur.toFixed(1) : '-'}
                  <span className="text-sm font-normal text-stone ml-1">{c.unite || ''}</span>
                </p>
                <p className="text-xs text-stone mt-1">
                  {c.derniereLecture ? `Mis à jour: ${new Date(c.derniereLecture).toLocaleTimeString('fr-FR')}` : 'Aucune lecture récente'}
                </p>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
}
