import { useState } from 'react';
import { Search, ChevronLeft, ChevronRight } from 'lucide-react';

export default function DataTable({ columns, data, actions, searchPlaceholder = 'Rechercher...', pageSize = 8, onRowClick }) {
  const [search, setSearch] = useState('');
  const [page, setPage] = useState(0);

  const filtered = data.filter(row =>
    columns.some(col => {
      const val = typeof col.accessor === 'function' ? col.accessor(row) : row[col.accessor];
      return String(val ?? '').toLowerCase().includes(search.toLowerCase());
    })
  );

  const totalPages = Math.ceil(filtered.length / pageSize);
  const paged = filtered.slice(page * pageSize, (page + 1) * pageSize);

  return (
    <div>
      <div className="flex items-center gap-3 mb-4">
        <div className="relative flex-1 max-w-sm">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-stone" />
          <input
            type="text"
            value={search}
            onChange={e => { setSearch(e.target.value); setPage(0); }}
            placeholder={searchPlaceholder}
            className="w-full pl-10 pr-4 py-2.5 bg-warm-white border border-parchment rounded-xl text-sm text-bark placeholder-stone-light focus:outline-none focus:border-sage focus:ring-1 focus:ring-sage"
          />
        </div>
      </div>
      <div className="bg-warm-white border border-parchment rounded-2xl overflow-hidden">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-parchment bg-cream">
              {columns.map(col => (
                <th key={col.header} className="text-left px-4 py-3 text-stone font-medium">{col.header}</th>
              ))}
              {actions && <th className="text-right px-4 py-3 text-stone font-medium">Actions</th>}
            </tr>
          </thead>
          <tbody>
            {paged.length === 0 ? (
              <tr><td colSpan={columns.length + (actions ? 1 : 0)} className="px-4 py-8 text-center text-stone">Aucune donnée trouvée</td></tr>
            ) : paged.map((row, i) => (
              <tr
                key={row.id ?? i}
                onClick={onRowClick ? () => onRowClick(row) : undefined}
                className={`border-b border-parchment/50 hover:bg-cream/50 transition-colors ${onRowClick ? 'cursor-pointer' : ''}`}
              >
                {columns.map(col => (
                  <td key={col.header} className="px-4 py-3 text-charcoal">
                    {col.render ? col.render(row) : (typeof col.accessor === 'function' ? col.accessor(row) : row[col.accessor])}
                  </td>
                ))}
                {actions && <td className="px-4 py-3 text-right">{actions(row)}</td>}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      {totalPages > 1 && (
        <div className="flex items-center justify-between mt-4 text-sm text-stone">
          <span>{filtered.length} résultat{filtered.length > 1 ? 's' : ''}</span>
          <div className="flex items-center gap-2">
            <button onClick={() => setPage(p => Math.max(0, p - 1))} disabled={page === 0}
              className="p-1.5 rounded-lg hover:bg-warm-white disabled:opacity-30 cursor-pointer disabled:cursor-not-allowed">
              <ChevronLeft className="w-4 h-4" />
            </button>
            <span>{page + 1} / {totalPages}</span>
            <button onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))} disabled={page >= totalPages - 1}
              className="p-1.5 rounded-lg hover:bg-warm-white disabled:opacity-30 cursor-pointer disabled:cursor-not-allowed">
              <ChevronRight className="w-4 h-4" />
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
