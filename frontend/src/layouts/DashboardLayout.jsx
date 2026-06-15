import { Outlet, useLocation } from 'react-router-dom';
import Sidebar from '../components/Sidebar';
import TopBar from '../components/TopBar';

const titles = {
  '/owner': 'Dashboard Propriétaire',
  '/owner/managers': 'Gestion des Gestionnaires',
  '/owner/workers': 'Gestion des Agriculteurs',
  '/owner/reports': 'Rapports & Plaintes',
  '/owner/assign-worker': 'Assigner un Travailleur',
  '/owner/assign-manager': 'Assigner un Gestionnaire',
  '/manager': 'Dashboard Gestionnaire',
  '/manager/parcels': 'Gestion des Parcelles',
  '/manager/tasks': 'Gestion des Tâches',
  '/manager/sensors': 'Capteurs',
  '/manager/disease-map': 'Carte des Maladies',
  '/manager/alerts': 'Alertes',
  '/manager/report': 'Soumettre un Rapport',
  '/manager/history': 'Historique',
  '/worker': 'Dashboard Agriculteur',
  '/worker/tasks': 'Mes Tâches',
  '/worker/parcel': 'Ma Parcelle',
  '/profile': 'Mon Profil',
};

export default function DashboardLayout() {
  const location = useLocation();
  const title = titles[location.pathname] || 'Ferme Intelligente';

  return (
    <div className="flex min-h-screen bg-cream">
      <Sidebar />
      <div className="flex-1 flex flex-col">
        <TopBar title={title} />
        <main className="flex-1 p-6 overflow-auto">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
