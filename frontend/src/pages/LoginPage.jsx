import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { login } from '../api/authApi';
import { Sprout, Mail, Lock, Crown, Shield, Wheat } from 'lucide-react';
import Button from '../components/ui/Button';
import Input from '../components/ui/Input';

const roleRoutes = {
  PROPRIETAIRE: '/owner',
  GESTIONNAIRE: '/manager',
  AGRICULTEUR: '/worker',
};

const demoAccounts = [
  { label: 'Propriétaire', email: 'ahmed@ferme.ma', icon: Crown, color: 'wheat' },
  { label: 'Gestionnaire', email: 'karim@ferme.ma', icon: Shield, color: 'olive' },
  { label: 'Agriculteur', email: 'youssef@ferme.ma', icon: Wheat, color: 'sage' },
];

export default function LoginPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { loginUser } = useAuth();
  const navigate = useNavigate();

  const handleLogin = async (loginEmail, loginPassword) => {
    setError('');
    setLoading(true);
    try {
      const res = await login(loginEmail, loginPassword);
      loginUser(res.data.token, res.data.refreshToken, res.data.user);
      if (res.data.user.mustResetPassword) {
        navigate('/reset-password');
      } else {
        navigate(roleRoutes[res.data.user.role] || '/');
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Erreur de connexion');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-cream flex items-center justify-center p-4">
      <div className="w-full max-w-md">
        <div className="text-center mb-8">
          <div className="w-16 h-16 bg-gradient-to-br from-sage to-sage-dark rounded-2xl flex items-center justify-center mx-auto mb-4 shadow-lg">
            <Sprout className="w-9 h-9 text-white" />
          </div>
          <h1 className="font-heading text-3xl font-semibold text-bark">Ferme Intelligente</h1>
          <p className="text-stone mt-1">Connectez-vous pour gérer votre exploitation</p>
        </div>

        <div className="bg-warm-white border border-parchment rounded-2xl p-6 shadow-sm">
          <form onSubmit={e => { e.preventDefault(); handleLogin(email, password); }} className="space-y-4">
            <Input
              label="Email"
              type="email"
              value={email}
              onChange={e => setEmail(e.target.value)}
              placeholder="votre@email.com"
              required
            />
            <Input
              label="Mot de passe"
              type="password"
              value={password}
              onChange={e => setPassword(e.target.value)}
              placeholder="••••••••"
              required
            />
            {error && (
              <div className="bg-terracotta-lt text-terracotta text-sm px-4 py-2.5 rounded-xl">
                {error}
              </div>
            )}
            <Button type="submit" disabled={loading} className="w-full py-3">
              {loading ? 'Connexion...' : 'Se connecter'}
            </Button>
          </form>
        </div>

        <div className="mt-6">
          <p className="text-center text-sm text-stone mb-3">Accès rapide (démo)</p>
          <div className="grid grid-cols-3 gap-2">
            {demoAccounts.map(acc => (
              <button
                key={acc.email}
                onClick={() => handleLogin(acc.email, 'password123')}
                disabled={loading}
                className="flex flex-col items-center gap-2 p-3 bg-warm-white border border-parchment rounded-xl hover:border-sage hover:bg-sage-mist/30 transition-all cursor-pointer disabled:opacity-50"
              >
                <acc.icon className="w-5 h-5 text-sage-dark" />
                <span className="text-xs font-medium text-charcoal">{acc.label}</span>
              </button>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}
