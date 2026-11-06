package com.campusexchange.repository;

import com.campusexchange.model.EmailNotification;
import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;

public interface EmailNotificationRepository extends JpaRepository<EmailNotification, Long> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  Optional<EmailNotification> findFirstByStatusAndNextAttemptAtLessThanEqualOrderByCreatedAtAsc(
      String status, Instant due);

  Optional<EmailNotification> findByItemId(Long itemId);
}
