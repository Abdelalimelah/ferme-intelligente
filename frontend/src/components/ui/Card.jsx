export default function Card({ children, className = '', elevated = false }) {
  return (
    <div className={`bg-warm-white border border-parchment rounded-2xl p-6 ${elevated ? 'shadow-md' : ''} ${className}`}>
      {children}
    </div>
  );
}
