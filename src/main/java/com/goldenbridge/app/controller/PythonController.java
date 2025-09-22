package com.goldenbridge.app.controller;

import com.goldenbridge.app.service.PythonScriptService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/python")
public class PythonController {

    private final PythonScriptService pythonScriptService;

    public PythonController(PythonScriptService pythonScriptService) {
        this.pythonScriptService = pythonScriptService;
    }

    @GetMapping("/hello")
    public ResponseEntity<String> runHelloScript(@RequestParam(defaultValue = "World") String name) {
        String result = pythonScriptService.executeHelloScript(name);
        return ResponseEntity.ok(result);
    }
}
