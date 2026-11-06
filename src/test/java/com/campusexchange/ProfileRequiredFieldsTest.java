package com.campusexchange;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.campusexchange.model.User;
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
class ProfileRequiredFieldsTest {
  @Autowired MockMvc mvc;
  @Autowired AccountService accounts;
  @Autowired UserRepository users;

  @Test
  void phoneAndHostelCannotBeOmittedOrCleared() throws Exception {
    User student =
        accounts.register(
            "Profile student", "profile-" + UUID.randomUUID() + "@thapar.edu", "Password123!");
    mvc.perform(post("/profile").with(user(student)).with(csrf()).param("name", "Student"))
        .andExpect(status().isOk())
        .andExpect(model().attributeHasFieldErrors("form", "phone", "hostel"));
    mvc.perform(
            post("/profile")
                .with(user(student))
                .with(csrf())
                .param("name", "Student")
                .param("phone", " ")
                .param("hostel", " "))
        .andExpect(status().isOk())
        .andExpect(model().attributeHasFieldErrors("form", "phone", "hostel"));
    mvc.perform(
            post("/profile")
                .with(user(student))
                .with(csrf())
                .param("name", "Student")
                .param("phone", "+91 9876543210")
                .param("hostel", "Hostel A")
                .param("department", ""))
        .andExpect(redirectedUrl("/profile"));
    mvc.perform(
            post("/profile")
                .with(user(student))
                .with(csrf())
                .param("name", "Changed")
                .param("phone", "")
                .param("hostel", "Hostel A"))
        .andExpect(status().isOk())
        .andExpect(model().attributeHasFieldErrors("form", "phone"));
    mvc.perform(
            post("/profile")
                .with(user(student))
                .with(csrf())
                .param("name", "Changed")
                .param("phone", "+91 9876543210")
                .param("hostel", ""))
        .andExpect(status().isOk())
        .andExpect(model().attributeHasFieldErrors("form", "hostel"));
    assertThrows(
        IllegalArgumentException.class,
        () -> accounts.updateProfile(student.getId(), "Changed", null, "", "Hostel A"));
    assertThrows(
        IllegalArgumentException.class,
        () -> accounts.updateProfile(student.getId(), "Changed", "+91 9876543210", "", null));
    User saved = users.findById(student.getId()).orElseThrow();
    assertEquals("Student", saved.getName());
    assertEquals("+91 9876543210", saved.getPhone());
    assertEquals("Hostel A", saved.getHostel());
    assertEquals("", saved.getDepartment());
  }
}
