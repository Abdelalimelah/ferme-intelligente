import { NavLink } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import {
  LayoutDashboard, Users, UserCheck, FileText, UserPlus, Link2,
  Map, ListTodo, Radio, AlertTriangle, FileUp, History,
  CheckSquare, Sprout, User, LogOut, Bug
} from 'lucide-react';

const navConfig = {
  proprietaire: [
    { to: '/owner', icon: LayoutDashboard, label: 'Dashboard' },
    { to: '/owner/managers', icon: Users, label: 'Gestionnaires' },
    { to: '/owner/workers', icon: UserCheck, label: 'Agriculteurs' },
    { to: '/owner/reports', icon: FileText, label: 'Rapports & Plaintes' },
    { to: '/owner/assign-worker', icon: UserPlus, label: 'Assigner Travailleur' },
    { to: '/owner/assign-manager', icon: Link2, label: 'Assigner Gestionnaire' },
  ],
  gestionnaire: [
    { to: '/manager', icon: LayoutDashboard, label: 'Dashboard' },
    { to: '/manager/parcels', icon: Map, label: 'Parcelles' },
    { to: '/manager/tasks', icon: ListTodo, label: 'Tâches' },
    { to: '/manager/sensors', icon: Radio, label: 'Capteurs' },
    { to: '/manager/disease-map', icon: Bug, label: 'Carte Maladies' },
    { to: '/manager/alerts', icon: AlertTriangle, label: 'Alertes' },
    { to: '/manager/report', icon: FileUp, label: 'Soumettre Rapport' },
    { to: '/manager/history', icon: History, label: 'Historique' },
  ],
  agriculteur: [
    { to: '/worker', icon: LayoutDashboard, label: 'Dashboard' },
    { to: '/worker/tasks', icon: CheckSquare, label: 'Mes Tâches' },
    { to: '/worker/parcel', icon: Sprout, label: 'Ma Parcelle' },
  ],
};

export default function Sidebar() {
  const { user, role, logout } = useAuth();
  const items = navConfig[role] || [];

  return (
    <aside className="w-64 bg-warm-white border-r border-parchment min-h-screen flex flex-col">
      <div className="p-5 border-b border-parchment">
        <div className="flex items-center gap-2.5">
          <div className="w-9 h-9 bg-gradient-to-br from-sage to-sage-dark rounded-xl flex items-center justify-center">
            <Sprout className="w-5 h-5 text-white" />
          </div>
          <div>
            <h1 className="font-heading text-base font-semibold text-bark leading-tight">Ferme</h1>
            <p className="text-xs text-stone">Intelligente</p>
          </div>
        </div>
      </div>

      <nav className="flex-1 p-3 space-y-1">
        {items.map(item => (
          <NavLink
            key={item.to}
            to={item.to}
            end={item.to.split('/').length <= 2}
            className={({ isActive }) =>
              `flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm transition-all ${
                isActive
                  ? 'bg-sage-mist text-sage-dark font-medium'
                  : 'text-charcoal hover:bg-cream'
              }`
            }
          >
            <item.icon className="w-[18px] h-[18px]" />
            {item.label}
          </NavLink>
        ))}
      </nav>

      <div className="p-3 border-t border-parchment space-y-1">
        <NavLink
          to="/profile"
          className={({ isActive }) =>
            `flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm transition-all ${
              isActive ? 'bg-sage-mist text-sage-dark font-medium' : 'text-charcoal hover:bg-cream'
            }`
          }
        >
          <User className="w-[18px] h-[18px]" />
          Profil
        </NavLink>
        <button
          onClick={logout}
          className="flex items-center gap-3 px-3 py-2.5 rounded-xl text-sm text-terracotta hover:bg-terracotta-lt transition-all w-full cursor-pointer"
        >
          <LogOut className="w-[18px] h-[18px]" />
          Déconnexion
        </button>
      </div>
    </aside>
  );
}
