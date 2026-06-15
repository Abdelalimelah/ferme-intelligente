import { useState, useEffect } from 'react';
import { getUtilisateursByRole, createUtilisateur, updateUtilisateur, deleteUtilisateur } from '../../api/utilisateurApi';
import DataTable from '../../components/ui/DataTable';
import Modal from '../../components/ui/Modal';
import Button from '../../components/ui/Button';
import Input from '../../components/ui/Input';
import Badge from '../../components/ui/Badge';
import LoadingSpinner from '../../components/ui/LoadingSpinner';
import { Plus, Pencil, Trash2, Mail } from 'lucide-react';

const emptyForm = { nom: '', prenom: '', email: '', telephone: '' };

export default function ManageManagers() {
  const [managers, setManagers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [editId, setEditId] = useState(null);
  const [form, setForm] = useState(emptyForm);
  const [error, setError] = useState('');
  const [successEmail, setSuccessEmail] = useState(null);

  const load = () => {
    setLoading(true);
    getUtilisateursByRole('GESTIONNAIRE').then(res => setManagers(res.data)).catch(() => {}).finally(() => setLoading(false));
  };

  useEffect(load, []);

  const openCreate = () => { setForm(emptyForm); setEditId(null); setError(''); setModalOpen(true); };
  const openEdit = (m) => { setForm({ nom: m.nom, prenom: m.prenom, email: m.email, telephone: m.telephone || '' }); setEditId(m.id); setError(''); setModalOpen(true); };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    try {
      if (editId) {
        await updateUtilisateur(editId, { ...form, role: 'GESTIONNAIRE' });
        setModalOpen(false);
      } else {
        await createUtilisateur({ ...form, role: 'GESTIONNAIRE' });
        setModalOpen(false);
        setSuccessEmail(form.email);
      }
      load();
    } catch (err) {
      setError(err.response?.data?.message || 'Erreur');
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Supprimer ce gestionnaire ?')) return;
    try { await deleteUtilisateur(id); load(); } catch {}
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
        data={managers}
        searchPlaceholder="Rechercher un gestionnaire..."
        actions={row => (
          <div className="flex gap-1.5 justify-end">
            <button onClick={() => openEdit(row)} className="p-1.5 rounded-lg hover:bg-sage-mist text-stone hover:text-sage-dark cursor-pointer"><Pencil className="w-4 h-4" /></button>
            <button onClick={() => handleDelete(row.id)} className="p-1.5 rounded-lg hover:bg-terracotta-lt text-stone hover:text-terracotta cursor-pointer"><Trash2 className="w-4 h-4" /></button>
          </div>
        )}
      />

      {/* Create / Edit Modal */}
      <Modal isOpen={modalOpen} onClose={() => setModalOpen(false)} title={editId ? 'Modifier Gestionnaire' : 'Nouveau Gestionnaire'}>
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

      {/* Email sent confirmation */}
      <Modal isOpen={!!successEmail} onClose={() => setSuccessEmail(null)} title="Compte créé">
        {successEmail && (
          <div className="space-y-4">
            <div className="flex items-center gap-3 p-4 bg-sage-mist/40 rounded-xl">
              <Mail className="w-5 h-5 text-sage-dark shrink-0" />
              <p className="text-sm text-charcoal">
                Un email contenant le mot de passe temporaire a été envoyé à{' '}
                <span className="font-semibold text-bark">{successEmail}</span>.
                Le gestionnaire devra changer son mot de passe à la première connexion.
              </p>
            </div>
            <div className="flex justify-end pt-2">
              <Button onClick={() => setSuccessEmail(null)}>OK</Button>
            </div>
          </div>
        )}
      </Modal>
    </div>
  );
}
