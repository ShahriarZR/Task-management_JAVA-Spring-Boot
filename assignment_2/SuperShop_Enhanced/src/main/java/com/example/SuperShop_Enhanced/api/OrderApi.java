package com.example.SuperShop_Enhanced.api;

import com.example.SuperShop_Enhanced.entity.Order;
import com.example.SuperShop_Enhanced.entity.User;
import com.example.SuperShop_Enhanced.enums.OrderStatus;
import com.example.SuperShop_Enhanced.service.OrderService;
import com.example.SuperShop_Enhanced.service.UserService;
import com.example.SuperShop_Enhanced.service.InvoiceService;
import com.example.SuperShop_Enhanced.entity.Invoice;
import com.example.SuperShop_Enhanced.entity.Order;
import com.example.SuperShop_Enhanced.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/orders")
public class OrderApi {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @Autowired
    private InvoiceService invoiceService;

    
    @PutMapping("/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Order> updateOrderStatus(@PathVariable int id, @RequestParam OrderStatus status) {
        Order updatedOrder = orderService.updateOrderStatus(id, status);
        if (updatedOrder == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updatedOrder);
    }

    @GetMapping("/user")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<List<Order>> getOrdersByUserId() {
        String userEmail = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> userOpt = userService.getUserByEmail(userEmail);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).build();
        }
        User user = userOpt.get();
        List<Order> orders = orderService.getOrdersByUserId(user.getId());
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Order>> getAllOrders() {
        List<Order> orders = orderService.getAllOrders();
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/admin/monthly-report")
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getMonthlySalesReport(@RequestParam int year, @RequestParam int month) {
        Map<String, Object> report = orderService.generateMonthlySalesReport(year, month);
        return ResponseEntity.ok(report);
    }

    @PostMapping("/confirm")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<?> confirmOrder() {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> userOpt = userService.getUserByEmail(userEmail);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body("User not found");
        }
        User user = userOpt.get();
        List<Order> orders = orderService.getOrdersByUserId(user.getId());
        if (orders.isEmpty()) {
            return ResponseEntity.badRequest().body("No orders found for user");
        }
        List<Order> confirmationOrders = orders.stream()
                .filter(order -> order.getStatus() == OrderStatus.CONFIRMATION)
                .toList();
        if (confirmationOrders.isEmpty()) {
            return ResponseEntity.badRequest().body("No orders with status CONFIRMATION found for user");
        }
        double totalAmount = confirmationOrders.stream().mapToDouble(Order::getTotalPrice).sum();

        Invoice invoice = new Invoice();
        invoice.setTotalAmount(totalAmount);
        invoice.setInvoiceDate(LocalDateTime.now());

        String orderIds = confirmationOrders.stream()
                .map(order -> String.valueOf(order.getId()))
                .reduce((id1, id2) -> id1 + "," + id2)
                .orElse("");
        invoice.setOrderIds(orderIds);

        Invoice createdInvoice = invoiceService.createInvoice(invoice);

        orderService.updateOrdersStatusByUserId(user.getId(), OrderStatus.PENDING);

        return ResponseEntity.ok(createdInvoice);
    }
}
