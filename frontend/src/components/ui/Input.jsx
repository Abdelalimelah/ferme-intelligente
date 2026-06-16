import { useId } from 'react';

export default function Input({ label, className = '', id, ...props }) {
  const generatedId = useId();
  const inputId = id || generatedId;

  return (
    <div className={className}>
      {label && (
        <label htmlFor={inputId} className="block text-sm font-medium text-charcoal mb-1.5">
          {label}
        </label>
      )}
      <input
        id={inputId}
        className="w-full px-3.5 py-2.5 bg-warm-white border border-parchment rounded-xl text-sm text-bark placeholder-stone-light focus:outline-none focus:border-sage focus:ring-1 focus:ring-sage transition-colors"
        {...props}
      />
    </div>
  );
}
