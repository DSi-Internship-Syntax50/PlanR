package com.example.PlanR.config;

import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.PlanR.config.seeder.DataSeederBase;

/**
 * Thin orchestrator that delegates to modular seeders.
 * Each seeder handles a single responsibility and declares its execution order.
 *
 * Previously this was a 329-line monolithic class with 7 injected dependencies
 * and 6 unrelated responsibilities in a single lambda.
 */
@Configuration
public class DataSeeder {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    @Bean
    public CommandLineRunner initDatabase(List<DataSeederBase> seeders) {
        return args -> {
            log.info("Running {} data seeders...", seeders.size());
            seeders.stream()
                    .sorted(Comparator.comparingInt(DataSeederBase::getOrder))
                    .forEach(seeder -> {
                        log.debug("Executing seeder: {} (order={})",
                                seeder.getClass().getSimpleName(), seeder.getOrder());
                        seeder.seed();
                    });
            log.info("All data seeders completed.");
        };
    }
}