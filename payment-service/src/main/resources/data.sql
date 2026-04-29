-- Seed payment methods for load test users (IDs 1 and 2)
INSERT INTO payment_methods (user_id, type, label, last_four, is_default, created_at, updated_at)
VALUES ('4Eaq5b7GopZg6vl7p0MstWE53b82', 'CREDIT_CARD', 'Visa ending 1234', '1234', true, NOW(), NOW());

INSERT INTO payment_methods (user_id, type, label, last_four, is_default, created_at, updated_at)
VALUES ('0ZYBK2HNDoSZV4EURSLcLbpLMvC2', 'CREDIT_CARD', 'Visa ending 5678', '5678', true, NOW(), NOW());
