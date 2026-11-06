package com.campusexchange.service;

import com.campusexchange.repository.EmailNotificationRepository;
import java.time.Instant;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.*;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.*;

@Service
public class EmailDelivery {
  private final EmailNotificationRepository notifications;
  private final JavaMailSender sender;
  private final boolean enabled;
  private final String from;

  public EmailDelivery(
      EmailNotificationRepository notifications,
      JavaMailSender sender,
      @Value("${app.notifications.enabled:false}") boolean enabled,
      @Value("${spring.mail.username:}") String from) {
    this.notifications = notifications;
    this.sender = sender;
    this.enabled = enabled;
    this.from = from;
  }

  @Transactional(isolation = Isolation.READ_COMMITTED)
  public boolean sendNext() {
    if (!enabled || from.isBlank()) return false;
    var next =
        notifications.findFirstByStatusAndNextAttemptAtLessThanEqualOrderByCreatedAtAsc(
            "PENDING", Instant.now());
    if (next.isEmpty()) return false;
    var notification = next.get();
    var message = new SimpleMailMessage();
    message.setFrom(from);
    message.setTo(notification.getRecipient());
    message.setSubject(notification.getSubject());
    message.setText(notification.getBody());
    try {
      sender.send(message);
      notification.sent();
    } catch (MailException ex) {
      notification.failed();
      LoggerFactory.getLogger(EmailDelivery.class)
          .warn(
              "Email notification {} delivery failed, attempt {}",
              notification.getId(),
              notification.getAttempts());
    }
    return true;
  }
}
