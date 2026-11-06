package com.campusexchange.service;

import com.campusexchange.model.*;
import com.campusexchange.repository.EmailNotificationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailNotifications {
  private final EmailNotificationRepository notifications;
  private final boolean enabled;

  public EmailNotifications(
      EmailNotificationRepository notifications,
      @Value("${app.notifications.enabled:false}") boolean enabled) {
    this.notifications = notifications;
    this.enabled = enabled;
  }

  // Called inside the listing transaction: rolled-back listings cannot produce an email.
  public void listingPublished(Item item) {
    if (!enabled) return;
    String body =
        "Hi "
            + item.getSeller().getName()
            + ",\n\nYour listing is now available on CampusExchange.\n\nItem: "
            + item.getTitle()
            + "\nCategory: "
            + item.getCategory().getName()
            + "\nPrice: INR "
            + item.getPrice().toPlainString()
            + "\nCondition: "
            + item.getCondition()
            + "\nListing ID: "
            + item.getId()
            + "\n\n"
            + "Open CampusExchange and go to My listings to manage it.\n\n"
            + "CampusExchange\n"
            + "This confirms publication only. Payment and handover are arranged offline.";
    notifications.save(
        new EmailNotification(
            item.getId(),
            item.getSeller().getEmail(),
            "Your CampusExchange listing is live",
            body));
  }
}
