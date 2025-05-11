package org.retrade.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("pings")
public class PingController {
    @GetMapping
    public ResponseEntity<String> pings() {
        return ResponseEntity.ok("Hello User Service");
    }
}
