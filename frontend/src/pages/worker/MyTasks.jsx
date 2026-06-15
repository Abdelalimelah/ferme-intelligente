import { useState, useEffect } from 'react';
import { useAuth } from '../../hooks/useAuth';
import { getTachesByAgriculteur, markTacheTerminee, markTacheDemarree } from '../../api/tacheApi';
import Badge from '../../components/ui/Badge';
import Button from '../../components/ui/Button';
import Modal from '../../components/ui/Modal';
import LoadingSpinner from '../../components/ui/LoadingSpinner';
import EmptyState from '../../components/ui/EmptyState';
import { CheckCircle, Play, ListTodo, MapPin, Calendar, User } from 'lucide-react';

export default function MyTasks() {
  const { user } = useAuth();
  const [taches, setTaches] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [filter, setFilter] = useState('ALL');
  const [busyId, setBusyId] = useState(null);
  const [detail, setDetail] = useState(null);

  const load = () => {
    setLoading(true);
    getTachesByAgriculteur(user.id)
      .then(res => { setTaches(res.data); setError(''); })
      .catch(() => setError('Erreur lors du chargement des tâches'))
      .finally(() => setLoading(false));
  };

  useEffect(load, [user.id]);

  const handleStart = async (id, e) => {
    e?.stopPropagation();
    setBusyId(id);
    try { await markTacheDemarree(id); load(); }
    catch { setError('Erreur lors du démarrage de la tâche'); }
    finally { setBusyId(null); }
  };

  const handleFinish = async (id, e) => {
    e?.stopPropagation();
    setBusyId(id);
    try { await markTacheTerminee(id); load(); }
    catch { setError('Erreur lors de la clôture de la tâche'); }
    finally { setBusyId(null); }
  };

  const filtered = filter === 'ALL' ? taches : taches.filter(t => t.statut === filter);

  if (loading) return <LoadingSpinner />;

  return (
    <div>
      <div className="flex gap-2 mb-4">
        {['ALL', 'A_FAIRE', 'EN_COURS', 'TERMINEE'].map(f => (
          <button
            key={f}
            onClick={() => setFilter(f)}
            className={`px-3 py-1.5 rounded-full text-xs font-medium transition-all cursor-pointer ${
              filter === f ? 'bg-sage text-white' : 'bg-warm-white text-stone border border-parchment hover:border-sage'
            }`}
          >
            {f === 'ALL' ? 'Toutes' : f === 'A_FAIRE' ? 'À faire' : f === 'EN_COURS' ? 'En cours' : 'Terminées'}
          </button>
        ))}
      </div>

      {error && <div className="bg-terracotta-lt text-terracotta text-sm px-4 py-2.5 rounded-xl mb-4">{error}</div>}

      {filtered.length === 0 ? <EmptyState message="Aucune tâche dans cette catégorie" icon={ListTodo} /> : (
        <div className="space-y-3">
          {filtered.map(t => (
            <div
              key={t.id}
              onClick={() => setDetail(t)}
              className="bg-warm-white border border-parchment rounded-2xl p-4 cursor-pointer transition-all hover:shadow-md hover:border-sage-light hover:-translate-y-0.5"
            >
              <div className="flex items-start justify-between">
                <div className="flex-1">
                  <div className="flex items-center gap-2 mb-1">
                    <h4 className="text-sm font-medium text-bark">{t.titre}</h4>
                    <Badge value={t.priorite} />
                    <Badge value={t.statut} />
                  </div>
                  {t.description && <p className="text-xs text-stone mt-1 line-clamp-1">{t.description}</p>}
                  <div className="flex items-center gap-3 mt-2 text-xs text-stone">
                    {t.parcelleNom && <span>Parcelle: {t.parcelleNom}</span>}
                    {t.dateEcheance && <span>Échéance: {new Date(t.dateEcheance).toLocaleDateString('fr-FR')}</span>}
                    <span>Par: {t.gestionnaireNom}</span>
                  </div>
                </div>
                <div className="flex gap-1.5 ml-3">
                  {t.statut === 'A_FAIRE' && (
                    <Button variant="secondary" onClick={(e) => handleStart(t.id, e)} disabled={busyId === t.id} className="text-xs px-3 py-1.5">
                      <Play className="w-3.5 h-3.5 mr-1 inline" />{busyId === t.id ? '...' : 'Démarrer'}
                    </Button>
                  )}
                  {t.statut !== 'TERMINEE' && (
                    <Button onClick={(e) => handleFinish(t.id, e)} disabled={busyId === t.id} className="text-xs px-3 py-1.5">
                      <CheckCircle className="w-3.5 h-3.5 mr-1 inline" />{busyId === t.id ? '...' : 'Terminer'}
                    </Button>
                  )}
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Task detail modal */}
      <Modal isOpen={!!detail} onClose={() => setDetail(null)} title={detail?.titre}>
        {detail && (
          <div className="space-y-4">
            <div className="flex items-center gap-2">
              <Badge value={detail.priorite} />
              <Badge value={detail.statut} />
            </div>
            {detail.description && (
              <div>
                <p className="text-xs text-stone mb-1">Description</p>
                <p className="text-sm text-charcoal">{detail.description}</p>
              </div>
            )}
            <div className="grid grid-cols-1 gap-3 text-sm">
              {detail.parcelleNom && (
                <div className="flex items-center gap-2 text-charcoal">
                  <MapPin className="w-4 h-4 text-sage-dark" /> Parcelle: {detail.parcelleNom}
                </div>
              )}
              {detail.dateEcheance && (
                <div className="flex items-center gap-2 text-charcoal">
                  <Calendar className="w-4 h-4 text-sage-dark" /> Échéance: {new Date(detail.dateEcheance).toLocaleDateString('fr-FR')}
                </div>
              )}
              <div className="flex items-center gap-2 text-charcoal">
                <User className="w-4 h-4 text-sage-dark" /> Assignée par: {detail.gestionnaireNom}
              </div>
            </div>
            <div className="flex justify-end gap-2 pt-2 border-t border-parchment">
              {detail.statut === 'A_FAIRE' && (
                <Button variant="secondary" onClick={(e) => { handleStart(detail.id, e); setDetail(null); }}>
                  <Play className="w-4 h-4 mr-1 inline" />Démarrer
                </Button>
              )}
              {detail.statut !== 'TERMINEE' && (
                <Button onClick={(e) => { handleFinish(detail.id, e); setDetail(null); }}>
                  <CheckCircle className="w-4 h-4 mr-1 inline" />Terminer
                </Button>
              )}
            </div>
          </div>
        )}
      </Modal>
    </div>
  );
}
