package com.example.SuperShop_Enhanced.api;
import com.example.SuperShop_Enhanced.entity.Invoice;
import com.example.SuperShop_Enhanced.service.InvoiceService;
import com.example.SuperShop_Enhanced.service.OrderService;
import com.example.SuperShop_Enhanced.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/invoices")
public class InvoiceApi {

    @Autowired
    private InvoiceService invoiceService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderService orderService;

    @GetMapping("/{id}")
    public ResponseEntity<java.util.Map<String, Object>> getInvoiceById(@PathVariable int id) {
        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Optional<com.example.SuperShop_Enhanced.entity.User> userOpt = userService.getUserByEmail(username);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).build();
        }
        int userId = userOpt.get().getId();

        java.util.Map<String, Object> detailedInvoice = invoiceService.getDetailedInvoiceMap(id);
        if (detailedInvoice == null) {
            return ResponseEntity.notFound().build();
        }

        Object orderIdsObj = detailedInvoice.get("products");
        if (orderIdsObj == null) {
            return ResponseEntity.status(403).body(null);
        }

        java.util.List<com.example.SuperShop_Enhanced.entity.Order> userOrders = orderService.getOrdersByUserId(userId);
        java.util.Set<Integer> userOrderIds = new java.util.HashSet<>();
        for (com.example.SuperShop_Enhanced.entity.Order order : userOrders) {
            userOrderIds.add(order.getId());
        }

        String orderIdsStr = (String) detailedInvoice.get("orderIds");
        if (orderIdsStr == null || orderIdsStr.isEmpty()) {
            return ResponseEntity.status(403).body(null);
        }
        String[] orderIdArray = orderIdsStr.split(",");

        for (String orderIdStr : orderIdArray) {
            int orderId;
            try {
                orderId = Integer.parseInt(orderIdStr.trim());
            } catch (NumberFormatException e) {
                return ResponseEntity.status(403).body(null);
            }
            if (!userOrderIds.contains(orderId)) {
                return ResponseEntity.status(403).body(null);
            }
        }

        java.util.Map<String, Object> responseMap = new java.util.HashMap<>(detailedInvoice);
        responseMap.put("user", userOpt.get());

        return ResponseEntity.ok(responseMap);
    }

    @GetMapping("/all")
    public ResponseEntity<List<java.util.Map<String, Object>>> getAllInvoices() {
        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Optional<com.example.SuperShop_Enhanced.entity.User> userOpt = userService.getUserByEmail(username);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).build();
        }
        int userId = userOpt.get().getId();

        List<Invoice> invoices = invoiceService.getAllInvoices();
        List<java.util.Map<String, Object>> detailedInvoices = new java.util.ArrayList<>();

        for (Invoice invoice : invoices) {
            java.util.Map<String, Object> detailedInvoice = invoiceService.getDetailedInvoiceMap(invoice.getId());
            if (detailedInvoice == null) {
                continue;
            }

            String orderIdsStr = (String) detailedInvoice.get("orderIds");
            if (orderIdsStr == null || orderIdsStr.isEmpty()) {
                continue;
            }
            String[] orderIdArray = orderIdsStr.split(",");

            java.util.List<com.example.SuperShop_Enhanced.entity.Order> userOrders = orderService.getOrdersByUserId(userId);
            java.util.Set<Integer> userOrderIds = new java.util.HashSet<>();
            for (com.example.SuperShop_Enhanced.entity.Order order : userOrders) {
                userOrderIds.add(order.getId());
            }

            boolean allOrdersBelongToUser = true;
            for (String orderIdStr : orderIdArray) {
                int orderId;
                try {
                    orderId = Integer.parseInt(orderIdStr.trim());
                } catch (NumberFormatException e) {
                    allOrdersBelongToUser = false;
                    break;
                }
                if (!userOrderIds.contains(orderId)) {
                    allOrdersBelongToUser = false;
                    break;
                }
            }
            if (!allOrdersBelongToUser) {
                continue;
            }

            detailedInvoice.put("user", userOpt.get());
            detailedInvoices.add(detailedInvoice);
        }

        return ResponseEntity.ok(detailedInvoices);
    }
}
