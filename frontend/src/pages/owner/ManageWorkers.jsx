import { useState, useEffect } from 'react';
import { getUtilisateursByRole, createUtilisateur, updateUtilisateur, deleteUtilisateur } from '../../api/utilisateurApi';
import DataTable from '../../components/ui/DataTable';
import Modal from '../../components/ui/Modal';
import Button from '../../components/ui/Button';
import Input from '../../components/ui/Input';
import Badge from '../../components/ui/Badge';
import LoadingSpinner from '../../components/ui/LoadingSpinner';
import { Plus, Pencil, Trash2, KeyRound, Copy, Check } from 'lucide-react';

const emptyForm = { nom: '', prenom: '', email: '', telephone: '' };

export default function ManageWorkers() {
  const [workers, setWorkers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [editId, setEditId] = useState(null);
  const [form, setForm] = useState(emptyForm);
  const [error, setError] = useState('');
  const [tempPassword, setTempPassword] = useState(null);
  const [copied, setCopied] = useState(false);

  const load = () => {
    setLoading(true);
    getUtilisateursByRole('AGRICULTEUR').then(res => setWorkers(res.data)).catch(() => {}).finally(() => setLoading(false));
  };

  useEffect(load, []);

  const openCreate = () => { setForm(emptyForm); setEditId(null); setError(''); setModalOpen(true); };
  const openEdit = (w) => { setForm({ nom: w.nom, prenom: w.prenom, email: w.email, telephone: w.telephone || '' }); setEditId(w.id); setError(''); setModalOpen(true); };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    try {
      if (editId) {
        await updateUtilisateur(editId, { ...form, role: 'AGRICULTEUR' });
        setModalOpen(false);
      } else {
        const res = await createUtilisateur({ ...form, role: 'AGRICULTEUR' });
        setModalOpen(false);
        setTempPassword({ password: res.data.temporaryPassword, name: `${form.prenom} ${form.nom}` });
      }
      load();
    } catch (err) {
      setError(err.response?.data?.message || 'Erreur');
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Supprimer cet agriculteur ?')) return;
    try { await deleteUtilisateur(id); load(); } catch {}
  };

  const copyPassword = () => {
    navigator.clipboard?.writeText(tempPassword.password);
    setCopied(true);
    setTimeout(() => setCopied(false), 2000);
  };

  const columns = [
    { header: 'Nom', accessor: row => `${row.prenom} ${row.nom}` },
    { header: 'Email', accessor: 'email' },
    { header: 'Téléphone', accessor: row => row.telephone || '-' },
    { header: 'Statut', accessor: 'statut', render: row => <Badge value={row.statut} /> },
  ];

  if (loading) return <LoadingSpinner />;

  return (
    <div>
      <div className="flex justify-end mb-4">
        <Button onClick={openCreate}><Plus className="w-4 h-4 mr-1.5 inline" />Ajouter</Button>
      </div>
      <DataTable
        columns={columns}
        data={workers}
        searchPlaceholder="Rechercher un agriculteur..."
        actions={row => (
          <div className="flex gap-1.5 justify-end">
            <button onClick={() => openEdit(row)} className="p-1.5 rounded-lg hover:bg-sage-mist text-stone hover:text-sage-dark cursor-pointer"><Pencil className="w-4 h-4" /></button>
            <button onClick={() => handleDelete(row.id)} className="p-1.5 rounded-lg hover:bg-terracotta-lt text-stone hover:text-terracotta cursor-pointer"><Trash2 className="w-4 h-4" /></button>
          </div>
        )}
      />

      {/* Create / Edit Modal */}
      <Modal isOpen={modalOpen} onClose={() => setModalOpen(false)} title={editId ? 'Modifier Agriculteur' : 'Nouvel Agriculteur'}>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="grid grid-cols-2 gap-3">
            <Input label="Prénom" value={form.prenom} onChange={e => setForm({ ...form, prenom: e.target.value })} required />
            <Input label="Nom" value={form.nom} onChange={e => setForm({ ...form, nom: e.target.value })} required />
          </div>
          <Input label="Email" type="email" value={form.email} onChange={e => setForm({ ...form, email: e.target.value })} required />
          <Input label="Téléphone" value={form.telephone} onChange={e => setForm({ ...form, telephone: e.target.value })} />
          {!editId && (
            <p className="text-xs text-stone bg-cream rounded-lg p-2.5">
              Un mot de passe temporaire sera généré automatiquement. Vous pourrez le communiquer au nouvel utilisateur.
            </p>
          )}
          {error && <p className="text-sm text-terracotta">{error}</p>}
          <div className="flex justify-end gap-2 pt-2">
            <Button variant="secondary" type="button" onClick={() => setModalOpen(false)}>Annuler</Button>
            <Button type="submit">{editId ? 'Modifier' : 'Créer'}</Button>
          </div>
        </form>
      </Modal>

      {/* Temp password success modal */}
      <Modal isOpen={!!tempPassword} onClose={() => setTempPassword(null)} title="Utilisateur créé">
        {tempPassword && (
          <div className="space-y-4">
            <div className="flex items-center gap-3 p-3 bg-sage-mist/40 rounded-xl">
              <KeyRound className="w-5 h-5 text-sage-dark shrink-0" />
              <p className="text-sm text-charcoal">
                Compte créé pour <span className="font-semibold text-bark">{tempPassword.name}</span>.
                Communiquez ce mot de passe temporaire — il devra le changer à la première connexion.
              </p>
            </div>
            <div>
              <label className="block text-xs text-stone mb-1.5">Mot de passe temporaire</label>
              <div className="flex items-center gap-2">
                <code className="flex-1 font-mono text-base bg-cream border border-parchment rounded-xl px-4 py-3 text-bark tracking-wider">
                  {tempPassword.password}
                </code>
                <button onClick={copyPassword} className="p-3 rounded-xl bg-sage-mist hover:bg-sage-mist/70 text-sage-dark cursor-pointer transition-colors" title="Copier">
                  {copied ? <Check className="w-5 h-5" /> : <Copy className="w-5 h-5" />}
                </button>
              </div>
            </div>
            <div className="flex justify-end pt-2">
              <Button onClick={() => setTempPassword(null)}>J'ai noté le mot de passe</Button>
            </div>
          </div>
        )}
      </Modal>
    </div>
  );
}
