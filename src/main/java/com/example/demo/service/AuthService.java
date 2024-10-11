package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.entity.RefreshToken;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.repository.RefreshTokenRepository;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    JWTService jwtService;

    PasswordEncoder encoder = new BCryptPasswordEncoder();
    public AuthResponse register(SignupRequest signupRequest) {
        Optional<User> user = userRepository.findByEmail(signupRequest.getEmail());
        if(user.isEmpty()){
            try{
                Optional<Role> userRole = roleRepository.findByName("USER");
                if(userRole.isPresent()){
                    User newUser = User.builder()
                            .name(signupRequest.getName())
                            .email(signupRequest.getEmail())
                            .password(encoder.encode(signupRequest.getPassword()))
                            .roles(Collections.singleton(userRole.get()))
                            .build();
                    userRepository.save(newUser);
                    return AuthResponse.builder()
                            .status(true)
                            .message("Account Created Successfully")
                            .build();
                }else{
                    return AuthResponse.builder()
                            .status(false)
                            .message("Role Not Found")
                            .build();
                }
            }catch (Exception e){
                System.out.println(e);
                return AuthResponse.builder()
                        .status(false)
                        .message("Internal Server Error")
                        .build();
            }
        }else{
            return AuthResponse.builder()
                    .status(false)
                    .message("Email Already Registered")
                    .build();
        }
    }

    public AuthResponse login(LoginRequest loginRequest) {
        try{
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(),loginRequest.getPassword())
            );
            if(authentication.isAuthenticated()){
                Optional<User> user = userRepository.findByEmail(loginRequest.getEmail());
                if(user.isPresent()){
                    User savedUser = user.get();
                    List<?> listRefreshToken = jwtService.generateRefreshToken(savedUser);
                    List<?> listAccessToken = jwtService.generateAccessToken(savedUser);
                    RefreshToken refreshToken = RefreshToken.builder()
                            .token((String) listRefreshToken.get(0))
                            .expireAt((Date) listRefreshToken.get(1))
                            .user(savedUser)
                            .build();
                    RefreshToken token = refreshTokenRepository.save(refreshToken);
                    savedUser.setToken(token);
                    userRepository.save(savedUser);
                    return AuthResponse.builder()
                            .status(true)
                            .message("Login Successfully")
                            .accessToken((String) listAccessToken.get(0))
                            .accessTokenExpireAt((Date) listAccessToken.get(1))
                            .refreshToken((String) listRefreshToken.get(0))
                            .refreshTokenExpireAt((Date) listRefreshToken.get(1))
                            .build();
                }else{
                    return null;
                }
            }else{
                Optional<User> user = userRepository.findByEmail(loginRequest.getEmail());
                if(user.isPresent()){
                    return AuthResponse.builder()
                            .status(false)
                            .message("Password Is Wrong")
                            .build();
                }else{
                    return AuthResponse.builder()
                            .status(false)
                            .message("Email Not Registered")
                            .build();
                }
            }
        }catch(Exception e){
            return AuthResponse.builder()
                    .status(false)
                    .message("Internal Server Error")
                    .build();
        }
    }

    public AuthResponse refresh(TokenRequest tokenRequest) {
        Optional<RefreshToken> refreshToken = refreshTokenRepository.findByToken(tokenRequest.getToken());
        if(refreshToken.isPresent()){
            if(refreshToken.get().getExpireAt().before(new Date())) {
                refreshTokenRepository.delete(refreshToken.get());
                return AuthResponse.builder()
                        .status(false)
                        .message("Token Is Expired")
                        .build();
            }else{
               User user = refreshToken.map(RefreshToken::getUser).get();
               List<?> listAccessToken = jwtService.generateAccessToken(user);
                return AuthResponse.builder()
                        .status(true)
                        .accessToken((String) listAccessToken.get(0))
                        .accessTokenExpireAt((Date) listAccessToken.get(1))
                        .build();
            }
        }else{
            return AuthResponse.builder()
                    .status(false)
                    .message("Token Not Matched")
                    .build();
        }
    }

    public Response<?> decode(TokenRequest tokenRequest) {
            String email = jwtService.extractUsername(tokenRequest.getToken());
            Optional<User> user = userRepository.findByEmail(email);
            if(user.isPresent()){
                return Response.builder()
                        .status(true)
                        .data(user.get())
                        .build();
            }else{
                return Response.builder()
                        .status(false)
                        .message("User Not Found")
                        .build();
            }
    }
}
