package com.campusexchange.config;

import com.campusexchange.service.EmailDelivery;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.*;

@Configuration
@EnableScheduling
public class EmailWorker {
  private final EmailDelivery delivery;

  public EmailWorker(EmailDelivery delivery) {
    this.delivery = delivery;
  }

  @Scheduled(
      initialDelayString = "${app.notifications.initial-delay-ms:15000}",
      fixedDelayString = "${app.notifications.poll-ms:10000}")
  public void tick() {
    delivery.sendNext();
  }
}
