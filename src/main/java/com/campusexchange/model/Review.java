package com.campusexchange.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "reviews")
public class Review {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(optional = false)
  @JoinColumn(name = "trade_id", nullable = false, unique = true)
  private Trade trade;

  @Column(nullable = false)
  private int rating;

  @Column(length = 500, nullable = false)
  private String comment;

  @Column(nullable = false)
  private Instant createdAt = Instant.now();

  protected Review() {}

  public Review(Trade trade, int rating, String comment) {
    this.trade = trade;
    this.rating = rating;
    this.comment = comment;
  }

  public Long getId() {
    return id;
  }

  public Trade getTrade() {
    return trade;
  }

  public int getRating() {
    return rating;
  }

  public String getComment() {
    return comment;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
