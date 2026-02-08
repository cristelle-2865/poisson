package com.example.poisson.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    
    @GetMapping("/test")
    public String test() {
        return "API fonctionne !";
    }
    
    @GetMapping("/plats/test")
    public String testPlats() {
        return "Route /plats accessible !";
    }
}

