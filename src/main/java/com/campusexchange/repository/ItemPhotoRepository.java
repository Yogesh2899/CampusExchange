package com.campusexchange.repository;

import com.campusexchange.model.ItemPhoto;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ItemPhotoRepository extends JpaRepository<ItemPhoto, Long> {
  Optional<ItemPhoto> findByItemId(Long itemId);

  @Query(
      "select p.item.id from ItemPhoto p where p.item.seller.enabled=true and"
          + " p.item.status<>com.campusexchange.model.ItemStatus.WITHDRAWN")
  List<Long> visibleItemIds();
}
