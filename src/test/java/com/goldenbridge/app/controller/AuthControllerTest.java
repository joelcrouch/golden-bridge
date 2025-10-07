package com.goldenbridge.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.goldenbridge.app.dto.AuthRequest;
import com.goldenbridge.app.dto.GarminLoginRequest;
import com.goldenbridge.app.dto.GarminLoginResponse;
import com.goldenbridge.app.dto.GarminLogoutResponse;
import com.goldenbridge.app.dto.GarminStatusResponse;
import com.goldenbridge.app.entity.User;
import com.goldenbridge.app.repository.UserRepository;
import com.goldenbridge.app.security.JwtTokenProvider;
import com.goldenbridge.app.service.GarminIntegrationService;
import com.goldenbridge.app.service.UserDetailsServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable Spring Security filters for this test
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Mock all dependencies injected into the controller
    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private GarminIntegrationService garminIntegrationService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    // ===== LOGIN TESTS =====
    @Test
    void loginUser_shouldReturnJwt_whenCredentialsAreValid() throws Exception {
        AuthRequest authRequest = new AuthRequest("testuser", "password");
        UserDetails userDetails = new org.springframework.security.core.userdetails.User(
                "testuser", "password", java.util.Collections.emptyList());
        String jwt = "mocked.jwt.token";

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(userDetails, "password", userDetails.getAuthorities()));
        when(jwtTokenProvider.generateToken(userDetails)).thenReturn(jwt);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value(jwt));
    }

    @Test
    void loginUser_shouldReturnUnauthorized_whenCredentialsAreInvalid() throws Exception {
        AuthRequest authRequest = new AuthRequest("testuser", "wrongpassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid username or password"));
    }

    // ===== REGISTER TESTS =====
    @Test
    void registerUser_shouldReturnSuccess_whenUserDoesNotExist() throws Exception {
        AuthRequest authRequest = new AuthRequest("newuser", "newuser@example.com", "password");
        User newUser = new User("newuser", "newuser@example.com", "encodedpassword");

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(newUser);
        when(passwordEncoder.encode("password")).thenReturn("encodedpassword");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User registered successfully"));
    }

    @Test
    void registerUser_shouldReturnBadRequest_whenUsernameExists() throws Exception {
        AuthRequest authRequest = new AuthRequest("existinguser", "newuser@example.com", "password");
        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Username is already taken!"));
    }

    @Test
    void registerUser_shouldReturnBadRequest_whenEmailExists() throws Exception {
        AuthRequest authRequest = new AuthRequest("newuser", "existing@example.com", "password");
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email is already in use!"));
    }

    // ===== GARMIN LOGIN/STATUS/LOGOUT TESTS =====
    @Test
    void garminLogin_shouldReturnSuccess_whenGarminLoginSuccessful() throws Exception {
        GarminLoginRequest garminLoginRequest = new GarminLoginRequest("garminuser", "garminpass");
        GarminLoginResponse garminLoginResponse = new GarminLoginResponse("success", "Garmin login successful");

        when(garminIntegrationService.loginToGarmin(any(GarminLoginRequest.class))).thenReturn(garminLoginResponse);

        mockMvc.perform(post("/api/auth/garmin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(garminLoginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
    }

    @Test
    void garminLogin_shouldReturnUnauthorized_whenGarminLoginFails() throws Exception {
        GarminLoginRequest garminLoginRequest = new GarminLoginRequest("badgarminuser", "badgarminpass");
        GarminLoginResponse garminLoginResponse = new GarminLoginResponse("error", "Failed to login to Garmin: Invalid credentials");

        when(garminIntegrationService.loginToGarmin(any(GarminLoginRequest.class))).thenReturn(garminLoginResponse);

        mockMvc.perform(post("/api/auth/garmin/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(garminLoginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value("Failed to login to Garmin: Invalid credentials"));
    }

    @Test
    void garminStatus_shouldReturnLoggedIn_whenGarminIsLoggedIn() throws Exception {
        GarminStatusResponse garminStatusResponse = new GarminStatusResponse("logged_in", "garminuser");

        when(garminIntegrationService.getGarminStatus()).thenReturn(garminStatusResponse);

        mockMvc.perform(get("/api/auth/garmin/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("logged_in"))
                .andExpect(jsonPath("$.username").value("garminuser"));
    }

    @Test
    void garminStatus_shouldReturnLoggedOut_whenGarminIsNotLoggedIn() throws Exception {
        GarminStatusResponse garminStatusResponse = new GarminStatusResponse("logged_out", null);

        when(garminIntegrationService.getGarminStatus()).thenReturn(garminStatusResponse);

        mockMvc.perform(get("/api/auth/garmin/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("logged_out"));
    }

    @Test
    void garminLogout_shouldReturnSuccess_whenGarminLogoutSuccessful() throws Exception {
        GarminLogoutResponse garminLogoutResponse = new GarminLogoutResponse("success", "Garmin logout successful");

        when(garminIntegrationService.logoutFromGarmin()).thenReturn(garminLogoutResponse);

        mockMvc.perform(post("/api/auth/garmin/logout"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));
    }
}































// package com.goldenbridge.app.controller;


// // import com.goldenbridge.app.controller.AuthController;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.goldenbridge.app.dto.AuthRequest;
// import com.goldenbridge.app.dto.GarminLoginRequest;
// import com.goldenbridge.app.dto.GarminLoginResponse;
// import com.goldenbridge.app.dto.GarminLogoutResponse;
// import com.goldenbridge.app.dto.GarminStatusResponse;
// import com.goldenbridge.app.entity.User;
// import com.goldenbridge.app.repository.UserRepository;
// import com.goldenbridge.app.security.JwtTokenProvider;
// import com.goldenbridge.app.service.UserDetailsServiceImpl;
// import com.goldenbridge.app.service.GarminIntegrationService;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
// import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
// import org.springframework.boot.test.mock.mockito.MockBean;
// import org.springframework.context.annotation.Bean;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.http.MediaType;
// import org.springframework.security.authentication.AuthenticationManager;
// import org.springframework.security.authentication.BadCredentialsException;
// import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
// import org.springframework.security.core.userdetails.UserDetails;
// import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
// import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.test.web.servlet.MockMvc;

// import java.util.Optional;

// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.Mockito.when;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// @WebMvcTest(AuthController.class)
// @AutoConfigureMockMvc(addFilters = false) // Disable Spring Security filters for this test
// class AuthControllerTest {

//     @Autowired
//     private MockMvc mockMvc;

//     @Autowired
//     private ObjectMapper objectMapper;

//     @MockBean
//     private AuthenticationManager authenticationManager;

//     @MockBean
//     private JwtTokenProvider jwtTokenProvider;

//     @MockBean
//     private UserDetailsServiceImpl userDetailsService;

//     @MockBean
//     private UserRepository userRepository;

//     @MockBean
//     private GarminIntegrationService garminIntegrationService;

//     @Configuration
//     static class TestConfig {
//         @Bean
//         public PasswordEncoder passwordEncoder() {
//             return new BCryptPasswordEncoder();
//         }
//     }

//     @Test
//     void loginUser_shouldReturnJwt_whenCredentialsAreValid() throws Exception {
//         // Given
//         AuthRequest authRequest = new AuthRequest("testuser", "password");
//         UserDetails userDetails = new org.springframework.security.core.userdetails.User("testuser", "password", java.util.Collections.emptyList());
//         String jwt = "mocked.jwt.token";

//         when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
//                 .thenReturn(new UsernamePasswordAuthenticationToken(userDetails, "password", userDetails.getAuthorities()));
//         when(userDetailsService.loadUserByUsername("testuser")).thenReturn(userDetails);
//         when(jwtTokenProvider.generateToken(userDetails)).thenReturn(jwt);

//         // When & Then
//         mockMvc.perform(post("/api/auth/login")
//                         .contentType(MediaType.APPLICATION_JSON)
//                         .content(objectMapper.writeValueAsString(authRequest)))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.accessToken").value(jwt));
//     }

//     @Test
//     void loginUser_shouldReturnUnauthorized_whenCredentialsAreInvalid() throws Exception {
//         // Given
//         AuthRequest authRequest = new AuthRequest("testuser", "wrongpassword");

//         when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
//                 .thenThrow(new BadCredentialsException("Invalid credentials"));

//         // When & Then
//         mockMvc.perform(post("/api/auth/login")
//                         .contentType(MediaType.APPLICATION_JSON)
//                         .content(objectMapper.writeValueAsString(authRequest)))
//                 .andExpect(status().isUnauthorized())
//                 .andExpect(jsonPath("$.message").value("Invalid username or password"));
//     }

//     @Test
//     void registerUser_shouldReturnSuccess_whenUserDoesNotExist() throws Exception {
//         // Given
//         AuthRequest authRequest = new AuthRequest("newuser", "newuser@example.com", "password");
//         User newUser = new User("newuser", "newuser@example.com", "password");

//         when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
//         when(userRepository.findByEmail("newuser@example.com")).thenReturn(Optional.empty());
//         when(userRepository.save(any(User.class))).thenReturn(newUser);

//         // When & Then
//         mockMvc.perform(post("/api/auth/register")
//                         .contentType(MediaType.APPLICATION_JSON)
//                         .content(objectMapper.writeValueAsString(authRequest)))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.message").value("User registered successfully"));
//     }

//     @Test
//     void registerUser_shouldReturnBadRequest_whenUsernameExists() throws Exception {
//         // Given
//         AuthRequest authRequest = new AuthRequest("existinguser", "newuser@example.example.com", "password");
//         User existingUser = new User("existinguser", "existinguser@example.com", "password");

//         when(userRepository.findByUsername("existinguser")).thenReturn(Optional.of(existingUser));

//         // When & Then
//         mockMvc.perform(post("/api/auth/register")
//                         .contentType(MediaType.APPLICATION_JSON)
//                         .content(objectMapper.writeValueAsString(authRequest)))
//                 .andExpect(status().isBadRequest())
//                 .andExpect(jsonPath("$.message").value("Username is already taken!"));
//     }

//     @Test
//     void registerUser_shouldReturnBadRequest_whenEmailExists() throws Exception {
//         // Given
//         AuthRequest authRequest = new AuthRequest("newuser", "existing@example.com", "password");
//         User existingUser = new User("anotheruser", "existing@example.com", "password");

//         when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
//         when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(existingUser));

//         // When & Then
//         mockMvc.perform(post("/api/auth/register")
//                         .contentType(MediaType.APPLICATION_JSON)
//                         .content(objectMapper.writeValueAsString(authRequest)))
//                 .andExpect(status().isBadRequest())
//                 .andExpect(jsonPath("$.message").value("Email is already in use!"));
//     }

//     @Test
//     void garminLogin_shouldReturnSuccess_whenGarminLoginSuccessful() throws Exception {
//         // Given
//         GarminLoginRequest garminLoginRequest = new GarminLoginRequest("garminuser", "garminpass");
//         GarminLoginResponse garminLoginResponse = new GarminLoginResponse("success", "Garmin login successful");

//         when(garminIntegrationService.loginToGarmin(any(GarminLoginRequest.class))).thenReturn(garminLoginResponse);

//         // When & Then
//         mockMvc.perform(post("/api/auth/garmin/login")
//                         .contentType(MediaType.APPLICATION_JSON)
//                         .content(objectMapper.writeValueAsString(garminLoginRequest)))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.status").value("success"));
//     }

//     @Test
//     void garminLogin_shouldReturnUnauthorized_whenGarminLoginFails() throws Exception {
//         // Given
//         GarminLoginRequest garminLoginRequest = new GarminLoginRequest("badgarminuser", "badgarminpass");
//         GarminLoginResponse garminLoginResponse = new GarminLoginResponse("error", "Failed to login to Garmin: Invalid credentials");

//         when(garminIntegrationService.loginToGarmin(any(GarminLoginRequest.class))).thenReturn(garminLoginResponse);

//         // When & Then
//         mockMvc.perform(post("/api/auth/garmin/login")
//                         .contentType(MediaType.APPLICATION_JSON)
//                         .content(objectMapper.writeValueAsString(garminLoginRequest)))
//                 .andExpect(status().isUnauthorized())
//                 .andExpect(jsonPath("$.status").value("error"))
//                 .andExpect(jsonPath("$.message").value("Failed to login to Garmin: Invalid credentials"));
//     }

//     @Test
//     void garminStatus_shouldReturnLoggedIn_whenGarminIsLoggedIn() throws Exception {
//         // Given
//         GarminStatusResponse garminStatusResponse = new GarminStatusResponse("logged_in", "garminuser");

//         when(garminIntegrationService.getGarminStatus()).thenReturn(garminStatusResponse);

//         // When & Then
//         mockMvc.perform(get("/api/auth/garmin/status"))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.status").value("logged_in"))
//                 .andExpect(jsonPath("$.username").value("garminuser"));
//     }

//     @Test
//     void garminStatus_shouldReturnLoggedOut_whenGarminIsNotLoggedIn() throws Exception {
//         // Given
//         GarminStatusResponse garminStatusResponse = new GarminStatusResponse("logged_out", null);

//         when(garminIntegrationService.getGarminStatus()).thenReturn(garminStatusResponse);

//         // When & Then
//         mockMvc.perform(get("/api/auth/garmin/status"))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.status").value("logged_out"));
//     }

//     @Test
//     void garminLogout_shouldReturnSuccess_whenGarminLogoutSuccessful() throws Exception {
//         // Given
//         GarminLogoutResponse garminLogoutResponse = new GarminLogoutResponse("success", "Garmin logout successful");

//         when(garminIntegrationService.logoutFromGarmin()).thenReturn(garminLogoutResponse);

//         // When & Then
//         mockMvc.perform(post("/api/auth/garmin/logout"))
//                 .andExpect(status().isOk())
//                 .andExpect(jsonPath("$.status").value("success"));
//     }
// }
