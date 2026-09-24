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
                        new Product("Hamburgare", new BigDecimal("99.00")),
                        new Product("Pizza", new BigDecimal("119.00")),
                        new Product("Sallad", new BigDecimal("79.00")),
                        new Product("Dryck", new BigDecimal("25.00"))
                ));
            }
        };
    }
}
