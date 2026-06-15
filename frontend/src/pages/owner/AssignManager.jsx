import { useState, useEffect } from 'react';
import { getUtilisateursByRole, assignGestionnaireToFerme } from '../../api/utilisateurApi';
import { getFermes } from '../../api/fermeApi';
import Button from '../../components/ui/Button';
import Select from '../../components/ui/Select';
import Card from '../../components/ui/Card';
import LoadingSpinner from '../../components/ui/LoadingSpinner';
import { Link2, CheckCircle } from 'lucide-react';

export default function AssignManager() {
  const [managers, setManagers] = useState([]);
  const [fermes, setFermes] = useState([]);
  const [selectedManager, setSelectedManager] = useState('');
  const [selectedFerme, setSelectedFerme] = useState('');
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState('');

  useEffect(() => {
    Promise.all([getUtilisateursByRole('GESTIONNAIRE'), getFermes()])
      .then(([m, f]) => { setManagers(m.data); setFermes(f.data); })
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  const handleAssign = async () => {
    if (!selectedManager || !selectedFerme) return;
    try {
      await assignGestionnaireToFerme(selectedManager, selectedFerme);
      setMessage('Gestionnaire assigné avec succès!');
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
            <Link2 className="w-5 h-5 text-sage-dark" />
          </div>
          <div>
            <h3 className="font-heading text-base font-semibold text-bark">Assigner un gestionnaire</h3>
            <p className="text-xs text-stone">Lier un gestionnaire à une ferme</p>
          </div>
        </div>
        <div className="space-y-4">
          <Select
            label="Gestionnaire"
            value={selectedManager}
            onChange={e => setSelectedManager(e.target.value)}
            options={[{ value: '', label: 'Sélectionner...' }, ...managers.map(m => ({ value: m.id, label: `${m.prenom} ${m.nom}` }))]}
          />
          <Select
            label="Ferme"
            value={selectedFerme}
            onChange={e => setSelectedFerme(e.target.value)}
            options={[{ value: '', label: 'Sélectionner...' }, ...fermes.map(f => ({ value: f.id, label: `${f.nom} (${f.localisation})` }))]}
          />
          {message && (
            <div className={`flex items-center gap-2 text-sm px-4 py-2.5 rounded-xl ${message.includes('succès') ? 'bg-sage-mist text-sage-dark' : 'bg-terracotta-lt text-terracotta'}`}>
              {message.includes('succès') && <CheckCircle className="w-4 h-4" />}
              {message}
            </div>
          )}
          <Button onClick={handleAssign} disabled={!selectedManager || !selectedFerme} className="w-full">
            Assigner
          </Button>
        </div>
      </Card>
    </div>
  );
}
