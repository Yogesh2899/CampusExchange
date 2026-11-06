package com.campusexchange;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.campusexchange.config.CollegeEmailPolicy;
import com.campusexchange.model.*;
import com.campusexchange.repository.UserRepository;
import com.campusexchange.service.AccountService;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class CollegeEmailTest {
  @Autowired AccountService accounts;
  @Autowired UserRepository users;
  @Autowired MockMvc mvc;

  @Test
  void exactCollegeDomainForRegistrationLoginAndExistingSessions() throws Exception {
    String id = UUID.randomUUID().toString();
    String email = "college-" + id + "@thapar.edu";
    User allowed =
        accounts.register("College student", "  " + email.toUpperCase() + "  ", "Password123!");
    assertEquals(email, allowed.getEmail());
    mvc.perform(
            post("/login")
                .with(csrf())
                .param("username", email.toUpperCase())
                .param("password", "Password123!"))
        .andExpect(redirectedUrl("/"));

    for (String blocked :
        new String[] {
          "student@gmail.com",
          "student@notthapar.edu",
          "student@sub.thapar.edu",
          "student@thapar.edu.evil.com",
          "student@@thapar.edu",
          "@thapar.edu",
          "student name@thapar.edu"
        }) {
      assertFalse(CollegeEmailPolicy.isAllowed(blocked), blocked);
      assertThrows(
          IllegalArgumentException.class,
          () -> accounts.register("Blocked", blocked, "Password123!"));
    }
    mvc.perform(
            post("/register")
                .with(csrf())
                .param("name", "Outside student")
                .param("email", "outside-" + id + "@gmail.com")
                .param("password", "Password123!")
                .param("confirmPassword", "Password123!"))
        .andExpect(status().isOk())
        .andExpect(model().attributeHasFieldErrors("form", "email"));

    User legacy =
        users.save(
            new User(
                "Legacy account",
                "legacy-" + id + "@gmail.com",
                allowed.getPassword(),
                Role.STUDENT));
    mvc.perform(
            post("/login")
                .with(csrf())
                .param("username", legacy.getEmail())
                .param("password", "Password123!"))
        .andExpect(redirectedUrl("/login?error"));
    mvc.perform(get("/profile").with(user(legacy))).andExpect(redirectedUrl("/login?college"));
    assertTrue(users.existsById(legacy.getId()));
  }
}
