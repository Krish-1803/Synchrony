package com.synchrony.inclusion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Entry point for the Synchrony Dynamic Risk Assessment service.
 *
 * <p>The service exposes a REST API that ingests alternative data streams,
 * runs a hybrid credit scoring engine, produces explainable risk drivers and
 * writes an immutable audit trail for every credit decision.</p>
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class InclusionApplication {

    public static void main(String[] args) {
        SpringApplication.run(InclusionApplication.class, args);
    }
}
