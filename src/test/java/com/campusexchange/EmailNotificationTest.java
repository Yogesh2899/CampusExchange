package com.campusexchange;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.campusexchange.model.*;
import com.campusexchange.repository.*;
import com.campusexchange.service.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.*;
import org.springframework.mail.javamail.JavaMailSender;

@SpringBootTest(
    properties = {
      "app.notifications.enabled=true",
      "app.notifications.initial-delay-ms=3600000",
      "spring.mail.username=sender@gmail.com"
    })
class EmailNotificationTest {
  @Autowired org.springframework.transaction.PlatformTransactionManager transactionManager;
  @MockBean JavaMailSender sender;
  @Autowired AccountService accounts;
  @Autowired MarketplaceService market;
  @Autowired EmailDelivery delivery;
  @Autowired EmailNotificationRepository notifications;
  @Autowired ItemRepository items;
  @Autowired JdbcTemplate jdbc;

  @Test
  void committedListingQueuesEmailAndSmtpFailureDoesNotLoseListing() {
    User seller =
        accounts.register("Mail test", "mail-" + UUID.randomUUID() + "@thapar.edu", "Password123!");
    Item item =
        market.createItem(
            seller,
            market.categories().get(0).getId(),
            "Email demo textbook",
            "Good condition",
            new BigDecimal("450.00"),
            ItemCondition.GOOD);
    var queued = notifications.findByItemId(item.getId()).orElseThrow();
    assertEquals(seller.getEmail(), queued.getRecipient());
    assertEquals("PENDING", queued.getStatus());
    doThrow(new MailSendException("Simulated outage"))
        .doNothing()
        .when(sender)
        .send(any(SimpleMailMessage.class));
    assertTrue(delivery.sendNext());
    var failed = notifications.findByItemId(item.getId()).orElseThrow();
    assertEquals("PENDING", failed.getStatus());
    assertEquals(1, failed.getAttempts());
    assertTrue(items.existsById(item.getId()));
    assertFalse(delivery.sendNext());
    jdbc.update(
        "update email_notifications set next_attempt_at=? where id=?",
        Timestamp.from(Instant.EPOCH),
        queued.getId());
    assertTrue(delivery.sendNext());
    var sent = notifications.findByItemId(item.getId()).orElseThrow();
    assertEquals("SENT", sent.getStatus());
    assertEquals(2, sent.getAttempts());
    var capture = org.mockito.ArgumentCaptor.forClass(SimpleMailMessage.class);
    verify(sender, times(2)).send(capture.capture());
    assertEquals(seller.getEmail(), capture.getValue().getTo()[0]);
    assertTrue(capture.getValue().getText().contains("Email demo textbook"));
    assertFalse(delivery.sendNext());
  }

  @Test
  void rolledBackListingCannotQueueAnEmail() {
    User seller =
        accounts.register(
            "Rollback test", "rollback-" + UUID.randomUUID() + "@thapar.edu", "Password123!");
    Long itemId =
        new org.springframework.transaction.support.TransactionTemplate(transactionManager)
            .execute(
                status -> {
                  Item item =
                      market.createItem(
                          seller,
                          market.categories().get(0).getId(),
                          "Rolled back item",
                          "This should not persist",
                          BigDecimal.TEN,
                          ItemCondition.GOOD);
                  status.setRollbackOnly();
                  return item.getId();
                });
    assertNotNull(itemId);
    assertFalse(items.existsById(itemId));
    assertTrue(notifications.findByItemId(itemId).isEmpty());
  }

  @Test
  void deliveryStopsAfterFiveFailures() {
    var notification = new EmailNotification(1L, "recipient@thapar.edu", "Test", "Body");
    for (int attempt = 1; attempt <= 5; attempt++) {
      notification.failed();
      assertEquals(attempt, notification.getAttempts());
      assertEquals(attempt < 5 ? "PENDING" : "FAILED", notification.getStatus());
    }
  }
}

