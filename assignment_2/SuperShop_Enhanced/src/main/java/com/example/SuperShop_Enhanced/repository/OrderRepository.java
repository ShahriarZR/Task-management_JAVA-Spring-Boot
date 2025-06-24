package com.example.SuperShop_Enhanced.repository;

import com.example.SuperShop_Enhanced.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
}
