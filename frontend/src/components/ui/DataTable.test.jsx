import { render, screen, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import DataTable from './DataTable';

const columns = [
  { header: 'Nom', accessor: 'nom' },
  { header: 'Surface', accessor: row => `${row.surface} ha` },
];

const data = [
  { id: 1, nom: 'Parcelle Nord', surface: 12.5 },
  { id: 2, nom: 'Parcelle Sud', surface: 10.2 },
];

describe('DataTable', () => {
  it('renders a row per data item with accessor and function columns', () => {
    render(<DataTable columns={columns} data={data} />);
    expect(screen.getByText('Parcelle Nord')).toBeInTheDocument();
    expect(screen.getByText('12.5 ha')).toBeInTheDocument();
  });

  it('shows an empty state when there is no data', () => {
    render(<DataTable columns={columns} data={[]} />);
    expect(screen.getByText('Aucune donnée trouvée')).toBeInTheDocument();
  });

  it('filters rows as the user types in the search box', () => {
    render(<DataTable columns={columns} data={data} />);
    fireEvent.change(screen.getByPlaceholderText('Rechercher...'), { target: { value: 'Sud' } });

    expect(screen.getByText('Parcelle Sud')).toBeInTheDocument();
    expect(screen.queryByText('Parcelle Nord')).not.toBeInTheDocument();
  });

  it('calls onRowClick with the row data when a row is clicked', () => {
    const onRowClick = vi.fn();
    render(<DataTable columns={columns} data={data} onRowClick={onRowClick} />);

    fireEvent.click(screen.getByText('Parcelle Nord'));
    expect(onRowClick).toHaveBeenCalledWith(data[0]);
  });

  it('renders a custom render function for a column over the accessor', () => {
    const withRender = [
      { header: 'Nom', accessor: 'nom', render: row => `★ ${row.nom}` },
    ];
    render(<DataTable columns={withRender} data={data} />);
    expect(screen.getByText('★ Parcelle Nord')).toBeInTheDocument();
  });

  it('renders the actions column when actions is provided', () => {
    render(<DataTable columns={columns} data={data} actions={() => <button>Modifier</button>} />);
    expect(screen.getAllByText('Modifier')).toHaveLength(2);
  });

  it('paginates rows beyond the page size and advances on next', () => {
    const manyRows = Array.from({ length: 10 }, (_, i) => ({ id: i, nom: `Parcelle ${i}`, surface: 1 }));
    render(<DataTable columns={columns} data={manyRows} pageSize={8} />);

    expect(screen.getByText('10 résultats')).toBeInTheDocument();
    expect(screen.getByText('1 / 2')).toBeInTheDocument();
    expect(screen.queryByText('Parcelle 9')).not.toBeInTheDocument();

    const [, nextPageButton] = screen.getAllByRole('button');
    fireEvent.click(nextPageButton);

    expect(screen.getByText('2 / 2')).toBeInTheDocument();
    expect(screen.getByText('Parcelle 9')).toBeInTheDocument();
    expect(screen.queryByText('Parcelle 0')).not.toBeInTheDocument();
  });
});
