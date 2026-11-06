package com.campusexchange.repository;

import com.campusexchange.model.*;
import jakarta.persistence.LockModeType;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface ItemRepository extends JpaRepository<Item, Long> {
  boolean existsByCategoryId(Long categoryId);

  List<Item> findBySellerIdOrderByListedAtDesc(Long sellerId);

  @Query(
      "select i from Item i join fetch i.seller join fetch i.category where i.seller.enabled=true"
          + " and i.status=:status and (:q is null or lower(i.title) like lower(concat('%',:q,'%'))"
          + " or lower(i.description) like lower(concat('%',:q,'%'))) and (:categoryId is null or"
          + " i.category.id=:categoryId) and (:minPrice is null or i.price>=:minPrice) and"
          + " (:maxPrice is null or i.price<=:maxPrice) and (:condition is null or"
          + " i.condition=:condition) order by i.listedAt desc")
  List<Item> search(
      @Param("status") ItemStatus status,
      @Param("q") String q,
      @Param("categoryId") Long categoryId,
      @Param("minPrice") java.math.BigDecimal minPrice,
      @Param("maxPrice") java.math.BigDecimal maxPrice,
      @Param("condition") ItemCondition condition);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select i from Item i join fetch i.seller where i.id=:id")
  Optional<Item> findByIdForUpdate(@Param("id") Long id);
}
