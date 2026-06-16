import { render, screen, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import Modal from './Modal';

describe('Modal', () => {
  it('renders nothing when closed', () => {
    render(<Modal isOpen={false} title="Test">Contenu</Modal>);
    expect(screen.queryByText('Contenu')).not.toBeInTheDocument();
  });

  it('renders the title and children when open', () => {
    render(<Modal isOpen title="Détails de la tâche">Contenu</Modal>);
    expect(screen.getByText('Détails de la tâche')).toBeInTheDocument();
    expect(screen.getByText('Contenu')).toBeInTheDocument();
  });

  it('calls onClose when the close button is clicked', () => {
    const onClose = vi.fn();
    render(<Modal isOpen title="Test" onClose={onClose}>Contenu</Modal>);
    fireEvent.click(screen.getByRole('button'));
    expect(onClose).toHaveBeenCalledTimes(1);
  });

  it('calls onClose when the backdrop is clicked', () => {
    const onClose = vi.fn();
    const { container } = render(<Modal isOpen title="Test" onClose={onClose}>Contenu</Modal>);
    fireEvent.click(container.querySelector('[role="presentation"]'));
    expect(onClose).toHaveBeenCalledTimes(1);
  });
});
