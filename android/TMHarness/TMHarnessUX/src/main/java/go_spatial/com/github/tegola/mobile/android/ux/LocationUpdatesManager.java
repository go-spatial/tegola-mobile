package go_spatial.com.github.tegola.mobile.android.ux;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.ArrayList;

//based on source from https://en.proft.me/2017/04/17/how-get-location-latitude-longitude-gps-android/
public class LocationUpdatesManager {
    private final String TAG = LocationUpdatesManager.class.getCanonicalName();

    private final static int ALL_PERMISSIONS_RESULT = 101;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1; //in meters
    private static final long MIN_TIME_BW_UPDATES = 500;  //in milliseconds

    private static LocationUpdatesManager m_this;

    private LocationManager locationManager;
    private Location loc;
    private final ArrayList<String> permissions = new ArrayList<>();
    private ArrayList<String> permissionsToRequest;
    private final ArrayList<String> permissionsRejected = new ArrayList<>();
    private boolean gps_enabled = false;
    private boolean network_enabled = false;
    private boolean canGetLocation;

    public interface LocationUpdatesBrokerListener {
        void onBrokerLocationUpdate(Location location);
    }
    private final LocationUpdatesBrokerListener location_update_broker_listener;

    public static abstract class LocationUpdatesBrokerActivity extends AppCompatActivity implements LocationListener {
        private final String TAG = LocationUpdatesBrokerActivity.class.getSimpleName();

        @TargetApi(Build.VERSION_CODES.M)
        @Override
        public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
            switch (requestCode) {
                case ALL_PERMISSIONS_RESULT:
                    Log.d(TAG, "onRequestPermissionsResult");
                    for (String perms : getInstance().permissionsToRequest) {
                        if (!getInstance().hasPermission(perms))
                            getInstance().permissionsRejected.add(perms);
                    }

                    if (getInstance().permissionsRejected.size() > 0) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (shouldShowRequestPermissionRationale(getInstance().permissionsRejected.get(0))) {
                                getInstance().showMessageOKCancel(
                                    "These permissions are mandatory for the application. Please allow access.",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                                                requestPermissions(getInstance().permissionsRejected.toArray(new String[getInstance().permissionsRejected.size()]), ALL_PERMISSIONS_RESULT);
                                        }
                                    }
                                );
                                return;
                            }
                        }
                    } else {
                        Log.d(TAG, "No rejected permissions.");
                        getInstance().canGetLocation = true;
                        getInstance().getLocation();
                    }
                    break;
            }
        }

        @Override
        public void onLocationChanged(Location location) {
            Log.d(TAG, "onLocationChanged");
            if (getInstance().location_update_broker_listener != null)
                getInstance().location_update_broker_listener.onBrokerLocationUpdate(location);
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {}

        @Override
        public void onProviderEnabled(String s) {
            getInstance().getLocation();
        }

        @Override
        public void onProviderDisabled(String s) {
            if (getInstance().locationManager != null) {
                getInstance().locationManager.removeUpdates(this);
            }
        }
    }
    private final LocationUpdatesBrokerActivity location_update_broker_activity;

    private LocationUpdatesManager(final LocationUpdatesBrokerActivity location_update_broker_activity, final LocationUpdatesBrokerListener location_update_broker_listener) {
        this.location_update_broker_activity = location_update_broker_activity;
        this.location_update_broker_listener = location_update_broker_listener;
        locationManager = (LocationManager) location_update_broker_activity.getSystemService(Service.LOCATION_SERVICE);
        gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    public static LocationUpdatesManager newInstance(@NonNull final LocationUpdatesBrokerActivity location_update_broker_activity, final LocationUpdatesBrokerListener location_update_broker_listener) {
        if (m_this != null)
            m_this.stop_updates();
        return (m_this = new LocationUpdatesManager(location_update_broker_activity, location_update_broker_listener));
    }

    public static LocationUpdatesManager getInstance() {
        return m_this;
    }

    public void start_updates() {
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        permissionsToRequest = findUnAskedPermissions(permissions);

        if (!gps_enabled && !network_enabled) {
            Log.d(TAG, "Connection off");
            showSettingsAlert();
            getLastLocation();
        } else {
            Log.d(TAG, "Connection on");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (permissionsToRequest.size() > 0) {
                    location_update_broker_activity.requestPermissions(permissionsToRequest.toArray(new String[permissionsToRequest.size()]), ALL_PERMISSIONS_RESULT);
                    Log.d(TAG, "Permission requests");
                    canGetLocation = false;
                } else
                    canGetLocation = true;
            }

            // get location
            getLocation();
        }
    }

    private void getLocation() {
        try {
            if (canGetLocation) {
                Log.d(TAG, "Can get location");
                if (gps_enabled) {
                    // from GPS
                    Log.d(TAG, "GPS on");
                    locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES,
                            location_update_broker_activity
                    );

                    if (locationManager != null) {
                        loc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (loc != null)
                            location_update_broker_activity.onLocationChanged(loc);
                    }
                } else if (network_enabled) {
                    // from Network Provider
                    Log.d(TAG, "NETWORK_PROVIDER on");
                    locationManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES,
                            location_update_broker_activity
                    );

                    if (locationManager != null) {
                        loc = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (loc != null)
                            location_update_broker_activity.onLocationChanged(loc);
                    }
                } else {
                    loc.setLatitude(0);
                    loc.setLongitude(0);
                    location_update_broker_activity.onLocationChanged(loc);
                }
            } else {
                Log.d(TAG, "Can't get location");
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void getLastLocation() {
        try {
            Criteria criteria = new Criteria();
            String provider = locationManager.getBestProvider(criteria, false);
            Location location = locationManager.getLastKnownLocation(provider);
            Log.d(TAG, provider);
            Log.d(TAG, location == null ? "NO LastLocation" : location.toString());
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private ArrayList findUnAskedPermissions(ArrayList<String> wanted) {
        ArrayList result = new ArrayList();

        for (String perm : wanted) {
            if (!hasPermission(perm)) {
                result.add(perm);
            }
        }

        return result;
    }

    private boolean hasPermission(String permission) {
        if (canAskPermission()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return (location_update_broker_activity.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
            }
        }
        return true;
    }

    private boolean canAskPermission() {
        return (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1);
    }

    public void showSettingsAlert() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(location_update_broker_activity);
        alertDialog.setTitle("GPS is not Enabled!");
        alertDialog.setMessage("Do you want to turn on GPS?");
        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                location_update_broker_activity.startActivity(intent);
            }
        });

        alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        alertDialog.show();
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(location_update_broker_activity)
            .setMessage(message)
            .setPositiveButton("OK", okListener)
            .setNegativeButton("Cancel", null)
            .create()
            .show();
    }

    public void stop_updates() {
        if (locationManager != null)
            locationManager.removeUpdates(location_update_broker_activity);
    }
}
