package org.retrade.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class UserServiceStartup {
    public static void main(String[] args) {
        SpringApplication.run(UserServiceStartup.class, args);
    }
}
