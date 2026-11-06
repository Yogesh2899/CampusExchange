package com.campusexchange.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
    name = "items",
    indexes = {
      @Index(name = "idx_item_status", columnList = "status"),
      @Index(name = "idx_item_category", columnList = "category_id")
    })
public class Item {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "seller_id")
  private User seller;

  @ManyToOne(optional = false)
  @JoinColumn(name = "category_id")
  private Category category;

  @Column(nullable = false, length = 150)
  private String title;

  @Column(nullable = false, length = 2000)
  private String description;

  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal price;

  @Enumerated(EnumType.STRING)
  @Column(name = "item_condition", nullable = false, length = 16)
  private ItemCondition condition;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 16)
  private ItemStatus status = ItemStatus.AVAILABLE;

  @Column(nullable = false)
  private Instant listedAt = Instant.now();

  protected Item() {}

  public Item(
      User seller,
      Category category,
      String title,
      String description,
      BigDecimal price,
      ItemCondition condition) {
    this.seller = seller;
    this.category = category;
    this.title = title;
    this.description = description;
    this.price = price;
    this.condition = condition;
  }

  public Long getId() {
    return id;
  }

  public User getSeller() {
    return seller;
  }

  public Category getCategory() {
    return category;
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  public BigDecimal getPrice() {
    return price;
  }

  public ItemCondition getCondition() {
    return condition;
  }

  public ItemStatus getStatus() {
    return status;
  }

  public Instant getListedAt() {
    return listedAt;
  }

  public void markSold() {
    status = ItemStatus.SOLD;
  }

  public void withdraw() {
    status = ItemStatus.WITHDRAWN;
  }

  public void update(
      Category category,
      String title,
      String description,
      BigDecimal price,
      ItemCondition condition) {
    this.category = category;
    this.title = title.trim();
    this.description = description.trim();
    this.price = price;
    this.condition = condition;
  }
}
