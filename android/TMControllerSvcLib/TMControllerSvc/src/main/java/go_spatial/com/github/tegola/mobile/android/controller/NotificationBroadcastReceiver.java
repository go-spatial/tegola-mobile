package go_spatial.com.github.tegola.mobile.android.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.util.Log;

final public class NotificationBroadcastReceiver extends BroadcastReceiver {
    final private String TAG = NotificationBroadcastReceiver.class.getCanonicalName();

    final private ClientAPI.ControllerNotificationsListener controllerNotificationsListener;

    private final IntentFilter default_intent_filter = new IntentFilter() {{
        addAction(Constants.Strings.INTENT.ACTION.NOTIFICATION.FGS.STATE.STARTING.STRING);
        addAction(Constants.Strings.INTENT.ACTION.NOTIFICATION.FGS.STATE.RUNNING.STRING);
        addAction(Constants.Strings.INTENT.ACTION.NOTIFICATION.FGS.STATE.STOPPING.STRING);
        addAction(Constants.Strings.INTENT.ACTION.NOTIFICATION.FGS.STATE.STOPPED.STRING);
        addAction(Constants.Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.STATE.STARTING.STRING);
        addAction(Constants.Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.STATE.START_FAILED.STRING);
        addAction(Constants.Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.STATE.RUNNING.STRING);
        addAction(Constants.Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.STATE.LISTENING.STRING);
        addAction(Constants.Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.MONITOR.LOGCAT.OUTPUT.STRING);
        addAction(Constants.Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.MONITOR.STDERR.OUTPUT.STRING);
        addAction(Constants.Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.MONITOR.STDOUT.OUTPUT.STRING);
        addAction(Constants.Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.HTTP_URL_API.GOT_JSON.STRING);
        addAction(Constants.Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.HTTP_URL_API.GET_JSON_FAILED.STRING);
        addAction(Constants.Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.STATE.STOPPING.STRING);
        addAction(Constants.Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.STATE.STOPPED.STRING);
    }};
    final public IntentFilter getDefaultIntentFilter() {return default_intent_filter;}

    public NotificationBroadcastReceiver(@NonNull final ClientAPI.ControllerNotificationsListener controllerNotificationsListener) {
        this.controllerNotificationsListener = controllerNotificationsListener;
    }

    @Override
    final public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            Log.d(TAG, "onReceive: " + intent.getAction());
            Constants.Enums.E_INTENT_ACTION__NOTIFICATION e_ctrlr_notification = Constants.Enums.E_INTENT_ACTION__NOTIFICATION.fromString(intent != null ? intent.getAction() : null);
            switch (e_ctrlr_notification) {
                case FGS_STATE_STARTING: {
                    controllerNotificationsListener.OnControllerStarting();
                    break;
                }
                case FGS_STATE_RUNNING: {
                    controllerNotificationsListener.OnControllerRunning();
                    break;
                }
                case FGS_STATE_STOPPING: {
                    controllerNotificationsListener.OnControllerStopping();
                    break;
                }
                case FGS_STATE_STOPPED: {
                    controllerNotificationsListener.OnControllerStopped();
                    break;
                }
                case MVT_SERVER_STATE_STARTING: {
                    controllerNotificationsListener.OnMVTServerStarting();
                    break;
                }
                case MVT_SERVER_STATE_START_FAILED: {
                    controllerNotificationsListener.OnMVTServerStartFailed(intent.getStringExtra(Constants.Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.STATE.START_FAILED.EXTRA_KEY.REASON.STRING));
                    break;
                }
                case MVT_SERVER_STATE_RUNNING: {
                    controllerNotificationsListener.OnMVTServerRunning(intent.getIntExtra(Constants.Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.STATE.RUNNING.EXTRA_KEY.PID.STRING, -1));
                    break;
                }
                case MVT_SERVER_STATE_LISTENING: {
                    controllerNotificationsListener.OnMVTServerListening(intent.getIntExtra(Constants.Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.STATE.LISTENING.EXTRA_KEY.PORT.STRING, 8080));
                    break;
                }
                case MVT_SERVER_MONITOR_LOGCAT_OUTPUT: {
                    controllerNotificationsListener.OnMVTServerOutputLogcat(intent.getStringExtra(Constants.Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.MONITOR.LOGCAT.OUTPUT.EXTRA_KEY.LINE.STRING));
                    break;
                }
                case MVT_SERVER_MONITOR_STDERR_OUTPUT: {
                    controllerNotificationsListener.OnMVTServerOutputStdErr(intent.getStringExtra(Constants.Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.MONITOR.STDERR.OUTPUT.EXTRA_KEY.LINE.STRING));
                    break;
                }
                case MVT_SERVER_MONITOR_STDOUT_OUTPUT: {
                    controllerNotificationsListener.OnMVTServerOutputStdOut(intent.getStringExtra(Constants.Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.MONITOR.STDOUT.OUTPUT.EXTRA_KEY.LINE.STRING));
                    break;
                }
                case MVT_SERVER_HTTP_URL_API_GOT_JSON: {
                    controllerNotificationsListener.OnMVTServerJSONRead(
                        intent.getStringExtra(Constants.Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.HTTP_URL_API.GOT_JSON.EXTRA_KEY.ROOT_URL.STRING),
                        intent.getStringExtra(Constants.Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.HTTP_URL_API.GOT_JSON.EXTRA_KEY.ENDPOINT.STRING),
                        intent.getStringExtra(Constants.Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.HTTP_URL_API.GOT_JSON.EXTRA_KEY.CONTENT.STRING),
                        intent.getStringExtra(Constants.Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.HTTP_URL_API.GOT_JSON.EXTRA_KEY.PURPOSE.STRING)
                    );
                    break;
                }
                case MVT_SERVER_HTTP_URL_API_GET_JSON_FAILED: {
                    controllerNotificationsListener.OnMVTServerJSONReadFailed(
                        intent.getStringExtra(Constants.Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.HTTP_URL_API.GET_JSON_FAILED.EXTRA_KEY.ROOT_URL.STRING),
                        intent.getStringExtra(Constants.Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.HTTP_URL_API.GET_JSON_FAILED.EXTRA_KEY.ENDPOINT.STRING),
                        intent.getStringExtra(Constants.Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.HTTP_URL_API.GET_JSON_FAILED.EXTRA_KEY.PURPOSE.STRING),
                        intent.getStringExtra(Constants.Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.HTTP_URL_API.GET_JSON_FAILED.EXTRA_KEY.REASON.STRING)
                    );
                    break;
                }
                case MVT_SERVER_STATE_STOPPING: {
                    controllerNotificationsListener.OnMVTServerStopping();
                    break;
                }
                case MVT_SERVER_STATE_STOPPED: {
                    controllerNotificationsListener.OnMVTServerStopped();
                    break;
                }
                default: {
                    Log.e(TAG, "onReceive: " + intent.getAction() + " but NO HANDLER IS DEFINED!");
                    break;
                }
            }
        } else {
            Log.e(TAG, "onReceive: null intent!");
        }
    }
}
