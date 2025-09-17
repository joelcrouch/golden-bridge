package com.goldenbridge.app.exception;

public class PythonScriptException extends RuntimeException {

    private final int exitCode;
    private final String errorOutput;

    public PythonScriptException(String message, int exitCode, String errorOutput) {
        super(message);
        this.exitCode = exitCode;
        this.errorOutput = errorOutput;
    }

    public int getExitCode() {
        return exitCode;
    }

    public String getErrorOutput() {
        return errorOutput;
    }
}
