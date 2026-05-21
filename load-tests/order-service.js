import http from 'k6/http';
import { check, group, sleep } from 'k6';
import { Trend, Rate } from 'k6/metrics';

// ─── Custom Metrics ───────────────────────────────────────────────────────────
const submitOrderDuration        = new Trend('submit_order_duration');
const submitOrderVoucherDuration = new Trend('submit_order_voucher_duration');
const getOrderDuration           = new Trend('get_order_duration');
const userOrdersDuration         = new Trend('user_orders_duration');
const errorRate                  = new Rate('error_rate');

// ─── Test Config ──────────────────────────────────────────────────────────────
const BASE_URL         = __ENV.ORDER_SERVICE_URL  || 'http://localhost:8083';
const FIREBASE_API_KEY = __ENV.FIREBASE_API_KEY   || '';
const TEST_EMAIL       = __ENV.TEST_EMAIL         || '';
const TEST_PASSWORD    = __ENV.TEST_PASSWORD      || '';

export const options = {
  stages: [
    { duration: '30s', target: 5  },  // ramp up gently (orders are heavy)
    { duration: '1m',  target: 5  },  // hold — steady load
    { duration: '20s', target: 15 },  // spike
    { duration: '30s', target: 15 },  // hold spike
    { duration: '20s', target: 0  },  // ramp down
  ],
  thresholds: {
    // Order submission can be slower — DB writes + external calls
    http_req_duration:             ['p(95)<1000'],
    // Use custom error_rate instead of http_req_failed — the exhausted-voucher
    // group intentionally returns 400s which would skew http_req_failed
    error_rate:                    ['rate<0.01'],
    submit_order_duration:         ['p(95)<1000'],
    submit_order_voucher_duration: ['p(95)<1200'],
    get_order_duration:            ['p(95)<500'],
    user_orders_duration:          ['p(95)<500'],
  },
};

// ─── Seed Data ────────────────────────────────────────────────────────────────
// These IDs must exist in your DB before running the test
// Firebase UIDs — replace with real UIDs from your Firebase/DB
const USER_IDS = __ENV.USER_IDS
  ? __ENV.USER_IDS.split(',')
  : ['4Eaq5b7GopZg6vl7p0MstWE53b82', '0ZYBK2HNDoSZV4EURSLcLbpLMvC2'];

const MENU_ITEM_IDS = __ENV.MENU_ITEM_IDS
  ? __ENV.MENU_ITEM_IDS.split(',')
  : ['13utNluWbgIHCcPajrav', '3l8rVyZDSFW4yXnA9AHM', '3vBDLqKMwMDine4eM3s5', '66sBNLxoOwZLSvmPjMe0', '78P9RAz78fIgop0h8Ily'];

const ADDRESS_IDS = __ENV.ADDRESS_IDS
  ? __ENV.ADDRESS_IDS.split(',')
  : ['1', '2'];

// IDs from the payment_methods table (PostgreSQL Long) — saved cards per user
const PAYMENT_METHOD_IDS = __ENV.PAYMENT_METHOD_IDS
  ? __ENV.PAYMENT_METHOD_IDS.split(',')
  : ['1', '2'];

// Active voucher codes from voucher-service data.sql
const VOUCHER_CODES = __ENV.VOUCHER_CODES
  ? __ENV.VOUCHER_CODES.split(',')
  : ['FLASH10'];

// Single-use voucher — exhausted in setup() to verify rejection under load
const EXHAUSTED_VOUCHER = __ENV.EXHAUSTED_VOUCHER || 'LIMITONE';

// Tracks order IDs created during this run so GET requests use real IDs
const createdOrderIds = [];

// ─── Helpers ──────────────────────────────────────────────────────────────────
function randomItem(arr) {
  return arr[Math.floor(Math.random() * arr.length)];
}

function randomInt(min, max) {
  return Math.floor(Math.random() * (max - min + 1)) + min;
}

function buildOrderPayload(userId, voucherCode, minQuantity = 1) {
  const itemCount = randomInt(1, 3);
  const items = Array.from({ length: itemCount }, () => ({
    menuItemId: randomItem(MENU_ITEM_IDS),
    quantity:   randomInt(minQuantity, 4),
  }));

  return JSON.stringify({
    userId:          userId,
    items:           items,
    addressId:       parseInt(randomItem(ADDRESS_IDS)),
    paymentMethodId: parseInt(randomItem(PAYMENT_METHOD_IDS)),
    voucherCode:     voucherCode || null,
  });
}

function logFailure(groupName, res) {
  console.error(`[FAIL] ${groupName} — status ${res.status} — ${res.url} — ${res.body}`);
}

const JSON_HEADERS = { 'Content-Type': 'application/json' };

// ─── Firebase Auth ────────────────────────────────────────────────────────────
function getFirebaseToken() {
  if (!FIREBASE_API_KEY || !TEST_EMAIL || !TEST_PASSWORD) {
    console.log('No Firebase credentials provided — running without auth (local only)');
    return null;
  }
  const res = http.post(
    `https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=${FIREBASE_API_KEY}`,
    JSON.stringify({ email: TEST_EMAIL, password: TEST_PASSWORD, returnSecureToken: true }),
    { headers: { 'Content-Type': 'application/json' } }
  );
  if (res.status !== 200) {
    console.error(`Firebase sign-in failed (${res.status}): ${res.body}`);
    return null;
  }
  const token = res.json('idToken');
  console.log(`Firebase sign-in successful for ${TEST_EMAIL}`);
  return token;
}

// ─── Setup ────────────────────────────────────────────────────────────────────
// Runs once before any VUs start — gets a Firebase token, then exhausts the
// single-use voucher so the rejection test case is guaranteed to see it used.
export function setup() {
  const token = getFirebaseToken();
  const headers = token
    ? { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` }
    : JSON_HEADERS;

  const res = http.post(
    `${BASE_URL}/api/orders`,
    buildOrderPayload(USER_IDS[0], EXHAUSTED_VOUCHER),
    { headers }
  );
  console.log(`Setup: redeemed ${EXHAUSTED_VOUCHER} once (status ${res.status}) — voucher is now exhausted`);

  return { token };
}

// ─── Teardown ─────────────────────────────────────────────────────────────────
// Runs once after all VUs finish — deletes all orders created for test users
// so the DB is clean for the next run.
export function teardown(data) {
  const headers = data && data.token
    ? { 'Content-Type': 'application/json', 'Authorization': `Bearer ${data.token}` }
    : JSON_HEADERS;

  for (const userId of USER_IDS) {
    const res = http.del(`${BASE_URL}/api/orders/user/${userId}`, null, { headers });
    if (res.status === 204) {
      console.log(`Teardown: deleted all orders for user ${userId}`);
    } else {
      console.error(`Teardown: failed to delete orders for user ${userId} (status ${res.status})`);
    }
  }
}

// ─── Main Scenario ────────────────────────────────────────────────────────────
// Simulates a user submitting an order then checking its status
export default function (data) {
  const userId = randomItem(USER_IDS);
  const authHeaders = data && data.token
    ? { 'Content-Type': 'application/json', 'Authorization': `Bearer ${data.token}` }
    : JSON_HEADERS;

  group('Submit new order', () => {
    const res = http.post(
      `${BASE_URL}/api/orders`,
      buildOrderPayload(userId, null),
      { headers: authHeaders }
    );
    submitOrderDuration.add(res.timings.duration);
    errorRate.add(res.status !== 200 && res.status !== 201);
    const ok = check(res, {
      'order created (200/201)': (r) => r.status === 200 || r.status === 201,
    });
    if (!ok) logFailure('Submit new order', res);

    if (res.status === 200 || res.status === 201) {
      try {
        const body = res.json();
        if (body && body.id) createdOrderIds.push(String(body.id));
      } catch (_) { /* ignore parse errors */ }
    }
  });

  sleep(1);

  group('Submit order with voucher', () => {
    const voucher = randomItem(VOUCHER_CODES);
    const res = http.post(
      `${BASE_URL}/api/orders`,
      buildOrderPayload(userId, voucher, 3),  // min qty 3 to clear $5 minimum
      { headers: authHeaders }
    );
    submitOrderVoucherDuration.add(res.timings.duration);
    errorRate.add(res.status !== 200 && res.status !== 201);
    const ok = check(res, {
      'voucher order created (200/201)': (r) => r.status === 200 || r.status === 201,
      'discount applied':                (r) => {
        try { return r.json().discountAmount > 0; } catch (_) { return false; }
      },
    });
    if (!ok) logFailure('Submit order with voucher', res);

    if (res.status === 200 || res.status === 201) {
      try {
        const body = res.json();
        if (body && body.id) createdOrderIds.push(String(body.id));
      } catch (_) { /* ignore parse errors */ }
    }
  });

  sleep(1);

  group('Submit order with exhausted voucher', () => {
    const res = http.post(
      `${BASE_URL}/api/orders`,
      buildOrderPayload(userId, EXHAUSTED_VOUCHER),
      { headers: authHeaders }
    );
    // Rejection is the expected outcome — do not count against errorRate
    const ok = check(res, {
      'exhausted voucher is rejected (400)': (r) => r.status === 400,
    });
    if (!ok) logFailure('Submit order with exhausted voucher', res);
  });

  sleep(1);

  group('Get order by ID', () => {
    // Use a real order ID if available, otherwise skip
    if (createdOrderIds.length === 0) return;
    const orderId = randomItem(createdOrderIds);
    const res = http.get(`${BASE_URL}/api/orders/${orderId}`, { headers: authHeaders });
    getOrderDuration.add(res.timings.duration);
    errorRate.add(res.status !== 200 && res.status !== 404);
    const ok = check(res, {
      'status is 200 or 404': (r) => r.status === 200 || r.status === 404,
    });
    if (!ok) logFailure('Get order by ID', res);
  });

  sleep(1);

  group('Get orders for user', () => {
    const res = http.get(`${BASE_URL}/api/orders/user/${userId}`, { headers: authHeaders });
    userOrdersDuration.add(res.timings.duration);
    errorRate.add(res.status !== 200 && res.status !== 404);
    const ok = check(res, {
      'status is 200 or 404': (r) => r.status === 200 || r.status === 404,
    });
    if (!ok) logFailure('Get orders for user', res);
  });

  sleep(1);

  // Mark a random past order as DELIVERED to release its rider back into the pool.
  // Without this, all riders get exhausted after the first few iterations and
  // no subsequent orders can be assigned a rider.
  group('Complete order (release rider)', () => {
    if (createdOrderIds.length === 0) return;
    const orderId = randomItem(createdOrderIds);
    http.patch(
      `${BASE_URL}/api/orders/${orderId}/status`,
      JSON.stringify({ status: 'DELIVERED' }),
      { headers: authHeaders }
    );
    // Don't assert — order may already be DELIVERED or not exist; we just want rider released
  });

  sleep(1);
}
