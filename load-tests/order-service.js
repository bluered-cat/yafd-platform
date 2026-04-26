import http from 'k6/http';
import { check, group, sleep } from 'k6';
import { Trend, Rate } from 'k6/metrics';

// ─── Custom Metrics ───────────────────────────────────────────────────────────
const submitOrderDuration  = new Trend('submit_order_duration');
const getOrderDuration     = new Trend('get_order_duration');
const userOrdersDuration   = new Trend('user_orders_duration');
const errorRate            = new Rate('error_rate');

// ─── Test Config ──────────────────────────────────────────────────────────────
const BASE_URL = __ENV.ORDER_SERVICE_URL || 'http://localhost:8083';

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
    http_req_duration:    ['p(95)<1000'],
    http_req_failed:      ['rate<0.01'],
    submit_order_duration: ['p(95)<1000'],
    get_order_duration:    ['p(95)<500'],
    user_orders_duration:  ['p(95)<500'],
  },
};

// ─── Seed Data ────────────────────────────────────────────────────────────────
// These IDs must exist in your DB before running the test
// Firebase UIDs — replace with real UIDs from your Firebase/DB
const USER_IDS        = __ENV.USER_IDS
  ? __ENV.USER_IDS.split(',')
  : ['4Eaq5b7GopZg6vl7p0MstWE53b82', '0ZYBK2HNDoSZV4EURSLcLbpLMvC2'];

const MENU_ITEM_IDS   = __ENV.MENU_ITEM_IDS
  ? __ENV.MENU_ITEM_IDS.split(',')
  : ['13utNluWbgIHCcPajrav', '3l8rVyZDSFW4yXnA9AHM', '3vBDLqKMwMDine4eM3s5', '66sBNLxoOwZLSvmPjMe0', '78P9RAz78fIgop0h8Ily'];

const ADDRESS_IDS     = __ENV.ADDRESS_IDS
  ? __ENV.ADDRESS_IDS.split(',')
  : ['1', '2'];

// IDs from the payment_methods table (PostgreSQL Long) — saved cards/wallets per user
const PAYMENT_METHOD_IDS = __ENV.PAYMENT_METHOD_IDS
  ? __ENV.PAYMENT_METHOD_IDS.split(',')
  : ['2', '3'];

// Tracks order IDs created during this run so GET requests use real IDs
const createdOrderIds = [];

// ─── Helpers ──────────────────────────────────────────────────────────────────
function randomItem(arr) {
  return arr[Math.floor(Math.random() * arr.length)];
}

function randomInt(min, max) {
  return Math.floor(Math.random() * (max - min + 1)) + min;
}

function buildOrderPayload(userId) {
  const itemCount = randomInt(1, 3);
  const items = Array.from({ length: itemCount }, () => ({
    menuItemId: randomItem(MENU_ITEM_IDS),
    quantity:   randomInt(1, 4),
  }));

  return JSON.stringify({
    userId:          userId,
    items:           items,
    addressId:       parseInt(randomItem(ADDRESS_IDS)),
    paymentMethodId: parseInt(randomItem(PAYMENT_METHOD_IDS)),
    voucherCode:     null,
  });
}

const JSON_HEADERS = { 'Content-Type': 'application/json' };

// ─── Main Scenario ────────────────────────────────────────────────────────────
// Simulates a user submitting an order then checking its status
export default function () {
  const userId = randomItem(USER_IDS);

  group('Submit new order', () => {
    const res = http.post(
      `${BASE_URL}/api/orders`,
      buildOrderPayload(userId),
      { headers: JSON_HEADERS }
    );
    submitOrderDuration.add(res.timings.duration);
    errorRate.add(res.status !== 200 && res.status !== 201);
    check(res, {
      'order created (200/201)': (r) => r.status === 200 || r.status === 201,
    });

    // Store created order ID for use in GET requests below
    if (res.status === 200 || res.status === 201) {
      try {
        const body = res.json();
        if (body && body.id) createdOrderIds.push(String(body.id));
      } catch (_) { /* ignore parse errors */ }
    }
  });

  sleep(1);

  group('Get order by ID', () => {
    // Use a real order ID if available, otherwise skip
    if (createdOrderIds.length === 0) return;
    const orderId = randomItem(createdOrderIds);
    const res = http.get(`${BASE_URL}/api/orders/${orderId}`);
    getOrderDuration.add(res.timings.duration);
    errorRate.add(res.status !== 200 && res.status !== 404);
    check(res, {
      'status is 200 or 404': (r) => r.status === 200 || r.status === 404,
    });
  });

  sleep(1);

  group('Get orders for user', () => {
    const res = http.get(`${BASE_URL}/api/orders/user/${userId}`);
    userOrdersDuration.add(res.timings.duration);
    errorRate.add(res.status !== 200 && res.status !== 404);
    check(res, {
      'status is 200 or 404': (r) => r.status === 200 || r.status === 404,
    });
  });

  sleep(2);
}
