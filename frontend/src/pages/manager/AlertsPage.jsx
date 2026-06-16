import { useState } from 'react';
import { getAlertes, markAlerteAsRead } from '../../api/alerteApi';
import { useAsyncData } from '../../hooks/useAsyncData';
import Badge from '../../components/ui/Badge';
import Button from '../../components/ui/Button';
import Modal from '../../components/ui/Modal';
import LoadingSpinner from '../../components/ui/LoadingSpinner';
import EmptyState from '../../components/ui/EmptyState';
import { AlertTriangle, CheckCircle, Bell, MapPin, Tag, Clock } from 'lucide-react';

export default function AlertsPage() {
  const { data: alertes, setData: setAlertes, loading, error, setError } = useAsyncData(
    () => getAlertes().then(res => res.data),
    [],
    { initialData: [], errorMessage: 'Erreur lors du chargement des alertes' },
  );
  const [detail, setDetail] = useState(null);

  const handleMarkRead = async (id, e) => {
    e?.stopPropagation();
    try {
      await markAlerteAsRead(id);
      setAlertes(prev => prev.map(a => a.id === id ? { ...a, estLue: true } : a));
      setDetail(d => d && d.id === id ? { ...d, estLue: true } : d);
    } catch {
      setError('Erreur lors du marquage de l\'alerte');
    }
  };

  if (loading) return <LoadingSpinner />;
  if (alertes.length === 0) return <EmptyState message="Aucune alerte" icon={Bell} />;

  return (
    <div className="space-y-3">
      {error && <div className="bg-terracotta-lt text-terracotta text-sm px-4 py-2.5 rounded-xl">{error}</div>}

      {alertes.map(a => (
        <div
          key={a.id}
          onClick={() => setDetail(a)}
          role="button"
          tabIndex={0}
          onKeyDown={(e) => { if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); setDetail(a); } }}
          className={`flex items-start gap-4 p-4 rounded-2xl border transition-all cursor-pointer hover:shadow-md hover:-translate-y-0.5 ${a.estLue ? 'bg-warm-white border-parchment' : 'bg-cream border-sage-light'}`}
        >
          <div className={`p-2 rounded-xl mt-0.5 ${a.niveau === 'CRITIQUE' ? 'bg-terracotta-lt' : a.niveau === 'WARNING' ? 'bg-wheat-light' : 'bg-sage-mist'}`}>
            <AlertTriangle className={`w-4 h-4 ${a.niveau === 'CRITIQUE' ? 'text-terracotta' : a.niveau === 'WARNING' ? 'text-wheat' : 'text-sage-dark'}`} />
          </div>
          <div className="flex-1">
            <div className="flex items-center gap-2 mb-1">
              <Badge value={a.niveau} />
              <span className="text-sm font-medium text-bark">{a.type}</span>
              <span className="text-xs text-stone">· {a.parcelleNom}</span>
            </div>
            <p className="text-sm text-charcoal">{a.message}</p>
            <p className="text-xs text-stone mt-1">{new Date(a.dateCreation).toLocaleString('fr-FR')}</p>
          </div>
          {!a.estLue && (
            <Button variant="ghost" onClick={(e) => handleMarkRead(a.id, e)} className="text-xs shrink-0">
              <CheckCircle className="w-4 h-4 mr-1 inline" />Lu
            </Button>
          )}
        </div>
      ))}

      {/* Alert detail modal */}
      <Modal isOpen={!!detail} onClose={() => setDetail(null)} title="Détail de l'alerte">
        {detail && (
          <div className="space-y-4">
            <div className="flex items-center gap-2">
              <Badge value={detail.niveau} />
              {detail.estLue ? <Badge value="TRAITE" /> : <span className="text-xs text-terracotta font-medium">Non lue</span>}
            </div>

            <div>
              <p className="text-xs text-stone mb-1">Message</p>
              <p className="text-sm text-charcoal bg-cream rounded-xl p-3">{detail.message}</p>
            </div>

            <div className="grid grid-cols-1 gap-2 text-sm">
              <div className="flex items-center gap-2 text-charcoal"><Tag className="w-4 h-4 text-sage-dark" />Source: {detail.type}</div>
              {detail.parcelleNom && <div className="flex items-center gap-2 text-charcoal"><MapPin className="w-4 h-4 text-sage-dark" />Parcelle: {detail.parcelleNom}</div>}
              <div className="flex items-center gap-2 text-charcoal"><Clock className="w-4 h-4 text-sage-dark" />{new Date(detail.dateCreation).toLocaleString('fr-FR')}</div>
            </div>

            <div className="flex justify-end gap-2 pt-2 border-t border-parchment">
              <Button variant="secondary" onClick={() => setDetail(null)}>Fermer</Button>
              {!detail.estLue && (
                <Button onClick={(e) => handleMarkRead(detail.id, e)}>
                  <CheckCircle className="w-4 h-4 mr-1 inline" />Marquer comme lu
                </Button>
              )}
            </div>
          </div>
        )}
      </Modal>
    </div>
  );
}
