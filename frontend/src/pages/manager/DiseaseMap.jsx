import { useState, useEffect } from 'react';
import { getParcelles } from '../../api/parcelleApi';
import { getParcelleDetail } from '../../api/iotApi';
import Card from '../../components/ui/Card';
import Badge from '../../components/ui/Badge';
import LoadingSpinner from '../../components/ui/LoadingSpinner';
import { DiseaseBarChart, DiseasePieChart } from '../../components/charts/DiseaseChart';
import { Bug, MapPin, Shield } from 'lucide-react';

export default function DiseaseMap() {
  const [parcelles, setParcelles] = useState([]);
  const [diseaseData, setDiseaseData] = useState({});
  const [loading, setLoading] = useState(true);
  const [allDiseases, setAllDiseases] = useState([]);

  useEffect(() => {
    getParcelles().then(async (res) => {
      setParcelles(res.data);
      const diseases = {};
      const all = [];
      for (const p of res.data) {
        try {
          const detail = await getParcelleDetail(p.id);
          diseases[p.id] = {
            diseases: detail.data.maladiesDetectees || [],
            stats: detail.data.stats,
            latitude: detail.data.latitude,
            longitude: detail.data.longitude,
          };
          all.push(...(detail.data.maladiesDetectees || []).map(d => ({ ...d, parcelleNom: p.nom })));
        } catch { diseases[p.id] = { diseases: [], stats: null }; }
      }
      setDiseaseData(diseases);
      setAllDiseases(all);
      setLoading(false);
    }).catch(() => setLoading(false));
  }, []);

  if (loading) return <LoadingSpinner />;

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-3">
        <Bug className="w-6 h-6 text-red-500" />
        <h2 className="font-heading text-xl font-bold text-bark">Carte des Maladies</h2>
      </div>

      {/* Overview stats */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        <Card>
          <div className="text-center">
            <p className="text-2xl font-heading font-bold text-bark">{parcelles.length}</p>
            <p className="text-xs text-stone">Parcelles</p>
          </div>
        </Card>
        <Card>
          <div className="text-center">
            <p className="text-2xl font-heading font-bold text-red-500">{allDiseases.length}</p>
            <p className="text-xs text-stone">Maladies détectées</p>
          </div>
        </Card>
        <Card>
          <div className="text-center">
            <p className="text-2xl font-heading font-bold text-amber-500">
              {parcelles.filter(p => (diseaseData[p.id]?.diseases?.length || 0) > 0).length}
            </p>
            <p className="text-xs text-stone">Parcelles affectées</p>
          </div>
        </Card>
        <Card>
          <div className="text-center">
            <p className="text-2xl font-heading font-bold text-sage-dark">
              {parcelles.filter(p => (diseaseData[p.id]?.diseases?.length || 0) === 0).length}
            </p>
            <p className="text-xs text-stone">Parcelles saines</p>
          </div>
        </Card>
      </div>

      {/* Charts */}
      {allDiseases.length > 0 && (
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <Card>
            <h3 className="font-heading text-base font-semibold text-bark mb-4">Maladies par type (toutes parcelles)</h3>
            <DiseaseBarChart diseases={allDiseases} />
          </Card>
          <Card>
            <h3 className="font-heading text-base font-semibold text-bark mb-4">Distribution globale</h3>
            <DiseasePieChart diseases={allDiseases} />
          </Card>
        </div>
      )}

      {/* Per-parcelle disease list */}
      <div className="space-y-4">
        <h3 className="font-heading text-base font-semibold text-bark">Maladies détectées par parcelle</h3>
        {parcelles.map(p => {
          const data = diseaseData[p.id] || {};
          const diseases = data.diseases || [];
          return (
            <Card key={p.id}>
              <div className="flex items-center justify-between mb-3">
                <div className="flex items-center gap-3">
                  <MapPin className="w-4 h-4 text-stone" />
                  <span className="font-medium text-bark">{p.nom}</span>
                  <span className="text-xs text-stone">{p.typeCulture}</span>
                </div>
                <Badge color={diseases.length > 0 ? 'red' : 'green'}>
                  {diseases.length > 0 ? `${diseases.length} maladie(s)` : 'Saine'}
                </Badge>
              </div>
              {diseases.length > 0 ? (
                <div className="space-y-2">
                  {diseases.map(d => (
                    <div key={d.id} className="bg-red-50 rounded-xl p-3">
                      <div className="flex items-center justify-between mb-1">
                        <div className="flex items-center gap-2">
                          <Bug className="w-4 h-4 text-red-500" />
                          <span className="text-bark font-medium text-sm">{d.maladieDetectee}</span>
                        </div>
                        <div className="flex items-center gap-2">
                          <span className="text-stone text-xs">{new Date(d.dateAnalyse).toLocaleDateString('fr-FR')}</span>
                          <Badge color="red">{(d.niveauConfiance * 100).toFixed(0)}%</Badge>
                        </div>
                      </div>
                      {d.recommandation && (
                        <p className="text-xs text-charcoal mt-1.5 pl-6">
                          <span className="font-medium text-sage-dark">Recommandation: </span>{d.recommandation}
                        </p>
                      )}
                    </div>
                  ))}
                </div>
              ) : (
                <div className="flex items-center gap-2 text-sm text-sage-dark">
                  <Shield className="w-4 h-4" /> Aucune maladie détectée — parcelle saine
                </div>
              )}
            </Card>
          );
        })}
      </div>
    </div>
  );
}
