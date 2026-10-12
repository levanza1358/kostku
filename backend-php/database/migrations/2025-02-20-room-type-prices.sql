-- Normalize room-type pricing for existing MySQL/MariaDB installations.
CREATE TABLE IF NOT EXISTS room_type_prices (
 id CHAR(36) NOT NULL PRIMARY KEY,
 room_type_id CHAR(36) NOT NULL,
 occupancy SMALLINT NOT NULL,
 billing_period VARCHAR(20) NOT NULL,
 amount DECIMAL(14,2) NOT NULL,
 created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
 updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
 CONSTRAINT room_type_prices_type_fk FOREIGN KEY (room_type_id) REFERENCES room_types(id) ON DELETE RESTRICT,
 CONSTRAINT room_type_prices_unique UNIQUE (room_type_id, occupancy, billing_period),
 CONSTRAINT room_type_prices_occupancy_ck CHECK (occupancy >= 1),
 CONSTRAINT room_type_prices_period_ck CHECK (billing_period IN ('daily','weekly','monthly')),
 CONSTRAINT room_type_prices_amount_ck CHECK (amount >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
