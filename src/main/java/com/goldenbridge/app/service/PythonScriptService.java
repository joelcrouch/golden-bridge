package com.goldenbridge.app.service;

import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class PythonScriptService {

    private final GarminIntegrationService garminIntegrationService;

    public PythonScriptService(GarminIntegrationService garminIntegrationService) {
        this.garminIntegrationService = garminIntegrationService;
    }

    public String executeHelloScript(String name) {
        return garminIntegrationService.callHello(name);
    }
}
