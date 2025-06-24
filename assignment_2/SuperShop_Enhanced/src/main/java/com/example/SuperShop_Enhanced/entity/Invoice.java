package com.example.SuperShop_Enhanced.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "invoices")
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private LocalDateTime invoiceDate;

    @Column(nullable = false)
    private double totalAmount;

    @Column(name = "order_ids")
    private String orderIds;

    public Invoice() {}

    public Invoice(int id, LocalDateTime invoiceDate, double totalAmount, String orderIds) {
        this.id = id;
        this.invoiceDate = invoiceDate;
        this.totalAmount = totalAmount;
        this.orderIds = orderIds;
    }

    public int getId() {
        return id;
    }

    public LocalDateTime getInvoiceDate() {
        return invoiceDate;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public String getOrderIds() {
        return orderIds;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setInvoiceDate(LocalDateTime invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public void setOrderIds(String orderIds) {
        this.orderIds = orderIds;
    }
}
