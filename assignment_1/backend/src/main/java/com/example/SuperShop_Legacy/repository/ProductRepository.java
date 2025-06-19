package com.example.SuperShop_Legacy.repository;
import com.example.SuperShop_Legacy.entity.Product;
import com.example.SuperShop_Legacy.enums.ProductCategory;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class ProductRepository {

    private static final List<Product> products = new ArrayList<>();

    static {
        products.add(new Product(1, "Shampoo", 10.99, 50, ProductCategory.BEAUTY_CARE, LocalDate.now().plusDays(30)));
        products.add(new Product(2, "Carrot", 2.99, 100, ProductCategory.VEGETABLES, LocalDate.now().plusDays(7)));
        products.add(new Product(3, "Chicken Breast", 7.99, 20, ProductCategory.MEAT, LocalDate.now().plusDays(5)));
        products.add(new Product(4, "Rice", 1.99, 200, ProductCategory.GROCERIES, LocalDate.now().plusDays(365)));
    }

    private void updateAvailability(Product product) {
        if (product.getExpireDate() != null) {
            product.setAvailability(product.getExpireDate().isAfter(LocalDate.now()));
        } else {
            product.setAvailability(false);
        }
    }

    private void updateAvailabilityForAll() {
        for (Product product : products) {
            updateAvailability(product);
        }
    }

    public List<Product> showAllProducts() {
        updateAvailabilityForAll();
        return new ArrayList<>(products);
    }

    public void addProduct(Product product) {
        updateAvailability(product);
        products.add(product);
    }

    public void addProducts(List<Product> productsToAdd) {
        for (Product product : productsToAdd) {
            updateAvailability(product);
        }
        products.addAll(productsToAdd);
    }

    public void updateProduct(Product product) {
        for (int i = 0; i < products.size(); i++) {
            Product existingProduct = products.get(i);
            if (existingProduct.getId() == product.getId()) {
                if (product.getName() != null) {
                    existingProduct.setName(product.getName());
                }
                if (product.getPrice() > 0) {
                    existingProduct.setPrice(product.getPrice());
                }
                if (product.getQuantity() > 0) {
                    existingProduct.setQuantity(product.getQuantity());
                }
                if (product.getCategory() != null) {
                    existingProduct.setCategory(product.getCategory());
                }
                if (product.getExpireDate() != null) {
                    existingProduct.setExpireDate(product.getExpireDate());
                    updateAvailability(existingProduct);
                }
                products.set(i, existingProduct);
                break;
            }
        }
        //return new ArrayList<>(products);
    }

    public List<Product> getExpiringSoonProducts() {
        LocalDate now = LocalDate.now();
        LocalDate in7Days = now.plusDays(7);
        return products.stream()
                .filter(p -> !p.getExpireDate().isBefore(now) && !p.getExpireDate().isAfter(in7Days))
                .collect(Collectors.toList());
    }

    public List<Product> getDiscountedExpiringProducts(double discountPercentage) {
        LocalDate now = LocalDate.now();
        LocalDate in7Days = now.plusDays(7);
        List<Product> discountedProducts = new ArrayList<>();
        for (Product p : products) {
            if (!p.getExpireDate().isBefore(now) && !p.getExpireDate().isAfter(in7Days)) {
                double discountedPrice = p.getPrice() * (1 - discountPercentage / 100);
                p.setDiscountedPrice(discountedPrice);
                discountedProducts.add(p);
            }
        }
        return discountedProducts;
    }

    public List<Product> getProductsByCategory(ProductCategory category) {
        return products.stream()
                .filter(p -> p.getCategory() == category)
                .collect(Collectors.toList());
    }

    public List<CategoryTotal> getTotalPriceByCategory() {
        return products.stream()
                .collect(Collectors.groupingBy(Product::getCategory,
                        Collectors.summingDouble(p -> p.getPrice() * p.getQuantity())))
                .entrySet().stream()
                .map(e -> new CategoryTotal(e.getKey(), e.getValue()))
                .collect(Collectors.toList());
    }

    public static class CategoryTotal {
        private ProductCategory category;
        private double totalPrice;

        public CategoryTotal(ProductCategory category, double totalPrice) {
            this.category = category;
            this.totalPrice = totalPrice;
        }

        public ProductCategory getCategory() {
            return category;
        }

        public double getTotalPrice() {
            return totalPrice;
        }
    }
}
