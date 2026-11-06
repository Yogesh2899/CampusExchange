package com.campusexchange.controller;

import com.campusexchange.model.User;
import com.campusexchange.service.PhotoService;
import java.util.Set;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class ViewAdvice {
  private final PhotoService photos;

  public ViewAdvice(PhotoService photos) {
    this.photos = photos;
  }

  @ModelAttribute("photoIds")
  public Set<Long> photos() {
    return photos.visibleIds();
  }

  @ModelAttribute("viewerId")
  public Long viewer(@AuthenticationPrincipal User user) {
    return user == null ? null : user.getId();
  }

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public String tooLarge(RedirectAttributes flash) {
    flash.addFlashAttribute("error", "The upload is too large. Choose a JPEG or PNG up to 3 MB.");
    return "redirect:/my-listings";
  }
}
