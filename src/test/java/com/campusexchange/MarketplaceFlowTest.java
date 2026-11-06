package com.campusexchange;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.campusexchange.model.*;
import com.campusexchange.repository.*;
import com.campusexchange.service.*;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class MarketplaceFlowTest {
  @Autowired MockMvc mvc;
  @Autowired AccountService accounts;
  @Autowired MarketplaceService market;
  @Autowired ItemRepository items;
  @Autowired PurchaseRequestRepository requests;
  @Autowired TradeRepository trades;
  @Autowired ReviewService reviews;
  @Autowired PhotoService photos;
  @Autowired CategoryRepository categories;
  @Autowired UserRepository users;

  @Test
  void pagesAndPurchaseFlow() throws Exception {
    mvc.perform(get("/")).andExpect(status().isOk());
    mvc.perform(get("/register")).andExpect(status().isOk());
    mvc.perform(get("/login")).andExpect(status().isOk());
    long initialTrades = trades.count();
    String suffix = java.util.UUID.randomUUID().toString();
    User seller = accounts.register("Seller", "seller-" + suffix + "@thapar.edu", "Password123!");
    User buyer = accounts.register("Buyer", "buyer-" + suffix + "@thapar.edu", "Password123!");
    User secondBuyer =
        accounts.register("Other Buyer", "other-" + suffix + "@thapar.edu", "Password123!");
    Item item =
        market.createItem(
            seller,
            market.categories().get(0).getId(),
            "Database textbook",
            "Good condition",
            new BigDecimal("450"),
            ItemCondition.GOOD);
    mvc.perform(get("/items/" + item.getId())).andExpect(status().isOk());
    mvc.perform(get("/items/new").with(user(seller))).andExpect(status().isOk());
    assertThrows(
        IllegalArgumentException.class,
        () -> market.requestItem(item.getId(), seller, "Own listing"));
    market.requestItem(item.getId(), buyer, "Interested");
    market.requestItem(item.getId(), secondBuyer, "Me too");
    assertThrows(
        IllegalArgumentException.class, () -> market.requestItem(item.getId(), buyer, "Duplicate"));
    Long requestId = market.buyerRequests(buyer.getId()).get(0).getId();
    assertThrows(IllegalArgumentException.class, () -> market.accept(requestId, buyer));
    mvc.perform(get("/requests").with(user(seller))).andExpect(status().isOk());
    mvc.perform(post("/requests/" + requestId + "/accept").with(user(seller)).with(csrf()))
        .andExpect(status().is3xxRedirection());
    assertEquals(ItemStatus.SOLD, items.findById(item.getId()).orElseThrow().getStatus());
    assertEquals(RequestStatus.ACCEPTED, requests.findById(requestId).orElseThrow().getStatus());
    assertEquals(
        RequestStatus.REJECTED, market.buyerRequests(secondBuyer.getId()).get(0).getStatus());
    assertEquals(initialTrades + 1, trades.count());
    assertThrows(IllegalArgumentException.class, () -> market.accept(requestId, seller));
    mvc.perform(get("/history").with(user(buyer))).andExpect(status().isOk());
    Trade completed = market.history(buyer.getId()).get(0);
    assertThrows(
        IllegalArgumentException.class,
        () -> reviews.submit(completed.getId(), seller, 5, "Self review"));
    assertThrows(
        IllegalArgumentException.class,
        () -> reviews.submit(completed.getId(), secondBuyer, 5, "Unrelated buyer"));
    assertThrows(
        IllegalArgumentException.class,
        () -> reviews.submit(completed.getId(), buyer, 6, "Out of range"));
    assertThrows(
        IllegalArgumentException.class,
        () -> reviews.submit(completed.getId(), buyer, 5, "x".repeat(501)));
    mvc.perform(
            post("/trades/" + completed.getId() + "/review")
                .with(user(buyer))
                .with(csrf())
                .param("rating", "5")
                .param("comment", "Helpful seller"))
        .andExpect(redirectedUrl("/history"));
    assertEquals("5.0 / 5", reviews.average(reviews.forSeller(seller.getId())));
    assertThrows(
        IllegalArgumentException.class,
        () -> reviews.submit(completed.getId(), buyer, 1, "Duplicate"));
    mvc.perform(get("/history").with(user(buyer))).andExpect(status().isOk());
    mvc.perform(get("/history").with(user(seller))).andExpect(status().isOk());
    mvc.perform(get("/items/" + item.getId())).andExpect(status().isOk());
    mvc.perform(post("/items/new").with(csrf())).andExpect(status().is3xxRedirection());
    Long category = market.categories().get(0).getId();
    Item listing =
        market.createItem(
            seller,
            category,
            "Old title",
            "Original description",
            new BigDecimal("50"),
            ItemCondition.FAIR);
    mvc.perform(get("/my-listings").with(user(seller))).andExpect(status().isOk());
    mvc.perform(get("/items/" + listing.getId() + "/edit").with(user(seller)))
        .andExpect(status().isOk());
    assertThrows(
        IllegalArgumentException.class,
        () ->
            market.updateItem(
                listing.getId(),
                buyer,
                category,
                "Stolen edit",
                "Description",
                BigDecimal.TEN,
                ItemCondition.GOOD));
    mvc.perform(
            post("/items/" + listing.getId() + "/edit")
                .with(user(seller))
                .with(csrf())
                .param("categoryId", category.toString())
                .param("title", "Updated title")
                .param("description", "Updated description")
                .param("price", "60.00")
                .param("condition", "GOOD"))
        .andExpect(redirectedUrl("/my-listings"));
    assertEquals("Updated title", items.findById(listing.getId()).orElseThrow().getTitle());
    market.requestItem(listing.getId(), buyer, "First attempt");
    Long pending = market.buyerRequests(buyer.getId()).get(0).getId();
    assertThrows(IllegalArgumentException.class, () -> market.cancel(pending, seller));
    assertThrows(IllegalArgumentException.class, () -> market.reject(pending, buyer));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            market.updateItem(
                listing.getId(),
                seller,
                category,
                "Changed offer",
                "Description",
                BigDecimal.TEN,
                ItemCondition.GOOD));
    mvc.perform(post("/requests/" + pending + "/cancel").with(user(buyer)).with(csrf()))
        .andExpect(redirectedUrl("/requests"));
    assertEquals(RequestStatus.CANCELLED, requests.findById(pending).orElseThrow().getStatus());
    assertThrows(IllegalArgumentException.class, () -> market.accept(pending, seller));
    market.requestItem(listing.getId(), buyer, "Try again");
    Long retry = market.buyerRequests(buyer.getId()).get(0).getId();
    mvc.perform(post("/requests/" + retry + "/reject").with(user(seller)).with(csrf()))
        .andExpect(redirectedUrl("/requests"));
    assertEquals(RequestStatus.REJECTED, requests.findById(retry).orElseThrow().getStatus());
    market.requestItem(listing.getId(), buyer, "Another attempt");
    assertThrows(IllegalArgumentException.class, () -> market.withdrawItem(listing.getId(), buyer));
    mvc.perform(post("/items/" + listing.getId() + "/withdraw").with(user(seller)).with(csrf()))
        .andExpect(redirectedUrl("/my-listings"));
    assertEquals(ItemStatus.WITHDRAWN, items.findById(listing.getId()).orElseThrow().getStatus());
    assertEquals(RequestStatus.REJECTED, market.buyerRequests(buyer.getId()).get(0).getStatus());
    assertTrue(market.search("Updated title", null).isEmpty());
    assertThrows(
        IllegalArgumentException.class,
        () -> market.requestItem(listing.getId(), buyer, "Unavailable"));
    assertThrows(IllegalArgumentException.class, () -> market.withdrawItem(item.getId(), seller));
    mvc.perform(get("/requests").with(user(buyer))).andExpect(status().isOk());
    Item camera =
        market.createItem(
            seller,
            category,
            "Camera " + suffix,
            "For photo checks",
            new BigDecimal("100.00"),
            ItemCondition.LIKE_NEW);
    assertEquals(
        1,
        market
            .search(
                suffix,
                category,
                new BigDecimal("100"),
                new BigDecimal("100"),
                ItemCondition.LIKE_NEW)
            .size());
    assertTrue(market.search(suffix, category, new BigDecimal("101"), null, null).isEmpty());
    assertTrue(market.search(suffix, category, null, null, ItemCondition.FAIR).isEmpty());
    assertThrows(
        IllegalArgumentException.class,
        () -> market.search(null, null, new BigDecimal("200"), BigDecimal.TEN, null));
    mvc.perform(
            get("/items")
                .param("q", suffix)
                .param("minPrice", "100")
                .param("maxPrice", "100")
                .param("condition", "LIKE_NEW"))
        .andExpect(status().isOk());
    var image = new java.awt.image.BufferedImage(10, 10, java.awt.image.BufferedImage.TYPE_INT_RGB);
    var bytes = new java.io.ByteArrayOutputStream();
    javax.imageio.ImageIO.write(image, "png", bytes);
    var photo =
        new org.springframework.mock.web.MockMultipartFile(
            "photo", "photo.png", "image/png", bytes.toByteArray());
    assertThrows(IllegalArgumentException.class, () -> photos.upload(camera.getId(), buyer, photo));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            photos.upload(
                camera.getId(),
                seller,
                new org.springframework.mock.web.MockMultipartFile(
                    "photo", "bad.png", "image/png", "not an image".getBytes())));
    assertThrows(
        IllegalArgumentException.class,
        () ->
            photos.upload(
                camera.getId(),
                seller,
                new org.springframework.mock.web.MockMultipartFile(
                    "photo", "large.png", "image/png", new byte[3 * 1024 * 1024 + 1])));
    mvc.perform(
            multipart("/items/" + camera.getId() + "/photo")
                .file(photo)
                .with(user(seller))
                .with(csrf()))
        .andExpect(redirectedUrl("/items/" + camera.getId()));
    mvc.perform(get("/photos/" + camera.getId()))
        .andExpect(status().isOk())
        .andExpect(content().contentType("image/png"));
    mvc.perform(get("/items/" + camera.getId()).with(user(seller))).andExpect(status().isOk());
    mvc.perform(get("/admin").with(user(buyer))).andExpect(status().isForbidden());
    mvc.perform(post("/admin/items/" + camera.getId() + "/remove").with(user(buyer)).with(csrf()))
        .andExpect(status().isForbidden());
    mvc.perform(get("/admin").with(user("admin").roles("ADMIN"))).andExpect(status().isOk());
    String catName = "Test category " + suffix;
    mvc.perform(
            post("/admin/categories")
                .with(user("admin").roles("ADMIN"))
                .with(csrf())
                .param("name", catName))
        .andExpect(redirectedUrl("/admin"));
    Long added =
        categories.findAll().stream()
            .filter(c -> c.getName().equals(catName))
            .findFirst()
            .orElseThrow()
            .getId();
    mvc.perform(
            post("/admin/categories/" + added + "/delete")
                .with(user("admin").roles("ADMIN"))
                .with(csrf()))
        .andExpect(redirectedUrl("/admin"));
    assertFalse(categories.existsById(added));
    mvc.perform(
            post("/admin/categories/" + category + "/delete")
                .with(user("admin").roles("ADMIN"))
                .with(csrf()))
        .andExpect(flash().attributeExists("error"));
    market.requestItem(camera.getId(), buyer, "Interested in camera");
    mvc.perform(
            post("/admin/items/" + camera.getId() + "/remove")
                .with(user("admin").roles("ADMIN"))
                .with(csrf()))
        .andExpect(redirectedUrl("/admin"));
    assertEquals(ItemStatus.WITHDRAWN, items.findById(camera.getId()).orElseThrow().getStatus());
    assertEquals(RequestStatus.REJECTED, market.buyerRequests(buyer.getId()).get(0).getStatus());
    mvc.perform(get("/photos/" + camera.getId())).andExpect(status().isNotFound());
    mvc.perform(get("/profile").with(user(seller))).andExpect(status().isOk());
    mvc.perform(
            post("/profile")
                .with(user(seller))
                .with(csrf())
                .param("name", "Updated Student")
                .param("phone", "+91 9876543210")
                .param("department", "Computer Science")
                .param("hostel", "Hostel A")
                .param("id", buyer.getId().toString())
                .param("role", "ADMIN"))
        .andExpect(redirectedUrl("/profile"));
    assertEquals("Updated Student", users.findById(seller.getId()).orElseThrow().getName());
    assertEquals(Role.STUDENT, users.findById(seller.getId()).orElseThrow().getRole());
    assertEquals("Buyer", users.findById(buyer.getId()).orElseThrow().getName());
    mvc.perform(
            post("/profile")
                .with(user(seller))
                .with(csrf())
                .param("name", " ")
                .param("phone", "invalid phone"))
        .andExpect(status().isOk())
        .andExpect(model().attributeHasFieldErrors("form", "name", "phone"));
    Item activeListing =
        market.createItem(
            seller,
            category,
            "Active " + suffix,
            "For suspension test",
            BigDecimal.TEN,
            ItemCondition.GOOD);
    var login =
        mvc.perform(
                post("/login")
                    .with(csrf())
                    .param("username", seller.getEmail())
                    .param("password", "Password123!"))
            .andExpect(redirectedUrl("/"))
            .andReturn();
    var sellerSession =
        (org.springframework.mock.web.MockHttpSession) login.getRequest().getSession(false);
    assertNotNull(sellerSession);
    mvc.perform(
            post("/admin/users/" + seller.getId() + "/status")
                .with(user(buyer))
                .with(csrf())
                .param("enabled", "false"))
        .andExpect(status().isForbidden());
    mvc.perform(
            post("/admin/users/" + seller.getId() + "/status")
                .with(user("admin").roles("ADMIN"))
                .with(csrf())
                .param("enabled", "false"))
        .andExpect(redirectedUrl("/admin"));
    assertFalse(users.findById(seller.getId()).orElseThrow().isEnabled());
    mvc.perform(get("/profile").session(sellerSession))
        .andExpect(redirectedUrl("/login?suspended"));
    assertTrue(sellerSession.isInvalid());
    mvc.perform(
            post("/login")
                .with(csrf())
                .param("username", seller.getEmail())
                .param("password", "Password123!"))
        .andExpect(redirectedUrl("/login?error"));
    assertTrue(market.search("Active " + suffix, null).isEmpty());
    mvc.perform(get("/items/" + activeListing.getId())).andExpect(status().isNotFound());
    assertThrows(
        IllegalArgumentException.class,
        () -> market.requestItem(activeListing.getId(), buyer, "Unavailable seller"));
    mvc.perform(
            post("/admin/users/" + seller.getId() + "/status")
                .with(user("admin").roles("ADMIN"))
                .with(csrf())
                .param("enabled", "true"))
        .andExpect(redirectedUrl("/admin"));
    assertTrue(users.findById(seller.getId()).orElseThrow().isEnabled());
    assertEquals(1, market.search("Active " + suffix, null).size());
    mvc.perform(
            post("/login")
                .with(csrf())
                .param("username", seller.getEmail())
                .param("password", "Password123!"))
        .andExpect(redirectedUrl("/"));
    User protectedAdmin =
        users.save(
            new User(
                "Protected admin",
                "admin-" + suffix + "@thapar.edu",
                seller.getPassword(),
                Role.ADMIN));
    mvc.perform(
            post("/admin/users/" + protectedAdmin.getId() + "/status")
                .with(user("admin").roles("ADMIN"))
                .with(csrf())
                .param("enabled", "false"))
        .andExpect(flash().attributeExists("error"));
    assertTrue(users.findById(protectedAdmin.getId()).orElseThrow().isEnabled());
    mvc.perform(get("/admin").with(user("admin").roles("ADMIN"))).andExpect(status().isOk());
  }
}
