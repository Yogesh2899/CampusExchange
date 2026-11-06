package com.campusexchange.controller;

import com.campusexchange.model.*;
import com.campusexchange.repository.ItemRepository;
import com.campusexchange.service.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class WebController {
  private final MarketplaceService market;
  private final AccountService accounts;
  private final ItemRepository items;
  private final ReviewService reviews;

  public WebController(
      MarketplaceService market,
      AccountService accounts,
      ItemRepository items,
      ReviewService reviews) {
    this.reviews = reviews;
    this.market = market;
    this.accounts = accounts;
    this.items = items;
  }

  @GetMapping({"/", "/items"})
  public String home(
      @RequestParam(required = false) String q,
      @RequestParam(required = false) Long category,
      @RequestParam(required = false) BigDecimal minPrice,
      @RequestParam(required = false) BigDecimal maxPrice,
      @RequestParam(required = false) ItemCondition condition,
      Model model) {
    try {
      model.addAttribute("items", market.search(q, category, minPrice, maxPrice, condition));
    } catch (IllegalArgumentException ex) {
      model.addAttribute("items", java.util.List.of());
      model.addAttribute("error", ex.getMessage());
    }
    model.addAttribute("minPrice", minPrice);
    model.addAttribute("maxPrice", maxPrice);
    model.addAttribute("selectedCondition", condition);
    model.addAttribute("conditions", ItemCondition.values());
    model.addAttribute("categories", market.categories());
    model.addAttribute("q", q == null ? "" : q);
    model.addAttribute("selectedCategory", category);
    return "home";
  }

  @GetMapping("/items/{id}")
  public String item(@PathVariable Long id, Model model) {
    Item item =
        items
            .findById(id)
            .orElseThrow(
                () ->
                    new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND));
    if (!item.getSeller().isEnabled())
      throw new org.springframework.web.server.ResponseStatusException(
          org.springframework.http.HttpStatus.NOT_FOUND);
    model.addAttribute("item", item);
    var sellerReviews = reviews.forSeller(item.getSeller().getId());
    model.addAttribute("sellerReviews", sellerReviews);
    model.addAttribute("sellerRating", reviews.average(sellerReviews));
    return "item";
  }

  @GetMapping("/register")
  public String register(Model model) {
    model.addAttribute("form", new RegisterForm());
    return "register";
  }

  @PostMapping("/register")
  public String register(
      @Valid @ModelAttribute("form") RegisterForm form,
      BindingResult result,
      RedirectAttributes flash) {
    if (form.password != null && !form.password.equals(form.confirmPassword))
      result.rejectValue("confirmPassword", "mismatch", "Passwords do not match.");
    if (result.hasErrors()) return "register";
    try {
      accounts.register(form.name, form.email, form.password);
    } catch (IllegalArgumentException ex) {
      result.reject("registration", ex.getMessage());
      return "register";
    }
    flash.addFlashAttribute("notice", "Account created. Sign in to continue.");
    return "redirect:/login";
  }

  @GetMapping("/login")
  public String login() {
    return "login";
  }

  @GetMapping("/items/new")
  public String newItem(Model model) {
    model.addAttribute("categories", market.categories());
    model.addAttribute("conditions", ItemCondition.values());
    return "new-item";
  }

  @PostMapping("/items/new")
  public String createItem(
      @AuthenticationPrincipal User user,
      @RequestParam Long categoryId,
      @RequestParam @NotBlank @Size(max = 150) String title,
      @RequestParam @NotBlank @Size(max = 2000) String description,
      @RequestParam @DecimalMin("0.01") BigDecimal price,
      @RequestParam ItemCondition condition,
      RedirectAttributes flash) {
    market.createItem(user, categoryId, title, description, price, condition);
    flash.addFlashAttribute("notice", "Your listing is live.");
    return "redirect:/";
  }

  @PostMapping("/items/{id}/request")
  public String request(
      @PathVariable Long id,
      @AuthenticationPrincipal User user,
      @RequestParam(required = false) String message,
      RedirectAttributes flash) {
    try {
      market.requestItem(id, user, message);
      flash.addFlashAttribute("notice", "Purchase request sent to the seller.");
    } catch (IllegalArgumentException ex) {
      flash.addFlashAttribute("error", ex.getMessage());
    }
    return "redirect:/items/" + id;
  }

  @GetMapping("/requests")
  public String requests(@AuthenticationPrincipal User user, Model model) {
    model.addAttribute("incoming", market.sellerRequests(user.getId()));
    model.addAttribute("outgoing", market.buyerRequests(user.getId()));
    return "requests";
  }

  @PostMapping("/requests/{id}/accept")
  public String accept(
      @PathVariable Long id, @AuthenticationPrincipal User user, RedirectAttributes flash) {
    try {
      market.accept(id, user);
      flash.addFlashAttribute("notice", "Request accepted. The listing is now marked sold.");
    } catch (IllegalArgumentException ex) {
      flash.addFlashAttribute("error", ex.getMessage());
    }
    return "redirect:/requests";
  }

  @GetMapping("/history")
  public String history(@AuthenticationPrincipal User user, Model model) {
    var history = market.history(user.getId());
    model.addAttribute("trades", history);
    model.addAttribute("reviewByTrade", reviews.forTrades(history));
    model.addAttribute("currentUserId", user.getId());
    return "history";
  }

  public static class RegisterForm {
    @NotBlank
    @Size(max = 100)
    private String name;

    @NotBlank
    @Email
    @Pattern(
        regexp = "(?i)^[^\\s@]+@thapar\\.edu$",
        message = "Use your college email ending in @thapar.edu.")
    @Size(max = 160)
    private String email;

    @NotBlank
    @Size(min = 8, max = 72)
    private String password;

    @NotBlank private String confirmPassword;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getEmail() {
      return email;
    }

    public void setEmail(String email) {
      this.email = email;
    }

    public String getPassword() {
      return password;
    }

    public void setPassword(String password) {
      this.password = password;
    }

    public String getConfirmPassword() {
      return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
      this.confirmPassword = confirmPassword;
    }
  }
}
