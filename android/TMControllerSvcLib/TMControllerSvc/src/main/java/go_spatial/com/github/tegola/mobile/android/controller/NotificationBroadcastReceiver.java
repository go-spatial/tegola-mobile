package go_spatial.com.github.tegola.mobile.android.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.util.Log;

final public class NotificationBroadcastReceiver extends BroadcastReceiver {
    final private String TAG = NotificationBroadcastReceiver.class.getCanonicalName();

    public interface Listener {
        void OnControllerStarting();
        void OnControllerRunning();
        void OnControllerStopping();
        void OnControllerStopped();
        void OnMVTServerStarting();
        void OnMVTServerStartFailed(@NonNull final String reason);
        void OnMVTServerRunning(final int pid);
        void OnMVTServerListening(final int port);
        void OnMVTServerOutputLogcat(@NonNull final String logcat_line);
        void OnMVTServerOutputStdErr(@NonNull final String stderr_line);
        void OnMVTServerOutputStdOut(@NonNull final String stdout_line);
        void OnMVTServerJSONRead(@NonNull final String tegola_url_root, @NonNull final String json_url_endpoint, @NonNull final String json, @NonNull final String purpose);
        void OnMVTServerJSONReadFailed(@NonNull final String tegola_url_root, @NonNull final String json_url_endpoint, @NonNull final String purpose, @NonNull final String reason);
        void OnMVTServerStopping();
        void OnMVTServerStopped();
    }
    final private Listener listener;

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

    public NotificationBroadcastReceiver(@NonNull final Listener listener) {
        this.listener = listener;
    }

    @Override
    final public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            Log.d(TAG, "onReceive: " + intent.getAction());
            Constants.Enums.E_INTENT_ACTION__NOTIFICATION e_ctrlr_notification = Constants.Enums.E_INTENT_ACTION__NOTIFICATION.fromString(intent != null ? intent.getAction() : null);
            switch (e_ctrlr_notification) {
                case FGS_STATE_STARTING: {
                    listener.OnControllerStarting();
                    break;
                }
                case FGS_STATE_RUNNING: {
                    listener.OnControllerRunning();
                    break;
                }
                case FGS_STATE_STOPPING: {
                    listener.OnControllerStopping();
                    break;
                }
                case FGS_STATE_STOPPED: {
                    listener.OnControllerStopped();
                    break;
                }
                case MVT_SERVER_STATE_STARTING: {
                    listener.OnMVTServerStarting();
                    break;
                }
                case MVT_SERVER_STATE_START_FAILED: {
                    listener.OnMVTServerStartFailed(intent.getStringExtra(Constants.Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.STATE.START_FAILED.EXTRA_KEY.REASON.STRING));
                    break;
                }
                case MVT_SERVER_STATE_RUNNING: {
                    listener.OnMVTServerRunning(intent.getIntExtra(Constants.Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.STATE.RUNNING.EXTRA_KEY.PID.STRING, -1));
                    break;
                }
                case MVT_SERVER_STATE_LISTENING: {
                    listener.OnMVTServerListening(intent.getIntExtra(Constants.Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.STATE.LISTENING.EXTRA_KEY.PORT.STRING, 8080));
                    break;
                }
                case MVT_SERVER_MONITOR_LOGCAT_OUTPUT: {
                    listener.OnMVTServerOutputLogcat(intent.getStringExtra(Constants.Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.MONITOR.LOGCAT.OUTPUT.EXTRA_KEY.LINE.STRING));
                    break;
                }
                case MVT_SERVER_MONITOR_STDERR_OUTPUT: {
                    listener.OnMVTServerOutputStdErr(intent.getStringExtra(Constants.Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.MONITOR.STDERR.OUTPUT.EXTRA_KEY.LINE.STRING));
                    break;
                }
                case MVT_SERVER_MONITOR_STDOUT_OUTPUT: {
                    listener.OnMVTServerOutputStdOut(intent.getStringExtra(Constants.Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.MONITOR.STDOUT.OUTPUT.EXTRA_KEY.LINE.STRING));
                    break;
                }
                case MVT_SERVER_HTTP_URL_API_GOT_JSON: {
                    listener.OnMVTServerJSONRead(
                        intent.getStringExtra(Constants.Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.HTTP_URL_API.GOT_JSON.EXTRA_KEY.ROOT_URL.STRING),
                        intent.getStringExtra(Constants.Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.HTTP_URL_API.GOT_JSON.EXTRA_KEY.ENDPOINT.STRING),
                        intent.getStringExtra(Constants.Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.HTTP_URL_API.GOT_JSON.EXTRA_KEY.CONTENT.STRING),
                        intent.getStringExtra(Constants.Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.HTTP_URL_API.GOT_JSON.EXTRA_KEY.PURPOSE.STRING)
                    );
                    break;
                }
                case MVT_SERVER_HTTP_URL_API_GET_JSON_FAILED: {
                    listener.OnMVTServerJSONReadFailed(
                        intent.getStringExtra(Constants.Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.HTTP_URL_API.GET_JSON_FAILED.EXTRA_KEY.ROOT_URL.STRING),
                        intent.getStringExtra(Constants.Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.HTTP_URL_API.GET_JSON_FAILED.EXTRA_KEY.ENDPOINT.STRING),
                        intent.getStringExtra(Constants.Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.HTTP_URL_API.GET_JSON_FAILED.EXTRA_KEY.PURPOSE.STRING),
                        intent.getStringExtra(Constants.Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.HTTP_URL_API.GET_JSON_FAILED.EXTRA_KEY.REASON.STRING)
                    );
                    break;
                }
                case MVT_SERVER_STATE_STOPPING: {
                    listener.OnMVTServerStopping();
                    break;
                }
                case MVT_SERVER_STATE_STOPPED: {
                    listener.OnMVTServerStopped();
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
