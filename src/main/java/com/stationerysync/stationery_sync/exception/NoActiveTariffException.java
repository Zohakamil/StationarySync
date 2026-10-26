package com.stationerysync.stationery_sync.exception;

public class NoActiveTariffException extends RuntimeException {
    public NoActiveTariffException(Integer tariffId) {
        super("No active tariff found for tariff ID " + tariffId);
    }
}