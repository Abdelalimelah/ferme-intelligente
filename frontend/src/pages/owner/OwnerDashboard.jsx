import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../hooks/useAuth';
import { getOwnerStats } from '../../api/dashboardApi';
import { getRapportById, updateRapportStatut } from '../../api/rapportApi';
import StatCard from '../../components/ui/StatCard';
import Card from '../../components/ui/Card';
import Badge from '../../components/ui/Badge';
import LoadingSpinner from '../../components/ui/LoadingSpinner';
import RapportDetailModal from '../../components/RapportDetailModal';
import { Building2, Users, UserCheck, FileText } from 'lucide-react';

export default function OwnerDashboard() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [selectedRapport, setSelectedRapport] = useState(null);

  const load = () => {
    getOwnerStats(user.id).then(res => setStats(res.data)).catch(() => {}).finally(() => setLoading(false));
  };

  useEffect(load, [user.id]);

  const openRapport = async (r) => {
    try {
      const res = await getRapportById(r.id);
      setSelectedRapport(res.data);
    } catch {
      setSelectedRapport(r); // fallback to summary data
    }
  };

  const handleChangeStatut = async (id, statut) => {
    await updateRapportStatut(id, statut);
    load();
  };

  if (loading) return <LoadingSpinner />;

  return (
    <div className="space-y-6">
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard icon={<Building2 className="w-5 h-5" />} label="Fermes" value={stats?.totalFermes ?? 0} color="sage" onClick={() => navigate('/owner/assign-manager')} />
        <StatCard icon={<Users className="w-5 h-5" />} label="Gestionnaires" value={stats?.totalGestionnaires ?? 0} color="olive" onClick={() => navigate('/owner/managers')} />
        <StatCard icon={<UserCheck className="w-5 h-5" />} label="Agriculteurs" value={stats?.totalAgriculteurs ?? 0} color="wheat" onClick={() => navigate('/owner/workers')} />
        <StatCard icon={<FileText className="w-5 h-5" />} label="Rapports en attente" value={stats?.rapportsEnAttente ?? 0} color="terracotta" onClick={() => navigate('/owner/reports')} />
      </div>

      <Card>
        <div className="flex items-center justify-between mb-4">
          <h3 className="font-heading text-base font-semibold text-bark">Rapports récents</h3>
          {stats?.recentRapports?.length > 0 && (
            <button onClick={() => navigate('/owner/reports')} className="text-xs text-sage-dark hover:underline cursor-pointer">Voir tout</button>
          )}
        </div>
        {stats?.recentRapports?.length > 0 ? (
          <div className="space-y-3">
            {stats.recentRapports.map(r => (
              <div
                key={r.id}
                onClick={() => openRapport(r)}
                className="flex items-center justify-between p-3 bg-cream rounded-xl cursor-pointer transition-all hover:shadow-sm hover:bg-cream/70"
              >
                <div className="flex-1">
                  <div className="flex items-center gap-2">
                    <p className="text-sm font-medium text-bark">{r.sujet}</p>
                    <Badge value={r.type} />
                  </div>
                  <p className="text-xs text-stone mt-0.5">Par {r.auteurNom} — {new Date(r.dateCreation).toLocaleDateString('fr-FR')}</p>
                </div>
                <Badge value={r.statut} />
              </div>
            ))}
          </div>
        ) : (
          <p className="text-sm text-stone">Aucun rapport récent</p>
        )}
      </Card>

      <RapportDetailModal
        key={selectedRapport?.id}
        rapport={selectedRapport}
        isOpen={!!selectedRapport}
        onClose={() => setSelectedRapport(null)}
        onChangeStatut={handleChangeStatut}
      />
    </div>
  );
}
