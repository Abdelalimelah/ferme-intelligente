import { render, screen } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import Card from './Card';

describe('Card', () => {
  it('renders its children', () => {
    render(<Card>Contenu de la carte</Card>);
    expect(screen.getByText('Contenu de la carte')).toBeInTheDocument();
  });

  it('does not apply the elevated shadow by default', () => {
    render(<Card>Contenu</Card>);
    expect(screen.getByText('Contenu')).not.toHaveClass('shadow-md');
  });

  it('applies the elevated shadow when requested', () => {
    render(<Card elevated>Contenu</Card>);
    expect(screen.getByText('Contenu')).toHaveClass('shadow-md');
  });

  it('merges a custom className', () => {
    render(<Card className="custom-class">Contenu</Card>);
    expect(screen.getByText('Contenu')).toHaveClass('custom-class');
  });
});
