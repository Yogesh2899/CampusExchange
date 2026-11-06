USE campusexchange;
CREATE TABLE IF NOT EXISTS reviews (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 trade_id BIGINT NOT NULL UNIQUE,
 rating INT NOT NULL,
 comment VARCHAR(500) NOT NULL,
 created_at DATETIME(6) NOT NULL,
 CONSTRAINT fk_review_trade FOREIGN KEY (trade_id) REFERENCES trades(id),
 CONSTRAINT chk_review_rating CHECK (rating BETWEEN 1 AND 5)
);
