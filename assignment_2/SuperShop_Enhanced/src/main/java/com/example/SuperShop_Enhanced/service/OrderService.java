package com.example.SuperShop_Enhanced.service;

import com.example.SuperShop_Enhanced.entity.Order;
import com.example.SuperShop_Enhanced.entity.Product;
import com.example.SuperShop_Enhanced.entity.User;
import com.example.SuperShop_Enhanced.enums.OrderStatus;
import com.example.SuperShop_Enhanced.repository.OrderRepository;
import com.example.SuperShop_Enhanced.repository.ProductRepository;
import com.example.SuperShop_Enhanced.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private ProductRepository productRepository;

    public Order createOrder(int userId, int productId) {
        Optional<User> userOpt = userService.getUserById(userId);
        Product product = productRepository.getProductById(productId);

        if (userOpt.isEmpty() || product == null) {
            return null;
        }

        Order order = new Order();
        order.setUser(userOpt.get());
        order.setProduct(product);
        order.setQuantity(1);
        if(product.getDiscountedPrice() != null) {
            double totalPrice = order.getQuantity() * product.getDiscountedPrice();
            order.setTotalPrice(totalPrice);
        } else {
            double totalPrice = order.getQuantity() * product.getPrice();
            order.setTotalPrice(totalPrice);
        }
        order.setStatus(OrderStatus.CONFIRMATION);
        order.setOrderDate(LocalDateTime.now());

        return orderRepository.save(order);
    }

    public Order updateOrderStatus(int orderId, OrderStatus status) {
        Optional<Order> existingOrderOpt = orderRepository.findById(orderId);
        if (existingOrderOpt.isPresent()) {
            Order existingOrder = existingOrderOpt.get();
            existingOrder.setStatus(status);
            return orderRepository.save(existingOrder);
        } else {
            return null;
        }
    }

    public List<Order> getOrdersByUserId(int userId) {
        return orderRepository.findAll().stream()
                .filter(o -> o.getUser().getId() == userId)
                .toList();
    }

    public Optional<Order> getOrderById(int orderId) {
        return orderRepository.findById(orderId);
    }

    public List<Order> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findAll().stream()
                .filter(o -> o.getStatus() == status)
                .toList();
    }

    public void updateOrdersStatusByUserId(int userId, OrderStatus status) {
        List<Order> userOrders = getOrdersByUserId(userId);
        for (Order order : userOrders) {
            order.setStatus(status);
            orderRepository.save(order);
        }
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Map<String, Object> generateMonthlySalesReport(int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime startDate = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endDate = yearMonth.atEndOfMonth().atTime(23, 59, 59);

        List<Order> ordersInMonth = orderRepository.findAll().stream()
                .filter(o -> !o.getOrderDate().isBefore(startDate) && !o.getOrderDate().isAfter(endDate))
                .toList();

        Map<String, Double> salesPerCategory = new HashMap<>();
        for (Order order : ordersInMonth) {
            String category = order.getProduct().getCategory().name();
            salesPerCategory.put(category, salesPerCategory.getOrDefault(category, 0.0) + order.getTotalPrice());
        }

        double totalRevenue = ordersInMonth.stream()
                .mapToDouble(Order::getTotalPrice)
                .sum();

        int numberOfOrders = ordersInMonth.size();

        Map<Integer, Integer> productQuantityMap = new HashMap<>();
        Map<Integer, String> productNameMap = new HashMap<>();
        for (Order order : ordersInMonth) {
            int productId = order.getProduct().getId();
            String productName = order.getProduct().getName();
            productQuantityMap.put(productId, productQuantityMap.getOrDefault(productId, 0) + order.getQuantity());
            productNameMap.put(productId, productName);
        }

        List<Map<String, Object>> bestSellingProducts = productQuantityMap.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .map(e -> {
                    Map<String, Object> productInfo = new HashMap<>();
                    productInfo.put("productId", e.getKey());
                    productInfo.put("productName", productNameMap.get(e.getKey()));
                    productInfo.put("quantitySold", e.getValue());
                    return productInfo;
                })
                .toList();

        Map<String, Object> report = new HashMap<>();
        report.put("totalSalesPerCategory", salesPerCategory);
        report.put("totalRevenue", totalRevenue);
        report.put("numberOfOrders", numberOfOrders);
        report.put("bestSellingProducts", bestSellingProducts);

        return report;
    }
}
