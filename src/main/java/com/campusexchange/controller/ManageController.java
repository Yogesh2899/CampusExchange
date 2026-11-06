package com.campusexchange.controller;

import com.campusexchange.model.*;
import com.campusexchange.repository.ItemRepository;
import com.campusexchange.service.MarketplaceService;
import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ManageController {
  private final MarketplaceService market;
  private final ItemRepository items;

  public ManageController(MarketplaceService market, ItemRepository items) {
    this.market = market;
    this.items = items;
  }

  @GetMapping("/my-listings")
  public String listings(@AuthenticationPrincipal User user, Model model) {
    model.addAttribute("items", market.listings(user.getId()));
    return "my-listings";
  }

  @GetMapping("/items/{id}/edit")
  public String edit(@PathVariable Long id, @AuthenticationPrincipal User user, Model model) {
    Item item =
        items.findById(id).orElseThrow(() -> new IllegalArgumentException("Listing not found."));
    if (!item.getSeller().getId().equals(user.getId()))
      throw new IllegalArgumentException("Only the seller can edit this listing.");
    if (item.getStatus() != ItemStatus.AVAILABLE)
      throw new IllegalArgumentException("Only available listings can be edited.");
    model.addAttribute("item", item);
    model.addAttribute("categories", market.categories());
    model.addAttribute("conditions", ItemCondition.values());
    return "edit-item";
  }

  @PostMapping("/items/{id}/edit")
  public String update(
      @PathVariable Long id,
      @AuthenticationPrincipal User user,
      @RequestParam Long categoryId,
      @RequestParam String title,
      @RequestParam String description,
      @RequestParam BigDecimal price,
      @RequestParam ItemCondition condition,
      RedirectAttributes flash) {
    market.updateItem(id, user, categoryId, title, description, price, condition);
    flash.addFlashAttribute("notice", "Listing updated.");
    return "redirect:/my-listings";
  }

  @PostMapping("/items/{id}/withdraw")
  public String withdraw(
      @PathVariable Long id, @AuthenticationPrincipal User user, RedirectAttributes flash) {
    market.withdrawItem(id, user);
    flash.addFlashAttribute(
        "notice", "Listing removed from browsing. Its request history is kept.");
    return "redirect:/my-listings";
  }

  @PostMapping("/requests/{id}/reject")
  public String reject(
      @PathVariable Long id, @AuthenticationPrincipal User user, RedirectAttributes flash) {
    market.reject(id, user);
    flash.addFlashAttribute("notice", "Request rejected.");
    return "redirect:/requests";
  }

  @PostMapping("/requests/{id}/cancel")
  public String cancel(
      @PathVariable Long id, @AuthenticationPrincipal User user, RedirectAttributes flash) {
    market.cancel(id, user);
    flash.addFlashAttribute("notice", "Request cancelled.");
    return "redirect:/requests";
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public String invalidAction(
      IllegalArgumentException ex, HttpServletRequest request, RedirectAttributes flash) {
    flash.addFlashAttribute("error", ex.getMessage());
    return request.getRequestURI().startsWith("/requests/")
        ? "redirect:/requests"
        : "redirect:/my-listings";
  }
}
