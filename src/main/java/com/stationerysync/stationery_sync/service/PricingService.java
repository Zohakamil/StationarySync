package com.stationerysync.stationery_sync.service;

import com.stationerysync.stationery_sync.entity.ServiceTariff;
import com.stationerysync.stationery_sync.exception.NoActiveTariffException;
import com.stationerysync.stationery_sync.repository.DiscountRuleRepository;
import com.stationerysync.stationery_sync.repository.ServiceTariffRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class PricingService {

    private final ServiceTariffRepository tariffRepo;
    private final DiscountRuleRepository discountRepo;

    public PricingService(ServiceTariffRepository tariffRepo,
                          DiscountRuleRepository discountRepo) {
        this.tariffRepo = tariffRepo;
        this.discountRepo = discountRepo;
    }

    public PricedItem priceItem(int tariffId, int quantity) {
        // BR-3: quantity must be a whole number greater than zero
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        // FR-2 + BR-2: look up the tariff; reject if missing OR inactive
        ServiceTariff tariff = tariffRepo.findById(tariffId)
                .filter(ServiceTariff::getIsActive)
                .orElseThrow(() -> new NoActiveTariffException(tariffId));

        // BR-4: find the applicable discount for this service + quantity
        BigDecimal discountPct = discountRepo.findApplicableDiscount(
                tariff.getServiceType(), quantity);

        // SRS §3.3: subtotal = quantity × unit_rate × (1 - discount_pct / 100)
        BigDecimal rate = tariff.getUnitRate();
        BigDecimal gross = rate.multiply(BigDecimal.valueOf(quantity));

        BigDecimal multiplier = BigDecimal.ONE
                .subtract(discountPct.divide(BigDecimal.valueOf(100)));

        BigDecimal subtotal = gross.multiply(multiplier)
                .setScale(2, RoundingMode.HALF_UP);

        return new PricedItem(tariff, quantity, rate, discountPct, subtotal);
    }
}