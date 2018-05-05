package go_spatial.com.github.tegola.mobile.android.ux;

import go_spatial.com.github.tegola.mobile.android.controller.TMApp;
import com.mapbox.mapboxsdk.Mapbox;

public class TMHarnessApp extends TMApp {
    public Mapbox mapbox = null;
    @Override
    public void onCreate() {
        super.onCreate();

        // Mapbox Access token
        Mapbox.getInstance(getApplicationContext(), BuildConfig.mbgl_api_access_token);
    }
}
