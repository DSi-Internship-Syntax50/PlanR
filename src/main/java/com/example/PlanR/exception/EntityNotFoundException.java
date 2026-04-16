package com.example.PlanR.exception;

/**
 * Thrown when a requested entity cannot be found in the database.
 * Replaces generic RuntimeException("X not found") throughout the codebase.
 */
public class EntityNotFoundException extends RuntimeException {

    private final String entityType;
    private final Object entityId;

    public EntityNotFoundException(String entityType, Object entityId) {
        super(entityType + " not found with ID: " + entityId);
        this.entityType = entityType;
        this.entityId = entityId;
    }

    public String getEntityType() {
        return entityType;
    }

    public Object getEntityId() {
        return entityId;
    }
}
