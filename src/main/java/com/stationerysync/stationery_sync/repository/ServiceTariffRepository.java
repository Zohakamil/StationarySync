package com.stationerysync.stationery_sync.repository;

import com.stationerysync.stationery_sync.entity.ServiceTariff;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ServiceTariffRepository extends JpaRepository<ServiceTariff, Integer> {

    Optional<ServiceTariff> findByServiceTypeAndPaperSizeAndColorModeAndIsActiveTrue(
            String serviceType, String paperSize, String colorMode);
}