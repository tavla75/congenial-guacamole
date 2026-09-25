package org.example.resturang_kassa;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
public class ProductDataInitializer {

    @Bean
    CommandLineRunner seedProducts(ProductRepository products) {
        return args -> {
            if (products.count() == 0) {
                products.saveAll(java.util.List.of(
                        new Product("Hamburgare", new BigDecimal("99.00"), "Mat"),
                        new Product("Pizza", new BigDecimal("119.00"), "Mat"),
                        new Product("Sallad", new BigDecimal("79.00"), "Mat"),
                        new Product("Dryck", new BigDecimal("25.00"), "Dryck")
                ));
            }
        };
    }
}
