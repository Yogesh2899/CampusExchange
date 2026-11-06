package com.campusexchange.controller;

import com.campusexchange.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {
  private final AdminService admin;
  private final MarketplaceService market;

  public AdminController(AdminService admin, MarketplaceService market) {
    this.admin = admin;
    this.market = market;
  }

  @GetMapping
  public String dashboard(Model model) {
    model.addAttribute("users", admin.users());
    model.addAttribute("items", admin.listings());
    model.addAttribute("categories", market.categories());
    return "admin";
  }

  @PostMapping("/items/{id}/remove")
  public String remove(@PathVariable Long id, RedirectAttributes flash) {
    admin.removeListing(id);
    flash.addFlashAttribute("notice", "Listing removed and pending requests rejected.");
    return "redirect:/admin";
  }

  @PostMapping("/categories")
  public String add(@RequestParam String name, RedirectAttributes flash) {
    admin.addCategory(name);
    flash.addFlashAttribute("notice", "Category added.");
    return "redirect:/admin";
  }

  @PostMapping("/categories/{id}/delete")
  public String delete(@PathVariable Long id, RedirectAttributes flash) {
    admin.deleteCategory(id);
    flash.addFlashAttribute("notice", "Category deleted.");
    return "redirect:/admin";
  }

  @PostMapping("/users/{id}/status")
  public String status(
      @PathVariable Long id, @RequestParam boolean enabled, RedirectAttributes flash) {
    admin.setAccountEnabled(id, enabled);
    flash.addFlashAttribute(
        "notice",
        enabled
            ? "Account restored."
            : "Account suspended. Existing sessions lose access on their next request.");
    return "redirect:/admin";
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public String invalid(IllegalArgumentException ex, RedirectAttributes flash) {
    flash.addFlashAttribute("error", ex.getMessage());
    return "redirect:/admin";
  }
}
