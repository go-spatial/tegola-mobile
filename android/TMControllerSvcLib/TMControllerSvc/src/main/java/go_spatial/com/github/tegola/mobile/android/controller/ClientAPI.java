package go_spatial.com.github.tegola.mobile.android.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.util.Log;

final public class ClientAPI {
    final static private String TAG = ClientAPI.class.getCanonicalName();

    public interface ControllerNotificationsListener {
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

    //client factory methods...
    static final public Client initClient(@NonNull final Context context, @NonNull final ControllerNotificationsListener controllerNotificationsListener, final Handler rcvr_handler) {
        Client client = new Client();
        client.context = context;   //beware that using the same context for multiple client instance can cause duplicate notifications send to controllerNotificationsListener
        client.controllerNotificationsListener = controllerNotificationsListener;
        client.controllerNotificationsBroadcastReceiver = new Client.ControllerNotificationsBroadcastReceiver(client.controllerNotificationsListener);
        if (rcvr_handler == null) {
            Log.d(TAG, "initClient: starting client.rcvr_hndlr_wrkr_thrd");
            client.rcvr_hndlr_wrkr_thrd = new HandlerThread("Thread_BroadcastReceiver_CtrlrNotifications");
            client.rcvr_hndlr_wrkr_thrd.start();
            client.rcvr_hndlr = new Handler(client.rcvr_hndlr_wrkr_thrd.getLooper());
        } else
            client.rcvr_hndlr = rcvr_handler;
        Log.d(TAG, "initClient: client.context.registerReceiver(client.controllerNotificationsBroadcastReceiver)");
        client.registerReceiverStickyIntent = client.context.registerReceiver(
                client.controllerNotificationsBroadcastReceiver,
                client.controllerNotificationsBroadcastReceiver.getDefaultIntentFilter(),
                null,
                client.rcvr_hndlr
        );
        Log.d(TAG, "initClient: client.registerReceiverStickyIntent: " + (client.registerReceiverStickyIntent != null ? client.registerReceiverStickyIntent.getAction() : "<null>"));
        return client;
    }
    static final public Client initClient(@NonNull final Context context, @NonNull final ControllerNotificationsListener controllerNotificationsListener) {
        return initClient(context, controllerNotificationsListener, null);
    }

    static final public void uninitClient(@NonNull Client client) {
        Log.d(TAG, "uninitClient: unregisterReceiver(client.client.controllerNotificationsBroadcastReceiver)");
        client.context.unregisterReceiver(client.controllerNotificationsBroadcastReceiver);
        if (client.rcvr_hndlr_wrkr_thrd != null) {
            Log.d(TAG, "uninitClient: stopping client.rcvr_hndlr_wrkr_thrd...");
            client.rcvr_hndlr_wrkr_thrd.getLooper().quit();
            client.rcvr_hndlr_wrkr_thrd.interrupt();
            try {
                client.rcvr_hndlr_wrkr_thrd.join(1000);
            } catch (InterruptedException e) {
                //e.printStackTrace();
            }
            Log.d(TAG, "uninitClient: client.rcvr_hndlr_wrkr_thrd stopped");
        }
    }


    final static public class Client {
        private Context context = null;
        public Context getContext() {return context;}

        private ControllerNotificationsBroadcastReceiver controllerNotificationsBroadcastReceiver = null;
        public ControllerNotificationsBroadcastReceiver getControllerNotificationsBroadcastReceiver() {return controllerNotificationsBroadcastReceiver;}

        private Intent registerReceiverStickyIntent = null;
        public Intent getRegisterReceiverStickyIntent() {return registerReceiverStickyIntent;}

        private ControllerNotificationsListener controllerNotificationsListener = null;
        public ControllerNotificationsListener getNotificationListener() {return controllerNotificationsListener;}

        private HandlerThread rcvr_hndlr_wrkr_thrd = null;
        public HandlerThread getRcvr_hndlr_wrkr_thrd() {return rcvr_hndlr_wrkr_thrd;}

        private Handler rcvr_hndlr = null;
        public Handler getRcvr_hndlr() {return rcvr_hndlr;}

        private Client() {}

        final public void controller__start(@NonNull final String client_class_name) {
            Intent intent_start_controller_fgs = new Intent(context, FGS.class);
            intent_start_controller_fgs.setAction(Constants.Strings.INTENT.ACTION.REQUEST.FGS.COMMAND.START.STRING);
            intent_start_controller_fgs.putExtra(Constants.Strings.INTENT.ACTION.REQUEST.FGS.COMMAND.START.EXTRA_KEY.HARNESS_CLASS_NAME.STRING, client_class_name);
            context.startService(intent_start_controller_fgs);
        }

        final public void mvt_server__start(@NonNull FGS.MVT_SERVER_START_SPEC__GPKG_PROVIDER start_spec__gpkg_provider) {
            Intent intent_start_mvt_server = new Intent(Constants.Strings.INTENT.ACTION.REQUEST.MVT_SERVER.CONTROL.START.STRING);
            String s_config_toml = null;
            intent_start_mvt_server.putExtra(Constants.Strings.INTENT.ACTION.REQUEST.MVT_SERVER.CONTROL.START.EXTRA__KEY.PROVIDER.GPKG.STRING, true);
            intent_start_mvt_server.putExtra(
                Constants.Strings.INTENT.ACTION.REQUEST.MVT_SERVER.CONTROL.START.EXTRA__KEY.PROVIDER.GPKG.BUNDLE.STRING,
                start_spec__gpkg_provider.gpkg_bundle
            );
            intent_start_mvt_server.putExtra(
                Constants.Strings.INTENT.ACTION.REQUEST.MVT_SERVER.CONTROL.START.EXTRA__KEY.PROVIDER.GPKG.BUNDLE.PROPS.STRING,
                start_spec__gpkg_provider.gpkg_bundle_props
            );
            intent_start_mvt_server.putExtra(Constants.Strings.INTENT.ACTION.REQUEST.MVT_SERVER.CONTROL.START.EXTRA__KEY.CONFIG.PATH.STRING, s_config_toml);
            context.sendBroadcast(intent_start_mvt_server);
        }
        final public void mvt_server__start(@NonNull FGS.MVT_SERVER_START_SPEC__POSTGIS_PROVIDER start_spec__postgis_provider) {
            Intent intent_start_mvt_server = new Intent(Constants.Strings.INTENT.ACTION.REQUEST.MVT_SERVER.CONTROL.START.STRING);
            intent_start_mvt_server.putExtra(Constants.Strings.INTENT.ACTION.REQUEST.MVT_SERVER.CONTROL.START.EXTRA__KEY.PROVIDER.GPKG.STRING, false);
            intent_start_mvt_server.putExtra(Constants.Strings.INTENT.ACTION.REQUEST.MVT_SERVER.CONTROL.START.EXTRA__KEY.CONFIG.REMOTE.STRING, start_spec__postgis_provider.config_toml__is_remote);
            intent_start_mvt_server.putExtra(Constants.Strings.INTENT.ACTION.REQUEST.MVT_SERVER.CONTROL.START.EXTRA__KEY.CONFIG.PATH.STRING, start_spec__postgis_provider.config_toml);
            context.sendBroadcast(intent_start_mvt_server);
        }

        final public void mvt_server__query_state__is_running() {
            Intent intent_query_mvt_server_is_running = new Intent(Constants.Strings.INTENT.ACTION.REQUEST.MVT_SERVER.STATE.IS_RUNNING.STRING);
            context.sendBroadcast(intent_query_mvt_server_is_running);
        }

        final public void mvt_server__query_state__listen_port() {
            Intent intent_query_mvt_server_listen_port = new Intent(Constants.Strings.INTENT.ACTION.REQUEST.MVT_SERVER.STATE.LISTEN_PORT.STRING);
            context.sendBroadcast(intent_query_mvt_server_listen_port);
        }

        final public void mvt_server__rest_api__get_json(String root_url, String json_endpoint, String purpose) {
            Intent intent_mvt_server__rest_api__get_json = new Intent(Constants.Strings.INTENT.ACTION.REQUEST.MVT_SERVER.REST_API.GET_JSON.STRING);
            if (root_url != null && !root_url.trim().isEmpty()) {
                intent_mvt_server__rest_api__get_json.putExtra(
                    Constants.Strings.INTENT.ACTION.REQUEST.MVT_SERVER.REST_API.GET_JSON.EXTRA_KEY.ROOT_URL.STRING,
                    root_url
                );
            }
            if (json_endpoint != null && !json_endpoint.trim().isEmpty()) {
                intent_mvt_server__rest_api__get_json.putExtra(
                    Constants.Strings.INTENT.ACTION.REQUEST.MVT_SERVER.REST_API.GET_JSON.EXTRA_KEY.ENDPOINT.STRING,
                    json_endpoint
                );
            }
            if (purpose != null && !purpose.trim().isEmpty()) {
                intent_mvt_server__rest_api__get_json.putExtra(
                    Constants.Strings.INTENT.ACTION.REQUEST.MVT_SERVER.REST_API.GET_JSON.EXTRA_KEY.PURPOSE.STRING,
                    purpose
                );
            }
            context.sendBroadcast(intent_mvt_server__rest_api__get_json);
        }
        final public void mvt_server__get_capabilities(String purpose) {
            mvt_server__rest_api__get_json(
                null,   //null will direct FGS to read from running instance of local mvt server
                null,   //null defaults to "/capabilities"
                purpose != null && !purpose.trim().isEmpty()
                    ? purpose
                    : Constants.Strings.INTENT.ACTION.REQUEST.MVT_SERVER.REST_API.GET_JSON.EXTRA_KEY.PURPOSE.VALUE.LOAD_MAP.STRING
            );
        }

        final public void mvt_server__stop() {
            Intent intent_stop_mvt_server = new Intent(Constants.Strings.INTENT.ACTION.REQUEST.MVT_SERVER.CONTROL.STOP.STRING);
            context.sendBroadcast(intent_stop_mvt_server);
        }

        final public void controller__stop() {
            Intent intent_stop_controller_fgs = new Intent(context, FGS.class);
            intent_stop_controller_fgs.setAction(Constants.Strings.INTENT.ACTION.REQUEST.FGS.COMMAND.STOP.STRING);
            context.stopService(intent_stop_controller_fgs);
        }

        static final private class ControllerNotificationsBroadcastReceiver extends BroadcastReceiver {
            final private String TAG = ControllerNotificationsBroadcastReceiver.class.getCanonicalName();

            final private ControllerNotificationsListener controllerNotificationsListener;

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

            public ControllerNotificationsBroadcastReceiver(@NonNull final ControllerNotificationsListener controllerNotificationsListener) {
                this.controllerNotificationsListener = controllerNotificationsListener;
            }

            @Override
            final public void onReceive(Context context, Intent intent) {
                if (intent != null) {
                    Log.d(TAG, String.format("onReceive: %s", intent.getAction()));
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
                            Log.e(TAG, String.format("onReceive: %s but NO HANDLER IS DEFINED!", intent.getAction()));
                            break;
                        }
                    }
                } else {
                    Log.e(TAG, "onReceive: null intent!");
                }
            }
        }
    }
}
