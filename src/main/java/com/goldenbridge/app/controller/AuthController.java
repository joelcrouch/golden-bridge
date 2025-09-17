package com.goldenbridge.app.controller;

import com.goldenbridge.app.dto.AuthRequest;
import com.goldenbridge.app.dto.AuthResponse;
import com.goldenbridge.app.dto.GarminLoginRequest;
import com.goldenbridge.app.dto.GarminLoginResponse;
import com.goldenbridge.app.dto.GarminLogoutResponse;
import com.goldenbridge.app.dto.GarminStatusResponse;
import com.goldenbridge.app.security.JwtTokenProvider;
import com.goldenbridge.app.service.GarminIntegrationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final GarminIntegrationService garminIntegrationService;

    public AuthController(AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider, GarminIntegrationService garminIntegrationService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.garminIntegrationService = garminIntegrationService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody AuthRequest authRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authRequest.getUsername(),
                        authRequest.getPassword()
                )
        );

        final UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        final String jwt = jwtTokenProvider.generateToken(userDetails);

        return ResponseEntity.ok(new AuthResponse(jwt));
    }

    @PostMapping("/garmin/login")
    public ResponseEntity<GarminLoginResponse> garminLogin(@RequestBody GarminLoginRequest garminLoginRequest) {
        GarminLoginResponse response = garminIntegrationService.loginToGarmin(garminLoginRequest);
        if ("success".equals(response.status())) {
            // Here you would typically save the credentials securely
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(401).body(response);
        }
    }

    @GetMapping("/garmin/status")
    public ResponseEntity<GarminStatusResponse> garminStatus() {
        return ResponseEntity.ok(garminIntegrationService.getGarminStatus());
    }

    @PostMapping("/garmin/logout")
    public ResponseEntity<GarminLogoutResponse> garminLogout() {
        return ResponseEntity.ok(garminIntegrationService.logoutFromGarmin());
    }

    @GetMapping("/protected")
    public ResponseEntity<String> protectedEndpoint() {
        return ResponseEntity.ok("You have accessed a protected resource!");
    }
}
