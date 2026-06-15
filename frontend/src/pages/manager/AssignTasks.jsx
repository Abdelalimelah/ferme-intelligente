import { useState, useEffect } from 'react';
import { useAuth } from '../../hooks/useAuth';
import { createTache, getTachesByGestionnaire } from '../../api/tacheApi';
import { getUtilisateursByRole } from '../../api/utilisateurApi';
import { getParcelles } from '../../api/parcelleApi';
import Card from '../../components/ui/Card';
import Button from '../../components/ui/Button';
import Input from '../../components/ui/Input';
import Select from '../../components/ui/Select';
import Badge from '../../components/ui/Badge';
import LoadingSpinner from '../../components/ui/LoadingSpinner';
import { ListTodo, CheckCircle } from 'lucide-react';

const emptyForm = { titre: '', description: '', agriculteurId: '', parcelleId: '', priorite: 'MOYENNE', dateEcheance: '' };

export default function AssignTasks() {
  const { user } = useAuth();
  const [form, setForm] = useState(emptyForm);
  const [workers, setWorkers] = useState([]);
  const [parcelles, setParcelles] = useState([]);
  const [recentTasks, setRecentTasks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState('');

  const load = () => {
    Promise.all([getUtilisateursByRole('AGRICULTEUR'), getParcelles(), getTachesByGestionnaire(user.id)])
      .then(([w, p, t]) => { setWorkers(w.data); setParcelles(p.data); setRecentTasks(t.data.slice(0, 5)); })
      .catch(() => {}).finally(() => setLoading(false));
  };

  useEffect(load, [user.id]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await createTache({ ...form, gestionnaireId: user.id, dateEcheance: form.dateEcheance ? form.dateEcheance + 'T00:00:00' : null });
      setMessage('Tâche créée avec succès!');
      setForm(emptyForm);
      load();
      setTimeout(() => setMessage(''), 3000);
    } catch (err) {
      setMessage(err.response?.data?.message || 'Erreur');
    }
  };

  if (loading) return <LoadingSpinner />;

  return (
    <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <Card>
        <div className="flex items-center gap-3 mb-5">
          <div className="p-2.5 bg-sage-mist rounded-xl"><ListTodo className="w-5 h-5 text-sage-dark" /></div>
          <h3 className="font-heading text-base font-semibold text-bark">Nouvelle tâche</h3>
        </div>
        <form onSubmit={handleSubmit} className="space-y-4">
          <Input label="Titre" value={form.titre} onChange={e => setForm({ ...form, titre: e.target.value })} required />
          <div>
            <label className="block text-sm font-medium text-charcoal mb-1.5">Description</label>
            <textarea
              value={form.description}
              onChange={e => setForm({ ...form, description: e.target.value })}
              className="w-full px-3.5 py-2.5 bg-warm-white border border-parchment rounded-xl text-sm text-bark placeholder-stone-light focus:outline-none focus:border-sage focus:ring-1 focus:ring-sage"
              rows={3}
            />
          </div>
          <Select label="Agriculteur" value={form.agriculteurId} onChange={e => setForm({ ...form, agriculteurId: e.target.value })}
            options={[{ value: '', label: 'Sélectionner...' }, ...workers.map(w => ({ value: w.id, label: `${w.prenom} ${w.nom}` }))]} />
          <Select label="Parcelle" value={form.parcelleId} onChange={e => setForm({ ...form, parcelleId: e.target.value })}
            options={[{ value: '', label: 'Aucune' }, ...parcelles.map(p => ({ value: p.id, label: p.nom }))]} />
          <div className="grid grid-cols-2 gap-3">
            <Select label="Priorité" value={form.priorite} onChange={e => setForm({ ...form, priorite: e.target.value })}
              options={[{ value: 'BASSE', label: 'Basse' }, { value: 'MOYENNE', label: 'Moyenne' }, { value: 'HAUTE', label: 'Haute' }]} />
            <Input label="Échéance" type="date" value={form.dateEcheance} onChange={e => setForm({ ...form, dateEcheance: e.target.value })} />
          </div>
          {message && (
            <div className={`flex items-center gap-2 text-sm px-4 py-2.5 rounded-xl ${message.includes('succès') ? 'bg-sage-mist text-sage-dark' : 'bg-terracotta-lt text-terracotta'}`}>
              {message.includes('succès') && <CheckCircle className="w-4 h-4" />}{message}
            </div>
          )}
          <Button type="submit" disabled={!form.titre || !form.agriculteurId} className="w-full">Créer la tâche</Button>
        </form>
      </Card>

      <Card>
        <h3 className="font-heading text-base font-semibold text-bark mb-4">Tâches récentes</h3>
        <div className="space-y-3">
          {recentTasks.map(t => (
            <div key={t.id} className="p-3 bg-cream rounded-xl">
              <div className="flex items-center justify-between mb-1">
                <span className="text-sm font-medium text-bark">{t.titre}</span>
                <Badge value={t.statut} />
              </div>
              <div className="flex items-center gap-2 text-xs text-stone">
                <span>{t.agriculteurNom}</span>
                <span>·</span>
                <Badge value={t.priorite} />
              </div>
            </div>
          ))}
          {recentTasks.length === 0 && <p className="text-sm text-stone">Aucune tâche</p>}
        </div>
      </Card>
    </div>
  );
}
