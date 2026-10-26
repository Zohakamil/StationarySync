package com.stationerysync.stationery_sync.controller;

import com.stationerysync.stationery_sync.dto.DiscountRequest;
import com.stationerysync.stationery_sync.entity.DiscountRule;
import com.stationerysync.stationery_sync.exception.NotFoundException;
import com.stationerysync.stationery_sync.repository.DiscountRuleRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/discounts")
public class DiscountController {

    private final DiscountRuleRepository discountRepo;

    public DiscountController(DiscountRuleRepository discountRepo) {
        this.discountRepo = discountRepo;
    }

    @GetMapping
    public List<DiscountRule> listActive() {
        return discountRepo.findAll().stream()
                .filter(DiscountRule::getIsActive)
                .toList();
    }

    @GetMapping("/all")
    public List<DiscountRule> listAll() {
        return discountRepo.findAll();
    }

    @PostMapping
    public DiscountRule create(@RequestBody DiscountRequest req) {
        DiscountRule r = new DiscountRule();
        r.setServiceType(req.serviceType());
        r.setMinQuantity(req.minQuantity());
        r.setDiscountPct(req.discountPct());
        r.setIsActive(req.isActive() == null ? true : req.isActive());
        return discountRepo.save(r);
    }

    @PutMapping("/{id}")
    public DiscountRule update(@PathVariable Integer id, @RequestBody DiscountRequest req) {
        DiscountRule r = discountRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Discount rule " + id + " not found"));
        r.setServiceType(req.serviceType());
        r.setMinQuantity(req.minQuantity());
        r.setDiscountPct(req.discountPct());
        if (req.isActive() != null) r.setIsActive(req.isActive());
        return discountRepo.save(r);
    }
}