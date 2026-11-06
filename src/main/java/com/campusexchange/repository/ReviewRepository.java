package com.campusexchange.repository;

import com.campusexchange.model.Review;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
  boolean existsByTradeId(Long tradeId);

  List<Review> findByTradeIdIn(Collection<Long> ids);

  List<Review> findByTradeSellerIdOrderByCreatedAtDesc(Long sellerId);
}
