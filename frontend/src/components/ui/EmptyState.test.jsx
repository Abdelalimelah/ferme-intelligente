import { render, screen } from '@testing-library/react';
import { describe, it, expect } from 'vitest';
import { ListTodo } from 'lucide-react';
import EmptyState from './EmptyState';

describe('EmptyState', () => {
  it('renders the default message when none is provided', () => {
    render(<EmptyState />);
    expect(screen.getByText('Aucune donnée disponible')).toBeInTheDocument();
  });

  it('renders a custom message', () => {
    render(<EmptyState message="Aucune tâche dans cette catégorie" />);
    expect(screen.getByText('Aucune tâche dans cette catégorie')).toBeInTheDocument();
  });

  it('renders a custom icon when provided', () => {
    const { container } = render(<EmptyState icon={ListTodo} />);
    expect(container.querySelector('svg')).toBeInTheDocument();
  });
});
