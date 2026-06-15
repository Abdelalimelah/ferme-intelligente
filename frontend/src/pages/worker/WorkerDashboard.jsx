import { useState, useEffect } from 'react';
import { useAuth } from '../../hooks/useAuth';
import { getWorkerStats } from '../../api/dashboardApi';
import StatCard from '../../components/ui/StatCard';
import Card from '../../components/ui/Card';
import Badge from '../../components/ui/Badge';
import LoadingSpinner from '../../components/ui/LoadingSpinner';
import { ClipboardList, Clock, CheckCircle2, ListTodo } from 'lucide-react';

export default function WorkerDashboard() {
  const { user } = useAuth();
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getWorkerStats(user.id).then(res => setStats(res.data)).catch(() => {}).finally(() => setLoading(false));
  }, [user.id]);

  if (loading) return <LoadingSpinner />;

  return (
    <div className="space-y-6">
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <StatCard icon={<ClipboardList className="w-5 h-5" />} label="À faire" value={stats?.tachesAFaire ?? 0} color="wheat" />
        <StatCard icon={<Clock className="w-5 h-5" />} label="En cours" value={stats?.tachesEnCours ?? 0} color="olive" />
        <StatCard icon={<CheckCircle2 className="w-5 h-5" />} label="Terminées" value={stats?.tachesTerminees ?? 0} color="sage" />
      </div>

      <Card>
        <h3 className="font-heading text-base font-semibold text-bark mb-4">Tâches récentes</h3>
        {stats?.recentTaches?.length > 0 ? (
          <div className="space-y-3">
            {stats.recentTaches.map(t => (
              <div key={t.id} className="flex items-center justify-between p-3 bg-cream rounded-xl">
                <div>
                  <p className="text-sm font-medium text-bark">{t.titre}</p>
                  <div className="flex items-center gap-2 mt-1 text-xs text-stone">
                    <span>{t.parcelleNom || 'Non assignée'}</span>
                    <Badge value={t.priorite} />
                  </div>
                </div>
                <Badge value={t.statut} />
              </div>
            ))}
          </div>
        ) : (
          <p className="text-sm text-stone">Aucune tâche</p>
        )}
      </Card>
    </div>
  );
}
