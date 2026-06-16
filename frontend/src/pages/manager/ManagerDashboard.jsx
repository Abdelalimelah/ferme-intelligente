import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import { getManagerStats } from '../../api/dashboardApi';
import StatCard from '../../components/ui/StatCard';
import Card from '../../components/ui/Card';
import Badge from '../../components/ui/Badge';
import LoadingSpinner from '../../components/ui/LoadingSpinner';
import { Map, Radio, AlertTriangle, ListTodo, Thermometer, Droplets, FlaskConical } from 'lucide-react';

export default function ManagerDashboard() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getManagerStats(user.id).then(res => setStats(res.data)).catch(() => {}).finally(() => setLoading(false));
  }, [user.id]);

  if (loading) return <LoadingSpinner />;

  return (
    <div className="space-y-6">
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard icon={<Map className="w-5 h-5" />} label="Parcelles" value={stats?.totalParcelles ?? 0} color="sage" onClick={() => navigate('/manager/parcels')} />
        <StatCard icon={<Radio className="w-5 h-5" />} label="Capteurs actifs" value={stats?.capteursActifs ?? 0} color="olive" onClick={() => navigate('/manager/sensors')} />
        <StatCard icon={<AlertTriangle className="w-5 h-5" />} label="Alertes actives" value={stats?.alertesActives ?? 0} color="terracotta" onClick={() => navigate('/manager/alerts')} />
        <StatCard icon={<ListTodo className="w-5 h-5" />} label="Tâches en cours" value={stats?.tachesEnCours ?? 0} color="wheat" onClick={() => navigate('/manager/tasks')} />
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <Card>
          <div className="flex items-center gap-2 mb-2">
            <Thermometer className="w-4 h-4 text-terracotta" />
            <span className="text-sm text-stone">Température moy.</span>
          </div>
          <p className="font-heading text-2xl font-semibold text-bark">{stats?.moyenneTemperature?.toFixed(1) ?? '-'}°C</p>
        </Card>
        <Card>
          <div className="flex items-center gap-2 mb-2">
            <Droplets className="w-4 h-4 text-sage" />
            <span className="text-sm text-stone">Humidité moy.</span>
          </div>
          <p className="font-heading text-2xl font-semibold text-bark">{stats?.moyenneHumidite?.toFixed(1) ?? '-'}%</p>
        </Card>
        <Card>
          <div className="flex items-center gap-2 mb-2">
            <FlaskConical className="w-4 h-4 text-olive" />
            <span className="text-sm text-stone">pH moyen</span>
          </div>
          <p className="font-heading text-2xl font-semibold text-bark">{stats?.moyennePH?.toFixed(1) ?? '-'}</p>
        </Card>
      </div>

      <Card>
        <div className="flex items-center justify-between mb-4">
          <h3 className="font-heading text-base font-semibold text-bark">Alertes récentes</h3>
          {stats?.recentAlertes?.length > 0 && (
            <button onClick={() => navigate('/manager/alerts')} className="text-xs text-sage-dark hover:underline cursor-pointer">Voir tout</button>
          )}
        </div>
        {stats?.recentAlertes?.length > 0 ? (
          <div className="space-y-3">
            {stats.recentAlertes.map(a => (
              <div
                key={a.id}
                onClick={() => navigate('/manager/alerts')}
                role="button"
                tabIndex={0}
                onKeyDown={(e) => { if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); navigate('/manager/alerts'); } }}
                className="flex items-center justify-between p-3 bg-cream rounded-xl cursor-pointer transition-all hover:shadow-sm hover:bg-cream/70"
              >
                <div>
                  <div className="flex items-center gap-2">
                    <Badge value={a.niveau} />
                    <span className="text-sm font-medium text-bark">{a.type}</span>
                  </div>
                  <p className="text-xs text-stone mt-1">{a.message}</p>
                </div>
                <span className="text-xs text-stone">{a.parcelleNom}</span>
              </div>
            ))}
          </div>
        ) : (
          <p className="text-sm text-stone">Aucune alerte récente</p>
        )}
      </Card>
    </div>
  );
}
