import { useState } from 'react';
import Modal from './ui/Modal';
import Badge from './ui/Badge';
import Button from './ui/Button';
import { FileText, User, Calendar } from 'lucide-react';

const STATUSES = ['NON_TRAITE', 'EN_COURS', 'TRAITE'];
const statusLabels = { NON_TRAITE: 'Non traité', EN_COURS: 'En cours', TRAITE: 'Traité' };

// Rendered with `key={rapport.id}` by the parent so a new `rapport` remounts
// this component instead of needing an effect to resync `statut`.
export default function RapportDetailModal({ rapport, isOpen, onClose, onChangeStatut }) {
  const [statut, setStatut] = useState(rapport?.statut);
  const [saving, setSaving] = useState(false);

  if (!rapport) return null;

  const handleSave = async () => {
    if (statut === rapport.statut) { onClose(); return; }
    setSaving(true);
    try {
      await onChangeStatut(rapport.id, statut);
      onClose();
    } finally {
      setSaving(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title={rapport.sujet}>
      <div className="space-y-4">
        <div className="flex items-center gap-2">
          <Badge value={rapport.type} />
          <Badge value={rapport.statut} />
        </div>

        <div>
          <p className="text-xs text-stone mb-1 flex items-center gap-1.5"><FileText className="w-3.5 h-3.5" />Contenu</p>
          <p className="text-sm text-charcoal whitespace-pre-wrap bg-cream rounded-xl p-3">{rapport.contenu}</p>
        </div>

        <div className="grid grid-cols-1 gap-2 text-sm">
          <div className="flex items-center gap-2 text-charcoal">
            <User className="w-4 h-4 text-sage-dark" />Auteur: {rapport.auteurNom}
          </div>
          <div className="flex items-center gap-2 text-charcoal">
            <Calendar className="w-4 h-4 text-sage-dark" />Date: {new Date(rapport.dateCreation).toLocaleString('fr-FR')}
          </div>
        </div>

        <div className="border-t border-parchment pt-4">
          <label className="block text-sm font-medium text-charcoal mb-2">Changer le statut</label>
          <div className="flex gap-2">
            {STATUSES.map(s => (
              <button
                key={s}
                onClick={() => setStatut(s)}
                className={`px-3 py-1.5 rounded-full text-xs font-medium transition-all cursor-pointer ${
                  statut === s ? 'bg-sage text-white' : 'bg-warm-white text-stone border border-parchment hover:border-sage'
                }`}
              >
                {statusLabels[s]}
              </button>
            ))}
          </div>
        </div>

        <div className="flex justify-end gap-2 pt-2">
          <Button variant="secondary" onClick={onClose}>Fermer</Button>
          <Button onClick={handleSave} disabled={saving}>{saving ? 'Enregistrement...' : 'Enregistrer'}</Button>
        </div>
      </div>
    </Modal>
  );
}
