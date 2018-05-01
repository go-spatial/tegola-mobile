package go_spatial.com.github.tegola.mobile.android.controller;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class TMApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
            nm.createNotificationChannel(
                    new NotificationChannel(
                            Constants.ASNB_NOTIFICATIONS.NOTIFICATION_CHANNEL_ID__CONTROLLER_SERVICE
                            , Constants.ASNB_NOTIFICATIONS.NOTIFICATION_CHANNEL_ID__CONTROLLER_SERVICE
                            , NotificationManager.IMPORTANCE_DEFAULT
                    )
            );
        }
    }
}
