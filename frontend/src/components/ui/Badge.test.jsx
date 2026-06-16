import { render, screen } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import Badge from './Badge';

describe('Badge', () => {
  it('renders the friendly label for a known value', () => {
    render(<Badge value="GESTIONNAIRE" />);
    expect(screen.getByText('Gestionnaire')).toBeInTheDocument();
  });

  it('falls back to the raw value for an unmapped status', () => {
    render(<Badge value="SOME_UNKNOWN_STATUS" />);
    expect(screen.getByText('SOME_UNKNOWN_STATUS')).toBeInTheDocument();
  });

  it('applies the color class for a known value', () => {
    render(<Badge value="CRITIQUE" />);
    expect(screen.getByText('CRITIQUE')).toHaveClass('text-terracotta');
  });
});
