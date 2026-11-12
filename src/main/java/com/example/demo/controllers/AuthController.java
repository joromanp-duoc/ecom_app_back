package com.example.demo.controllers;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager manager;
    private final JwtEncoder encoder;

    AuthController(AuthenticationManager manager, JwtEncoder encoder) {
        this.manager = manager;
        this.encoder = encoder;
    }

    record LoginRequest(String username, String password) {
    }

    record LoginResponse(String accessToken, String tokenType) {
    }

    @PostMapping("/login")
    LoginResponse login(@RequestBody LoginRequest input) {
        Authentication auth = manager.authenticate(
                new UsernamePasswordAuthenticationToken(input.username(), input.password()));
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("ecommerce-clase")
                .subject(auth.getName())
                .issuedAt(now).expiresAt(now.plus(30, ChronoUnit.MINUTES)).build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = encoder.encode(JwtEncoderParameters.from(header, claims))
                .getTokenValue();
        return new LoginResponse(token, "Bearer");
    }
}
