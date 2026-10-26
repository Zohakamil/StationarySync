package com.stationerysync.stationery_sync.repository;

import com.stationerysync.stationery_sync.entity.DiscountRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

public interface DiscountRuleRepository extends JpaRepository<DiscountRule, Integer> {

    @Query("""
        SELECT COALESCE(MAX(d.discountPct), 0)
        FROM DiscountRule d
        WHERE d.serviceType = :serviceType
          AND d.isActive = true
          AND :quantity > d.minQuantity
    """)
    BigDecimal findApplicableDiscount(@Param("serviceType") String serviceType,
                                      @Param("quantity") int quantity);
}