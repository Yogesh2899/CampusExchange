package com.campusexchange.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "trades")
public class Trade {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(optional = false)
  @JoinColumn(name = "request_id", nullable = false, unique = true)
  private PurchaseRequest request;

  @ManyToOne(optional = false)
  @JoinColumn(name = "item_id")
  private Item item;

  @ManyToOne(optional = false)
  @JoinColumn(name = "buyer_id")
  private User buyer;

  @ManyToOne(optional = false)
  @JoinColumn(name = "seller_id")
  private User seller;

  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal finalPrice;

  @Column(nullable = false)
  private Instant completedAt = Instant.now();

  protected Trade() {}

  public Trade(PurchaseRequest request) {
    this.request = request;
    this.item = request.getItem();
    this.buyer = request.getBuyer();
    this.seller = item.getSeller();
    this.finalPrice = item.getPrice();
  }

  public Long getId() {
    return id;
  }

  public PurchaseRequest getRequest() {
    return request;
  }

  public Item getItem() {
    return item;
  }

  public User getBuyer() {
    return buyer;
  }

  public User getSeller() {
    return seller;
  }

  public BigDecimal getFinalPrice() {
    return finalPrice;
  }

  public Instant getCompletedAt() {
    return completedAt;
  }
}
