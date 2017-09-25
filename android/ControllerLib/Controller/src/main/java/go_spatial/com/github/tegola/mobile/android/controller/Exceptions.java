package go_spatial.com.github.tegola.mobile.android.controller;


public class Exceptions {
    public static class UnsupportedCPUABIException extends Exception {
        public UnsupportedCPUABIException(final String message) {
            super(message);
        }
    }
    public static class InvalidTegolaArgumentException extends Exception {
        public InvalidTegolaArgumentException(final String message) {
            super(message);
        }
    }

    //used in jni tcs_native_aux_supp.c
    public static class NativeSignalException extends Exception {
        public NativeSignalException(final String s_signame_from_native) {
            super(s_signame_from_native);
        }
    }
}
