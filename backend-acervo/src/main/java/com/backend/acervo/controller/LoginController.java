package com.backend.acervo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;

@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
@Controller
public class LoginController {
    
    @GetMapping("/login")
    public void loginPage() {
        // Método vazio - só para evitar o warning
    }
}