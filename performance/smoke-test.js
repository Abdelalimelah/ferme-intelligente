import http from 'k6/http';
import { check, sleep } from 'k6';

// Lightweight smoke test — not a stress test. Confirms the API holds up
// under a small concurrent load before every merge, with hard thresholds
// that fail the CI job if breached.
const BASE_URL = __ENV.BASE_URL || 'http://localhost';

export const options = {
  vus: 5,
  duration: '20s',
  // Thresholds are loosened for CI: the whole docker-compose stack (Postgres,
  // Redis, backend JVM, AI service, frontend, nginx) runs cold on the same
  // shared 2-core runner as k6 itself, so latency here isn't representative
  // of production. Tighten these if running against dedicated infra.
  thresholds: {
    http_req_duration: ['p(95)<3000'],
    http_req_failed: ['rate<0.05'],
  },
};

// Logs in once per test run, not per request — /api/auth/login is
// rate-limited to 5 attempts/minute/IP (RateLimitFilter), so hammering it
// from every VU/iteration would just trip that limiter, not test anything.
export function setup() {
  const loginRes = http.post(
    `${BASE_URL}/api/auth/login`,
    JSON.stringify({ email: 'karim@ferme.ma', motDePasse: 'password123' }),
    { headers: { 'Content-Type': 'application/json' } }
  );

  check(loginRes, {
    'login status is 200': (r) => r.status === 200,
    'login returns a token': (r) => !!r.json('token'),
  });

  return { token: loginRes.json('token') };
}

export default function (data) {
  // Real authenticated read, representative of normal traffic
  const res = http.get(`${BASE_URL}/api/parcelles`, {
    headers: { Authorization: `Bearer ${data.token}` },
  });

  check(res, {
    'parcelles status is 200': (r) => r.status === 200,
  });

  sleep(1);
}
