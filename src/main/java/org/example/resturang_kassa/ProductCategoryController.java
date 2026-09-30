package org.example.resturang_kassa;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashSet;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class ProductCategoryController {

    private final ProductCategoryRepository categories;
    private final ProductRepository products;

    public ProductCategoryController(
            ProductCategoryRepository categories, ProductRepository products) {
        this.categories = categories;
        this.products = products;
    }

    @GetMapping
    public List<String> all() {
        LinkedHashSet<String> names = new LinkedHashSet<>(List.of("Mat", "Dryck"));
        categories.findAll().forEach(category -> names.add(category.getName()));
        products.findAll().forEach(product -> names.add(product.getCategory()));
        return List.copyOf(names);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductCategory create(@RequestBody CreateCategoryRequest request) {
        if (request.name() == null || request.name().isBlank()) {
            throw new IllegalArgumentException("Ange ett namn på fliken.");
        }
        String name = request.name().trim();
        return categories.findByNameIgnoreCase(name)
                .orElseGet(() -> categories.save(new ProductCategory(name)));
    }

    public record CreateCategoryRequest(String name) {
    }
}
