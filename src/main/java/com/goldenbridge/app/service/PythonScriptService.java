package com.goldenbridge.app.service;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

@Service
public class PythonScriptService {

    public String executeHelloScript(String name) {
        StringBuilder output = new StringBuilder();
        StringBuilder errorOutput = new StringBuilder();
        Process process;
        try {
            // Command to execute the Python script inside the Docker container
            // Using ProcessBuilder for more control over process execution
            List<String> command = Arrays.asList(
                "docker-compose",
                "exec",
                "golden-bridge-python",
                "/usr/local/bin/python3",
                "/app/python-scripts/hello.py",
                name
            );

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true); // Redirect error stream to output stream

            process = processBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            int exitVal = process.waitFor();
            if (exitVal == 0) {
                System.out.println("Python script executed successfully!");
            } else {
                System.err.println("Python script execution failed with exit code: " + exitVal);
                // If redirectErrorStream(true) is used, errors are in getInputStream()
                // Otherwise, read from getErrorStream()
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                while ((line = errorReader.readLine()) != null) {
                    errorOutput.append(line).append("\n");
                }
                System.err.println("Error output:" + errorOutput.toString());
                return "Error executing Python script: " + errorOutput.toString();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "Error executing Python script: " + e.getMessage();
        }
        return output.toString();
    }
}
