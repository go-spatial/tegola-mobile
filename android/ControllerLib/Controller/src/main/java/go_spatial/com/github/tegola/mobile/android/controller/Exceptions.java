package go_spatial.com.github.tegola.mobile.android.controller;


public class Exceptions {
    public static class UnsupportedCPUABIException extends Exception {
        public UnsupportedCPUABIException(String message) {
            super(message);
        }
    }
    public static class InvalidTegolaArgumentException extends Exception {
        public InvalidTegolaArgumentException(String message) {
            super(message);
        }
    }
}
