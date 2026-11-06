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
