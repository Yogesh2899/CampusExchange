package com.campusexchange.service;

import com.campusexchange.model.*;
import com.campusexchange.repository.*;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReviewService {
  private final TradeRepository trades;
  private final ReviewRepository reviews;

  public ReviewService(TradeRepository trades, ReviewRepository reviews) {
    this.trades = trades;
    this.reviews = reviews;
  }

  @Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
  public void submit(Long tradeId, User buyer, int rating, String comment) {
    if (rating < 1 || rating > 5)
      throw new IllegalArgumentException("Choose a rating from 1 to 5.");
    if (comment == null) comment = "";
    if (comment.length() > 500)
      throw new IllegalArgumentException("Keep your review within 500 characters.");
    Trade trade =
        trades
            .findForUpdate(tradeId)
            .orElseThrow(() -> new IllegalArgumentException("Trade not found."));
    if (!trade.getBuyer().getId().equals(buyer.getId()))
      throw new IllegalArgumentException("Only the buyer in this trade can review its seller.");
    if (reviews.existsByTradeId(tradeId))
      throw new IllegalArgumentException("You already reviewed this trade.");
    reviews.save(new Review(trade, rating, comment.trim()));
  }

  public Map<Long, Review> forTrades(List<Trade> history) {
    if (history.isEmpty()) return Map.of();
    return reviews.findByTradeIdIn(history.stream().map(Trade::getId).toList()).stream()
        .collect(Collectors.toMap(r -> r.getTrade().getId(), r -> r));
  }

  public List<Review> forSeller(Long id) {
    return reviews.findByTradeSellerIdOrderByCreatedAtDesc(id);
  }

  public String average(List<Review> list) {
    return list.isEmpty()
        ? "No ratings yet"
        : String.format(
            Locale.ROOT, "%.1f / 5", list.stream().mapToInt(Review::getRating).average().orElse(0));
  }
}
