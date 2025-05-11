package org.retrade.authentication.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/registers")
public class RegisterController {
    @PostMapping(path = "/customers/account") /// Use For Internal Authentication Only
    public ResponseEntity<?> customerRegisterAccount () {
        return null;
    }
    @PutMapping(path = "/customers/profile") /// Use For Posting Updating A Profile Customer Account When Account Create Success
    public ResponseEntity<?> customerRegisterProfile () {
        return null;
    }
    @PostMapping(path = "partners/account")
    public ResponseEntity<?> partnerRegisterAccount () {
        return null;
    }
}
