import { useEffect, useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';
import { useClickOutside } from '../hooks/useClickOutside';
import { getAlertesUnread, markAlerteAsRead } from '../api/alerteApi';
import { Bell, AlertTriangle, ChevronDown, User, Settings, LogOut, CheckCheck } from 'lucide-react';
import Badge from './ui/Badge';

const alertsPathByRole = {
  GESTIONNAIRE: '/manager/alerts',
};

export default function TopBar({ title }) {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [alertes, setAlertes] = useState([]);
  const [notifOpen, setNotifOpen] = useState(false);
  const [profileOpen, setProfileOpen] = useState(false);

  const notifRef = useClickOutside(useCallback(() => setNotifOpen(false), []));
  const profileRef = useClickOutside(useCallback(() => setProfileOpen(false), []));

  const loadAlertes = useCallback(() => {
    getAlertesUnread().then(res => setAlertes(res.data)).catch(() => {});
  }, []);

  useEffect(() => {
    loadAlertes();
    const interval = setInterval(loadAlertes, 15000); // refresh count periodically
    return () => clearInterval(interval);
  }, [loadAlertes]);

  const unreadCount = alertes.length;
  const alertsPath = alertsPathByRole[user?.role];

  const handleNotifClick = async (alerte) => {
    try { await markAlerteAsRead(alerte.id); } catch {}
    setAlertes(prev => prev.filter(a => a.id !== alerte.id));
    setNotifOpen(false);
    if (alertsPath) navigate(alertsPath);
  };

  const handleMarkAll = async () => {
    const ids = alertes.map(a => a.id);
    try { await Promise.all(ids.map(id => markAlerteAsRead(id))); } catch {}
    setAlertes([]);
  };

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <header className="h-16 border-b border-parchment bg-warm-white/80 backdrop-blur-sm flex items-center justify-between px-6">
      <h2 className="font-heading text-lg font-semibold text-bark">{title}</h2>
      <div className="flex items-center gap-4">
        {/* Notification Bell */}
        <div className="relative" ref={notifRef}>
          <button
            onClick={() => { setNotifOpen(o => !o); setProfileOpen(false); }}
            className="relative p-1.5 rounded-lg hover:bg-cream transition-colors cursor-pointer"
          >
            <Bell className="w-5 h-5 text-stone hover:text-bark transition-colors" />
            {unreadCount > 0 && (
              <span className="absolute -top-0.5 -right-0.5 bg-terracotta text-white text-[10px] font-bold min-w-[18px] h-[18px] px-1 rounded-full flex items-center justify-center">
                {unreadCount > 9 ? '9+' : unreadCount}
              </span>
            )}
          </button>

          {notifOpen && (
            <div className="absolute right-0 mt-2 w-80 bg-warm-white border border-parchment rounded-2xl shadow-xl z-50 overflow-hidden">
              <div className="px-4 py-3 border-b border-parchment flex items-center justify-between">
                <h3 className="font-heading text-sm font-semibold text-bark">Notifications</h3>
                {unreadCount > 0 && <span className="text-xs text-stone">{unreadCount} non lue{unreadCount > 1 ? 's' : ''}</span>}
              </div>
              <div className="max-h-80 overflow-y-auto">
                {alertes.length === 0 ? (
                  <div className="px-4 py-8 text-center text-sm text-stone">
                    <Bell className="w-8 h-8 mx-auto mb-2 text-stone-light opacity-50" />
                    Aucune notification non lue
                  </div>
                ) : (
                  alertes.map(a => (
                    <button
                      key={a.id}
                      onClick={() => handleNotifClick(a)}
                      className="w-full text-left px-4 py-3 hover:bg-cream transition-colors border-b border-parchment/50 last:border-0 cursor-pointer flex items-start gap-3"
                    >
                      <div className={`p-1.5 rounded-lg mt-0.5 shrink-0 ${a.niveau === 'CRITIQUE' ? 'bg-terracotta-lt' : a.niveau === 'WARNING' ? 'bg-wheat-light' : 'bg-sage-mist'}`}>
                        <AlertTriangle className={`w-3.5 h-3.5 ${a.niveau === 'CRITIQUE' ? 'text-terracotta' : a.niveau === 'WARNING' ? 'text-wheat' : 'text-sage-dark'}`} />
                      </div>
                      <div className="flex-1 min-w-0">
                        <div className="flex items-center gap-1.5 mb-0.5">
                          <Badge value={a.niveau} />
                          {a.parcelleNom && <span className="text-xs text-stone truncate">{a.parcelleNom}</span>}
                        </div>
                        <p className="text-xs text-charcoal line-clamp-2">{a.message}</p>
                        <p className="text-[11px] text-stone mt-0.5">{new Date(a.dateCreation).toLocaleString('fr-FR')}</p>
                      </div>
                    </button>
                  ))
                )}
              </div>
              {alertes.length > 0 && (
                <button
                  onClick={handleMarkAll}
                  className="w-full px-4 py-2.5 text-xs font-medium text-sage-dark hover:bg-sage-mist/40 transition-colors border-t border-parchment flex items-center justify-center gap-1.5 cursor-pointer"
                >
                  <CheckCheck className="w-4 h-4" />Tout marquer comme lu
                </button>
              )}
            </div>
          )}
        </div>

        {/* Profile dropdown */}
        <div className="relative pl-4 border-l border-parchment" ref={profileRef}>
          <button
            onClick={() => { setProfileOpen(o => !o); setNotifOpen(false); }}
            className="flex items-center gap-2.5 cursor-pointer hover:bg-cream rounded-xl px-2 py-1.5 transition-colors"
          >
            <div className="w-8 h-8 bg-sage-mist rounded-full flex items-center justify-center text-sage-dark font-medium text-sm">
              {user?.prenom?.[0]}{user?.nom?.[0]}
            </div>
            <div className="text-right">
              <p className="text-sm font-medium text-bark leading-tight">{user?.prenom} {user?.nom}</p>
              <Badge value={user?.role} />
            </div>
            <ChevronDown className={`w-4 h-4 text-stone transition-transform ${profileOpen ? 'rotate-180' : ''}`} />
          </button>

          {profileOpen && (
            <div className="absolute right-0 mt-2 w-56 bg-warm-white border border-parchment rounded-2xl shadow-xl z-50 overflow-hidden">
              <div className="px-4 py-3 border-b border-parchment">
                <p className="text-sm font-semibold text-bark">{user?.prenom} {user?.nom}</p>
                <p className="text-xs text-stone mt-0.5">{user?.email}</p>
              </div>
              <div className="py-1">
                <button
                  onClick={() => { setProfileOpen(false); navigate('/profile'); }}
                  className="w-full flex items-center gap-3 px-4 py-2.5 text-sm text-charcoal hover:bg-cream transition-colors cursor-pointer"
                >
                  <User className="w-4 h-4 text-stone" />Mon profil
                </button>
                <button
                  onClick={() => { setProfileOpen(false); navigate('/profile'); }}
                  className="w-full flex items-center gap-3 px-4 py-2.5 text-sm text-charcoal hover:bg-cream transition-colors cursor-pointer"
                >
                  <Settings className="w-4 h-4 text-stone" />Paramètres
                </button>
              </div>
              <div className="py-1 border-t border-parchment">
                <button
                  onClick={handleLogout}
                  className="w-full flex items-center gap-3 px-4 py-2.5 text-sm text-terracotta hover:bg-terracotta-lt transition-colors cursor-pointer"
                >
                  <LogOut className="w-4 h-4" />Déconnexion
                </button>
              </div>
            </div>
          )}
        </div>
      </div>
    </header>
  );
}
