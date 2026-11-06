package com.campusexchange.service;

import com.campusexchange.model.*;
import com.campusexchange.repository.*;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@PreAuthorize("hasRole('ADMIN')")
public class AdminService {
  private final UserRepository users;
  private final ItemRepository items;
  private final CategoryRepository categories;
  private final PurchaseRequestRepository requests;

  public AdminService(
      ItemRepository items,
      CategoryRepository categories,
      PurchaseRequestRepository requests,
      UserRepository users) {
    this.users = users;
    this.items = items;
    this.categories = categories;
    this.requests = requests;
  }

  public List<User> users() {
    return users.findAll();
  }

  @Transactional
  public void setAccountEnabled(Long id, boolean enabled) {
    User user =
        users.findById(id).orElseThrow(() -> new IllegalArgumentException("Account not found."));
    if (user.getRole() == Role.ADMIN)
      throw new IllegalArgumentException("Administrator accounts cannot be suspended here.");
    user.setEnabled(enabled);
  }

  public List<Item> listings() {
    return items.findAll();
  }

  @Transactional
  public void removeListing(Long id) {
    Item item =
        items
            .findByIdForUpdate(id)
            .orElseThrow(() -> new IllegalArgumentException("Listing not found."));
    if (item.getStatus() != ItemStatus.AVAILABLE)
      throw new IllegalArgumentException("Only available listings can be removed.");
    item.withdraw();
    requests.findByItemSellerIdOrderByRequestedAtDesc(item.getSeller().getId()).stream()
        .filter(r -> r.getItem().getId().equals(id) && r.getStatus() == RequestStatus.PENDING)
        .forEach(r -> r.setStatus(RequestStatus.REJECTED));
  }

  @Transactional
  public void addCategory(String name) {
    if (name == null || name.isBlank() || name.trim().length() > 60)
      throw new IllegalArgumentException("Category names must contain 1–60 characters.");
    if (categories.findAll().stream().anyMatch(c -> c.getName().equalsIgnoreCase(name.trim())))
      throw new IllegalArgumentException("That category already exists.");
    categories.save(new Category(name.trim()));
  }

  @Transactional
  public void deleteCategory(Long id) {
    Category category =
        categories
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Category not found."));
    if (items.existsByCategoryId(id))
      throw new IllegalArgumentException(
          "This category is used by listings and cannot be deleted.");
    categories.delete(category);
  }
}
