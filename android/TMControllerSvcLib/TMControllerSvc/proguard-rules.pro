#native/JNI related - BEGIN
-keepclasseswithmembernames,includedescriptorclasses class * {
    native <methods>;
}

-keep class Loader {
    public static void main(...);
}

-keep class go_spatial.com.github.tegola.mobile.android.controller.Exceptions$NativeSignalException
#native/JNI related - END
