package com.campusexchange.model;

import jakarta.persistence.*;

@Entity
@Table(name = "item_photos")
public class ItemPhoto {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(optional = false)
  @JoinColumn(name = "item_id", nullable = false, unique = true)
  private Item item;

  @Lob
  @Column(nullable = false, columnDefinition = "LONGBLOB")
  private byte[] data;

  protected ItemPhoto() {}

  public ItemPhoto(Item item, byte[] data) {
    this.item = item;
    this.data = data;
  }

  public Item getItem() {
    return item;
  }

  public byte[] getData() {
    return data;
  }

  public void replace(byte[] data) {
    this.data = data;
  }
}
