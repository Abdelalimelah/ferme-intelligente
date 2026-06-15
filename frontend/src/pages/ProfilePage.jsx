import { useAuth } from '../hooks/useAuth';
import Card from '../components/ui/Card';
import Badge from '../components/ui/Badge';
import { User, Mail, Phone, Calendar, Shield } from 'lucide-react';

export default function ProfilePage() {
  const { user } = useAuth();

  const fields = [
    { icon: User, label: 'Nom complet', value: `${user?.prenom} ${user?.nom}` },
    { icon: Mail, label: 'Email', value: user?.email },
    { icon: Phone, label: 'Téléphone', value: user?.telephone || 'Non renseigné' },
    { icon: Shield, label: 'Rôle', value: <Badge value={user?.role} /> },
    { icon: Calendar, label: 'Membre depuis', value: user?.dateCreation ? new Date(user.dateCreation).toLocaleDateString('fr-FR') : '-' },
  ];

  return (
    <div className="max-w-2xl mx-auto">
      <Card>
        <div className="flex items-center gap-4 mb-6 pb-6 border-b border-parchment">
          <div className="w-16 h-16 bg-sage-mist rounded-2xl flex items-center justify-center text-sage-dark font-heading text-2xl font-semibold">
            {user?.prenom?.[0]}{user?.nom?.[0]}
          </div>
          <div>
            <h3 className="font-heading text-xl font-semibold text-bark">{user?.prenom} {user?.nom}</h3>
            <p className="text-stone text-sm">{user?.email}</p>
          </div>
        </div>
        <div className="space-y-4">
          {fields.map(f => (
            <div key={f.label} className="flex items-center gap-3">
              <f.icon className="w-4.5 h-4.5 text-stone" />
              <span className="text-sm text-stone w-32">{f.label}</span>
              <span className="text-sm text-bark font-medium">{f.value}</span>
            </div>
          ))}
        </div>
      </Card>
    </div>
  );
}
