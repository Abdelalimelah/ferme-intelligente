import { useState, useRef } from 'react';
import { getParcelles } from '../../api/parcelleApi';
import { analyserUpload, getDiseasesByParcelle } from '../../api/aiApi';
import { useAsyncData } from '../../hooks/useAsyncData';
import Button from '../../components/ui/Button';
import LoadingSpinner from '../../components/ui/LoadingSpinner';
import EmptyState from '../../components/ui/EmptyState';
import {
  Microscope, Leaf, AlertTriangle, CheckCircle, ChevronDown, Loader,
  Image as ImageIcon,
} from 'lucide-react';

export default function AnalysePlante() {
  const { data: parcelles, loading, error, setError } = useAsyncData(
    () => getParcelles().then(r => r.data),
    [],
    { initialData: [], errorMessage: 'Erreur lors du chargement des parcelles' },
  );
  const [selectedIdOverride, setSelectedIdOverride] = useState(null);
  const selectedId = selectedIdOverride ?? (parcelles[0] ? String(parcelles[0].id) : '');

  const { data: history, setData: setHistory } = useAsyncData(
    () => selectedId ? getDiseasesByParcelle(selectedId).then(r => r.data) : Promise.resolve([]),
    [selectedId],
    { initialData: [] },
  );
  const latest = history[0] || null;

  const [analysing, setAnalysing]   = useState(false);
  const [file, setFile]             = useState(null);
  const [previewUrl, setPreviewUrl] = useState(null);
  const fileInputRef = useRef(null);

  const handleFileChange = (e) => {
    const f = e.target.files?.[0];
    if (!f) return;
    setFile(f);
    setPreviewUrl(URL.createObjectURL(f));
  };

  const handleAnalyse = async () => {
    if (!selectedId || !file) return;
    setAnalysing(true);
    setError('');
    try {
      const res = await analyserUpload(selectedId, file);
      setHistory(prev => [res.data, ...prev]);
      setFile(null);
      setPreviewUrl(null);
      if (fileInputRef.current) fileInputRef.current.value = '';
    } catch (e) {
      setError(e.response?.data?.message || 'Erreur lors de l\'analyse');
    } finally {
      setAnalysing(false);
    }
  };

  const selectedParcelle = parcelles.find(p => String(p.id) === selectedId);

  if (loading) return <LoadingSpinner />;

  return (
    <div className="space-y-6">
      <div>
        <h2 className="font-heading text-xl font-semibold text-bark">Analyse IA des Maladies</h2>
        <p className="text-sm text-stone mt-0.5">
          Choisis une parcelle, importe une photo, et le modèle détecte la maladie.
        </p>
      </div>

      <div className="bg-warm-white border border-parchment rounded-2xl p-5 flex flex-wrap items-center gap-4">
        {/* Parcelle selector */}
        <div className="relative">
          <select
            value={selectedId}
            onChange={e => setSelectedIdOverride(e.target.value)}
            className="appearance-none bg-cream border border-parchment text-bark text-sm rounded-xl pl-3 pr-8 py-2.5 focus:outline-none focus:ring-2 focus:ring-sage cursor-pointer"
          >
            {parcelles.map(p => (
              <option key={p.id} value={p.id}>
                {p.nom} — {p.typeCulture}
              </option>
            ))}
          </select>
          <ChevronDown className="absolute right-2.5 top-1/2 -translate-y-1/2 w-4 h-4 text-stone pointer-events-none" />
        </div>

        {/* File picker */}
        <label className="flex items-center gap-2 px-3 py-2.5 rounded-xl border border-dashed border-sage text-sage-dark text-sm font-medium cursor-pointer hover:bg-sage-mist transition-colors">
          <ImageIcon className="w-4 h-4" />
          {file ? file.name : 'Choisir une photo…'}
          <input ref={fileInputRef} type="file" accept="image/*" onChange={handleFileChange} className="hidden" />
        </label>

        {previewUrl && (
          <img src={previewUrl} alt="Aperçu" className="w-12 h-12 object-cover rounded-lg border border-parchment" />
        )}

        <Button onClick={handleAnalyse} disabled={analysing || !selectedId || !file} className="ml-auto">
          {analysing
            ? <><Loader className="w-4 h-4 mr-2 animate-spin inline" />Analyse en cours…</>
            : <><Microscope className="w-4 h-4 mr-1.5 inline" />Lancer l'analyse</>}
        </Button>
      </div>

      {error && (
        <div className="bg-terracotta-lt text-terracotta text-sm px-4 py-2.5 rounded-xl">
          {error}
        </div>
      )}

      {latest ? (
        <AnalysisCard result={latest} />
      ) : (
        !analysing && (
          <EmptyState
            message={`Aucune analyse pour ${selectedParcelle?.nom || 'cette parcelle'}. Choisis une photo et lance l'analyse.`}
            icon={Microscope}
          />
        )
      )}

      {history.length > 1 && (
        <div>
          <h3 className="font-medium text-bark mb-3">Historique des analyses</h3>
          <div className="space-y-3">
            {history.slice(1).map(r => (
              <HistoryRow key={r.id} result={r} />
            ))}
          </div>
        </div>
      )}
    </div>
  );
}

function AnalysisCard({ result }) {
  const healthy    = result.sain || result.maladieDetectee === 'healthy' || result.maladieDetectee === 'Healthy';
  const pct        = result.niveauConfiance ? Math.round(result.niveauConfiance * 100) : 0;
  const displayName = result.maladieFr || result.maladieDetectee || '—';

  return (
    <div className="bg-warm-white border border-parchment rounded-2xl overflow-hidden">
      <div className="grid md:grid-cols-2 gap-0">
        {/* Image */}
        <div className="bg-parchment min-h-56 flex items-center justify-center">
          {result.imageUrl ? (
            <img
              src={result.imageUrl}
              alt="Image analysée"
              className="w-full h-64 object-cover"
            />
          ) : (
            <div className="flex flex-col items-center gap-2 text-stone p-8">
              <Leaf className="w-10 h-10 opacity-40" />
              <span className="text-sm">Image non disponible</span>
            </div>
          )}
        </div>

        {/* Result */}
        <div className="p-6 space-y-4">
          <div className="flex items-center gap-2">
            {healthy
              ? <CheckCircle className="w-5 h-5 text-sage-dark" />
              : <AlertTriangle className="w-5 h-5 text-terracotta" />}
            <span className={`text-sm font-medium ${healthy ? 'text-sage-dark' : 'text-terracotta'}`}>
              {healthy ? 'Plante saine' : 'Maladie détectée'}
            </span>
          </div>

          <div>
            <p className="text-2xl font-heading font-semibold text-bark">{displayName}</p>
            {result.className && (
              <p className="text-xs text-stone mt-1 font-mono">{result.className}</p>
            )}
          </div>

          {/* Confidence bar */}
          <div>
            <div className="flex justify-between text-xs text-stone mb-1">
              <span>Confiance</span>
              <span className="font-medium text-bark">{pct}%</span>
            </div>
            <div className="h-2 bg-parchment rounded-full overflow-hidden">
              <div
                className={`h-full rounded-full transition-all ${healthy ? 'bg-sage' : pct > 85 ? 'bg-terracotta' : 'bg-amber-400'}`}
                style={{ width: `${pct}%` }}
              />
            </div>
          </div>

          {result.typeCulture && (
            <div className="text-xs text-stone">
              Culture : <span className="font-medium text-bark">{result.typeCulture}</span>
              {result.parcelleNom && <> — {result.parcelleNom}</>}
            </div>
          )}

          {result.recommandation && (
            <div className="bg-cream rounded-xl p-3">
              <p className="text-xs font-medium text-bark mb-1">Recommandation</p>
              <p className="text-xs text-charcoal leading-relaxed">{result.recommandation}</p>
            </div>
          )}

          <p className="text-xs text-stone">
            {result.dateAnalyse
              ? new Date(result.dateAnalyse).toLocaleString('fr-FR')
              : ''}
          </p>
        </div>
      </div>
    </div>
  );
}

function HistoryRow({ result }) {
  const healthy    = result.sain || result.maladieDetectee === 'Healthy';
  const pct        = result.niveauConfiance ? Math.round(result.niveauConfiance * 100) : 0;
  const displayName = result.maladieFr || result.maladieDetectee || '—';

  return (
    <div className="flex items-center gap-4 bg-warm-white border border-parchment rounded-xl p-4">
      {result.imageUrl ? (
        <img src={result.imageUrl} alt="" className="w-14 h-14 object-cover rounded-lg flex-shrink-0" />
      ) : (
        <div className="w-14 h-14 bg-parchment rounded-lg flex items-center justify-center flex-shrink-0">
          <Leaf className="w-6 h-6 text-stone opacity-40" />
        </div>
      )}
      <div className="flex-1 min-w-0">
        <p className="text-sm font-medium text-bark truncate">{displayName}</p>
        <p className="text-xs text-stone">
          {result.dateAnalyse ? new Date(result.dateAnalyse).toLocaleString('fr-FR') : ''}
        </p>
      </div>
      <div className="text-right flex-shrink-0">
        <span className={`text-xs font-medium px-2 py-1 rounded-lg ${healthy ? 'bg-sage-mist text-sage-dark' : 'bg-terracotta-lt text-terracotta'}`}>
          {healthy ? 'Sain' : 'Maladie'}
        </span>
        <p className="text-xs text-stone mt-1">{pct}%</p>
      </div>
    </div>
  );
}
