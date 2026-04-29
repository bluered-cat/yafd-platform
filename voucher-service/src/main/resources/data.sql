-- Seed vouchers for demo/testing
INSERT INTO vouchers (code, description, discount_type, discount_value, max_usage, current_usage, min_order_amount, valid_from, valid_until, active, created_at, updated_at)
SELECT 'CNY2026', 'Chinese New Year 20% Off', 'PERCENTAGE', 20.00, 1000, 0, 10.00, '2026-01-28 00:00:00+08', '2026-02-28 23:59:59+08', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM vouchers WHERE code = 'CNY2026');

INSERT INTO vouchers (code, description, discount_type, discount_value, max_usage, current_usage, min_order_amount, valid_from, valid_until, active, created_at, updated_at)
SELECT 'XMAS2025', 'Christmas $5 Off', 'FIXED_AMOUNT', 5.00, 500, 0, 15.00, '2025-12-20 00:00:00+08', '2025-12-31 23:59:59+08', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM vouchers WHERE code = 'XMAS2025');

INSERT INTO vouchers (code, description, discount_type, discount_value, max_usage, current_usage, min_order_amount, valid_from, valid_until, active, created_at, updated_at)
SELECT 'FLASH10', 'Flash Sale 10% Off', 'PERCENTAGE', 10.00, 10000, 0, 5.00, '2026-01-01 00:00:00+08', '2026-12-31 23:59:59+08', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM vouchers WHERE code = 'FLASH10');

-- Single-use voucher for load test — exhausted after one redemption to verify rejection behaviour
INSERT INTO vouchers (code, description, discount_type, discount_value, max_usage, current_usage, min_order_amount, valid_from, valid_until, active, created_at, updated_at)
SELECT 'LIMITONE', 'Load Test Single-Use', 'FIXED_AMOUNT', 1.00, 1, 0, 0.00, '2026-01-01 00:00:00+08', '2026-12-31 23:59:59+08', true, NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM vouchers WHERE code = 'LIMITONE');
