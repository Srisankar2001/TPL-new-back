package com.example.demo.service;

import com.example.demo.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.websocket.Decoder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Service
public class JWTService {

    public String extractUsername(String token){
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token){
        return extractClaim(token, Claims::getExpiration);
    }
    private <T> T extractClaim(String token, Function<Claims,T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
    public List<?> generateAccessToken(User user){
        Map<String,Object> claims = new HashMap<>();
        Date issueAt = new Date(System.currentTimeMillis());
        Date expireAt = new Date(System.currentTimeMillis() +  1000*60*60);
        String token =  Jwts.builder()
                .claims()
                .add(claims)
                .subject(user.getEmail())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000*60*30))
                .and()
                .signWith(getSignKey())
                .compact();
        return List.of(token,expireAt);
    }

    public List<?> generateRefreshToken(User user){
        Map<String,Object> claims = new HashMap<>();
        Date issueAt = new Date(System.currentTimeMillis());
        Date expireAt = new Date(System.currentTimeMillis() +  1000*60*60*24);
        String token = Jwts.builder()
                .claims()
                .add(claims)
                .subject(user.getEmail())
                .issuedAt(issueAt)
                .expiration(expireAt)
                .and()
                .signWith(getSignKey())
                .compact();
        return List.of(token,expireAt);
    }

    private SecretKey getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode("${SECRET_KEY}");
        return Keys.hmacShaKeyFor(keyBytes);
    }


}
