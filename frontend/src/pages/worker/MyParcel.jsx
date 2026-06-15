import { useState, useEffect } from 'react';
import { useAuth } from '../../hooks/useAuth';
import { getParcelles } from '../../api/parcelleApi';
import { getCapteursByParcelle } from '../../api/capteurApi';
import Card from '../../components/ui/Card';
import LoadingSpinner from '../../components/ui/LoadingSpinner';
import EmptyState from '../../components/ui/EmptyState';
import { Sprout, Thermometer, Droplets, FlaskConical, Radio, MapPin } from 'lucide-react';

const typeIcons = {
  'Température': Thermometer,
  'Humidité': Droplets,
  'pH': FlaskConical,
};

export default function MyParcel() {
  const { user } = useAuth();
  const [parcelles, setParcelles] = useState([]);
  const [capteurs, setCapteurs] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getParcelles()
      .then(async (res) => {
        setParcelles(res.data);
        if (res.data.length > 0) {
          const capteursRes = await getCapteursByParcelle(res.data[0].id);
          setCapteurs(capteursRes.data);
        }
      })
      .catch(() => {})
      .finally(() => setLoading(false));
  }, [user.id]);

  if (loading) return <LoadingSpinner />;
  if (parcelles.length === 0) return <EmptyState message="Aucune parcelle assignée" icon={Sprout} />;

  const parcelle = parcelles[0];

  return (
    <div className="space-y-6">
      <Card>
        <div className="flex items-center gap-4">
          <div className="p-3 bg-sage-mist rounded-xl">
            <Sprout className="w-6 h-6 text-sage-dark" />
          </div>
          <div>
            <h3 className="font-heading text-lg font-semibold text-bark">{parcelle.nom}</h3>
            <div className="flex items-center gap-3 text-sm text-stone mt-1">
              <span>{parcelle.typeCulture || 'Culture non définie'}</span>
              <span>·</span>
              <span>{parcelle.surface ? `${parcelle.surface} ha` : '-'}</span>
              {parcelle.coordonneesGps && (
                <>
                  <span>·</span>
                  <span className="flex items-center gap-1"><MapPin className="w-3.5 h-3.5" />{parcelle.coordonneesGps}</span>
                </>
              )}
            </div>
          </div>
        </div>
      </Card>

      <div>
        <h3 className="font-heading text-base font-semibold text-bark mb-3">Capteurs</h3>
        {capteurs.length > 0 ? (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {capteurs.map(c => {
              const Icon = typeIcons[c.type] || Radio;
              const isActive = c.statut === 'ACTIF';
              return (
                <Card key={c.id}>
                  <div className="flex items-center justify-between mb-3">
                    <div className="flex items-center gap-2">
                      <Icon className={`w-5 h-5 ${isActive ? 'text-sage-dark' : 'text-terracotta'}`} />
                      <span className="text-sm font-medium text-bark">{c.type}</span>
                    </div>
                    <span className={`w-2 h-2 rounded-full ${isActive ? 'bg-sage' : 'bg-terracotta'}`} />
                  </div>
                  <p className="font-heading text-2xl font-semibold text-bark">
                    {c.derniereValeur != null ? c.derniereValeur.toFixed(1) : '-'}
                    <span className="text-sm font-normal text-stone ml-1">{c.unite}</span>
                  </p>
                </Card>
              );
            })}
          </div>
        ) : (
          <EmptyState message="Aucun capteur sur cette parcelle" icon={Radio} />
        )}
      </div>
    </div>
  );
}
