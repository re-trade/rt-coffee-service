package org.retrade.main.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("pings")
public class PingController {
    public ResponseEntity<String> pings () {
        return ResponseEntity.ok("Pong");
    }
}
