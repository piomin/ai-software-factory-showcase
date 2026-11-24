-- Adds the payment-lifecycle columns to bookings.
-- All nullable so existing CONFIRMED rows remain valid with a NULL amount (Assumption 4).
ALTER TABLE bookings ADD COLUMN amount DECIMAL(10, 2) NULL;
ALTER TABLE bookings ADD COLUMN created_at TIMESTAMP NULL;
ALTER TABLE bookings ADD COLUMN expires_at TIMESTAMP NULL;
