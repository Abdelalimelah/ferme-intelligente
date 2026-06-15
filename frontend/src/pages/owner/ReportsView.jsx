import { useState, useEffect } from 'react';
import { getRapports, getRapportById, updateRapportStatut } from '../../api/rapportApi';
import DataTable from '../../components/ui/DataTable';
import Badge from '../../components/ui/Badge';
import Select from '../../components/ui/Select';
import LoadingSpinner from '../../components/ui/LoadingSpinner';
import RapportDetailModal from '../../components/RapportDetailModal';
import { Eye } from 'lucide-react';

export default function ReportsView() {
  const [rapports, setRapports] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState('ALL');
  const [selectedRapport, setSelectedRapport] = useState(null);

  const load = () => {
    setLoading(true);
    getRapports().then(res => setRapports(res.data)).catch(() => {}).finally(() => setLoading(false));
  };

  useEffect(load, []);

  const filtered = filter === 'ALL' ? rapports : rapports.filter(r => r.type === filter || r.statut === filter);

  const handleStatut = async (id, statut) => {
    await updateRapportStatut(id, statut);
    load();
  };

  const openRapport = async (r) => {
    try {
      const res = await getRapportById(r.id);
      setSelectedRapport(res.data);
    } catch {
      setSelectedRapport(r);
    }
  };

  const columns = [
    { header: 'Sujet', accessor: 'sujet' },
    { header: 'Type', accessor: 'type', render: row => <Badge value={row.type} /> },
    { header: 'Auteur', accessor: 'auteurNom' },
    { header: 'Date', accessor: row => new Date(row.dateCreation).toLocaleDateString('fr-FR') },
    { header: 'Statut', accessor: 'statut', render: row => <Badge value={row.statut} /> },
  ];

  if (loading) return <LoadingSpinner />;

  return (
    <div>
      <div className="flex gap-3 mb-4">
        <Select
          options={[
            { value: 'ALL', label: 'Tous' },
            { value: 'RAPPORT', label: 'Rapports' },
            { value: 'PLAINTE', label: 'Plaintes' },
            { value: 'NON_TRAITE', label: 'Non traités' },
            { value: 'EN_COURS', label: 'En cours' },
            { value: 'TRAITE', label: 'Traités' },
          ]}
          value={filter}
          onChange={e => setFilter(e.target.value)}
          className="w-48"
        />
      </div>
      <DataTable
        columns={columns}
        data={filtered}
        searchPlaceholder="Rechercher un rapport..."
        actions={row => (
          <div className="flex items-center gap-2 justify-end" onClick={e => e.stopPropagation()}>
            <button onClick={() => openRapport(row)} className="p-1.5 rounded-lg hover:bg-sage-mist text-stone hover:text-sage-dark cursor-pointer" title="Détail"><Eye className="w-4 h-4" /></button>
            <select
              value={row.statut}
              onChange={e => handleStatut(row.id, e.target.value)}
              className="text-xs px-2 py-1 border border-parchment rounded-lg bg-cream text-bark focus:outline-none focus:border-sage cursor-pointer"
            >
              <option value="NON_TRAITE">Non traité</option>
              <option value="EN_COURS">En cours</option>
              <option value="TRAITE">Traité</option>
            </select>
          </div>
        )}
        onRowClick={openRapport}
      />

      <RapportDetailModal
        rapport={selectedRapport}
        isOpen={!!selectedRapport}
        onClose={() => setSelectedRapport(null)}
        onChangeStatut={handleStatut}
      />
    </div>
  );
}
