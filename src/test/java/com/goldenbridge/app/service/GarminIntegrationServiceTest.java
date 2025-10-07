package com.goldenbridge.app.service;

import com.goldenbridge.app.dto.GarminLoginRequest;
import com.goldenbridge.app.dto.GarminLoginResponse;
import com.goldenbridge.app.dto.GarminLogoutResponse;
import com.goldenbridge.app.dto.GarminStatusResponse;
import com.goldenbridge.app.exception.ActivityDownloadException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import org.springframework.http.HttpStatus;

class GarminIntegrationServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private GarminIntegrationService garminIntegrationService;

    private final String baseUrl = "http://localhost:5001";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Inject base URL manually
        garminIntegrationService = new GarminIntegrationService(restTemplate, baseUrl);
    }

    @Test
    void callHello_shouldReturnMessageFromPythonService() {
        String name = "Test";
        String expectedResponse = "Hello, Test from Python!";
        String url = baseUrl + "/hello?name=Test";

        when(restTemplate.getForObject(url, String.class)).thenReturn(expectedResponse);

        String actualResponse = garminIntegrationService.callHello(name);

        assertThat(actualResponse).isEqualTo(expectedResponse);
    }

    @Test
    void loginToGarmin_shouldReturnSuccess_whenLoginIsSuccessful() {
        GarminLoginRequest request = new GarminLoginRequest("user", "pass");
        GarminLoginResponse expectedResponse = new GarminLoginResponse("success", "Logged in!");
        String url = baseUrl + "/garmin/login";

        when(restTemplate.postForObject(url, request, GarminLoginResponse.class))
                .thenReturn(expectedResponse);

        GarminLoginResponse actualResponse = garminIntegrationService.loginToGarmin(request);

        assertThat(actualResponse.status()).isEqualTo("success");
    }

    @Test
    void loginToGarmin_shouldReturnError_whenLoginFails() {
        GarminLoginRequest request = new GarminLoginRequest("bad", "user");
        String url = baseUrl + "/garmin/login";

        when(restTemplate.postForObject(url, request, GarminLoginResponse.class))
                .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "Unauthorized", "{\"status\":\"error\"}".getBytes(), null));

        GarminLoginResponse response = garminIntegrationService.loginToGarmin(request);

        assertThat(response.status()).isEqualTo("error");
        assertThat(response.message()).contains("Failed to login to Garmin");
    }

    @Test
    void getGarminStatus_shouldReturnLoggedInStatus() {
        GarminStatusResponse expectedResponse = new GarminStatusResponse("logged_in", "user1");
        String url = baseUrl + "/garmin/status";

        when(restTemplate.getForObject(url, GarminStatusResponse.class)).thenReturn(expectedResponse);

        GarminStatusResponse actualResponse = garminIntegrationService.getGarminStatus();

        assertThat(actualResponse.status()).isEqualTo("logged_in");
        assertThat(actualResponse.username()).isEqualTo("user1");
    }

    @Test
    void getGarminStatus_shouldReturnLoggedOutStatus() {
        GarminStatusResponse expectedResponse = new GarminStatusResponse("logged_out", null);
        String url = baseUrl + "/garmin/status";

        when(restTemplate.getForObject(url, GarminStatusResponse.class)).thenReturn(expectedResponse);

        GarminStatusResponse actualResponse = garminIntegrationService.getGarminStatus();

        assertThat(actualResponse.status()).isEqualTo("logged_out");
        assertThat(actualResponse.username()).isNull();
    }

    @Test
    void logoutFromGarmin_shouldReturnSuccess() {
        GarminLogoutResponse expectedResponse = new GarminLogoutResponse("success", "Logged out!");
        String url = baseUrl + "/garmin/logout";

        when(restTemplate.postForObject(url, null, GarminLogoutResponse.class)).thenReturn(expectedResponse);

        GarminLogoutResponse actualResponse = garminIntegrationService.logoutFromGarmin();

        assertThat(actualResponse.status()).isEqualTo("success");
    }

    @Test
    void getGarminActivities_shouldReturnActivities() {
        int start = 0, limit = 5;
        String expectedActivities = "[{\"activityId\":\"1\"}]";
        String url = baseUrl + "/garmin/activities?start=0&limit=5";

        when(restTemplate.getForObject(url, String.class)).thenReturn(expectedActivities);

        String actualActivities = garminIntegrationService.getGarminActivities(start, limit);

        assertThat(actualActivities).isEqualTo(expectedActivities);
    }

    @Test
    void getGarminActivities_shouldThrowHttpClientErrorException() {
        int start = 0, limit = 5;
        String url = baseUrl + "/garmin/activities?start=0&limit=5";

        when(restTemplate.getForObject(url, String.class))
                .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        assertThrows(HttpClientErrorException.class, () ->
                garminIntegrationService.getGarminActivities(start, limit));
    }

    @Test
    void downloadActivityFitFile_shouldReturnFitFile() {
        String activityId = "123";
        byte[] expectedData = "mock_fit".getBytes();
        String url = baseUrl + "/garmin/activity/123/download";

        when(restTemplate.getForObject(url, byte[].class)).thenReturn(expectedData);

        byte[] actualData = garminIntegrationService.downloadActivityFitFile(activityId);

        assertThat(actualData).isEqualTo(expectedData);
    }

    @Test
    void downloadActivityFitFile_shouldThrowActivityDownloadException_onHttpClientError() {
        String activityId = "404";
        String url = baseUrl + "/garmin/activity/404/download";

        when(restTemplate.getForObject(url, byte[].class))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        ActivityDownloadException ex = assertThrows(ActivityDownloadException.class, () ->
                garminIntegrationService.downloadActivityFitFile(activityId));

        assertThat(ex.getCause()).isInstanceOf(HttpClientErrorException.class);
    }

    @Test
    void downloadActivityFitFile_shouldThrowActivityDownloadException_onGenericError() {
        String activityId = "error";
        String url = baseUrl + "/garmin/activity/error/download";

        when(restTemplate.getForObject(url, byte[].class))
                .thenThrow(new IllegalArgumentException("Bad URI"));

        ActivityDownloadException ex = assertThrows(ActivityDownloadException.class, () ->
                garminIntegrationService.downloadActivityFitFile(activityId));

        assertThat(ex.getCause()).isInstanceOf(IllegalArgumentException.class);
    }
}































// package com.goldenbridge.app.service;

// import com.goldenbridge.app.dto.*;
// import com.goldenbridge.app.exception.ActivityDownloadException;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.InjectMocks;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;
// import org.springframework.http.HttpStatus;
// import org.springframework.web.client.HttpClientErrorException;
// import org.springframework.web.client.RestTemplate;

// import static org.assertj.core.api.Assertions.assertThat;
// import static org.junit.jupiter.api.Assertions.assertThrows;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.ArgumentMatchers.eq;
// import static org.mockito.Mockito.when;

// @ExtendWith(MockitoExtension.class)
// class GarminIntegrationServiceTest {

//     @InjectMocks
//     private GarminIntegrationService garminIntegrationService;

//     @Mock
//     private RestTemplate restTemplate;

//     @Test
//     void callHello_shouldReturnMessageFromPythonService() {
//         String name = "Test";
//         String expectedResponse = "Hello, Test from Python!";
//         String expectedUrl = "http://localhost:5001/hello?name=Test";

//         when(restTemplate.getForObject(expectedUrl, String.class))
//                 .thenReturn(expectedResponse);

//         String actualResponse = garminIntegrationService.callHello(name);
//         assertThat(actualResponse).isEqualTo(expectedResponse);
//     }

//     @Test
//     void loginToGarmin_shouldReturnSuccess_whenLoginIsSuccessful() {
//         GarminLoginRequest request = new GarminLoginRequest("testuser", "testpass");
//         GarminLoginResponse expectedResponse = new GarminLoginResponse("success", "Garmin login successful");
//         String expectedUrl = "http://localhost:5001/garmin/login";

//         when(restTemplate.postForObject(eq(expectedUrl), any(GarminLoginRequest.class), eq(GarminLoginResponse.class)))
//                 .thenReturn(expectedResponse);

//         GarminLoginResponse response = garminIntegrationService.loginToGarmin(request);
//         assertThat(response.status()).isEqualTo("success");
//         assertThat(response.message()).isEqualTo("Garmin login successful");
//     }

//     @Test
//     void loginToGarmin_shouldReturnError_whenLoginFails() {
//         GarminLoginRequest request = new GarminLoginRequest("baduser", "badpass");
//         String expectedUrl = "http://localhost:5001/garmin/login";
//         String errorBody = "{\"status\":\"error\", \"message\":\"Invalid credentials\"}";

//         when(restTemplate.postForObject(eq(expectedUrl), any(GarminLoginRequest.class), eq(GarminLoginResponse.class)))
//                 .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "Unauthorized", errorBody.getBytes(), null));

//         GarminLoginResponse response = garminIntegrationService.loginToGarmin(request);
//         assertThat(response.status()).isEqualTo("error");
//         assertThat(response.message()).contains("Failed to login to Garmin");
//     }

//     @Test
//     void getGarminStatus_shouldReturnLoggedInStatus() {
//         GarminStatusResponse expectedResponse = new GarminStatusResponse("logged_in", "testuser");
//         String expectedUrl = "http://localhost:5001/garmin/status";

//         when(restTemplate.getForObject(expectedUrl, GarminStatusResponse.class))
//                 .thenReturn(expectedResponse);

//         GarminStatusResponse response = garminIntegrationService.getGarminStatus();
//         assertThat(response.status()).isEqualTo("logged_in");
//         assertThat(response.username()).isEqualTo("testuser");
//     }

//     @Test
//     void getGarminStatus_shouldReturnLoggedOutStatus() {
//         GarminStatusResponse expectedResponse = new GarminStatusResponse("logged_out", null);
//         String expectedUrl = "http://localhost:5001/garmin/status";

//         when(restTemplate.getForObject(expectedUrl, GarminStatusResponse.class))
//                 .thenReturn(expectedResponse);

//         GarminStatusResponse response = garminIntegrationService.getGarminStatus();
//         assertThat(response.status()).isEqualTo("logged_out");
//         assertThat(response.username()).isNull();
//     }

//     @Test
//     void logoutFromGarmin_shouldReturnSuccess() {
//         GarminLogoutResponse expectedResponse = new GarminLogoutResponse("success", "Garmin logout successful");
//         String expectedUrl = "http://localhost:5001/garmin/logout";

//         when(restTemplate.postForObject(eq(expectedUrl), any(), eq(GarminLogoutResponse.class)))
//                 .thenReturn(expectedResponse);

//         GarminLogoutResponse response = garminIntegrationService.logoutFromGarmin();
//         assertThat(response.status()).isEqualTo("success");
//         assertThat(response.message()).isEqualTo("Garmin logout successful");
//     }

//     @Test
//     void getGarminActivities_shouldReturnActivities_whenSuccessful() {
//         int start = 0;
//         int limit = 10;
//         String expectedActivitiesJson = "[{\"activityId\":\"1\", \"name\":\"Run\"}]";
//         String expectedUrl = "http://localhost:5001/garmin/activities?start=0&limit=10";

//         when(restTemplate.getForObject(expectedUrl, String.class))
//                 .thenReturn(expectedActivitiesJson);

//         String actualActivitiesJson = garminIntegrationService.getGarminActivities(start, limit);
//         assertThat(actualActivitiesJson).isEqualTo(expectedActivitiesJson);
//     }

//     @Test
//     void getGarminActivities_shouldReturnEmptyList_whenNoActivities() {
//         int start = 0;
//         int limit = 10;
//         String expectedActivitiesJson = "[]";
//         String expectedUrl = "http://localhost:5001/garmin/activities?start=0&limit=10";

//         when(restTemplate.getForObject(expectedUrl, String.class))
//                 .thenReturn(expectedActivitiesJson);

//         String actualActivitiesJson = garminIntegrationService.getGarminActivities(start, limit);
//         assertThat(actualActivitiesJson).isEqualTo(expectedActivitiesJson);
//     }

//     @Test
//     void getGarminActivities_shouldThrowException_whenPythonServiceErrors() {
//         int start = 0;
//         int limit = 10;
//         String expectedUrl = "http://localhost:5001/garmin/activities?start=0&limit=10";

//         when(restTemplate.getForObject(expectedUrl, String.class))
//                 .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

//         assertThrows(HttpClientErrorException.class, () -> {
//             garminIntegrationService.getGarminActivities(start, limit);
//         });
//     }

//     @Test
//     void downloadActivityFitFile_shouldReturnFitFile_whenSuccessful() {
//         String activityId = "12345";
//         byte[] expectedFitFile = "mock_fit_data".getBytes();
//         String expectedUrl = "http://localhost:5001/garmin/activity/12345/download";

//         when(restTemplate.getForObject(eq(expectedUrl), eq(byte[].class)))
//                 .thenReturn(expectedFitFile);

//         byte[] actualFitFile = garminIntegrationService.downloadActivityFitFile(activityId);
//         assertThat(actualFitFile).isEqualTo(expectedFitFile);
//     }

//     @Test
//     void downloadActivityFitFile_shouldThrowActivityDownloadException_whenHttpClientErrorExceptionOccurs() {
//         String activityId = "nonexistent";
//         String expectedUrl = "http://localhost:5001/garmin/activity/nonexistent/download";

//         when(restTemplate.getForObject(eq(expectedUrl), eq(byte[].class)))
//                 .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

//         ActivityDownloadException thrown = assertThrows(ActivityDownloadException.class, () -> {
//             garminIntegrationService.downloadActivityFitFile(activityId);
//         });

//         assertThat(thrown.getMessage()).contains("Failed to download FIT file for activity nonexistent");
//         assertThat(thrown.getCause()).isInstanceOf(HttpClientErrorException.class);
//     }

//     @Test
//     void downloadActivityFitFile_shouldThrowActivityDownloadException_whenGenericExceptionOccurs() {
//         String activityId = "error_id";
//         String expectedUrl = "http://localhost:5001/garmin/activity/error_id/download";

//         when(restTemplate.getForObject(eq(expectedUrl), eq(byte[].class)))
//                 .thenThrow(new RuntimeException("Network error"));

//         ActivityDownloadException thrown = assertThrows(ActivityDownloadException.class, () -> {
//             garminIntegrationService.downloadActivityFitFile(activityId);
//         });

//         assertThat(thrown.getMessage()).contains("Unexpected error downloading FIT file for activity error_id");
//         assertThat(thrown.getCause()).isInstanceOf(RuntimeException.class);
//     }
// }




















// package com.goldenbridge.app.service;

// import com.goldenbridge.app.dto.GarminLoginRequest;
// import com.goldenbridge.app.dto.GarminLoginResponse;
// import com.goldenbridge.app.dto.GarminLogoutResponse;
// import com.goldenbridge.app.dto.GarminStatusResponse;
// import com.goldenbridge.app.exception.ActivityDownloadException;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.boot.test.mock.mockito.MockBean;
// import org.springframework.http.HttpStatus;
// import org.springframework.web.client.HttpClientErrorException;
// import org.springframework.web.client.RestTemplate;

// import static org.assertj.core.api.Assertions.assertThat;
// import static org.junit.jupiter.api.Assertions.assertThrows;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.ArgumentMatchers.eq;
// import static org.mockito.Mockito.when;

// @SpringBootTest
// class GarminIntegrationServiceTest {

//     @Autowired
//     private GarminIntegrationService garminIntegrationService;

//     @MockBean
//     private RestTemplate restTemplate;

//     @Test
//     void callHello_shouldReturnMessageFromPythonService() {
//         // Given
//         String name = "Test";
//         String expectedResponse = "Hello, Test from Python!";
//         String expectedUrl = "http://localhost:5001/hello?name=Test";

//         when(restTemplate.getForObject(expectedUrl, String.class))
//                 .thenReturn(expectedResponse);

//         // When
//         String actualResponse = garminIntegrationService.callHello(name);

//         // Then
//         assertThat(actualResponse).isEqualTo(expectedResponse);
//     }

//     @Test
//     void loginToGarmin_shouldReturnSuccess_whenLoginIsSuccessful() {
//         // Given
//         GarminLoginRequest request = new GarminLoginRequest("testuser", "testpass");
//         GarminLoginResponse expectedResponse = new GarminLoginResponse("success", "Garmin login successful");
//         String expectedUrl = "http://localhost:5001/garmin/login";

//         when(restTemplate.postForObject(eq(expectedUrl), any(GarminLoginRequest.class), eq(GarminLoginResponse.class)))
//                 .thenReturn(expectedResponse);

//         // When
//         GarminLoginResponse response = garminIntegrationService.loginToGarmin(request);

//         // Then
//         assertThat(response.status()).isEqualTo("success");
//         assertThat(response.message()).isEqualTo("Garmin login successful");
//     }

//     @Test
//     void loginToGarmin_shouldReturnError_whenLoginFails() {
//         // Given
//         GarminLoginRequest request = new GarminLoginRequest("baduser", "badpass");
//         String expectedErrorBody = "{\"status\":\"error\", \"message\":\"Invalid credentials\"}";
//         String expectedUrl = "http://localhost:5001/garmin/login";

//         when(restTemplate.postForObject(eq(expectedUrl), any(GarminLoginRequest.class), eq(GarminLoginResponse.class)))
//                 .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "Unauthorized", expectedErrorBody.getBytes(), null));

//         // When
//         GarminLoginResponse response = garminIntegrationService.loginToGarmin(request);

//         // Then
//         assertThat(response.status()).isEqualTo("error");
//         assertThat(response.message()).contains("Failed to login to Garmin");
//     }

//     @Test
//     void getGarminStatus_shouldReturnLoggedInStatus() {
//         // Given
//         GarminStatusResponse expectedResponse = new GarminStatusResponse("logged_in", "testuser");
//         String expectedUrl = "http://localhost:5001/garmin/status";

//         when(restTemplate.getForObject(expectedUrl, GarminStatusResponse.class))
//                 .thenReturn(expectedResponse);

//         // When
//         GarminStatusResponse response = garminIntegrationService.getGarminStatus();

//         // Then
//         assertThat(response.status()).isEqualTo("logged_in");
//         assertThat(response.username()).isEqualTo("testuser");
//     }

//     @Test
//     void getGarminStatus_shouldReturnLoggedOutStatus() {
//         // Given
//         GarminStatusResponse expectedResponse = new GarminStatusResponse("logged_out", null);
//         String expectedUrl = "http://localhost:5001/garmin/status";

//         when(restTemplate.getForObject(expectedUrl, GarminStatusResponse.class))
//                 .thenReturn(expectedResponse);

//         // When
//         GarminStatusResponse response = garminIntegrationService.getGarminStatus();

//         // Then
//         assertThat(response.status()).isEqualTo("logged_out");
//         assertThat(response.username()).isNull();
//     }

//     @Test
//     void logoutFromGarmin_shouldReturnSuccess() {
//         // Given
//         GarminLogoutResponse expectedResponse = new GarminLogoutResponse("success", "Garmin logout successful");
//         String expectedUrl = "http://localhost:5001/garmin/logout";

//         when(restTemplate.postForObject(eq(expectedUrl), any(), eq(GarminLogoutResponse.class)))
//                 .thenReturn(expectedResponse);

//         // When
//         GarminLogoutResponse response = garminIntegrationService.logoutFromGarmin();

//         // Then
//         assertThat(response.status()).isEqualTo("success");
//         assertThat(response.message()).isEqualTo("Garmin logout successful");
//     }

//     @Test
//     void getGarminActivities_shouldReturnActivities_whenSuccessful() {
//         // Given
//         int start = 0;
//         int limit = 10;
//         String expectedActivitiesJson = "[{\"activityId\":\"1\", \"name\":\"Run\"}]";
//         String expectedUrl = "http://localhost:5001/garmin/activities?start=0&limit=10";

//         when(restTemplate.getForObject(expectedUrl, String.class))
//                 .thenReturn(expectedActivitiesJson);

//         // When
//         String actualActivitiesJson = garminIntegrationService.getGarminActivities(start, limit);

//         // Then
//         assertThat(actualActivitiesJson).isEqualTo(expectedActivitiesJson);
//     }

//     @Test
//     void getGarminActivities_shouldReturnEmptyList_whenNoActivities() {
//         // Given
//         int start = 0;
//         int limit = 10;
//         String expectedActivitiesJson = "[]";
//         String expectedUrl = "http://localhost:5001/garmin/activities?start=0&limit=10";

//         when(restTemplate.getForObject(expectedUrl, String.class))
//                 .thenReturn(expectedActivitiesJson);

//         // When
//         String actualActivitiesJson = garminIntegrationService.getGarminActivities(start, limit);

//         // Then
//         assertThat(actualActivitiesJson).isEqualTo(expectedActivitiesJson);
//     }

//     @Test
//     void getGarminActivities_shouldThrowException_whenPythonServiceErrors() {
//         // Given
//         int start = 0;
//         int limit = 10;
//         String expectedUrl = "http://localhost:5001/garmin/activities?start=0&limit=10";

//         when(restTemplate.getForObject(expectedUrl, String.class))
//                 .thenThrow(new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error"));

//         // When / Then
//         assertThrows(HttpClientErrorException.class, () -> {
//             garminIntegrationService.getGarminActivities(start, limit);
//         });
//     }

//     @Test
//     void downloadActivityFitFile_shouldReturnFitFile_whenSuccessful() {
//         // Given
//         String activityId = "12345";
//         byte[] expectedFitFile = "mock_fit_data".getBytes();
//         String expectedUrl = "http://localhost:5001/garmin/activity/12345/download";

//         when(restTemplate.getForObject(eq(expectedUrl), eq(byte[].class)))
//                 .thenReturn(expectedFitFile);

//         // When
//         byte[] actualFitFile = garminIntegrationService.downloadActivityFitFile(activityId);

//         // Then
//         assertThat(actualFitFile).isEqualTo(expectedFitFile);
//     }

//     @Test
//     void downloadActivityFitFile_shouldThrowActivityDownloadException_whenHttpClientErrorExceptionOccurs() {
//         // Given
//         String activityId = "nonexistent";
//         String expectedUrl = "http://localhost:5001/garmin/activity/nonexistent/download";

//         when(restTemplate.getForObject(eq(expectedUrl), eq(byte[].class)))
//                 .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND, "Not Found"));

//         // When / Then
//         ActivityDownloadException thrown = assertThrows(ActivityDownloadException.class, () -> {
//             garminIntegrationService.downloadActivityFitFile(activityId);
//         });

//         assertThat(thrown.getMessage()).contains("Failed to download FIT file for activity nonexistent");
//         assertThat(thrown.getCause()).isInstanceOf(HttpClientErrorException.class);
//     }

//     @Test
//     void downloadActivityFitFile_shouldThrowActivityDownloadException_whenGenericExceptionOccurs() {
//         // Given
//         String activityId = "error_id";
//         String expectedUrl = "http://localhost:5001/garmin/activity/error_id/download";

//         when(restTemplate.getForObject(eq(expectedUrl), eq(byte[].class)))
//                 .thenThrow(new RuntimeException("Network error"));

//         // When / Then
//         ActivityDownloadException thrown = assertThrows(ActivityDownloadException.class, () -> {
//             garminIntegrationService.downloadActivityFitFile(activityId);
//         });

//         assertThat(thrown.getMessage()).contains("Unexpected error downloading FIT file for activity error_id");
//         assertThat(thrown.getCause()).isInstanceOf(RuntimeException.class);
//     }
// }
