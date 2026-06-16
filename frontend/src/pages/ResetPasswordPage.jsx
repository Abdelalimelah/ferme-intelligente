import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { resetPassword } from '../api/authApi';
import { Lock, KeyRound } from 'lucide-react';
import Button from '../components/ui/Button';
import Input from '../components/ui/Input';

const roleRoutes = {
  PROPRIETAIRE: '/owner',
  GESTIONNAIRE: '/manager',
  AGRICULTEUR: '/worker',
};

export default function ResetPasswordPage() {
  const { user, updateUser } = useAuth();
  const navigate = useNavigate();
  const [tempPassword, setTempPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirm, setConfirm] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    if (newPassword.length < 6) { setError('Le nouveau mot de passe doit contenir au moins 6 caractères'); return; }
    if (newPassword !== confirm) { setError('Les mots de passe ne correspondent pas'); return; }

    setLoading(true);
    try {
      await resetPassword({ email: user.email, oldPassword: tempPassword, newPassword });
      updateUser({ ...user, mustResetPassword: false });
      navigate(roleRoutes[user.role] || '/');
    } catch (err) {
      setError(err.response?.data?.message || 'Erreur lors du changement de mot de passe');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-cream flex items-center justify-center p-4">
      <div className="w-full max-w-md">
        <div className="text-center mb-8">
          <div className="w-16 h-16 bg-gradient-to-br from-sage to-sage-dark rounded-2xl flex items-center justify-center mx-auto mb-4 shadow-lg">
            <KeyRound className="w-9 h-9 text-white" />
          </div>
          <h1 className="font-heading text-2xl font-semibold text-bark">Changer votre mot de passe</h1>
          <p className="text-stone mt-1">Pour des raisons de sécurité, veuillez définir un nouveau mot de passe avant de continuer.</p>
        </div>

        <div className="bg-warm-white border border-parchment rounded-2xl p-6 shadow-sm">
          <form onSubmit={handleSubmit} className="space-y-4">
            <Input
              label="Mot de passe temporaire"
              type="password"
              value={tempPassword}
              onChange={e => setTempPassword(e.target.value)}
              placeholder="Celui communiqué par votre responsable"
              required
            />
            <Input
              label="Nouveau mot de passe"
              type="password"
              value={newPassword}
              onChange={e => setNewPassword(e.target.value)}
              placeholder="Au moins 6 caractères"
              required
            />
            <Input
              label="Confirmer le mot de passe"
              type="password"
              value={confirm}
              onChange={e => setConfirm(e.target.value)}
              placeholder="••••••••"
              required
            />
            {error && (
              <div className="bg-terracotta-lt text-terracotta text-sm px-4 py-2.5 rounded-xl">{error}</div>
            )}
            <Button type="submit" disabled={loading} className="w-full py-3">
              <Lock className="w-4 h-4 mr-1.5 inline" />
              {loading ? 'Enregistrement...' : 'Définir le mot de passe'}
            </Button>
          </form>
        </div>

        <p className="text-center text-xs text-stone mt-4">
          Connecté en tant que {user?.prenom} {user?.nom} ({user?.email})
        </p>
      </div>
    </div>
  );
}
