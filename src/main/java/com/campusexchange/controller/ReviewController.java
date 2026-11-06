package com.campusexchange.controller;

import com.campusexchange.model.User;
import com.campusexchange.service.ReviewService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ReviewController {
  private final ReviewService reviews;

  public ReviewController(ReviewService reviews) {
    this.reviews = reviews;
  }

  @PostMapping("/trades/{id}/review")
  public String review(
      @PathVariable Long id,
      @AuthenticationPrincipal User user,
      @RequestParam int rating,
      @RequestParam(defaultValue = "") String comment,
      RedirectAttributes flash) {
    try {
      reviews.submit(id, user, rating, comment);
      flash.addFlashAttribute("notice", "Thank you. Your review has been published.");
    } catch (IllegalArgumentException ex) {
      flash.addFlashAttribute("error", ex.getMessage());
    }
    return "redirect:/history";
  }
}
