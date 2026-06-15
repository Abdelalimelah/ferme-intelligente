import { useState } from 'react';
import { useAuth } from '../../hooks/useAuth';
import { createRapport } from '../../api/rapportApi';
import Card from '../../components/ui/Card';
import Button from '../../components/ui/Button';
import Input from '../../components/ui/Input';
import Select from '../../components/ui/Select';
import { FileUp, CheckCircle } from 'lucide-react';

export default function SubmitReport() {
  const { user } = useAuth();
  const [form, setForm] = useState({ type: 'RAPPORT', sujet: '', contenu: '' });
  const [message, setMessage] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await createRapport({ ...form, auteurId: user.id });
      setMessage('Rapport soumis avec succès!');
      setForm({ type: 'RAPPORT', sujet: '', contenu: '' });
      setTimeout(() => setMessage(''), 3000);
    } catch (err) {
      setMessage(err.response?.data?.message || 'Erreur');
    }
  };

  return (
    <div className="max-w-2xl mx-auto">
      <Card>
        <div className="flex items-center gap-3 mb-6">
          <div className="p-2.5 bg-sage-mist rounded-xl"><FileUp className="w-5 h-5 text-sage-dark" /></div>
          <div>
            <h3 className="font-heading text-base font-semibold text-bark">Soumettre un rapport</h3>
            <p className="text-xs text-stone">Rapport d'activité ou plainte</p>
          </div>
        </div>
        <form onSubmit={handleSubmit} className="space-y-4">
          <Select label="Type" value={form.type} onChange={e => setForm({ ...form, type: e.target.value })}
            options={[{ value: 'RAPPORT', label: 'Rapport' }, { value: 'PLAINTE', label: 'Plainte' }]} />
          <Input label="Sujet" value={form.sujet} onChange={e => setForm({ ...form, sujet: e.target.value })} required />
          <div>
            <label className="block text-sm font-medium text-charcoal mb-1.5">Contenu</label>
            <textarea
              value={form.contenu}
              onChange={e => setForm({ ...form, contenu: e.target.value })}
              required
              rows={6}
              className="w-full px-3.5 py-2.5 bg-warm-white border border-parchment rounded-xl text-sm text-bark placeholder-stone-light focus:outline-none focus:border-sage focus:ring-1 focus:ring-sage"
            />
          </div>
          {message && (
            <div className={`flex items-center gap-2 text-sm px-4 py-2.5 rounded-xl ${message.includes('succès') ? 'bg-sage-mist text-sage-dark' : 'bg-terracotta-lt text-terracotta'}`}>
              {message.includes('succès') && <CheckCircle className="w-4 h-4" />}{message}
            </div>
          )}
          <Button type="submit" disabled={!form.sujet || !form.contenu} className="w-full">Soumettre</Button>
        </form>
      </Card>
    </div>
  );
}
