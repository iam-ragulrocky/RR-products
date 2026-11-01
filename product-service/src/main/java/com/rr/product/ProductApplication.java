package com.rr.product;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ProductApplication {
    public static void main(String[] args) { SpringApplication.run(ProductApplication.class, args); }

    @Bean
    CommandLineRunner seed(ProductRepo repo) {
        return a -> {
            if (repo.count() > 0) return;
            repo.save(p("Wireless headphones", "Bluetooth over-ear, 30 hour battery", 2499, 24, "Audio", "🎧"));
            repo.save(p("Smart watch", "Heart rate, steps and sleep tracking", 3999, 12, "Wearables", "⌚"));
            repo.save(p("Backpack", "Water resistant, fits a 15 inch laptop", 1299, 40, "Bags", "🎒"));
            repo.save(p("Desk lamp", "Dimmable LED with USB charging", 899, 18, "Home", "💡"));
            repo.save(p("Water bottle", "Steel, keeps drinks cold for 24 hours", 499, 60, "Home", "🧴"));
            repo.save(p("Sunglasses", "Polarised, UV400 protection", 1499, 30, "Fashion", "🕶️"));
        };
    }

    private static Product p(String n, String d, double price, int stock, String cat, String emoji) {
        Product x = new Product();
        x.name = n; x.description = d; x.price = price; x.stock = stock; x.category = cat; x.emoji = emoji;
        return x;
    }
}
