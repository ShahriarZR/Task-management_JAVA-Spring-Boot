package com.example.SuperShop_Enhanced.service;

import com.example.SuperShop_Enhanced.entity.Invoice;
import com.example.SuperShop_Enhanced.repository.InvoiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

import com.example.SuperShop_Enhanced.entity.Order;
import com.example.SuperShop_Enhanced.entity.Product;
import com.example.SuperShop_Enhanced.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class InvoiceService {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private OrderService orderService;

    public Invoice createInvoice(Invoice invoice) {
        return invoiceRepository.save(invoice);
    }

    public Optional<Invoice> getInvoiceById(int id) {
        return invoiceRepository.findById(id);
    }

    public List<Invoice> getInvoicesByOrderId(int orderId) {
        return invoiceRepository.findAll().stream()
                .filter(i -> {
                    String orderIds = i.getOrderIds();
                    if (orderIds == null || orderIds.isEmpty()) {
                        return false;
                    }
                    String[] orderIdArray = orderIds.split(",");
                    for (String idStr : orderIdArray) {
                        if (idStr.trim().equals(String.valueOf(orderId))) {
                            return true;
                        }
                    }
                    return false;
                })
                .toList();
    }

    public Map<String, Object> getDetailedInvoiceMap(int invoiceId) {
        Optional<Invoice> invoiceOpt = invoiceRepository.findById(invoiceId);
        if (invoiceOpt.isEmpty()) {
            return null;
        }
        Invoice invoice = invoiceOpt.get();

        Map<String, Object> result = new HashMap<>();
        result.put("id", invoice.getId());
        result.put("invoiceDate", invoice.getInvoiceDate());
        result.put("totalAmount", invoice.getTotalAmount());

        String orderIdsStr = invoice.getOrderIds();
        result.put("orderIds", orderIdsStr); 

        if (orderIdsStr == null || orderIdsStr.isEmpty()) {
            result.put("products", new ArrayList<>());
            return result;
        }
        String[] orderIdArray = orderIdsStr.split(",");

        List<Map<String, Object>> products = new ArrayList<>();
        for (String orderIdStr : orderIdArray) {
            int orderId;
            try {
                orderId = Integer.parseInt(orderIdStr.trim());
            } catch (NumberFormatException e) {
                continue;
            }
            Optional<Order> orderOpt = orderService.getOrderById(orderId);
            if (orderOpt.isPresent()) {
                Order order = orderOpt.get();
                Product product = order.getProduct();
                Map<String, Object> productMap = new HashMap<>();
                productMap.put("productId", product.getId());
                productMap.put("productName", product.getName());
                productMap.put("quantity", order.getQuantity());
                productMap.put("price", product.getPrice());
                productMap.put("totalPrice", order.getTotalPrice());
                products.add(productMap);
            }
        }
        result.put("products", products);
        return result;
    }

    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAll();
    }
}