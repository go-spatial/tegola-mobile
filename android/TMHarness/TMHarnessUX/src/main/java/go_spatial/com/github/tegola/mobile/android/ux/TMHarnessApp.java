package go_spatial.com.github.tegola.mobile.android.ux;

import go_spatial.com.github.tegola.mobile.android.controller.TMApp;

import com.mapbox.android.telemetry.MapboxTelemetry;
import com.mapbox.mapboxsdk.Mapbox;

public class TMHarnessApp extends TMApp {

    @Override
    public void onCreate() {
        super.onCreate();

        // Mapbox Access token
        Mapbox.getInstance(getApplicationContext(), BuildConfig.mbglapi_access_token);
    }
}
