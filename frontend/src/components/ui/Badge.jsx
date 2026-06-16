const badgeColors = {
  ACTIF: 'bg-sage-mist text-sage-dark',
  INACTIF: 'bg-warm-white text-stone',
  HAUTE: 'bg-terracotta-lt text-terracotta',
  MOYENNE: 'bg-wheat-light text-wheat',
  BASSE: 'bg-sage-mist text-sage-dark',
  A_FAIRE: 'bg-wheat-light text-wheat',
  EN_COURS: 'bg-sage-mist text-olive',
  TERMINEE: 'bg-sage-mist text-sage-dark',
  NON_TRAITE: 'bg-terracotta-lt text-terracotta',
  TRAITE: 'bg-sage-mist text-sage-dark',
  CRITIQUE: 'bg-terracotta-lt text-terracotta',
  WARNING: 'bg-wheat-light text-wheat',
  INFO: 'bg-sage-mist text-sage-dark',
  RAPPORT: 'bg-sage-mist text-sage-dark',
  PLAINTE: 'bg-terracotta-lt text-terracotta',
  PROPRIETAIRE: 'bg-wheat-light text-wheat',
  GESTIONNAIRE: 'bg-sage-mist text-olive',
  AGRICULTEUR: 'bg-sage-mist text-sage-dark',
};

const labels = {
  A_FAIRE: 'À faire',
  EN_COURS: 'En cours',
  TERMINEE: 'Terminée',
  NON_TRAITE: 'Non traité',
  TRAITE: 'Traité',
  PROPRIETAIRE: 'Propriétaire',
  GESTIONNAIRE: 'Gestionnaire',
  AGRICULTEUR: 'Agriculteur',
};

const colorClasses = {
  red: 'bg-terracotta-lt text-terracotta',
  green: 'bg-sage-mist text-sage-dark',
  yellow: 'bg-wheat-light text-wheat',
  blue: 'bg-sky-100 text-sky-700',
  gray: 'bg-warm-white text-stone',
};

export default function Badge({ value, color, children }) {
  if (color) {
    const className = colorClasses[color] || colorClasses.gray;
    return (
      <span className={`inline-flex px-2.5 py-0.5 rounded-full text-xs font-medium ${className}`}>
        {children}
      </span>
    );
  }

  const className = badgeColors[value] || 'bg-warm-white text-stone';
  const label = labels[value] || value;
  return (
    <span className={`inline-flex px-2.5 py-0.5 rounded-full text-xs font-medium ${className}`}>
      {label}
    </span>
  );
}
