import { useEffect, useRef, useState, useCallback } from 'react';

/**
 * Subscribes to live sensor readings via STOMP over SockJS.
 *
 * @param {function} onReading  - Called with each IoTDataResponse object
 * @param {number}   [parcelleId] - If provided, subscribes to /topic/sensors/{id}
 *                                  otherwise subscribes to /topic/sensors (all)
 *
 * @returns {{ connected: boolean, error: string|null }}
 *
 * Usage:
 *   const { connected } = useSensorSocket((data) => console.log(data));
 *   const { connected } = useSensorSocket((data) => …, parcelleId);
 */
export function useSensorSocket(onReading, parcelleId = null) {
  const clientRef   = useRef(null);
  const [connected, setConnected] = useState(false);
  const [error,     setError]     = useState(null);

  const stableOnReading = useCallback(onReading, []); // eslint-disable-line

  useEffect(() => {
    let client = null;
    let active = true;

    const connect = async () => {
      try {
        // Lazy-import to avoid SSR / bundler issues
        const [{ Client }, SockJS] = await Promise.all([
          import('@stomp/stompjs'),
          import('sockjs-client').then(m => m.default ?? m),
        ]);

        const wsUrl = (import.meta.env.VITE_WS_URL || 'http://localhost:8080') + '/ws';
        const token = localStorage.getItem('token');

        client = new Client({
          webSocketFactory: () => new SockJS(wsUrl),
          connectHeaders: token ? { Authorization: `Bearer ${token}` } : {},
          reconnectDelay: 5000,
          heartbeatIncoming: 4000,
          heartbeatOutgoing: 4000,

          onConnect: () => {
            if (!active) return;
            setConnected(true);
            setError(null);

            const topic = parcelleId
              ? `/topic/sensors/${parcelleId}`
              : '/topic/sensors';

            client.subscribe(topic, (msg) => {
              try {
                const data = JSON.parse(msg.body);
                stableOnReading(data);
              } catch (e) {
                console.warn('useSensorSocket: failed to parse message', e);
              }
            });
          },

          onDisconnect: () => {
            if (active) setConnected(false);
          },

          onStompError: (frame) => {
            console.error('STOMP error:', frame);
            if (active) setError('Connexion temps réel indisponible');
          },

          onWebSocketError: () => {
            if (active) setError('WebSocket non disponible — utilisation du mode polling');
          },
        });

        client.activate();
        clientRef.current = client;

      } catch (e) {
        // @stomp/stompjs not yet installed — silently fall back to polling
        if (active) setError('WebSocket non disponible');
      }
    };

    connect();

    return () => {
      active = false;
      if (clientRef.current) {
        clientRef.current.deactivate();
        clientRef.current = null;
      }
      setConnected(false);
    };
  }, [parcelleId, stableOnReading]);

  return { connected, error };
}
