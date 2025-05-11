package org.retrade.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class APIGatewayStartup {
    public static void main(String[] args) {
        SpringApplication.run(APIGatewayStartup.class, args);
    }
}
