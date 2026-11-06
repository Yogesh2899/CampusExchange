CREATE DATABASE IF NOT EXISTS campusexchange;
USE campusexchange;
CREATE TABLE IF NOT EXISTS users (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 name VARCHAR(100) NOT NULL,
 email VARCHAR(160) NOT NULL UNIQUE,
 password_hash VARCHAR(255) NOT NULL,
 role VARCHAR(16) NOT NULL DEFAULT 'STUDENT',
 enabled BIT NOT NULL DEFAULT b'1',
 phone VARCHAR(30) DEFAULT '',
 department VARCHAR(100) DEFAULT '',
 hostel VARCHAR(100) DEFAULT ''
);
CREATE TABLE IF NOT EXISTS categories (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 name VARCHAR(60) NOT NULL UNIQUE
);
CREATE TABLE IF NOT EXISTS items (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 seller_id BIGINT NOT NULL,
 category_id BIGINT NOT NULL,
 title VARCHAR(150) NOT NULL,
 description VARCHAR(2000) NOT NULL,
 price DECIMAL(10,2) NOT NULL,
 item_condition VARCHAR(16) NOT NULL,
 status VARCHAR(16) NOT NULL DEFAULT 'AVAILABLE',
 listed_at DATETIME(6) NOT NULL,
 CONSTRAINT fk_item_seller FOREIGN KEY (seller_id) REFERENCES users(id),
 CONSTRAINT fk_item_category FOREIGN KEY (category_id) REFERENCES categories(id),
 CONSTRAINT chk_item_price CHECK (price > 0),
 INDEX idx_item_status(status), INDEX idx_item_category(category_id)
);
CREATE TABLE IF NOT EXISTS purchase_requests (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 item_id BIGINT NOT NULL,
 buyer_id BIGINT NOT NULL,
 status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
 message VARCHAR(255),
 requested_at DATETIME(6) NOT NULL,
 CONSTRAINT fk_request_item FOREIGN KEY (item_id) REFERENCES items(id),
 CONSTRAINT fk_request_buyer FOREIGN KEY (buyer_id) REFERENCES users(id),
 INDEX idx_request_item_status(item_id,status)
);
CREATE TABLE IF NOT EXISTS trades (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 request_id BIGINT NOT NULL UNIQUE,
 item_id BIGINT NOT NULL,
 buyer_id BIGINT NOT NULL,
 seller_id BIGINT NOT NULL,
 final_price DECIMAL(10,2) NOT NULL,
 completed_at DATETIME(6) NOT NULL,
 CONSTRAINT fk_trade_request FOREIGN KEY (request_id) REFERENCES purchase_requests(id),
 CONSTRAINT fk_trade_item FOREIGN KEY (item_id) REFERENCES items(id),
 CONSTRAINT fk_trade_buyer FOREIGN KEY (buyer_id) REFERENCES users(id),
 CONSTRAINT fk_trade_seller FOREIGN KEY (seller_id) REFERENCES users(id)
);
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
USE campusexchange;
CREATE TABLE IF NOT EXISTS item_photos (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 item_id BIGINT NOT NULL UNIQUE,
 data LONGBLOB NOT NULL,
 CONSTRAINT fk_photo_item FOREIGN KEY (item_id) REFERENCES items(id)
);

USE campusexchange;
CREATE TABLE IF NOT EXISTS email_notifications (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 item_id BIGINT NOT NULL UNIQUE,
 recipient VARCHAR(160) NOT NULL,
 subject VARCHAR(200) NOT NULL,
 body VARCHAR(4000) NOT NULL,
 status VARCHAR(16) NOT NULL,
 attempts INT NOT NULL,
 next_attempt_at DATETIME(6) NOT NULL,
 created_at DATETIME(6) NOT NULL,
 sent_at DATETIME(6),
 INDEX idx_email_pending(status,next_attempt_at)
);
