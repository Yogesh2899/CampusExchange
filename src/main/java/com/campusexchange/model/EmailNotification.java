package com.campusexchange.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
    name = "email_notifications",
    indexes = @Index(name = "idx_email_pending", columnList = "status,next_attempt_at"))
public class EmailNotification {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "item_id", nullable = false, unique = true)
  private Long itemId;

  @Column(nullable = false, length = 160)
  private String recipient;

  @Column(nullable = false, length = 200)
  private String subject;

  @Column(nullable = false, length = 4000)
  private String body;

  @Column(nullable = false, length = 16)
  private String status = "PENDING";

  @Column(nullable = false)
  private int attempts;

  @Column(nullable = false)
  private Instant nextAttemptAt = Instant.now();

  @Column(nullable = false)
  private Instant createdAt = Instant.now();

  private Instant sentAt;

  protected EmailNotification() {}

  public EmailNotification(Long itemId, String recipient, String subject, String body) {
    this.itemId = itemId;
    this.recipient = recipient;
    this.subject = subject;
    this.body = body;
  }

  public Long getId() {
    return id;
  }

  public Long getItemId() {
    return itemId;
  }

  public String getRecipient() {
    return recipient;
  }

  public String getSubject() {
    return subject;
  }

  public String getBody() {
    return body;
  }

  public String getStatus() {
    return status;
  }

  public int getAttempts() {
    return attempts;
  }

  public void sent() {
    attempts++;
    status = "SENT";
    sentAt = Instant.now();
  }

  public void failed() {
    attempts++;
    if (attempts >= 5) status = "FAILED";
    else nextAttemptAt = Instant.now().plusSeconds(30L * (1L << (attempts - 1)));
  }
}
