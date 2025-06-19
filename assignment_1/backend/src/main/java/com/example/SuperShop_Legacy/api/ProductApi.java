package com.example.SuperShop_Legacy.api;
import com.example.SuperShop_Legacy.entity.Product;
import com.example.SuperShop_Legacy.repository.ProductRepository;
import com.example.SuperShop_Legacy.service.ProductService;
import com.example.SuperShop_Legacy.enums.ProductCategory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@CrossOrigin
public class ProductApi {

    private final ProductService productService;

    public ProductApi(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/all")
    public List<Product> getAllProducts() {
        return productService.getAllProducts();
    }

    @PostMapping("/add")
    public void addProduct(@RequestBody Product product) {
        productService.addProduct(product);
    }

    @PostMapping("/bulk")
    public void addProducts(@RequestBody List<Product> products) {
        productService.addProducts(products);
    }

    @PutMapping("/update")
    public void updateProduct(@RequestBody Product product) {
        productService.updateProduct(product);
    }

    @GetMapping("/expiring-soon")
    public List<Product> getExpiringSoonProducts() {
        return productService.getExpiringSoonProducts();
    }

    @GetMapping("/discounted")
    public List<Product> getDiscountedExpiringProducts(@RequestParam double discount) {
        return productService.getDiscountedExpiringProducts(discount);
    }

    @GetMapping("/total-by-category")
    public List<ProductRepository.CategoryTotal> getTotalPriceByCategory() {
        return productService.getTotalPriceByCategory();
    }

    @GetMapping("/by-category-with-discounts")
    public List<Product> getProductsByCategoryWithDiscount(
            @RequestParam("category") String categoryStr) {
        ProductCategory category;
        try {
            category = ProductCategory.valueOf(categoryStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid category: " + categoryStr);
        }
        return productService.getProductsByCategoryWithDiscount(category);
    }
}
