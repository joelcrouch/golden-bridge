package com.goldenbridge.app.exception;

public class ActivityDownloadException extends RuntimeException {

    public ActivityDownloadException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public ActivityDownloadException(String message) {
        super(message);
    }
}
