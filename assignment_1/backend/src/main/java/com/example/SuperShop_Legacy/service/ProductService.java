package com.example.SuperShop_Legacy.service;
import com.example.SuperShop_Legacy.entity.Product;
import com.example.SuperShop_Legacy.repository.ProductRepository;
import com.example.SuperShop_Legacy.enums.ProductCategory;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

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
        return productRepository.getDiscountedExpiringProducts(discountPercentage);
    }

    public List<ProductRepository.CategoryTotal> getTotalPriceByCategory() {
        return productRepository.getTotalPriceByCategory();
    }

    public List<Product> getProductsByCategoryWithDiscount(ProductCategory category) {
        List<Product> productsByCategory = productRepository.getProductsByCategory(category);
            return productsByCategory;
    }
}
