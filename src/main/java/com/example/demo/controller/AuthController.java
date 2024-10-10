package com.example.demo.controller;

import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.SignupRequest;
import com.example.demo.dto.TokenRequest;
import com.example.demo.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin("*")
@RestController
@RequestMapping("api/auth")
public class AuthController {
    @Autowired
    AuthService authService;
    @PostMapping("/register")
    public AuthResponse register(@RequestBody SignupRequest signupRequest){
        return authService.register(signupRequest);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest loginRequest){
        return authService.login(loginRequest);
    }

    @PostMapping("/token")
    public AuthResponse token(@RequestBody TokenRequest tokenRequest){
        return authService.token(tokenRequest);
    }
}
