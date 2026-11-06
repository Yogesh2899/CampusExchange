package com.campusexchange.controller;

import com.campusexchange.model.User;
import com.campusexchange.service.PhotoService;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PhotoController {
  private final PhotoService photos;

  public PhotoController(PhotoService photos) {
    this.photos = photos;
  }

  @GetMapping("/photos/{id}")
  @ResponseBody
  public ResponseEntity<byte[]> photo(@PathVariable Long id) {
    return photos
        .visible(id)
        .map(
            p ->
                ResponseEntity.ok()
                    .cacheControl(CacheControl.noCache())
                    .contentType(MediaType.IMAGE_PNG)
                    .body(p.getData()))
        .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @PostMapping("/items/{id}/photo")
  public String upload(
      @PathVariable Long id,
      @AuthenticationPrincipal User user,
      @RequestParam MultipartFile photo,
      RedirectAttributes flash) {
    try {
      photos.upload(id, user, photo);
      flash.addFlashAttribute("notice", "Photo saved.");
    } catch (IllegalArgumentException ex) {
      flash.addFlashAttribute("error", ex.getMessage());
    }
    return "redirect:/items/" + id;
  }
}
