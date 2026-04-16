package com.example.PlanR.config.seeder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Seeder #1: Handles legacy data migrations.
 * - ADMIN → COORDINATOR role migration
 * - Self-healing: Populates slot_count for existing courses where it's NULL
 */
@Component
public class MigrationSeeder implements DataSeederBase {

    private static final Logger log = LoggerFactory.getLogger(MigrationSeeder.class);
    private final JdbcTemplate jdbcTemplate;

    public MigrationSeeder(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void seed() {
        // Legacy role migration
        int migrated = jdbcTemplate.update("UPDATE users SET role = 'COORDINATOR' WHERE role = 'ADMIN'");
        if (migrated > 0) {
            log.info("MIGRATION: Updated {} user(s) from ADMIN to COORDINATOR role.", migrated);
        }

        // Self-healing: Ensure 'slot_count' is populated if missing (Migration for existing data)
        int fixedSlots = jdbcTemplate.update(
                "UPDATE courses SET slot_count = COALESCE(required_slots, CASE WHEN is_lab = true THEN 3 ELSE 1 END) WHERE slot_count IS NULL");
        if (fixedSlots > 0) {
            log.info("MIGRATION: Populated 'slot_count' for {} existing courses.", fixedSlots);
        }
    }

    @Override
    public int getOrder() {
        return 1;
    }
}
