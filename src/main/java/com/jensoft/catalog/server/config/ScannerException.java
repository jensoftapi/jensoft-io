package com.jensoft.catalog.server.config;



public class ScannerException extends RuntimeException {

    /**
     * Construct a new instance with the supplied message
     */
    public ScannerException() {
        super();
    }

    /**
     * Construct a new instance with the supplied message
     * @param message the message
     */
    public ScannerException(String message) {
        super(message);
    }

    /**
     * Construct a new instance with the supplied message and cause
     * @param message the message
     * @param cause the Throwable that caused the exception to be thrown
     */
    public ScannerException(String message, Throwable cause) {
        super(message, cause);
    }
        
    /**
     * Construct a new instance with the supplied cause
     * @param cause the Throwable that caused the exception to be thrown
     */
    public ScannerException(Throwable cause) {
        super(cause);
    }
}
