package com.campusexchange.service;

import com.campusexchange.model.*;
import com.campusexchange.repository.*;
import java.io.*;
import java.util.*;
import javax.imageio.ImageIO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class PhotoService {
  private final ItemRepository items;
  private final ItemPhotoRepository photos;

  public PhotoService(ItemRepository items, ItemPhotoRepository photos) {
    this.items = items;
    this.photos = photos;
  }

  @Transactional
  public void upload(Long id, User owner, MultipartFile file) {
    if (file == null || file.isEmpty() || file.getSize() > 3 * 1024 * 1024)
      throw new IllegalArgumentException("Choose a JPEG or PNG image up to 3 MB.");
    byte[] canonical;
    try (var input = ImageIO.createImageInputStream(file.getInputStream())) {
      var readers = ImageIO.getImageReaders(input);
      if (!readers.hasNext())
        throw new IllegalArgumentException("This file is not a supported image.");
      var reader = readers.next();
      try {
        reader.setInput(input);
        String format = reader.getFormatName();
        if (!format.equalsIgnoreCase("png") && !format.equalsIgnoreCase("jpeg"))
          throw new IllegalArgumentException("Only JPEG and PNG images are supported.");
        int w = reader.getWidth(0), h = reader.getHeight(0);
        if (w < 1 || h < 1 || w > 4000 || h > 4000 || (long) w * h > 12000000)
          throw new IllegalArgumentException(
              "Use an image up to 4000 pixels per side and 12 megapixels.");
        var output = new ByteArrayOutputStream();
        ImageIO.write(reader.read(0), "png", output);
        canonical = output.toByteArray();
        if (canonical.length > 8 * 1024 * 1024)
          throw new IllegalArgumentException(
              "This image is too large after processing. Please resize it.");
      } finally {
        reader.dispose();
      }
    } catch (IOException ex) {
      throw new IllegalArgumentException("The image could not be read. Try another JPEG or PNG.");
    }
    Item item =
        items
            .findByIdForUpdate(id)
            .orElseThrow(() -> new IllegalArgumentException("Listing not found."));
    if (!item.getSeller().getId().equals(owner.getId()))
      throw new IllegalArgumentException("Only the seller can change the photo.");
    if (item.getStatus() != ItemStatus.AVAILABLE)
      throw new IllegalArgumentException("Only available listings can have their photo changed.");
    var existing = photos.findByItemId(id);
    if (existing.isPresent()) existing.get().replace(canonical);
    else photos.save(new ItemPhoto(item, canonical));
  }

  public Optional<ItemPhoto> visible(Long id) {
    return photos
        .findByItemId(id)
        .filter(
            p ->
                p.getItem().getSeller().isEnabled()
                    && p.getItem().getStatus() != ItemStatus.WITHDRAWN);
  }

  public Set<Long> visibleIds() {
    return new HashSet<>(photos.visibleItemIds());
  }
}
