package com.example.SuperShop_Enhanced.repository;
import com.example.SuperShop_Enhanced.entity.Product;
import com.example.SuperShop_Enhanced.enums.ProductCategory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import javax.sql.DataSource;
import java.util.List;

@Repository
public class ProductRepository {

    private static final String INSERT_ONE = "INSERT INTO products (name, price, quantity, category, expire_date, availability, discounted_price) VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String UPDATE_PRODUCT = "UPDATE products SET name=?, price=?, quantity=?, category=?, expire_date=?, availability=?, discounted_price=? WHERE id=?";
    private static final String GET_EXPIRING_IN_7_DAYS = "SELECT id, name, price, quantity, category, expire_date, availability, discounted_price FROM products WHERE expire_date BETWEEN CURRENT_DATE AND CURRENT_DATE + INTERVAL '7 days'";
    private static final String GET_DISCOUNTED_PRODUCTS = "SELECT id, name, price, quantity, category, expire_date, availability, discounted_price FROM products WHERE expire_date BETWEEN CURRENT_DATE AND CURRENT_DATE + INTERVAL '7 days'";
    private static final String GET_TOTAL_BY_CATEGORY = "SELECT category, SUM(price * quantity) AS total_price FROM products GROUP BY category";
    private static final String GET_ALL_PRODUCTS = "SELECT * FROM products";
    private static final String GET_PRODUCTS_BY_CATEGORY = "SELECT * FROM products WHERE category = ?";
    private static final String GET_PRODUCT_BY_ID = "SELECT * FROM products WHERE id = ?";

    public List<Product> showAllProducts() {
        return jdbcTemplate.query(GET_ALL_PRODUCTS, new BeanPropertyRowMapper<>(Product.class));
    }

    private final JdbcTemplate jdbcTemplate;

    public ProductRepository(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void addProduct(Product product) {
        boolean availability = product.getExpireDate() != null && product.getExpireDate().isAfter(java.time.LocalDate.now());
        jdbcTemplate.update(INSERT_ONE,
                product.getName(),
                product.getPrice(),
                product.getQuantity(),
                product.getCategory().toString(),
                product.getExpireDate() != null ? java.sql.Date.valueOf(product.getExpireDate()) : null,
                availability,
                product.getDiscountedPrice()
        );
    }

    public void addProducts(List<Product> products) {
        for (Product product : products) {
            addProduct(product);
        }
    }

    public Product getProductById(int id) {
        List<Product> products = jdbcTemplate.query(GET_PRODUCT_BY_ID, new Object[]{id}, new BeanPropertyRowMapper<>(Product.class));
        if (products.isEmpty()) {
            return null;
        }
        return products.get(0);
    }

    public void updateProduct(Product product) {
        Product existingProduct = getProductById(product.getId());
        if (existingProduct == null) {
            return;
        }

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
        }
        if (product.getDiscountedPrice() != null) {
            existingProduct.setDiscountedPrice(product.getDiscountedPrice());
        }

        boolean availability = existingProduct.getExpireDate() != null && existingProduct.isAvailability();

        jdbcTemplate.update(UPDATE_PRODUCT,
                existingProduct.getName(),
                existingProduct.getPrice(),
                existingProduct.getQuantity(),
                existingProduct.getCategory().toString(),
                existingProduct.getExpireDate() != null ? java.sql.Date.valueOf(existingProduct.getExpireDate()) : null,
                availability,
                existingProduct.getDiscountedPrice(),
                existingProduct.getId()
        );
    }

    public List<Product> getExpiringSoonProducts() {
        return jdbcTemplate.query(GET_EXPIRING_IN_7_DAYS, new BeanPropertyRowMapper<>(Product.class));
    }

    public List<Product> getDiscountedExpiringProducts(double discountPercentage) {
        return jdbcTemplate.query(GET_DISCOUNTED_PRODUCTS,
                new BeanPropertyRowMapper<>(Product.class));
    }

    public List<Product> getProductsByCategoryWithDiscount(ProductCategory category) {
        return jdbcTemplate.query(GET_PRODUCTS_BY_CATEGORY,
                new Object[]{category.toString()},
                new BeanPropertyRowMapper<>(Product.class));
    }

    public List<CategoryTotal> getTotalPriceByCategory() {
        return jdbcTemplate.query(GET_TOTAL_BY_CATEGORY,
                (rs, rowNum) -> new CategoryTotal(ProductCategory.valueOf(rs.getString("category")), rs.getDouble("total_price")));
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
