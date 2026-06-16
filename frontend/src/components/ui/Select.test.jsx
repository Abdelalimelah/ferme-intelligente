import { render, screen, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import Select from './Select';

const options = [
  { value: 'A_FAIRE', label: 'À faire' },
  { value: 'EN_COURS', label: 'En cours' },
  { value: 'TERMINEE', label: 'Terminée' },
];

describe('Select', () => {
  it('renders all options', () => {
    render(<Select options={options} />);
    expect(screen.getByRole('option', { name: 'À faire' })).toBeInTheDocument();
    expect(screen.getByRole('option', { name: 'En cours' })).toBeInTheDocument();
    expect(screen.getByRole('option', { name: 'Terminée' })).toBeInTheDocument();
  });

  it('renders a label when provided', () => {
    render(<Select label="Statut" options={options} />);
    expect(screen.getByText('Statut')).toBeInTheDocument();
  });

  it('calls onChange when a different option is selected', () => {
    const onChange = vi.fn();
    render(<Select options={options} onChange={onChange} value="A_FAIRE" />);
    fireEvent.change(screen.getByRole('combobox'), { target: { value: 'TERMINEE' } });
    expect(onChange).toHaveBeenCalled();
  });

  it('renders no options when none are provided', () => {
    render(<Select />);
    expect(screen.queryAllByRole('option')).toHaveLength(0);
  });
});
