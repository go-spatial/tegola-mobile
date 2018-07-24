#-injars      bin/classes
-injars      libs
-outjars     bin/classes-processed.jar

# preverify is apparently needed for Java 7+
#-dontpreverify
-repackageclasses ''
-allowaccessmodification
-optimizations !code/simplification/arithmetic
#-optimizations !code/simplification/arithmetic,!field/*,!class/merging*/
-keepattributes *Annotation*

-optimizationpasses 5
-dump class_files.txt
-printseeds seeds.txt
-printusage unused.txt
-printmapping mapping.txt

#-assumenosideeffects class android.util.Log {
#   public static boolean isLoggable(java.lang.String, int);
#   public static int v(...);
#   public static int i(...);
#   #keep log warnings
#   #public static int w(...);
#   public static int d(...);
#   #keep log errors
#   #public static int e(...);
#}

-keep public class * extends android.app.Activity
-keep public class * extends android.support.v7.app.AppCompatActivity
-keep public class * extends android.support.v4.app.Fragment
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

-keep public class * extends android.view.View {
      public <init>(android.content.Context);
      public <init>(android.content.Context, android.util.AttributeSet);
      public <init>(android.content.Context, android.util.AttributeSet, int);
      public void set*(...);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembers class * extends android.content.Context {
    public void *(android.view.View);
    public void *(android.view.MenuItem);
}

-keepclassmembers class * implements android.os.Parcelable {
    static ** CREATOR;
}

-keepclassmembers class **.R$* {
    public static <fields>;
}

-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-dontwarn android.support.**

#native/JNI related - BEGIN
-keepclasseswithmembernames,includedescriptorclasses class * {
    native <methods>;
}

-keep class Loader {
    public static void main(...);
}

-keep class go_spatial.com.github.tegola.mobile.android.controller.Exceptions$NativeSignalException
#native/JNI related - END

-keep,includedescriptorclasses class com.mapbox.geojson.** { *; }
-dontwarn com.mapbox.geojson.**

-keep class com.github.aakira.expandablelayout.ExpandableLayoutListener
-keep class okio.BufferedSink
-keep class android.arch.lifecycle.**
-keep class com.mapbox.android.core.location.LocationEnginePriority

-keep,includedescriptorclasses class com.google.gson.** { *; }