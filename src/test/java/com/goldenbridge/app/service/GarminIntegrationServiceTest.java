package com.goldenbridge.app.service;

import com.goldenbridge.app.dto.GarminLoginRequest;
import com.goldenbridge.app.dto.GarminLoginResponse;
import com.goldenbridge.app.dto.GarminLogoutResponse;
import com.goldenbridge.app.dto.GarminStatusResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest
class GarminIntegrationServiceTest {

    @Autowired
    private GarminIntegrationService garminIntegrationService;

    @MockBean
    private RestTemplate restTemplate;

    @Test
    void callHello_shouldReturnMessageFromPythonService() {
        // Given
        String name = "Test";
        String expectedResponse = "Hello, Test from Python!";
        String expectedUrl = "http://localhost:5001/hello?name=Test";

        when(restTemplate.getForObject(expectedUrl, String.class))
                .thenReturn(expectedResponse);

        // When
        String actualResponse = garminIntegrationService.callHello(name);

        // Then
        assertThat(actualResponse).isEqualTo(expectedResponse);
    }

    @Test
    void loginToGarmin_shouldReturnSuccess_whenLoginIsSuccessful() {
        // Given
        GarminLoginRequest request = new GarminLoginRequest("testuser", "testpass");
        GarminLoginResponse expectedResponse = new GarminLoginResponse("success", "Garmin login successful");
        String expectedUrl = "http://localhost:5001/garmin/login";

        when(restTemplate.postForObject(eq(expectedUrl), any(GarminLoginRequest.class), eq(GarminLoginResponse.class)))
                .thenReturn(expectedResponse);

        // When
        GarminLoginResponse response = garminIntegrationService.loginToGarmin(request);

        // Then
        assertThat(response.status()).isEqualTo("success");
        assertThat(response.message()).isEqualTo("Garmin login successful");
    }

    @Test
    void loginToGarmin_shouldReturnError_whenLoginFails() {
        // Given
        GarminLoginRequest request = new GarminLoginRequest("baduser", "badpass");
        String expectedErrorBody = "{\"status\":\"error\", \"message\":\"Invalid credentials\"}";
        String expectedUrl = "http://localhost:5001/garmin/login";

        // Mocking HttpClientErrorException requires a different approach with exchange()
        // For simplicity in this test, we'll mock the postForObject to return an error response directly
        // In a real scenario, you might use MockRestServiceServer or a custom RestTemplate error handler
        when(restTemplate.postForObject(eq(expectedUrl), any(GarminLoginRequest.class), eq(GarminLoginResponse.class)))
                .thenReturn(new GarminLoginResponse("error", "Failed to login to Garmin: " + expectedErrorBody));

        // When
        GarminLoginResponse response = garminIntegrationService.loginToGarmin(request);

        // Then
        assertThat(response.status()).isEqualTo("error");
        assertThat(response.message()).contains("Invalid credentials");
    }

    @Test
    void getGarminStatus_shouldReturnLoggedInStatus() {
        // Given
        GarminStatusResponse expectedResponse = new GarminStatusResponse("logged_in", "testuser");
        String expectedUrl = "http://localhost:5001/garmin/status";

        when(restTemplate.getForObject(expectedUrl, GarminStatusResponse.class))
                .thenReturn(expectedResponse);

        // When
        GarminStatusResponse response = garminIntegrationService.getGarminStatus();

        // Then
        assertThat(response.status()).isEqualTo("logged_in");
        assertThat(response.username()).isEqualTo("testuser");
    }

    @Test
    void getGarminStatus_shouldReturnLoggedOutStatus() {
        // Given
        GarminStatusResponse expectedResponse = new GarminStatusResponse("logged_out", null);
        String expectedUrl = "http://localhost:5001/garmin/status";

        when(restTemplate.getForObject(expectedUrl, GarminStatusResponse.class))
                .thenReturn(expectedResponse);

        // When
        GarminStatusResponse response = garminIntegrationService.getGarminStatus();

        // Then
        assertThat(response.status()).isEqualTo("logged_out");
        assertThat(response.username()).isNull();
    }

    @Test
    void logoutFromGarmin_shouldReturnSuccess() {
        // Given
        GarminLogoutResponse expectedResponse = new GarminLogoutResponse("success", "Garmin logout successful");
        String expectedUrl = "http://localhost:5001/garmin/logout";

        when(restTemplate.postForObject(eq(expectedUrl), any(), eq(GarminLogoutResponse.class)))
                .thenReturn(expectedResponse);

        // When
        GarminLogoutResponse response = garminIntegrationService.logoutFromGarmin();

        // Then
        assertThat(response.status()).isEqualTo("success");
        assertThat(response.message()).isEqualTo("Garmin logout successful");
    }
}
