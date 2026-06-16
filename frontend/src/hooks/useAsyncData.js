import { useEffect, useState } from 'react';

// Fetches data on mount/dep-change and exposes a `reload` for re-fetching
// after mutations. The fetch always runs inside an async function invoked
// from the effect (never a synchronous setState call in the effect body
// itself), which keeps it cancellation-safe and avoids cascading renders.
export function useAsyncData(fetchFn, deps = [], { initialData = null, errorMessage = 'Erreur lors du chargement' } = {}) {
  const [data, setData] = useState(initialData);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  function reload() {
    let cancelled = false;
    async function run() {
      setLoading(true);
      try {
        const result = await fetchFn();
        if (!cancelled) { setData(result); setError(''); }
      } catch {
        if (!cancelled) setError(errorMessage);
      } finally {
        if (!cancelled) setLoading(false);
      }
    }
    run();
    return () => { cancelled = true; };
  }

  useEffect(reload, deps); // eslint-disable-line react-hooks/exhaustive-deps

  return { data, setData, loading, error, setError, reload };
}
