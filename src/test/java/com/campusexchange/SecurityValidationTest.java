package com.campusexchange;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.campusexchange.model.*;
import com.campusexchange.service.*;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityValidationTest {
  @Autowired MockMvc mvc;
  @Autowired AccountService accounts;
  @Autowired MarketplaceService market;

  @Test
  void csrfEscapingAndInvalidInput() throws Exception {
    mvc.perform(get("/js/password-visibility.js"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("data-password-toggle")));
    mvc.perform(get("/register"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Show confirm password")));
    mvc.perform(get("/login"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Show password")));
    String id = UUID.randomUUID().toString();
    User seller =
        accounts.register("Validation seller", "check-" + id + "@thapar.edu", "Password123!");
    Long category = market.categories().get(0).getId();
    var item =
        market.createItem(
            seller,
            category,
            "<script>alert('x')</script>",
            "Escaping check",
            BigDecimal.TEN,
            ItemCondition.GOOD);
    mvc.perform(get("/items/" + item.getId()))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("&lt;script&gt;")))
        .andExpect(content().string(not(containsString("<script>alert"))));
    mvc.perform(post("/items/" + item.getId() + "/withdraw").with(user(seller)))
        .andExpect(status().isForbidden());
    mvc.perform(post("/profile").with(user(seller)).param("name", "CSRF change"))
        .andExpect(status().isForbidden());
    mvc.perform(get("/profile")).andExpect(status().is3xxRedirection());
    mvc.perform(get("/items").param("minPrice", "not-a-price"))
        .andExpect(status().isBadRequest())
        .andExpect(view().name("problem"));
    mvc.perform(get("/items").param("condition", "UNKNOWN")).andExpect(status().isBadRequest());
    mvc.perform(get("/items").param("minPrice", "100").param("maxPrice", "1"))
        .andExpect(status().isOk())
        .andExpect(model().attributeExists("error"));
    mvc.perform(get("/items/9223372036854775807")).andExpect(status().isNotFound());
    mvc.perform(
            post("/items/new")
                .with(user(seller))
                .with(csrf())
                .param("categoryId", category.toString())
                .param("title", "Bad price")
                .param("description", "Description")
                .param("price", "-1")
                .param("condition", "GOOD"))
        .andExpect(status().isBadRequest());
    mvc.perform(
            post("/register")
                .with(csrf())
                .param("name", " ")
                .param("email", "bad")
                .param("password", "short")
                .param("confirmPassword", "different"))
        .andExpect(status().isOk())
        .andExpect(model().attributeHasErrors("form"));
  }
}
