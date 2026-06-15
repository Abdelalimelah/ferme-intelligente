import { Inbox } from 'lucide-react';

export default function EmptyState({ message = 'Aucune donnée disponible', icon: Icon = Inbox }) {
  return (
    <div className="flex flex-col items-center justify-center py-12 text-stone">
      <Icon className="w-12 h-12 mb-3 text-stone-light" />
      <p className="text-sm">{message}</p>
    </div>
  );
}
