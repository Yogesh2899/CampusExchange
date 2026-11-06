package com.campusexchange.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(
    name = "purchase_requests",
    indexes = @Index(name = "idx_request_item_status", columnList = "item_id,status"))
public class PurchaseRequest {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "item_id")
  private Item item;

  @ManyToOne(optional = false)
  @JoinColumn(name = "buyer_id")
  private User buyer;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private RequestStatus status = RequestStatus.PENDING;

  @Column(length = 255)
  private String message;

  @Column(nullable = false)
  private Instant requestedAt = Instant.now();

  protected PurchaseRequest() {}

  public PurchaseRequest(Item item, User buyer, String message) {
    this.item = item;
    this.buyer = buyer;
    this.message = message;
  }

  public Long getId() {
    return id;
  }

  public Item getItem() {
    return item;
  }

  public User getBuyer() {
    return buyer;
  }

  public RequestStatus getStatus() {
    return status;
  }

  public String getMessage() {
    return message;
  }

  public Instant getRequestedAt() {
    return requestedAt;
  }

  public void setStatus(RequestStatus status) {
    this.status = status;
  }
}
