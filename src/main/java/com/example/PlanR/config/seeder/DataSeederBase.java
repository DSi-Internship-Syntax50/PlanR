package com.example.PlanR.config.seeder;

/**
 * Contract for modular data seeders that run on application startup.
 * Each seeder handles a single responsibility and declares its execution order.
 */
public interface DataSeederBase {

    /**
     * Executes the seeding logic.
     */
    void seed();

    /**
     * Returns the execution order (lower = runs first).
     */
    int getOrder();
}
