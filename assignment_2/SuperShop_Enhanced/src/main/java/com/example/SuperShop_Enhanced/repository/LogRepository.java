package com.example.SuperShop_Enhanced.repository;

import com.example.SuperShop_Enhanced.entity.Log;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogRepository extends JpaRepository<Log, Integer> {
}
