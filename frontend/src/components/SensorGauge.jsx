import { Thermometer, Droplets, FlaskConical, Radio, AlertTriangle } from 'lucide-react';

const typeConfig = {
  'Temperature': { icon: Thermometer, color: 'text-terracotta', bg: 'bg-terracotta/10', unit: '°C' },
  'Humidite': { icon: Droplets, color: 'text-blue-500', bg: 'bg-blue-50', unit: '%' },
  'pH': { icon: FlaskConical, color: 'text-sage-dark', bg: 'bg-sage-mist', unit: 'pH' },
};

export default function SensorGauge({ capteur }) {
  const config = typeConfig[capteur.type] || { icon: Radio, color: 'text-stone', bg: 'bg-parchment', unit: '' };
  const Icon = config.icon;
  const isActive = capteur.statut === 'ACTIF';
  const value = capteur.derniereValeur;
  const hasValue = value != null;

  // Calculate fill percentage for gauge bar
  let fillPercent = 50;
  if (hasValue && capteur.valeurMin != null && capteur.valeurMax != null) {
    const range = capteur.valeurMax - capteur.valeurMin;
    fillPercent = Math.max(0, Math.min(100, ((value - capteur.valeurMin) / range) * 100));
  }

  const lastUpdate = capteur.derniereLecture
    ? new Date(capteur.derniereLecture).toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit', second: '2-digit' })
    : 'Aucune donnée';

  return (
    <div className={`p-4 rounded-xl border ${capteur.enAlerte ? 'border-terracotta bg-terracotta/5' : 'border-parchment bg-white'}`}>
      <div className="flex items-center justify-between mb-3">
        <div className="flex items-center gap-2">
          <div className={`p-2 rounded-lg ${config.bg}`}>
            <Icon className={`w-5 h-5 ${config.color}`} />
          </div>
          <div>
            <p className="text-sm font-medium text-bark">{capteur.type}</p>
            <p className="text-xs text-stone">{lastUpdate}</p>
          </div>
        </div>
        <div className="flex items-center gap-1.5">
          {capteur.enAlerte && <AlertTriangle className="w-4 h-4 text-terracotta animate-pulse" />}
          <span className={`w-2.5 h-2.5 rounded-full ${isActive ? 'bg-sage animate-pulse' : 'bg-gray-300'}`} />
        </div>
      </div>

      <div className="text-center py-2">
        <p className={`font-heading text-3xl font-bold ${capteur.enAlerte ? 'text-terracotta' : 'text-bark'}`}>
          {hasValue ? value.toFixed(1) : '--'}
          <span className="text-sm font-normal text-stone ml-1">{config.unit}</span>
        </p>
      </div>

      {/* Gauge bar */}
      {capteur.valeurMin != null && capteur.valeurMax != null && (
        <div className="mt-2">
          <div className="flex justify-between text-xs text-stone mb-1">
            <span>{capteur.valeurMin}</span>
            <span>{capteur.valeurMax}</span>
          </div>
          <div className="h-2 bg-parchment rounded-full overflow-hidden">
            <div
              className={`h-full rounded-full transition-all duration-500 ${capteur.enAlerte ? 'bg-terracotta' : 'bg-sage'}`}
              style={{ width: `${fillPercent}%` }}
            />
          </div>
        </div>
      )}
    </div>
  );
}
