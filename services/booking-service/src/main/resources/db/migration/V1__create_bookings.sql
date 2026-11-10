CREATE TABLE bookings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    event_id BIGINT NOT NULL,
    customer_name VARCHAR(255) NOT NULL,
    tickets INTEGER NOT NULL,
    status VARCHAR(50) NOT NULL
);
