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
    { duration: '20s', target: 10 },  // spike — 2× normal (single-instance limit)
    { duration: '30s', target: 10 },  // hold spike
    { duration: '20s', target: 0  },  // ramp down
  ],
  thresholds: {
    // Thresholds reflect a single-EC2 deployment where all services share one
    // host.  Each order submission makes 5+ sequential inter-service HTTP calls
    // (menu → account × 3 → payment), so end-to-end latency is higher than
    // single-service benchmarks.  The critical gate is error_rate — latency
    // budgets are set to what the deployment actually sustains under 10 VUs.
    http_req_duration:             ['p(95)<4000'],
    // Custom metric excludes intentional 400s from the exhausted-voucher group
    error_rate:                    ['rate<0.01'],
    submit_order_duration:         ['p(95)<4000'],
    submit_order_voucher_duration: ['p(95)<5000'],
    get_order_duration:            ['p(95)<1500'],
    user_orders_duration:          ['p(95)<1500'],
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
// Runs once before any VUs start — resets FLASH10's usage limit so it never
// exhausts mid-test, gets a Firebase token, then exhausts the single-use
// voucher so the rejection test case is guaranteed to see it used.
export function setup() {
  const token = getFirebaseToken();
  const headers = token
    ? { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token}` }
    : JSON_HEADERS;

  // Bump FLASH10 max usage to a ceiling that no test run can reach.
  // The DB's current_usage accumulates across runs (data.sql uses WHERE NOT EXISTS
  // so it never resets); without this the voucher stays exhausted after the first run.
  const vouchersRes = http.get(`${BASE_URL}/api/vouchers`, { headers });
  if (vouchersRes.status === 200) {
    const vouchers = vouchersRes.json();
    for (const v of vouchers) {
      if (v.code === 'FLASH10') {
        const resetRes = http.put(
          `${BASE_URL}/api/vouchers/${v.id}`,
          JSON.stringify({ maxUsage: 999999 }),
          { headers }
        );
        console.log(`Setup: FLASH10 maxUsage → 999999 (was ${v.maxUsage}, currentUsage ${v.currentUsage}), status ${resetRes.status}`);
      }
    }
  } else {
    console.warn(`Setup: could not fetch vouchers (${vouchersRes.status}) — FLASH10 may be exhausted`);
  }

  // Exhaust LIMITONE so the rejection test case always sees a 400.
  // If it is already exhausted from a prior run the order returns 400 — that is fine,
  // the VU check just needs the voucher to be unusable before VUs start.
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
  // NOTE: controller uses @PutMapping — must be PUT, not PATCH.
  group('Complete order (release rider)', () => {
    if (createdOrderIds.length === 0) return;
    const orderId = randomItem(createdOrderIds);
    http.put(
      `${BASE_URL}/api/orders/${orderId}/status`,
      JSON.stringify({ status: 'DELIVERED' }),
      { headers: authHeaders }
    );
    // Don't assert — order may already be DELIVERED or not exist; we just want rider released
  });

  sleep(1);
}
