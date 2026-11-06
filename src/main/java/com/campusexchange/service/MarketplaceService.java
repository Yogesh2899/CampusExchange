package com.campusexchange.service;

import com.campusexchange.model.*;
import com.campusexchange.repository.*;
import java.math.BigDecimal;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MarketplaceService {
  @jakarta.persistence.PersistenceContext private jakarta.persistence.EntityManager entityManager;

  public List<Item> listings(Long id) {
    return items.findBySellerIdOrderByListedAtDesc(id);
  }

  @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
  public void updateItem(
      Long id,
      User seller,
      Long categoryId,
      String title,
      String description,
      BigDecimal price,
      ItemCondition condition) {
    Item item = lockedItem(id);
    owner(item, seller);
    available(item);
    validateListing(title, description, price);
    if (pending(id).size() > 0)
      throw new IllegalArgumentException("Resolve pending requests before editing this listing.");
    Category category =
        categories
            .findById(categoryId)
            .orElseThrow(() -> new IllegalArgumentException("Choose a valid category."));
    item.update(category, title, description, price, condition);
  }

  @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
  public void withdrawItem(Long id, User seller) {
    Item item = lockedItem(id);
    owner(item, seller);
    available(item);
    item.withdraw();
    pending(id).forEach(r -> r.setStatus(RequestStatus.REJECTED));
  }

  @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
  public void reject(Long id, User seller) {
    PurchaseRequest r = lockedRequest(id);
    owner(r.getItem(), seller);
    requirePending(r);
    r.setStatus(RequestStatus.REJECTED);
  }

  @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
  public void cancel(Long id, User buyer) {
    PurchaseRequest r = lockedRequest(id);
    if (!r.getBuyer().getId().equals(buyer.getId()))
      throw new IllegalArgumentException("Only the buyer can cancel this request.");
    requirePending(r);
    r.setStatus(RequestStatus.CANCELLED);
  }

  // READ_COMMITTED ensures reads made after a waited row lock see the previous owner's commit.
  private Item lockedItem(Long id) {
    Item item =
        items
            .findByIdForUpdate(id)
            .orElseThrow(() -> new IllegalArgumentException("Listing not found."));
    entityManager.refresh(item);
    return item;
  }

  private PurchaseRequest lockedRequest(Long id) {
    PurchaseRequest r =
        requests.findById(id).orElseThrow(() -> new IllegalArgumentException("Request not found."));
    lockedItem(r.getItem().getId());
    entityManager.refresh(r);
    return r;
  }

  private List<PurchaseRequest> pending(Long itemId) {
    return requests
        .findByItemSellerIdOrderByRequestedAtDesc(
            items.findById(itemId).orElseThrow().getSeller().getId())
        .stream()
        .filter(r -> r.getItem().getId().equals(itemId) && r.getStatus() == RequestStatus.PENDING)
        .toList();
  }

  private void owner(Item item, User user) {
    if (!item.getSeller().getId().equals(user.getId()))
      throw new IllegalArgumentException("Only the seller can manage this listing.");
  }

  private void available(Item item) {
    if (item.getStatus() != ItemStatus.AVAILABLE)
      throw new IllegalArgumentException("This listing is no longer available.");
  }

  private void requirePending(PurchaseRequest r) {
    if (r.getStatus() != RequestStatus.PENDING)
      throw new IllegalArgumentException("This request is no longer pending.");
  }

  private void validateListing(String title, String description, BigDecimal price) {
    if (title == null
        || title.isBlank()
        || title.trim().length() > 150
        || description == null
        || description.isBlank()
        || description.trim().length() > 2000)
      throw new IllegalArgumentException(
          "Enter a title up to 150 characters and description up to 2000 characters.");
    if (price == null
        || price.signum() <= 0
        || price.compareTo(new BigDecimal("99999999.99")) > 0
        || price.stripTrailingZeros().scale() > 2)
      throw new IllegalArgumentException("Enter a positive price with at most two decimal places.");
  }

  private final EmailNotifications notifications;
  private final ItemRepository items;
  private final CategoryRepository categories;
  private final PurchaseRequestRepository requests;
  private final TradeRepository trades;

  public MarketplaceService(
      ItemRepository items,
      CategoryRepository categories,
      PurchaseRequestRepository requests,
      TradeRepository trades,
      EmailNotifications notifications) {
    this.notifications = notifications;
    this.items = items;
    this.categories = categories;
    this.requests = requests;
    this.trades = trades;
  }

  public List<Item> search(String q, Long categoryId) {
    return items.search(
        ItemStatus.AVAILABLE,
        (q == null || q.isBlank()) ? null : q.trim(),
        categoryId,
        null,
        null,
        null);
  }

  @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
  public Item createItem(
      User seller,
      Long categoryId,
      String title,
      String description,
      BigDecimal price,
      ItemCondition condition) {
    validateListing(title, description, price);
    Category category =
        categories
            .findById(categoryId)
            .orElseThrow(() -> new IllegalArgumentException("Choose a valid category."));
    Item item =
        items.save(new Item(seller, category, title.trim(), description.trim(), price, condition));
    notifications.listingPublished(item);
    return item;
  }

  @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
  public void requestItem(Long itemId, User buyer, String message) {
    Item item = lockedItem(itemId);
    if (!item.getSeller().isEnabled())
      throw new IllegalArgumentException("This seller is currently unavailable.");
    if (message != null && message.length() > 255)
      throw new IllegalArgumentException("Message must be at most 255 characters.");
    if (item.getStatus() != ItemStatus.AVAILABLE)
      throw new IllegalArgumentException("This item is no longer available.");
    if (item.getSeller().getId().equals(buyer.getId()))
      throw new IllegalArgumentException("You cannot request your own listing.");
    if (requests.existsByItemIdAndBuyerIdAndStatus(itemId, buyer.getId(), RequestStatus.PENDING))
      throw new IllegalArgumentException("You already have a pending request for this item.");
    requests.save(new PurchaseRequest(item, buyer, message == null ? "" : message.trim()));
  }

  @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
  public void accept(Long requestId, User actor) {
    PurchaseRequest request = lockedRequest(requestId);
    Item locked = request.getItem();
    if (!request.getBuyer().isEnabled())
      throw new IllegalArgumentException("This buyer is currently suspended.");
    if (!locked.getSeller().getId().equals(actor.getId()))
      throw new IllegalArgumentException("Only the seller can accept this request.");
    if (request.getStatus() != RequestStatus.PENDING || locked.getStatus() != ItemStatus.AVAILABLE)
      throw new IllegalArgumentException("This request is no longer pending or the item has sold.");
    request.setStatus(RequestStatus.ACCEPTED);
    locked.markSold();
    trades.save(new Trade(request));
    for (PurchaseRequest other : requests.findByItemSellerIdOrderByRequestedAtDesc(actor.getId())) {
      if (other.getItem().getId().equals(locked.getId())
          && !other.getId().equals(requestId)
          && other.getStatus() == RequestStatus.PENDING) other.setStatus(RequestStatus.REJECTED);
    }
  }

  public List<Item> search(
      String q, Long categoryId, BigDecimal min, BigDecimal max, ItemCondition condition) {
    if ((min != null && min.signum() < 0)
        || (max != null && max.signum() < 0)
        || (min != null && max != null && min.compareTo(max) > 0))
      throw new IllegalArgumentException("Use a valid nonnegative price range.");
    return items.search(
        ItemStatus.AVAILABLE,
        (q == null || q.isBlank()) ? null : q.trim(),
        categoryId,
        min,
        max,
        condition);
  }

  public List<PurchaseRequest> sellerRequests(Long id) {
    return requests.findByItemSellerIdOrderByRequestedAtDesc(id);
  }

  public List<PurchaseRequest> buyerRequests(Long id) {
    return requests.findByBuyerIdOrderByRequestedAtDesc(id);
  }

  public List<Trade> history(Long id) {
    return trades.findByBuyerIdOrSellerIdOrderByCompletedAtDesc(id, id);
  }

  public List<Category> categories() {
    return categories.findAll();
  }
}
