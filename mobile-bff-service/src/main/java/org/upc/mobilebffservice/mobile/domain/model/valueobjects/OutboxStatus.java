package org.upc.mobilebffservice.mobile.domain.model.valueobjects;

public enum OutboxStatus {
    PENDING,
    PROCESSING,
    PUBLISHED,
    FAILED,
    DEAD_LETTERED
}
