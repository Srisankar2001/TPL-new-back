package com.example.demo.controller;

import com.example.demo.dto.*;
import com.example.demo.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/refresh")
    public AuthResponse refresh(@RequestBody TokenRequest tokenRequest){
        return authService.refresh(tokenRequest);
    }

    @PostMapping("/decode")
    public Response<?> decode(@RequestBody TokenRequest tokenRequest){
        return authService.decode(tokenRequest);
    }
}
