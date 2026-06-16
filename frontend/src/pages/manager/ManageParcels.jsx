import { useState } from 'react';
import { getParcelles, createParcelle, updateParcelle, deleteParcelle } from '../../api/parcelleApi';
import { getFermes } from '../../api/fermeApi';
import { useAsyncData } from '../../hooks/useAsyncData';
import DataTable from '../../components/ui/DataTable';
import Modal from '../../components/ui/Modal';
import Button from '../../components/ui/Button';
import Input from '../../components/ui/Input';
import Select from '../../components/ui/Select';
import LoadingSpinner from '../../components/ui/LoadingSpinner';
import { useNavigate } from 'react-router-dom';
import { Plus, Pencil, Trash2, Eye } from 'lucide-react';

const emptyForm = { nom: '', surface: '', typeCulture: '', coordonneesGps: '', fermeId: '' };
const emptyData = { parcelles: [], fermes: [] };

export default function ManageParcels() {
  const navigate = useNavigate();
  const { data: { parcelles, fermes }, loading, reload } = useAsyncData(
    () => Promise.all([getParcelles(), getFermes()]).then(([p, f]) => ({ parcelles: p.data, fermes: f.data })),
    [],
    { initialData: emptyData },
  );
  const [modalOpen, setModalOpen] = useState(false);
  const [editId, setEditId] = useState(null);
  const [form, setForm] = useState(emptyForm);
  const [error, setError] = useState('');

  const openCreate = () => { setForm(emptyForm); setEditId(null); setError(''); setModalOpen(true); };
  const openEdit = (p) => { setForm({ nom: p.nom, surface: p.surface || '', typeCulture: p.typeCulture || '', coordonneesGps: p.coordonneesGps || '', fermeId: p.fermeId }); setEditId(p.id); setError(''); setModalOpen(true); };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    try {
      const data = { ...form, surface: form.surface ? parseFloat(form.surface) : null };
      if (editId) await updateParcelle(editId, data);
      else await createParcelle(data);
      setModalOpen(false);
      reload();
    } catch (err) {
      setError(err.response?.data?.message || 'Erreur');
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Supprimer cette parcelle ?')) return;
    try { await deleteParcelle(id); reload(); } catch { /* best-effort */ }
  };

  const columns = [
    { header: 'Nom', accessor: 'nom' },
    { header: 'Ferme', accessor: 'fermeNom' },
    { header: 'Culture', accessor: row => row.typeCulture || '-' },
    { header: 'Surface (ha)', accessor: row => row.surface?.toFixed(1) || '-' },
    { header: 'Capteurs', accessor: 'nombreCapteurs' },
  ];

  if (loading) return <LoadingSpinner />;

  return (
    <div>
      <div className="flex justify-end mb-4">
        <Button onClick={openCreate}><Plus className="w-4 h-4 mr-1.5 inline" />Ajouter</Button>
      </div>
      <DataTable
        columns={columns}
        data={parcelles}
        searchPlaceholder="Rechercher une parcelle..."
        onRowClick={row => navigate(`/manager/parcelle/${row.id}`)}
        actions={row => (
          <div className="flex gap-1.5 justify-end" onClick={e => e.stopPropagation()}>
            <button onClick={() => navigate(`/manager/parcelle/${row.id}`)} className="p-1.5 rounded-lg hover:bg-sage-mist text-stone hover:text-sage-dark cursor-pointer" title="Détail"><Eye className="w-4 h-4" /></button>
            <button onClick={() => openEdit(row)} className="p-1.5 rounded-lg hover:bg-sage-mist text-stone hover:text-sage-dark cursor-pointer" title="Modifier"><Pencil className="w-4 h-4" /></button>
            <button onClick={() => handleDelete(row.id)} className="p-1.5 rounded-lg hover:bg-terracotta-lt text-stone hover:text-terracotta cursor-pointer" title="Supprimer"><Trash2 className="w-4 h-4" /></button>
          </div>
        )}
      />
      <Modal isOpen={modalOpen} onClose={() => setModalOpen(false)} title={editId ? 'Modifier Parcelle' : 'Nouvelle Parcelle'}>
        <form onSubmit={handleSubmit} className="space-y-4">
          <Input label="Nom" value={form.nom} onChange={e => setForm({ ...form, nom: e.target.value })} required />
          <Select
            label="Ferme"
            value={form.fermeId}
            onChange={e => setForm({ ...form, fermeId: e.target.value })}
            options={[{ value: '', label: 'Sélectionner...' }, ...fermes.map(f => ({ value: f.id, label: f.nom }))]}
          />
          <div className="grid grid-cols-2 gap-3">
            <Input label="Surface (ha)" type="number" step="0.1" value={form.surface} onChange={e => setForm({ ...form, surface: e.target.value })} />
            <Input label="Type de culture" value={form.typeCulture} onChange={e => setForm({ ...form, typeCulture: e.target.value })} />
          </div>
          <Input label="Coordonnées GPS" value={form.coordonneesGps} onChange={e => setForm({ ...form, coordonneesGps: e.target.value })} />
          {error && <p className="text-sm text-terracotta">{error}</p>}
          <div className="flex justify-end gap-2 pt-2">
            <Button variant="secondary" type="button" onClick={() => setModalOpen(false)}>Annuler</Button>
            <Button type="submit">{editId ? 'Modifier' : 'Créer'}</Button>
          </div>
        </form>
      </Modal>
    </div>
  );
}
