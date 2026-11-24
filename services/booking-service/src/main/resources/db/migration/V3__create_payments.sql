-- Payment history per booking (req 3.7). Only the last 4 card digits are stored (N1).
CREATE TABLE payments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id BIGINT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    card_last4 VARCHAR(4) NOT NULL
);

CREATE INDEX ix_payments_booking_id ON payments (booking_id);
