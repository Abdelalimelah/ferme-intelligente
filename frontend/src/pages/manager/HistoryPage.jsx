import { useState, useEffect } from 'react';
import { getCapteurs } from '../../api/capteurApi';
import { getDonneesByRange } from '../../api/donneeCapteurApi';
import Card from '../../components/ui/Card';
import Select from '../../components/ui/Select';
import Input from '../../components/ui/Input';
import Button from '../../components/ui/Button';
import LoadingSpinner from '../../components/ui/LoadingSpinner';
import EmptyState from '../../components/ui/EmptyState';
import { History, BarChart3 } from 'lucide-react';

export default function HistoryPage() {
  const [capteurs, setCapteurs] = useState([]);
  const [selectedCapteur, setSelectedCapteur] = useState('');
  const [start, setStart] = useState('');
  const [end, setEnd] = useState('');
  const [data, setData] = useState([]);
  const [loading, setLoading] = useState(false);
  const [initLoading, setInitLoading] = useState(true);

  useEffect(() => {
    getCapteurs().then(res => setCapteurs(res.data)).catch(() => {}).finally(() => setInitLoading(false));
  }, []);

  const handleSearch = async () => {
    if (!selectedCapteur || !start || !end) return;
    setLoading(true);
    try {
      const res = await getDonneesByRange(selectedCapteur, start + 'T00:00:00', end + 'T23:59:59');
      setData(res.data);
    } catch { /* best-effort */ }
    setLoading(false);
  };

  const maxVal = data.length > 0 ? Math.max(...data.map(d => d.valeur)) : 1;

  if (initLoading) return <LoadingSpinner />;

  return (
    <div className="space-y-6">
      <Card>
        <div className="flex items-center gap-3 mb-5">
          <div className="p-2.5 bg-sage-mist rounded-xl"><History className="w-5 h-5 text-sage-dark" /></div>
          <h3 className="font-heading text-base font-semibold text-bark">Historique des capteurs</h3>
        </div>
        <div className="grid grid-cols-1 md:grid-cols-4 gap-3">
          <Select
            label="Capteur"
            value={selectedCapteur}
            onChange={e => setSelectedCapteur(e.target.value)}
            options={[{ value: '', label: 'Sélectionner...' }, ...capteurs.map(c => ({ value: c.id, label: `${c.type} - ${c.parcelleNom}` }))]}
          />
          <Input label="Date début" type="date" value={start} onChange={e => setStart(e.target.value)} />
          <Input label="Date fin" type="date" value={end} onChange={e => setEnd(e.target.value)} />
          <div className="flex items-end">
            <Button onClick={handleSearch} disabled={!selectedCapteur || !start || !end} className="w-full">Rechercher</Button>
          </div>
        </div>
      </Card>

      {loading ? <LoadingSpinner /> : data.length > 0 ? (
        <Card>
          <h4 className="font-heading text-sm font-semibold text-bark mb-4">{data.length} relevés</h4>
          <div className="flex items-end gap-1 h-48">
            {data.slice(0, 50).map((d, i) => (
              <div key={d.id ?? i} className="flex-1 flex flex-col items-center justify-end h-full group relative">
                <div
                  className="w-full bg-gradient-to-t from-sage to-sage-light rounded-t-md min-h-[4px] transition-all hover:from-sage-dark hover:to-sage"
                  style={{ height: `${(d.valeur / maxVal) * 100}%` }}
                />
                <div className="hidden group-hover:block absolute -top-8 bg-bark text-white text-xs px-2 py-1 rounded-lg whitespace-nowrap z-10">
                  {d.valeur.toFixed(1)} {d.unite} — {new Date(d.dateReleve).toLocaleDateString('fr-FR')}
                </div>
              </div>
            ))}
          </div>
          <div className="flex justify-between mt-2 text-xs text-stone">
            <span>{data.length > 0 ? new Date(data[data.length - 1].dateReleve).toLocaleDateString('fr-FR') : ''}</span>
            <span>{data.length > 0 ? new Date(data[0].dateReleve).toLocaleDateString('fr-FR') : ''}</span>
          </div>
        </Card>
      ) : selectedCapteur && start && end ? (
        <EmptyState message="Aucune donnée pour cette période" icon={BarChart3} />
      ) : null}
    </div>
  );
}
