package com.campusexchange.controller;

import com.campusexchange.model.User;
import com.campusexchange.repository.UserRepository;
import com.campusexchange.service.AccountService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ProfileController {
  private final UserRepository users;
  private final AccountService accounts;

  public ProfileController(UserRepository users, AccountService accounts) {
    this.users = users;
    this.accounts = accounts;
  }

  @GetMapping("/profile")
  public String profile(@AuthenticationPrincipal User user, Model model) {
    User saved = users.findById(user.getId()).orElseThrow();
    Form form = new Form();
    form.setName(saved.getName());
    form.setPhone(saved.getPhone());
    form.setDepartment(saved.getDepartment());
    form.setHostel(saved.getHostel());
    model.addAttribute("form", form);
    model.addAttribute("email", saved.getEmail());
    return "profile";
  }

  @PostMapping("/profile")
  public String update(
      @AuthenticationPrincipal User user,
      @Valid @ModelAttribute("form") Form form,
      BindingResult result,
      Model model,
      RedirectAttributes flash) {
    if (result.hasErrors()) {
      model.addAttribute("email", user.getEmail());
      return "profile";
    }
    accounts.updateProfile(
        user.getId(), form.getName(), form.getPhone(), form.getDepartment(), form.getHostel());
    flash.addFlashAttribute("notice", "Profile updated.");
    return "redirect:/profile";
  }

  public static class Form {
    @NotBlank
    @Size(max = 100)
    private String name;

    @NotBlank(message = "Phone is required.")
    @Pattern(regexp = "[0-9+() .-]{0,30}", message = "Use a phone number up to 30 characters.")
    private String phone = "";

    @Size(max = 100)
    private String department = "";

    @NotBlank(message = "Hostel is required.")
    @Size(max = 100)
    private String hostel = "";

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public String getPhone() {
      return phone;
    }

    public void setPhone(String phone) {
      this.phone = phone;
    }

    public String getDepartment() {
      return department;
    }

    public void setDepartment(String department) {
      this.department = department;
    }

    public String getHostel() {
      return hostel;
    }

    public void setHostel(String hostel) {
      this.hostel = hostel;
    }
  }
}
