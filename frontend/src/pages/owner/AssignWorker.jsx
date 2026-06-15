import { useState, useEffect } from 'react';
import { getUtilisateursByRole, assignAgriculteurToParcelle } from '../../api/utilisateurApi';
import { getParcelles } from '../../api/parcelleApi';
import Button from '../../components/ui/Button';
import Select from '../../components/ui/Select';
import Card from '../../components/ui/Card';
import LoadingSpinner from '../../components/ui/LoadingSpinner';
import { UserPlus, CheckCircle } from 'lucide-react';

export default function AssignWorker() {
  const [workers, setWorkers] = useState([]);
  const [parcelles, setParcelles] = useState([]);
  const [selectedWorker, setSelectedWorker] = useState('');
  const [selectedParcelle, setSelectedParcelle] = useState('');
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState('');

  useEffect(() => {
    Promise.all([getUtilisateursByRole('AGRICULTEUR'), getParcelles()])
      .then(([w, p]) => { setWorkers(w.data); setParcelles(p.data); })
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  const handleAssign = async () => {
    if (!selectedWorker || !selectedParcelle) return;
    try {
      await assignAgriculteurToParcelle(selectedWorker, selectedParcelle);
      setMessage('Agriculteur assigné avec succès!');
      setTimeout(() => setMessage(''), 3000);
    } catch (err) {
      setMessage(err.response?.data?.message || 'Erreur lors de l\'assignation');
    }
  };

  if (loading) return <LoadingSpinner />;

  return (
    <div className="max-w-xl mx-auto">
      <Card>
        <div className="flex items-center gap-3 mb-6">
          <div className="p-2.5 bg-sage-mist rounded-xl">
            <UserPlus className="w-5 h-5 text-sage-dark" />
          </div>
          <div>
            <h3 className="font-heading text-base font-semibold text-bark">Assigner un agriculteur</h3>
            <p className="text-xs text-stone">Lier un agriculteur à une parcelle</p>
          </div>
        </div>
        <div className="space-y-4">
          <Select
            label="Agriculteur"
            value={selectedWorker}
            onChange={e => setSelectedWorker(e.target.value)}
            options={[{ value: '', label: 'Sélectionner...' }, ...workers.map(w => ({ value: w.id, label: `${w.prenom} ${w.nom}` }))]}
          />
          <Select
            label="Parcelle"
            value={selectedParcelle}
            onChange={e => setSelectedParcelle(e.target.value)}
            options={[{ value: '', label: 'Sélectionner...' }, ...parcelles.map(p => ({ value: p.id, label: `${p.nom} (${p.fermeNom})` }))]}
          />
          {message && (
            <div className={`flex items-center gap-2 text-sm px-4 py-2.5 rounded-xl ${message.includes('succès') ? 'bg-sage-mist text-sage-dark' : 'bg-terracotta-lt text-terracotta'}`}>
              {message.includes('succès') && <CheckCircle className="w-4 h-4" />}
              {message}
            </div>
          )}
          <Button onClick={handleAssign} disabled={!selectedWorker || !selectedParcelle} className="w-full">
            Assigner
          </Button>
        </div>
      </Card>
    </div>
  );
}
