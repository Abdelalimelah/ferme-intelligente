import { useState, useEffect, useCallback } from 'react';
import { useParams, Link } from 'react-router-dom';
import { getParcelleDetail, getSensorHistory } from '../../api/iotApi';
import { getDiseasesByParcelle } from '../../api/aiApi';
import Card from '../../components/ui/Card';
import Badge from '../../components/ui/Badge';
import LoadingSpinner from '../../components/ui/LoadingSpinner';
import SensorGauge from '../../components/SensorGauge';
import SensorChart from '../../components/charts/SensorChart';
import { DiseaseBarChart, DiseasePieChart } from '../../components/charts/DiseaseChart';
import {
  Sprout, MapPin, AlertTriangle, Bug, Image as ImageIcon, Activity,
  RefreshCw, Thermometer, Droplets, FlaskConical, ArrowLeft, Bell
} from 'lucide-react';

const POLL_INTERVAL = 5000; // 5 seconds

export default function ParcelleDetail() {
  const { id } = useParams();
  const [detail, setDetail] = useState(null);
  const [loading, setLoading] = useState(true);
  const [sensorHistory, setSensorHistory] = useState({});
  const [selectedCapteur, setSelectedCapteur] = useState(null);
  const [autoRefresh, setAutoRefresh] = useState(true);
  const [lastRefresh, setLastRefresh] = useState(null);
  const [tab, setTab] = useState('sensors');

  const fetchDetail = useCallback(() => {
    getParcelleDetail(id).then(res => {
      setDetail(res.data);
      setLastRefresh(new Date());
      setLoading(false);
    }).catch(() => setLoading(false));
  }, [id]);

  // Initial load
  useEffect(() => { fetchDetail(); }, [fetchDetail]);

  // Auto-refresh polling
  useEffect(() => {
    if (!autoRefresh) return;
    const interval = setInterval(fetchDetail, POLL_INTERVAL);
    return () => clearInterval(interval);
  }, [autoRefresh, fetchDetail]);

  // Load sensor history when capteur selected
  useEffect(() => {
    if (!selectedCapteur) return;
    getSensorHistory(id, selectedCapteur.id).then(res => {
      setSensorHistory(prev => ({ ...prev, [selectedCapteur.id]: res.data }));
    }).catch(() => {});
  }, [id, selectedCapteur]);

  if (loading) return <LoadingSpinner />;
  if (!detail) return <div className="text-center text-stone py-12">Parcelle non trouvée</div>;

  const tabs = [
    { key: 'sensors', label: 'Capteurs Live', icon: Activity },
    { key: 'alerts', label: `Alertes (${detail.alertesRecentes?.length || 0})`, icon: Bell },
    { key: 'diseases', label: `Maladies (${detail.maladiesDetectees?.length || 0})`, icon: Bug },
    { key: 'images', label: `Images (${detail.imagesRecentes?.length || 0})`, icon: ImageIcon },
  ];

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-4">
          <Link to="/manager/parcels" className="p-2 hover:bg-parchment rounded-lg transition-colors">
            <ArrowLeft className="w-5 h-5 text-stone" />
          </Link>
          <div className="p-3 bg-sage-mist rounded-xl">
            <Sprout className="w-6 h-6 text-sage-dark" />
          </div>
          <div>
            <h2 className="font-heading text-xl font-bold text-bark">{detail.nom}</h2>
            <div className="flex items-center gap-3 text-sm text-stone">
              <span>{detail.typeCulture || 'N/A'}</span>
              <span>·</span>
              <span>{detail.surface ? `${detail.surface} ha` : 'N/A'}</span>
              {detail.latitude && (
                <>
                  <span>·</span>
                  <span className="flex items-center gap-1">
                    <MapPin className="w-3.5 h-3.5" />
                    {detail.latitude.toFixed(4)}, {detail.longitude.toFixed(4)}
                  </span>
                </>
              )}
              <span>·</span>
              <span>{detail.fermeNom}</span>
            </div>
          </div>
        </div>
        <div className="flex items-center gap-3">
          <button
            onClick={() => setAutoRefresh(!autoRefresh)}
            className={`flex items-center gap-2 px-3 py-1.5 rounded-lg text-sm font-medium transition-colors ${
              autoRefresh ? 'bg-sage-mist text-sage-dark' : 'bg-parchment text-stone'
            }`}
          >
            <RefreshCw className={`w-4 h-4 ${autoRefresh ? 'animate-spin' : ''}`} style={{ animationDuration: '3s' }} />
            {autoRefresh ? 'Live' : 'Paused'}
          </button>
          {lastRefresh && (
            <span className="text-xs text-stone">
              {lastRefresh.toLocaleTimeString('fr-FR')}
            </span>
          )}
        </div>
      </div>

      {/* Stats Overview */}
      {detail.stats && (
        <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-3">
          <MiniStat icon={Thermometer} label="Temp. moy." value={detail.stats.tempMoyenne != null ? `${detail.stats.tempMoyenne.toFixed(1)}°C` : '--'} color="text-terracotta" />
          <MiniStat icon={Droplets} label="Humid. moy." value={detail.stats.humiditeMoyenne != null ? `${detail.stats.humiditeMoyenne.toFixed(1)}%` : '--'} color="text-blue-500" />
          <MiniStat icon={FlaskConical} label="pH moy." value={detail.stats.phMoyen != null ? detail.stats.phMoyen.toFixed(1) : '--'} color="text-sage-dark" />
          <MiniStat icon={AlertTriangle} label="Alertes" value={detail.stats.totalAlertes} color="text-amber-500" />
          <MiniStat icon={AlertTriangle} label="Critiques" value={detail.stats.alertesCritiques} color="text-terracotta" />
          <MiniStat icon={Bug} label="Maladies" value={detail.stats.maladiesDetectees} color="text-red-500" />
        </div>
      )}

      {/* Tabs */}
      <div className="flex gap-1 bg-parchment p-1 rounded-xl">
        {tabs.map(t => (
          <button
            key={t.key}
            onClick={() => setTab(t.key)}
            className={`flex items-center gap-2 px-4 py-2 rounded-lg text-sm font-medium transition-colors flex-1 justify-center ${
              tab === t.key ? 'bg-white text-bark shadow-sm' : 'text-stone hover:text-bark'
            }`}
          >
            <t.icon className="w-4 h-4" />
            {t.label}
          </button>
        ))}
      </div>

      {/* Tab Content */}
      {tab === 'sensors' && <SensorsTab detail={detail} selectedCapteur={selectedCapteur} setSelectedCapteur={setSelectedCapteur} sensorHistory={sensorHistory} />}
      {tab === 'alerts' && <AlertsTab alertes={detail.alertesRecentes} />}
      {tab === 'diseases' && <DiseasesTab diseases={detail.maladiesDetectees} />}
      {tab === 'images' && <ImagesTab images={detail.imagesRecentes} />}
    </div>
  );
}

function MiniStat({ icon: Icon, label, value, color }) {
  return (
    <div className="bg-white rounded-xl p-3 border border-parchment">
      <div className="flex items-center gap-2 mb-1">
        <Icon className={`w-4 h-4 ${color}`} />
        <span className="text-xs text-stone">{label}</span>
      </div>
      <p className="font-heading text-lg font-bold text-bark">{value}</p>
    </div>
  );
}

function SensorsTab({ detail, selectedCapteur, setSelectedCapteur, sensorHistory }) {
  return (
    <div className="space-y-6">
      {/* Live gauges */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {detail.capteurs?.map(c => (
          <div key={c.id} onClick={() => setSelectedCapteur(c)} className="cursor-pointer">
            <SensorGauge capteur={c} />
          </div>
        ))}
      </div>

      {/* Chart for selected sensor */}
      {selectedCapteur && (
        <Card>
          <h3 className="font-heading text-base font-semibold text-bark mb-4">
            Historique - {selectedCapteur.type}
          </h3>
          <SensorChart
            data={sensorHistory[selectedCapteur.id] || []}
            type={selectedCapteur.type}
            min={selectedCapteur.valeurMin}
            max={selectedCapteur.valeurMax}
            unite={selectedCapteur.unite}
          />
        </Card>
      )}
    </div>
  );
}

function AlertsTab({ alertes = [] }) {
  const niveauColors = { CRITIQUE: 'red', WARNING: 'yellow', INFO: 'blue' };
  return (
    <div className="space-y-3">
      {alertes.length === 0 ? (
        <div className="text-center py-8 text-stone">Aucune alerte</div>
      ) : alertes.map(a => (
        <Card key={a.id}>
          <div className="flex items-start justify-between">
            <div className="flex items-start gap-3">
              <AlertTriangle className={`w-5 h-5 mt-0.5 ${a.niveau === 'CRITIQUE' ? 'text-red-500' : a.niveau === 'WARNING' ? 'text-amber-500' : 'text-blue-500'}`} />
              <div>
                <p className="text-sm font-medium text-bark">{a.message}</p>
                <p className="text-xs text-stone mt-1">
                  {new Date(a.dateCreation).toLocaleString('fr-FR')} · {a.type}
                </p>
              </div>
            </div>
            <Badge color={niveauColors[a.niveau]}>{a.niveau}</Badge>
          </div>
        </Card>
      ))}
    </div>
  );
}

function DiseasesTab({ diseases = [] }) {
  return (
    <div className="space-y-6">
      {diseases.length > 0 && (
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <Card>
            <h3 className="font-heading text-base font-semibold text-bark mb-4">Maladies par type</h3>
            <DiseaseBarChart diseases={diseases} />
          </Card>
          <Card>
            <h3 className="font-heading text-base font-semibold text-bark mb-4">Distribution</h3>
            <DiseasePieChart diseases={diseases} />
          </Card>
        </div>
      )}

      <div className="space-y-3">
        {diseases.length === 0 ? (
          <div className="text-center py-8 text-stone">Aucune maladie détectée</div>
        ) : diseases.map(d => (
          <Card key={d.id}>
            <div className="flex items-start gap-3">
              <Bug className="w-5 h-5 text-red-500 mt-0.5" />
              <div className="flex-1">
                <div className="flex items-center justify-between">
                  <p className="text-sm font-semibold text-bark">{d.maladieDetectee}</p>
                  <Badge color={d.niveauConfiance > 0.9 ? 'red' : 'yellow'}>
                    {(d.niveauConfiance * 100).toFixed(0)}%
                  </Badge>
                </div>
                <p className="text-xs text-stone mt-1">
                  {new Date(d.dateAnalyse).toLocaleString('fr-FR')}
                </p>
                {d.recommandation && (
                  <p className="text-sm text-sage-dark mt-2 bg-sage-mist/50 p-2 rounded-lg">
                    {d.recommandation}
                  </p>
                )}
              </div>
            </div>
          </Card>
        ))}
      </div>
    </div>
  );
}

function ImagesTab({ images = [] }) {
  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
      {images.length === 0 ? (
        <div className="col-span-full text-center py-8 text-stone">Aucune image drone</div>
      ) : images.map(img => (
        <Card key={img.id}>
          <div className="aspect-video bg-parchment rounded-lg flex items-center justify-center mb-3">
            <ImageIcon className="w-8 h-8 text-stone" />
          </div>
          <div className="flex items-center justify-between">
            <div>
              <p className="text-xs text-stone">{new Date(img.dateCapture).toLocaleString('fr-FR')}</p>
              <p className="text-xs text-stone">{img.resolution}</p>
            </div>
            {img.analysee ? (
              <Badge color={img.maladieDetectee === 'Healthy' ? 'green' : 'red'}>
                {img.maladieDetectee || 'Analysée'}
              </Badge>
            ) : (
              <Badge color="gray">Non analysée</Badge>
            )}
          </div>
        </Card>
      ))}
    </div>
  );
}
