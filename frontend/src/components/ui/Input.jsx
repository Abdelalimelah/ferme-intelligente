export default function Input({ label, className = '', ...props }) {
  return (
    <div className={className}>
      {label && <label className="block text-sm font-medium text-charcoal mb-1.5">{label}</label>}
      <input
        className="w-full px-3.5 py-2.5 bg-warm-white border border-parchment rounded-xl text-sm text-bark placeholder-stone-light focus:outline-none focus:border-sage focus:ring-1 focus:ring-sage transition-colors"
        {...props}
      />
    </div>
  );
}
