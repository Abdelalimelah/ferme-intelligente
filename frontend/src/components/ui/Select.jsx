export default function Select({ label, options = [], className = '', ...props }) {
  return (
    <div className={className}>
      {label && <label className="block text-sm font-medium text-charcoal mb-1.5">{label}</label>}
      <select
        className="w-full px-3.5 py-2.5 bg-warm-white border border-parchment rounded-xl text-sm text-bark focus:outline-none focus:border-sage focus:ring-1 focus:ring-sage transition-colors"
        {...props}
      >
        {options.map(opt => (
          <option key={opt.value} value={opt.value}>{opt.label}</option>
        ))}
      </select>
    </div>
  );
}
