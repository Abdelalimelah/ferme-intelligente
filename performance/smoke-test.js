import http from 'k6/http';
import { check, sleep } from 'k6';

// Lightweight smoke test — not a stress test. Confirms the API holds up
// under a small concurrent load before every merge, with hard thresholds
// that fail the CI job if breached.
const BASE_URL = __ENV.BASE_URL || 'http://localhost';

export const options = {
  vus: 10,
  duration: '20s',
  thresholds: {
    http_req_duration: ['p(95)<800'],   // 95% of requests under 800ms
    http_req_failed: ['rate<0.01'],     // less than 1% errors
  },
};

export default function () {
  // Real auth flow: login with a seeded demo account
  const loginRes = http.post(
    `${BASE_URL}/api/auth/login`,
    JSON.stringify({ email: 'karim@ferme.ma', motDePasse: 'password123' }),
    { headers: { 'Content-Type': 'application/json' } }
  );

  check(loginRes, {
    'login status is 200': (r) => r.status === 200,
    'login returns a token': (r) => !!r.json('token'),
  });

  sleep(1);
}
