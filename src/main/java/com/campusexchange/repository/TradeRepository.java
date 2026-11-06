package com.campusexchange.repository;

import com.campusexchange.model.Trade;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TradeRepository extends JpaRepository<Trade, Long> {
  List<Trade> findByBuyerIdOrSellerIdOrderByCompletedAtDesc(Long buyerId, Long sellerId);

  @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
  @org.springframework.data.jpa.repository.Query("select t from Trade t where t.id=:id")
  java.util.Optional<Trade> findForUpdate(
      @org.springframework.data.repository.query.Param("id") Long id);
}
