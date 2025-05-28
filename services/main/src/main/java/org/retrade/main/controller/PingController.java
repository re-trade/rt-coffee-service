package org.retrade.main.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("pings")
@Tag(name = "Health Check", description = "Service health and connectivity endpoints")
public class PingController {

    @Operation(
            summary = "Health check endpoint",
            description = "Simple health check endpoint to verify service is running. Returns 'Pong' response."
    )
    @GetMapping
    public ResponseEntity<String> pings() {
        return ResponseEntity.ok("Pong");
    }
}
