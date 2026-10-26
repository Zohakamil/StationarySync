package com.stationerysync.stationery_sync.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "Service_Tariffs")
public class ServiceTariff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tariff_id")
    private Integer tariffId;

    @Column(name = "service_type", nullable = false, length = 10)
    private String serviceType;

    @Column(name = "paper_size", nullable = false, length = 5)
    private String paperSize;

    @Column(name = "color_mode", nullable = false, length = 5)
    private String colorMode;

    @Column(name = "unit_rate", nullable = false, precision = 8, scale = 2)
    private BigDecimal unitRate;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    // getters and setters
    public Integer getTariffId() { return tariffId; }
    public void setTariffId(Integer tariffId) { this.tariffId = tariffId; }
    public String getServiceType() { return serviceType; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }
    public String getPaperSize() { return paperSize; }
    public void setPaperSize(String paperSize) { this.paperSize = paperSize; }
    public String getColorMode() { return colorMode; }
    public void setColorMode(String colorMode) { this.colorMode = colorMode; }
    public BigDecimal getUnitRate() { return unitRate; }
    public void setUnitRate(BigDecimal unitRate) { this.unitRate = unitRate; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}