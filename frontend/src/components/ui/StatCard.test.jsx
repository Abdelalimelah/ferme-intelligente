import { render, screen, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import StatCard from './StatCard';

describe('StatCard', () => {
  it('renders label, value and subtitle', () => {
    render(<StatCard label="Parcelles" value={12} subtitle="+2 ce mois" />);
    expect(screen.getByText('Parcelles')).toBeInTheDocument();
    expect(screen.getByText('12')).toBeInTheDocument();
    expect(screen.getByText('+2 ce mois')).toBeInTheDocument();
  });

  it('is not interactive when no onClick is given', () => {
    render(<StatCard label="Parcelles" value={12} />);
    expect(screen.queryByRole('button')).not.toBeInTheDocument();
  });

  it('is keyboard- and click-accessible when onClick is given', () => {
    const onClick = vi.fn();
    render(<StatCard label="Alertes" value={3} onClick={onClick} />);

    const card = screen.getByRole('button');
    expect(card).toHaveAttribute('tabIndex', '0');

    fireEvent.click(card);
    expect(onClick).toHaveBeenCalledTimes(1);

    fireEvent.keyDown(card, { key: 'Enter' });
    expect(onClick).toHaveBeenCalledTimes(2);

    fireEvent.keyDown(card, { key: ' ' });
    expect(onClick).toHaveBeenCalledTimes(3);

    fireEvent.keyDown(card, { key: 'Tab' });
    expect(onClick).toHaveBeenCalledTimes(3);
  });
});
