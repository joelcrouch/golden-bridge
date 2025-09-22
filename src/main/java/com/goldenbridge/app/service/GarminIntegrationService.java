package com.goldenbridge.app.service;

import com.goldenbridge.app.dto.GarminLoginRequest;
import com.goldenbridge.app.dto.GarminLoginResponse;
import com.goldenbridge.app.dto.GarminLogoutResponse;
import com.goldenbridge.app.dto.GarminStatusResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class GarminIntegrationService {

    private final RestTemplate restTemplate;
    private final String pythonServiceBaseUrl;

    public GarminIntegrationService(RestTemplate restTemplate, @Value("${python.service.base-url}") String pythonServiceBaseUrl) {
        this.restTemplate = restTemplate;
        this.pythonServiceBaseUrl = pythonServiceBaseUrl;
    }

    public String callHello(String name) {
        String url = UriComponentsBuilder.fromHttpUrl(pythonServiceBaseUrl)
                .path("/hello")
                .queryParam("name", name)
                .toUriString();

        return restTemplate.getForObject(url, String.class);
    }

    public GarminLoginResponse loginToGarmin(GarminLoginRequest loginRequest) {
        String url = UriComponentsBuilder.fromHttpUrl(pythonServiceBaseUrl)
                .path("/garmin/login")
                .toUriString();

        try {
            return restTemplate.postForObject(url, loginRequest, GarminLoginResponse.class);
        } catch (HttpClientErrorException e) {
            // The Python service returns a 401 on login failure, which throws this exception.
            // We can return a custom response or re-throw a custom exception.
            return new GarminLoginResponse("error", "Failed to login to Garmin: " + e.getResponseBodyAsString());
        }
    }

    public GarminStatusResponse getGarminStatus() {
        String url = UriComponentsBuilder.fromHttpUrl(pythonServiceBaseUrl)
                .path("/garmin/status")
                .toUriString();

        return restTemplate.getForObject(url, GarminStatusResponse.class);
    }

    public GarminLogoutResponse logoutFromGarmin() {
        String url = UriComponentsBuilder.fromHttpUrl(pythonServiceBaseUrl)
                .path("/garmin/logout")
                .toUriString();

        return restTemplate.postForObject(url, null, GarminLogoutResponse.class);
    }

    public String getGarminActivities(int start, int limit) {
        String url = UriComponentsBuilder.fromHttpUrl(pythonServiceBaseUrl)
                .path("/garmin/activities")
                .queryParam("start", start)
                .queryParam("limit", limit)
                .toUriString();

        return restTemplate.getForObject(url, String.class);
    }
}
