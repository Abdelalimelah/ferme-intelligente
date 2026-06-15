const variants = {
  primary: 'bg-gradient-to-r from-sage to-sage-dark text-white hover:from-sage-dark hover:to-sage-dark shadow-sm',
  secondary: 'border-2 border-sage text-sage-dark hover:bg-sage-mist',
  ghost: 'text-charcoal hover:bg-warm-white',
  danger: 'bg-terracotta text-white hover:bg-terracotta/90',
};

export default function Button({ children, variant = 'primary', className = '', ...props }) {
  return (
    <button
      className={`px-4 py-2 rounded-xl font-medium text-sm transition-all duration-200 cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed ${variants[variant]} ${className}`}
      {...props}
    >
      {children}
    </button>
  );
}
