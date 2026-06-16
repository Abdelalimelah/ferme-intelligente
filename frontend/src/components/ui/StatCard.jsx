export default function StatCard({ icon, label, value, subtitle, color = 'sage', onClick }) {
  const colors = {
    sage: 'bg-sage-mist text-sage-dark',
    terracotta: 'bg-terracotta-lt text-terracotta',
    wheat: 'bg-wheat-light text-wheat',
    olive: 'bg-sage-mist text-olive',
  };

  const clickable = typeof onClick === 'function';

  return (
    <div
      onClick={onClick}
      role={clickable ? 'button' : undefined}
      tabIndex={clickable ? 0 : undefined}
      onKeyDown={clickable ? (e) => { if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); onClick(e); } } : undefined}
      className={`bg-warm-white border border-parchment rounded-2xl p-5 flex items-start gap-4 transition-all ${
        clickable ? 'cursor-pointer hover:shadow-md hover:border-sage-light hover:-translate-y-0.5' : ''
      }`}
    >
      <div className={`p-3 rounded-xl ${colors[color]}`}>
        {icon}
      </div>
      <div>
        <p className="text-stone text-sm">{label}</p>
        <p className="font-heading text-2xl font-semibold text-bark mt-0.5">{value}</p>
        {subtitle && <p className="text-stone text-xs mt-1">{subtitle}</p>}
      </div>
    </div>
  );
}
