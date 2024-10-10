package com.example.demo.service;

import com.example.demo.dto.AuthResponse;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.SignupRequest;
import com.example.demo.dto.TokenRequest;
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

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

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
            User newUser = User.builder()
                    .name(signupRequest.getName())
                    .email(signupRequest.getEmail())
                    .password(encoder.encode(signupRequest.getPassword()))
                    .roles(signupRequest.getRoles())
                    .build();

            List<Role> rolesToSave = new ArrayList<>();
            for (Role role : signupRequest.getRoles()) {
                if (!roleRepository.existsByName(role.getName())) {
                    rolesToSave.add(role);
                }
            }
            if (!rolesToSave.isEmpty()) {
                roleRepository.saveAll(rolesToSave);
            }
            userRepository.save(newUser);
            Optional<User> savedUser = userRepository.findByEmail(signupRequest.getEmail());
            if(savedUser.isPresent()){
                List<?> listRefreshToken = jwtService.generateRefreshToken(user.get());
                List<?> listAccessToken = jwtService.generateAccessToken(user.get());

                RefreshToken refreshToken = RefreshToken.builder()
                        .token((String) listRefreshToken.get(0))
                        .expireAt((Instant) listRefreshToken.get(1))
                        .build();
                refreshTokenRepository.save(refreshToken);

                return AuthResponse.builder()
                        .status(true)
                        .message("Login Successfully")
                        .accessToken((String) listAccessToken.get(0))
                        .accessTokenExpireAt((Date) listAccessToken.get(1))
                        .refreshToken((String) listRefreshToken.get(0))
                        .refreshTokenExpireAt((Date) listRefreshToken.get(1))
                        .build();
            }else{
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
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(),loginRequest.getPassword())
        );
        if(authentication.isAuthenticated()){
            Optional<User> user = userRepository.findByEmail(loginRequest.getEmail());
            if(user.isPresent()){
                List listRefreshToken = jwtService.generateRefreshToken(user.get());
                List listAccessToken = jwtService.generateAccessToken(user.get());
                RefreshToken refreshToken = RefreshToken.builder()
                        .token((String) listRefreshToken.get(0))
                        .expireAt((Instant) listRefreshToken.get(1))
                        .build();
                refreshTokenRepository.save(refreshToken);

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
                        .message("Email Is Wrong")
                        .build();
            }else{
                return AuthResponse.builder()
                        .status(false)
                        .message("Email Not Registered")
                        .build();
            }
        }

    }

    public AuthResponse token(TokenRequest tokenRequest) {
        Optional<RefreshToken> refreshToken = refreshTokenRepository.findByToken(tokenRequest.getToken());
        if(refreshToken.isPresent()){
            if(refreshToken.get().getExpireAt().compareTo(Instant.now()) < 0 ){
                refreshTokenRepository.delete(refreshToken.get());
                return AuthResponse.builder()
                        .status(false)
                        .message("Token Is Expired")
                        .build();
            }else{
               User user = refreshToken.map(RefreshToken::getUser).get();
               List listAccessToken = jwtService.generateAccessToken(user);
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
}
