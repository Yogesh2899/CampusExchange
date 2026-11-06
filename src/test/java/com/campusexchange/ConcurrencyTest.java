package com.campusexchange;

import static org.junit.jupiter.api.Assertions.*;

import com.campusexchange.model.*;
import com.campusexchange.repository.*;
import com.campusexchange.service.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ConcurrencyTest {
  @Autowired AccountService accounts;
  @Autowired MarketplaceService market;
  @Autowired ReviewService reviews;
  @Autowired ItemRepository items;
  @Autowired TradeRepository trades;
  @Autowired ReviewRepository reviewRepository;

  private int race(Runnable first, Runnable second) throws Exception {
    ExecutorService pool = Executors.newFixedThreadPool(2);
    CountDownLatch start = new CountDownLatch(1);
    try {
      List<Future<Boolean>> results = new ArrayList<>();
      for (Runnable action : List.of(first, second))
        results.add(
            pool.submit(
                () -> {
                  start.await();
                  try {
                    action.run();
                    return true;
                  } catch (IllegalArgumentException expected) {
                    return false;
                  }
                }));
      start.countDown();
      int successes = 0;
      for (var result : results) if (result.get(20, TimeUnit.SECONDS)) successes++;
      return successes;
    } finally {
      pool.shutdownNow();
    }
  }

  @org.junit.jupiter.api.RepeatedTest(5)
  void concurrentRequestsSalesAndReviews() throws Exception {
    String key = UUID.randomUUID().toString();
    User seller = accounts.register("Race seller", "seller-" + key + "@thapar.edu", "Password123!");
    User a = accounts.register("Buyer A", "a-" + key + "@thapar.edu", "Password123!");
    User b = accounts.register("Buyer B", "b-" + key + "@thapar.edu", "Password123!");
    Item item =
        market.createItem(
            seller,
            market.categories().get(0).getId(),
            "Concurrent sale",
            "Single physical item",
            BigDecimal.TEN,
            ItemCondition.GOOD);
    assertEquals(
        1,
        race(
            () -> market.requestItem(item.getId(), a, "First"),
            () -> market.requestItem(item.getId(), a, "Duplicate")));
    market.requestItem(item.getId(), b, "Second buyer");
    Long aId = market.buyerRequests(a.getId()).get(0).getId(),
        bId = market.buyerRequests(b.getId()).get(0).getId();
    assertEquals(1, race(() -> market.accept(aId, seller), () -> market.accept(bId, seller)));
    assertEquals(ItemStatus.SOLD, items.findById(item.getId()).orElseThrow().getStatus());
    List<Trade> sales =
        trades.findAll().stream().filter(t -> t.getItem().getId().equals(item.getId())).toList();
    assertEquals(1, sales.size());
    long accepted =
        market.sellerRequests(seller.getId()).stream()
            .filter(r -> r.getStatus() == RequestStatus.ACCEPTED)
            .count();
    assertEquals(1, accepted);
    long rejected =
        market.sellerRequests(seller.getId()).stream()
            .filter(r -> r.getStatus() == RequestStatus.REJECTED)
            .count();
    assertEquals(1, rejected);
    Trade trade = sales.get(0);
    assertEquals(
        1,
        race(
            () -> reviews.submit(trade.getId(), trade.getBuyer(), 5, "First review"),
            () -> reviews.submit(trade.getId(), trade.getBuyer(), 4, "Duplicate review")));
    assertEquals(1, reviewRepository.findByTradeIdIn(List.of(trade.getId())).size());
  }
}
