package org.retrade.migration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MigrationServiceStartup {
    public static void main(String[] args) {
        SpringApplication.run(MigrationServiceStartup.class, args);
    }
}
