package com.example.SuperShop_Enhanced.service;
import com.example.SuperShop_Enhanced.entity.Product;
import com.example.SuperShop_Enhanced.enums.ProductCategory;
import com.example.SuperShop_Enhanced.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> getAllProducts() {
        return productRepository.showAllProducts();
    }

    public void addProduct(Product product) {
        productRepository.addProduct(product);
    }

    public void addProducts(List<Product> products) {
        productRepository.addProducts(products);
    }

    public void updateProduct(Product product) {
        productRepository.updateProduct(product);
    }

    public List<Product> getExpiringSoonProducts() {
        return productRepository.getExpiringSoonProducts();
    }

    public List<Product> getDiscountedExpiringProducts(double discountPercentage) {
        List<Product> products = productRepository.getDiscountedExpiringProducts(discountPercentage);
        for (Product product : products) {
            double discountedPrice = product.getPrice() * (1 - discountPercentage / 100);
            product.setDiscountedPrice(discountedPrice);
            updateProduct(product);
        }
        return products;
    }

    public List<ProductRepository.CategoryTotal> getTotalPriceByCategory() {
        return productRepository.getTotalPriceByCategory();
    }

    public List<Product> getProductsByCategoryWithDiscount(ProductCategory category) {
        List<Product> productsByCategory = productRepository.getProductsByCategoryWithDiscount(category);
        return productsByCategory;
    }
}
