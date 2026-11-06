package com.campusexchange.config;

import com.campusexchange.model.Category;
import com.campusexchange.repository.CategoryRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.*;

@Configuration
public class DataInitializer {
  @Bean
  CommandLineRunner seedCategories(CategoryRepository categories) {
    return args -> {
      if (categories.count() == 0)
        categories.saveAll(
            java.util.List.of(
                new Category("Books"),
                new Category("Electronics"),
                new Category("Furniture"),
                new Category("Academic supplies"),
                new Category("Clothing"),
                new Category("Other")));
    };
  }
}
