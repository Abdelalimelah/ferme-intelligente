import { render, screen, fireEvent } from '@testing-library/react';
import { describe, it, expect, vi } from 'vitest';
import Input from './Input';

describe('Input', () => {
  it('renders without a label', () => {
    render(<Input placeholder="Email" />);
    expect(screen.getByPlaceholderText('Email')).toBeInTheDocument();
  });

  it('renders a label associated with the input via htmlFor/id', () => {
    render(<Input label="Email" />);
    const input = screen.getByLabelText('Email');
    expect(input).toBeInTheDocument();
  });

  it('uses the provided id instead of generating one', () => {
    render(<Input label="Mot de passe" id="pwd" />);
    expect(screen.getByLabelText('Mot de passe')).toHaveAttribute('id', 'pwd');
  });

  it('forwards input props like value and onChange', () => {
    const onChange = vi.fn();
    render(<Input label="Nom" value="Karim" onChange={onChange} />);
    const input = screen.getByLabelText('Nom');
    expect(input).toHaveValue('Karim');
    fireEvent.change(input, { target: { value: 'Ahmed' } });
    expect(onChange).toHaveBeenCalled();
  });
});
