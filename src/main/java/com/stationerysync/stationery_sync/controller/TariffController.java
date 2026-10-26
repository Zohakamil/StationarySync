package com.stationerysync.stationery_sync.controller;

import com.stationerysync.stationery_sync.dto.TariffRequest;
import com.stationerysync.stationery_sync.entity.ServiceTariff;
import com.stationerysync.stationery_sync.exception.NotFoundException;
import com.stationerysync.stationery_sync.repository.ServiceTariffRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tariffs")
public class TariffController {

    private final ServiceTariffRepository tariffRepo;

    public TariffController(ServiceTariffRepository tariffRepo) {
        this.tariffRepo = tariffRepo;
    }

    // FR-1: list active tariffs (used by the order form)
    @GetMapping
    public List<ServiceTariff> listActive() {
        return tariffRepo.findAll().stream()
                .filter(ServiceTariff::getIsActive)
                .toList();
    }

    // FR-11: admin can list ALL tariffs, including inactive
    @GetMapping("/all")
    public List<ServiceTariff> listAll() {
        return tariffRepo.findAll();
    }

    // FR-11: add a new tariff
    @PostMapping
    public ServiceTariff create(@RequestBody TariffRequest req) {
        ServiceTariff t = new ServiceTariff();
        t.setServiceType(req.serviceType());
        t.setPaperSize(req.paperSize());
        t.setColorMode(req.colorMode());
        t.setUnitRate(req.unitRate());
        t.setIsActive(req.isActive() == null ? true : req.isActive());
        return tariffRepo.save(t);
    }

    // FR-11: change a tariff (rate, or deactivate)
    @PutMapping("/{id}")
    public ServiceTariff update(@PathVariable Integer id, @RequestBody TariffRequest req) {
        ServiceTariff t = tariffRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Tariff " + id + " not found"));
        t.setServiceType(req.serviceType());
        t.setPaperSize(req.paperSize());
        t.setColorMode(req.colorMode());
        t.setUnitRate(req.unitRate());
        if (req.isActive() != null) t.setIsActive(req.isActive());
        return tariffRepo.save(t);
    }
}