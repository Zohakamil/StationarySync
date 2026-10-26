package com.stationerysync.stationery_sync.repository;

import com.stationerysync.stationery_sync.entity.SalesOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, Integer> {

    List<SalesOrder> findByOrderTimestampBetween(LocalDateTime from, LocalDateTime to);

    List<SalesOrder> findByOrderTimestampBetweenOrderByOrderTimestampDesc(LocalDateTime from, LocalDateTime to);
}