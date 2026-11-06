package com.campusexchange.repository;

import com.campusexchange.model.*;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseRequestRepository extends JpaRepository<PurchaseRequest, Long> {
  boolean existsByItemIdAndBuyerIdAndStatus(Long itemId, Long buyerId, RequestStatus status);

  List<PurchaseRequest> findByItemSellerIdOrderByRequestedAtDesc(Long sellerId);

  List<PurchaseRequest> findByBuyerIdOrderByRequestedAtDesc(Long buyerId);
}
