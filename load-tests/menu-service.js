import http from 'k6/http';
import { check, group, sleep } from 'k6';
import { Trend, Rate } from 'k6/metrics';

// ─── Custom Metrics ───────────────────────────────────────────────────────────
const restaurantListDuration = new Trend('restaurant_list_duration');
const searchDuration         = new Trend('search_duration');
const menuItemsDuration      = new Trend('menu_items_duration');
const errorRate              = new Rate('error_rate');

// ─── Test Config ──────────────────────────────────────────────────────────────
const BASE_URL = __ENV.MENU_SERVICE_URL || 'http://localhost:8082';

export const options = {
  stages: [
    { duration: '30s', target: 10 },  // ramp up to 10 virtual users
    { duration: '1m',  target: 10 },  // hold — steady load
    { duration: '30s', target: 30 },  // spike to 30 users
    { duration: '30s', target: 30 },  // hold spike
    { duration: '20s', target: 0  },  // ramp down
  ],
  thresholds: {
    // 95% of all requests must complete under 500ms
    http_req_duration:          ['p(95)<500'],
    // Less than 1% of requests can fail
    http_req_failed:            ['rate<0.01'],
    // Per-endpoint thresholds
    restaurant_list_duration:   ['p(95)<400'],
    search_duration:            ['p(95)<400'],
    menu_items_duration:        ['p(95)<400'],
  },
};

// ─── Seed Data ────────────────────────────────────────────────────────────────
// These IDs must exist in your DB before running the test
const RESTAURANT_IDS = __ENV.RESTAURANT_IDS
  ? __ENV.RESTAURANT_IDS.split(',')
  : ['IxvrRDMgFr6JNTqcHnkS', 'Rl3cRB1Ox2bPn1kbexXk'];

const MENU_IDS = __ENV.MENU_IDS
  ? __ENV.MENU_IDS.split(',')
  : ['pQKaoMqkJLjMvKv8kAHd', 'kYRiv1wNyeFLG1n8DCTe', '04AxttjNymeaWJ936Kcm'];

const SEARCH_TERMS = ['noodles', 'rice', 'juice', 'sides', 'malay', 'chinese'];

// ─── Helpers ──────────────────────────────────────────────────────────────────
function randomItem(arr) {
  return arr[Math.floor(Math.random() * arr.length)];
}

function logFailure(groupName, res) {
  console.error(`[FAIL] ${groupName} — status ${res.status} — ${res.url} — ${res.body}`);
}

// ─── Main Scenario ────────────────────────────────────────────────────────────
// Simulates a user browsing the menu: list → search → view restaurant → view items
export default function () {

  group('List all restaurants', () => {
    const res = http.get(`${BASE_URL}/api/restaurants`);
    restaurantListDuration.add(res.timings.duration);
    errorRate.add(res.status !== 200);
    const ok = check(res, {
      'status is 200':  (r) => r.status === 200,
      'returns array':  (r) => Array.isArray(r.json()),
    });
    if (!ok) logFailure('List all restaurants', res);
  });

  sleep(1);

  group('Search restaurants', () => {
    const term = randomItem(SEARCH_TERMS);
    const res = http.get(`${BASE_URL}/api/restaurants/search?q=${term}`);
    searchDuration.add(res.timings.duration);
    errorRate.add(res.status !== 200);
    const ok = check(res, {
      'status is 200': (r) => r.status === 200,
    });
    if (!ok) logFailure('Search restaurants', res);
  });

  sleep(1);

  group('Get restaurant by ID', () => {
    const id = randomItem(RESTAURANT_IDS);
    const res = http.get(`${BASE_URL}/api/restaurants/${id}`);
    errorRate.add(res.status !== 200 && res.status !== 404);
    const ok = check(res, {
      'status is 200 or 404': (r) => r.status === 200 || r.status === 404,
    });
    if (!ok) logFailure('Get restaurant by ID', res);
  });

  sleep(1);

  group('Get menus for restaurant', () => {
    const restaurantId = randomItem(RESTAURANT_IDS);
    const res = http.get(`${BASE_URL}/api/restaurants/${restaurantId}/menus`);
    errorRate.add(res.status !== 200 && res.status !== 404);
    const ok = check(res, {
      'status is 200 or 404': (r) => r.status === 200 || r.status === 404,
    });
    if (!ok) logFailure('Get menus for restaurant', res);
  });

  sleep(1);

  group('Get menu items', () => {
    const restaurantId = randomItem(RESTAURANT_IDS);
    const menuId       = randomItem(MENU_IDS);
    const res = http.get(
      `${BASE_URL}/api/restaurants/${restaurantId}/menus/${menuId}/items`
    );
    menuItemsDuration.add(res.timings.duration);
    errorRate.add(res.status !== 200 && res.status !== 404);
    const ok = check(res, {
      'status is 200 or 404': (r) => r.status === 200 || r.status === 404,
    });
    if (!ok) logFailure('Get menu items', res);
  });

  sleep(1);
}
