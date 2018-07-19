package go_spatial.com.github.tegola.mobile.android.controller;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Map;


public class FGS extends Service {
    private static final String TAG = FGS.class.getCanonicalName();

    private BroadcastReceiver m_br_client_control_request = null;
    private IntentFilter m_filter_br_client_control_request = null;
    private HandlerThread m_handlerthread_br_client_control_request = null;


    //statically load native libraries here
    static {
        System.loadLibrary("tcsnativeauxsupp"); //for signal trapping - note that this will convert native signals (only three we are interested in) into java exceptions
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private Class<?> m_class_harness = null;
    private boolean m_is_running = false;

    @Override
    public int onStartCommand(Intent intent, /*@IntDef(value = {Service.START_FLAG_REDELIVERY, Service.START_FLAG_RETRY}, flag = true)*/ int flags, int startId) {
        Constants.Enums.E_INTENT_ACTION__REQUEST e_fgs_ctrl_request = Constants.Enums.E_INTENT_ACTION__REQUEST.fromString(intent != null ? intent.getAction() : null);
        if (e_fgs_ctrl_request != null) {
            switch (e_fgs_ctrl_request) {
                case FGS_COMMAND_START: {
                    String s_class_harness = intent.getStringExtra(Constants.Strings.INTENT.ACTION.REQUEST.FGS.COMMAND.START.EXTRA_KEY.HARNESS_CLASS_NAME.STRING);
                    Log.i(TAG, "onStartCommand: Received START request from harness " + s_class_harness);

                    if (!m_is_running) {
                        Intent intent_notify_service_starting = new Intent(Constants.Strings.INTENT.ACTION.NOTIFICATION.FGS.STATE.STARTING.STRING);
                        sendBroadcast(intent_notify_service_starting);

                        try {
                            m_class_harness = Class.forName(s_class_harness);
                            Log.d(TAG, "onStartCommand: mapped class for harness " + s_class_harness);
                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                        Log.d(TAG, "onStartCommand: setting up ASN for FGS...");

                        Log.d(TAG, "onStartCommand: starting FGS...");
                        startForeground(Constants.ASNB_NOTIFICATIONS.NOTIFICATION_ID__CONTROLLER_SERVICE, fgs_asn__prepare(getString(R.string.fgs_asn_title), getString(R.string.stopped)));
                        m_is_running = true;

                        Log.d(TAG, "onStartCommand: ASN dispatched and FGS started - init'ing...");
                        init();

                        m_handlerthread_br_client_control_request = new HandlerThread("Thread_BroadcastReceiver_CliCtrlRequest_TCS");
                        m_handlerthread_br_client_control_request.start();
                        getApplicationContext().registerReceiver(
                            m_br_client_control_request,
                            m_filter_br_client_control_request,
                            null,
                            new Handler(m_handlerthread_br_client_control_request.getLooper())
                        );
                    } else {
                        Log.d(TAG, "onStartCommand: FGS is already running/init'ed - skipping startup sequence");
                    }

                    Log.d(TAG, "onStartCommand: start sequence complete - notifying harness...");
                    Intent intent_notify_service_started = new Intent(Constants.Strings.INTENT.ACTION.NOTIFICATION.FGS.STATE.RUNNING.STRING);
                    sendBroadcast(intent_notify_service_started);
                    break;
                }
                default: {
                    break;
                }
            }
        }
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        try {
            Log.i(TAG, "onDestroy: FGS is being destroyed (stopService) - notifying harness...");
            Intent intent_notify_service_stopping = new Intent(Constants.Strings.INTENT.ACTION.NOTIFICATION.FGS.STATE.STOPPING.STRING);
            sendBroadcast(intent_notify_service_stopping);
            shell__tegola__stop();
            Log.d(TAG, "onDestroy: stopForeground");
            stopForeground(true);
            Log.d(TAG, "onDestroy: stopSelf");
            stopSelf();
            Log.d(TAG, "onDestroy: unregisterReceiver(m_br_client_control_request)");
            getApplicationContext().unregisterReceiver(m_br_client_control_request);
            Log.d(TAG, "onDestroy: stopping m_handlerthread_br_client_control_request...");
            m_handlerthread_br_client_control_request.getLooper().quit();
            m_handlerthread_br_client_control_request.interrupt();
            m_handlerthread_br_client_control_request.join(1000);
        } catch (InterruptedException e) {
            //e.printStackTrace();
        } finally {
            m_is_running = false;
            Log.d(TAG, "onDestroy: controller stopped - notifying harness...");
            Intent intent_notify_service_stopped = new Intent(Constants.Strings.INTENT.ACTION.NOTIFICATION.FGS.STATE.STOPPED.STRING);
            sendBroadcast(intent_notify_service_stopped);
        }
    }

    private Notification fgs_asn__prepare(final String s_title, final String s_status) {
        Intent intent_bring_harness_to_foreground = new Intent(getApplicationContext(), m_class_harness);
        intent_bring_harness_to_foreground.setAction(Intent.ACTION_MAIN);
        intent_bring_harness_to_foreground.addCategory(Intent.CATEGORY_LAUNCHER);
        PendingIntent pending_intent_bring_harness_to_foreground = PendingIntent.getActivity(getApplicationContext(), 0, intent_bring_harness_to_foreground, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder notificationBuilder = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? new NotificationCompat.Builder(this, Constants.ASNB_NOTIFICATIONS.NOTIFICATION_CHANNEL_ID__CONTROLLER_SERVICE) : new NotificationCompat.Builder(this));
        return notificationBuilder
            .setContentTitle(s_title)
            .setContentText(s_status)
            .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
            .setSmallIcon(R.drawable.ic_stat_satellite_black)
            .setContentIntent(pending_intent_bring_harness_to_foreground)
            .setOngoing(true)
            .build();
    }

    private void fgs_asn__update(final String s_title, final String s_status) {
        NotificationManager asn_mgr = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        asn_mgr.notify(Constants.ASNB_NOTIFICATIONS.NOTIFICATION_ID__CONTROLLER_SERVICE, fgs_asn__prepare(s_title, s_status));
    }
    private void fgs_asn__update(final String s_status) {
        NotificationManager asn_mgr = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        asn_mgr.notify(Constants.ASNB_NOTIFICATIONS.NOTIFICATION_ID__CONTROLLER_SERVICE, fgs_asn__prepare(getString(R.string.fgs_asn_title), s_status));
    }

    private void init() {
        //check for existence (in app libs directory) of executable tegola binary for this device ABI
        File f_filesDir = getFilesDir();
        try {
            final Constants.Enums.CPU_ABI e_device_abi = Constants.Enums.CPU_ABI.fromDevice();
            if (e_device_abi == null)
                throw new Exceptions.UnsupportedCPUABIException(Build.CPU_ABI);
            Utils.TEGOLA_BIN.getInstance(getApplicationContext());  //mapping from ABI to tegola bin that supports it is done in the ctor of TEGOLA_BIN classß
            get__tegola_version();
        } catch (Exceptions.UnsupportedCPUABIException e) {
            e.printStackTrace();
        } catch (Exceptions.TegolaBinaryNotExecutableException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        //create geopackage bundle dir
        try {
            File f_gpkg_root_dir = Utils.GPKG.Local.F_GPKG_DIR.getInstance(getApplicationContext());
            if (!f_gpkg_root_dir.exists()) {
                boolean created = f_gpkg_root_dir.mkdirs();
                if (created) {
                    Log.d(TAG, "init: successfully created gepackage-bundle root directory: " + f_gpkg_root_dir.getPath() + " (canonical path: " + f_gpkg_root_dir.getCanonicalPath() + ")");
                } else {
                    Log.d(TAG, "init: failed to create gepackage-bundle root directory: " + f_gpkg_root_dir.getPath());
                }
            }
            Log.d(TAG, "init: gepackage-bundle root directory " + f_gpkg_root_dir.getPath() + " " + (f_gpkg_root_dir.exists() ? "exists" : "DOES NOT EXIST!!!"));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        //set BR to listen for client mvt-server-control-request
        m_filter_br_client_control_request = new IntentFilter();
        m_filter_br_client_control_request.addAction(Constants.Strings.INTENT.ACTION.REQUEST.MVT_SERVER.CONTROL.START.STRING);
        m_filter_br_client_control_request.addAction(Constants.Strings.INTENT.ACTION.REQUEST.MVT_SERVER.CONTROL.STOP.STRING);
        m_filter_br_client_control_request.addAction(Constants.Strings.INTENT.ACTION.REQUEST.MVT_SERVER.REST_API.GET_JSON.STRING);
        m_filter_br_client_control_request.addAction(Constants.Strings.INTENT.ACTION.REQUEST.MVT_SERVER.STATE.IS_RUNNING.STRING);
        m_filter_br_client_control_request.addAction(Constants.Strings.INTENT.ACTION.REQUEST.MVT_SERVER.STATE.LISTEN_PORT.STRING);
        m_br_client_control_request = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent != null) {
                    Log.d(TAG, "m_br_client_control_request received: " + intent.getAction());
                    Constants.Enums.E_INTENT_ACTION__REQUEST eReq = Constants.Enums.E_INTENT_ACTION__REQUEST.fromString(intent.getAction());
                    switch (eReq) {
                        case MVT_SERVER_CONTROL_START: {
                            MVT_SERVER_START_SPEC server_start_spec = null;
                            if (intent.getBooleanExtra(Constants.Strings.INTENT.ACTION.REQUEST.MVT_SERVER.CONTROL.START.EXTRA__KEY.PROVIDER.GPKG.STRING, false))
                                server_start_spec = new MVT_SERVER_START_SPEC__GPKG_PROVIDER(
                                        intent.getStringExtra(Constants.Strings.INTENT.ACTION.REQUEST.MVT_SERVER.CONTROL.START.EXTRA__KEY.PROVIDER.GPKG.BUNDLE.STRING),
                                        intent.getStringExtra(Constants.Strings.INTENT.ACTION.REQUEST.MVT_SERVER.CONTROL.START.EXTRA__KEY.PROVIDER.GPKG.BUNDLE.PROPS.STRING)
                                );
                            else {
                                server_start_spec = new MVT_SERVER_START_SPEC__POSTGIS_PROVIDER(
                                        intent.getBooleanExtra(Constants.Strings.INTENT.ACTION.REQUEST.MVT_SERVER.CONTROL.START.EXTRA__KEY.CONFIG.REMOTE.STRING, false)
                                        , intent.getStringExtra(Constants.Strings.INTENT.ACTION.REQUEST.MVT_SERVER.CONTROL.START.EXTRA__KEY.CONFIG.PATH.STRING)
                                );
                            }
                            handle_mvt_server_control_request__start(server_start_spec);
                            break;
                        }
                        case MVT_SERVER_REST_API_GET_JSON: {
                            String s_purpose = intent.getStringExtra(Constants.Strings.INTENT.ACTION.REQUEST.MVT_SERVER.REST_API.GET_JSON.EXTRA_KEY.PURPOSE.STRING);
                            if (s_purpose == null || s_purpose.isEmpty()) {
                                s_purpose = Constants.Strings.INTENT.ACTION.REQUEST.MVT_SERVER.REST_API.GET_JSON.EXTRA_KEY.PURPOSE.VALUE.LOAD_MAP.STRING;
                                Log.d(TAG, "m_br_client_control_request.onReceive(MVT_SERVER_HTTP_URL_API_GOT_JSON): "
                                    + Constants.Strings.INTENT.ACTION.REQUEST.MVT_SERVER.REST_API.GET_JSON.EXTRA_KEY.PURPOSE.STRING
                                    + " string extra is null or empty - setting s_purpose==\"" + s_purpose + "\""
                                );
                            }
                            String s_root_url = intent.getStringExtra(Constants.Strings.INTENT.ACTION.REQUEST.MVT_SERVER.REST_API.GET_JSON.EXTRA_KEY.ROOT_URL.STRING);
                            if (s_root_url == null || s_root_url.isEmpty()) {
                                s_root_url = "http://localhost:" + m_i_tegola_listen_port;
                                Log.d(TAG, "m_br_client_control_request.onReceive(MVT_SERVER_HTTP_URL_API_GOT_JSON): "
                                        + Constants.Strings.INTENT.ACTION.REQUEST.MVT_SERVER.REST_API.GET_JSON.EXTRA_KEY.ROOT_URL.STRING
                                        + " string extra is null or empty - setting s_root_url==\"" + s_root_url + "\""
                                );
                            }
                            String s_endpoint = intent.getStringExtra(Constants.Strings.INTENT.ACTION.REQUEST.MVT_SERVER.REST_API.GET_JSON.EXTRA_KEY.ENDPOINT.STRING);
                            if (s_endpoint == null || s_endpoint.isEmpty()) {
                                s_endpoint = "/capabilities";
                                Log.d(TAG, "m_br_client_control_request.onReceive(MVT_SERVER_HTTP_URL_API_GOT_JSON): "
                                        + Constants.Strings.INTENT.ACTION.REQUEST.MVT_SERVER.REST_API.GET_JSON.EXTRA_KEY.ENDPOINT.STRING
                                        + " string extra is null or empty - setting s_endpoint==\"" + s_endpoint + "\""
                                );
                            }
                            switch (s_purpose) {
                                case Constants.Strings.INTENT.ACTION.REQUEST.MVT_SERVER.REST_API.GET_JSON.EXTRA_KEY.PURPOSE.VALUE.LOAD_MAP.STRING: {
                                    rest_api__tegola__get_json(
                                        s_root_url,
                                        s_endpoint,
                                        Constants.Strings.INTENT.ACTION.REQUEST.MVT_SERVER.REST_API.GET_JSON.EXTRA_KEY.PURPOSE.VALUE.LOAD_MAP.STRING
                                    );
                                    break;
                                }
                                default: {
                                    rest_api__tegola__get_json(
                                        s_root_url,
                                        s_endpoint,
                                        s_purpose
                                    );
                                }
                            }
                            break;
                        }
                        case MVT_SERVER_CONTROL_STOP: {
                            shell__tegola__stop();
                            break;
                        }
                        case MVT_SERVER_STATE_IS_RUNNING: {
                            if (m_process_tegola != null && m_thread_tegola_process_monitor != null && m_thread_tegola_process_monitor.isAlive()) {
                                //notify br receivers MVT_SERVER_STATE_RUNNING
                                Intent intent_notify_server_started = new Intent(Constants.Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.STATE.RUNNING.STRING);
                                if (m_process_tegola_pid != -1)
                                    intent_notify_server_started.putExtra(Constants.Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.STATE.RUNNING.EXTRA_KEY.PID.STRING, m_process_tegola_pid.intValue());
                                fgs_asn__update(getString(R.string.running) + (m_process_tegola_pid != -1 ? ", pid " + m_process_tegola_pid: ""));
                                sendBroadcast(intent_notify_server_started);
                            } else {
                                Intent intent_notify_server_stopped = new Intent(Constants.Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.STATE.STOPPED.STRING);
                                fgs_asn__update(getString(R.string.stopped));
                                sendBroadcast(intent_notify_server_stopped);
                            }
                            break;
                        }
                        case MVT_SERVER_STATE_LISTEN_PORT: {
                            if (m_process_tegola != null && m_thread_tegola_process_monitor != null && m_thread_tegola_process_monitor.isAlive() && m_i_tegola_listen_port != null) {
                                Intent intent_notify_server_listening = new Intent(Constants.Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.STATE.LISTENING.STRING);
                                intent_notify_server_listening.putExtra(Constants.Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.STATE.LISTENING.EXTRA_KEY.PORT.STRING, m_i_tegola_listen_port.intValue());
                                fgs_asn__update(
                            getString(R.string.running)
                                    + (m_process_tegola_pid != -1 ? ", pid " + m_process_tegola_pid : "")
                                    + ", listening on port " + m_i_tegola_listen_port.intValue()
                                );
                                sendBroadcast(intent_notify_server_listening);
                            }
                            break;
                        }
                        default: {
                            Log.e(TAG, "m_br_client_control_request received: " + intent.getAction() + " but NO HANDLER IS DEFINED!");
                            break;
                        }
                    }
                } else {
                    Log.e(TAG, "m_br_client_control_request received null intent!");
                }
            }
        };
    }

    private Process m_process_tegola = null;
    private Thread
            m_thread_tegola_process_monitor = null
            , m_thread_tegola_process_stdout_monitor = null
            , m_thread_tegola_process_stderr_monitor = null
            , m_thread_logcat_process_monitor = null;
    private volatile boolean m_tegola_process_is_running = false;

    private Integer m_process_tegola_pid = null;
    private int getPid(Process p) {
        int pid = -1;
        try {
            Field f = p.getClass().getDeclaredField("pid");
            f.setAccessible(true);
            pid = f.getInt(p);
            f.setAccessible(false);
        } catch (Throwable e) {
            pid = -1;
        }
        return pid;
    }

    private String m_s_version = null;

    private Integer m_i_tegola_listen_port = null;

    private void get__tegola_version() {
        try {
            final File f_tegola_bin_executable = Utils.TEGOLA_BIN.getInstance(getApplicationContext()).get();
            final String
                    s_tegola_bin_executable_path = f_tegola_bin_executable.getCanonicalPath()
                    ;
            if (!f_tegola_bin_executable.exists())
                throw new FileNotFoundException("tegola bin file " + s_tegola_bin_executable_path + " does not exist");
            Log.d(TAG, "get__tegola_version: found/using tegola bin: " + s_tegola_bin_executable_path);
            StringBuilder sb_cmd_line = new StringBuilder()
                    .append(s_tegola_bin_executable_path)
                    .append(" ")
                    .append("version");
            String s_cmdline = sb_cmd_line.toString();
            Log.d(TAG, "get__tegola_version: starting tegola version process (cmdline: '" + s_cmdline + "')...");
            Process process_tegola_version = Runtime.getRuntime().exec(s_cmdline);
            if (process_tegola_version != null) {
                final int pid = getPid(process_tegola_version);
                Log.i(TAG, "get__tegola_version: tegola version process (" + pid + ") started");
                InputStream inputstream_tegola_process = process_tegola_version.getInputStream();
                if (inputstream_tegola_process != null) {
                    Log.d(TAG, "get__tegola_version: got ref to tegola version process inputstream");
                    BufferedReader reader_tegola_version_process_inputstream = new BufferedReader(new InputStreamReader(inputstream_tegola_process));
                    try {
                        m_s_version = reader_tegola_version_process_inputstream.readLine();
                        Log.d(TAG, "get__tegola_version: tegola version process output: \"" + m_s_version + "\"");
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (reader_tegola_version_process_inputstream != null) {
                            try {
                                reader_tegola_version_process_inputstream.close();
                            } catch (Exception e2) {}
                        }
                    }
                } else {
                    Log.e(TAG, "get__tegola_version: could not get ref to tegola version process inputstream!");
                }
                if (m_s_version == null)
                    m_s_version = "unknown";
                Utils.TEGOLA_BIN.getInstance(getApplicationContext()).set_version_string(m_s_version);
            }
        } catch (IOException e) {
            e.printStackTrace();
            try {
                Utils.TEGOLA_BIN.getInstance(getApplicationContext()).set_version_string("unknown");
            } catch (PackageManager.NameNotFoundException e1) {
                //
            } catch (IOException e1) {
                //e1.printStackTrace();
            } catch (Exceptions.TegolaBinaryNotExecutableException e1) {
                //e1.printStackTrace();
            } catch (Exceptions.UnsupportedCPUABIException e1) {
                //e1.printStackTrace();
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (Exceptions.TegolaBinaryNotExecutableException e) {
            e.printStackTrace();
        } catch (Exceptions.UnsupportedCPUABIException e) {
            e.printStackTrace();
        }
    }

    public static abstract class MVT_SERVER_START_SPEC {
        public final boolean provider__is_gpkg;
        public final boolean config_toml__is_remote;
        private MVT_SERVER_START_SPEC(final boolean provider__is_gpkg, final boolean config_toml__is_remote) {
            this.provider__is_gpkg = provider__is_gpkg;
            this.config_toml__is_remote = config_toml__is_remote;
        }
    }
    public static class MVT_SERVER_START_SPEC__POSTGIS_PROVIDER extends MVT_SERVER_START_SPEC {
        public final String config_toml;
        public MVT_SERVER_START_SPEC__POSTGIS_PROVIDER(final boolean config_toml__is_remote, @NonNull final String config_toml) {
            super(false, config_toml__is_remote);
            this.config_toml = config_toml;
        }
    }
    public static class MVT_SERVER_START_SPEC__GPKG_PROVIDER extends MVT_SERVER_START_SPEC {
        public final String gpkg_bundle;
        public final String gpkg_bundle_props;
        public MVT_SERVER_START_SPEC__GPKG_PROVIDER(final String gpkg_bundle, final String gpkg_bundle_props) {
            super(true, false);
            this.gpkg_bundle = gpkg_bundle;
            this.gpkg_bundle_props = gpkg_bundle_props;
        }
    }
    public class UnknownMVTServerStartSpecType extends Exception {
        public UnknownMVTServerStartSpecType(@NonNull final MVT_SERVER_START_SPEC unknown) {
            super(unknown.getClass().getName());
        }
    }
    private void handle_mvt_server_control_request__start(@NonNull final MVT_SERVER_START_SPEC server_start_spec) {
        try {
            Utils.TEGOLA_BIN.getInstance(getApplicationContext()).get();
            shell__tegola__start(server_start_spec);  //note that this function internally handles sending the MVT_SERVER_STATE_STARTING and MVT_SERVER_STATE_RUNNING notifications - on failure an exception will be thrown on the SEH below will send the failure notification in that case
        } catch (IOException e) {
            e.printStackTrace();
            Intent intent_notify_mvt_server_start_failed = new Intent(Constants.Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.STATE.START_FAILED.STRING);
            intent_notify_mvt_server_start_failed.putExtra(Constants.Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.STATE.START_FAILED.EXTRA_KEY.REASON.STRING, e.getMessage());
            fgs_asn__update(getString(R.string.stopped));
            sendBroadcast(intent_notify_mvt_server_start_failed);
        } catch (Exceptions.UnsupportedCPUABIException e) {
            e.printStackTrace();
            Intent intent_notify_mvt_server_start_failed = new Intent(Constants.Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.STATE.START_FAILED.STRING);
            intent_notify_mvt_server_start_failed.putExtra(Constants.Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.STATE.START_FAILED.EXTRA_KEY.REASON.STRING, e.getMessage());
            fgs_asn__update(getString(R.string.stopped));
            sendBroadcast(intent_notify_mvt_server_start_failed);
        } catch (Exceptions.InvalidTegolaArgumentException e) {
            e.printStackTrace();
            Intent intent_notify_mvt_server_start_failed = new Intent(Constants.Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.STATE.START_FAILED.STRING);
            intent_notify_mvt_server_start_failed.putExtra(Constants.Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.STATE.START_FAILED.EXTRA_KEY.REASON.STRING, e.getMessage());
            fgs_asn__update(getString(R.string.stopped));
            sendBroadcast(intent_notify_mvt_server_start_failed);
        } catch (UnknownMVTServerStartSpecType e) {
            e.printStackTrace();
            Intent intent_notify_mvt_server_start_failed = new Intent(Constants.Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.STATE.START_FAILED.STRING);
            intent_notify_mvt_server_start_failed.putExtra(Constants.Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.STATE.START_FAILED.EXTRA_KEY.REASON.STRING, e.getMessage());
            fgs_asn__update(getString(R.string.stopped));
            sendBroadcast(intent_notify_mvt_server_start_failed);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            Intent intent_notify_mvt_server_start_failed = new Intent(Constants.Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.STATE.START_FAILED.STRING);
            intent_notify_mvt_server_start_failed.putExtra(Constants.Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.STATE.START_FAILED.EXTRA_KEY.REASON.STRING, e.getMessage());
            fgs_asn__update(getString(R.string.stopped));
            sendBroadcast(intent_notify_mvt_server_start_failed);
        } catch (Exceptions.TegolaBinaryNotExecutableException e) {
            e.printStackTrace();
            Intent intent_notify_mvt_server_start_failed = new Intent(Constants.Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.STATE.START_FAILED.STRING);
            intent_notify_mvt_server_start_failed.putExtra(Constants.Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.STATE.START_FAILED.EXTRA_KEY.REASON.STRING, e.getMessage());
            fgs_asn__update(getString(R.string.stopped));
            sendBroadcast(intent_notify_mvt_server_start_failed);
        }
    }

    private Process m_logcat_process = null;

    private boolean shell__tegola__start(@NonNull final MVT_SERVER_START_SPEC server_start_spec) throws IOException, Exceptions.UnsupportedCPUABIException, Exceptions.InvalidTegolaArgumentException, UnknownMVTServerStartSpecType, PackageManager.NameNotFoundException, Exceptions.TegolaBinaryNotExecutableException {
        m_process_tegola_pid = null;
        final File
                f_filesDir = getFilesDir()
                , f_tegola_bin_executable = Utils.TEGOLA_BIN.getInstance(getApplicationContext()).get();
        final String
                s_tegola_bin_executable_path = f_tegola_bin_executable.getCanonicalPath()
                ;
        if (!f_tegola_bin_executable.exists())
            throw new FileNotFoundException("tegola bin file " + s_tegola_bin_executable_path + " does not exist");
        Log.d(TAG, "shell__tegola__start: found/using tegola bin: " + s_tegola_bin_executable_path);
        ArrayList<String> als_cmd_line = new ArrayList<String>();
        als_cmd_line.add("./" + f_tegola_bin_executable.getName());
        als_cmd_line.add("serve");
        ProcessBuilder pb = new ProcessBuilder();
        pb = pb.directory(f_tegola_bin_executable.getParentFile());
        Map<String, String> pb_env = pb.environment();

        if (server_start_spec instanceof MVT_SERVER_START_SPEC__GPKG_PROVIDER) {
            final MVT_SERVER_START_SPEC__GPKG_PROVIDER server_start_spec__gpkg_provider = (MVT_SERVER_START_SPEC__GPKG_PROVIDER)server_start_spec;
            File
                    f_gpkg_bundles_root_dir = null
                    , f_gpkg_bundle = null
                    , f_gpkg_bundle__toml = null
                    , f_gpkg_bundle__gpkg = null;
            try {
                f_gpkg_bundles_root_dir = Utils.GPKG.Local.F_GPKG_DIR.getInstance(getApplicationContext());
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                throw new FileNotFoundException("geopcackage-bundle root directory not found");
            }
            f_gpkg_bundle = new File(f_gpkg_bundles_root_dir.getPath(), server_start_spec__gpkg_provider.gpkg_bundle);
            if (!f_gpkg_bundle.exists())
                throw new FileNotFoundException("geopcackage-bundle " + f_gpkg_bundle.getCanonicalPath() + " not found");
            Log.d(TAG, "shell__tegola__start: found/using gpkg-bundle: " + f_gpkg_bundle.getName());

//            //process version.properties file for this gpk-bundle
            String s_prop_val = "";
            File f_gpkg_bundle_ver_props = new File(f_gpkg_bundle.getPath(), server_start_spec__gpkg_provider.gpkg_bundle_props);
            if (!f_gpkg_bundle_ver_props.exists())
                throw new FileNotFoundException("geopcackage-bundle version.properties file " + f_gpkg_bundle_ver_props.getCanonicalPath() + " not found");
            Log.d(TAG, "shell__tegola__start: found/using gpkg-bundle version.properties: " + f_gpkg_bundle_ver_props.getCanonicalPath());
            //get gpkg-bundle toml file spec from version.props, confirm existence, then build "--config" arg for tegola commandline
            s_prop_val = Utils.getProperty(f_gpkg_bundle_ver_props, Constants.Strings.GPKG_BUNDLE.VERSION_PROPS.PROP.TOML_FILE);
            String
                    s_toml_file_remote = s_prop_val,
                    s_toml_file_local = s_toml_file_remote;
            if (Utils.HTTP.isValidUrl(s_toml_file_remote)) {//then retrieve only last part for local file name
                Log.d(TAG, "shell__tegola__start: \t\t" + Constants.Strings.GPKG_BUNDLE.VERSION_PROPS.PROP.TOML_FILE + ":\"" + s_toml_file_remote + "\" IS a uri");
                s_toml_file_local = s_toml_file_remote.substring(s_toml_file_remote.lastIndexOf("/") + 1);
            } else {
                Log.d(TAG, "shell__tegola__start: \t\t" + Constants.Strings.GPKG_BUNDLE.VERSION_PROPS.PROP.TOML_FILE + ":\"" + s_toml_file_remote + "\" IS NOT a uri");
            }
            Log.d(TAG, "shell__tegola__start: \t\tlocal: \"" + s_toml_file_local + "\"; remote: \"" + s_toml_file_remote + "\"");
            f_gpkg_bundle__toml = new File(f_gpkg_bundle.getPath(), s_toml_file_local);
            Log.d(TAG, "shell__tegola__start: version.properties: " + Constants.Strings.GPKG_BUNDLE.VERSION_PROPS.PROP.TOML_FILE + " == " + f_gpkg_bundle__toml.getCanonicalPath());
            if (!f_gpkg_bundle__toml.exists())
                throw new FileNotFoundException("geopcackage-bundle toml file " + f_gpkg_bundle__toml.getCanonicalPath() + " not found");
            Log.d(TAG, "shell__tegola__start: \tfound/using gpkg-bundle toml file: " + f_gpkg_bundle__toml.getCanonicalPath());
            als_cmd_line.add("--" + Constants.Strings.TEGOLA_ARG.CONFIG);
            als_cmd_line.add(f_gpkg_bundle__toml.getCanonicalPath());

            //set env var for tegola file cache base path
            pb_env.put(Constants.Strings.TEGOLA_PROCESS.TILE_CACHE.FILE.BASE_PATH_ENV_VAR, f_gpkg_bundle.getCanonicalPath() + File.separator + Constants.Strings.TEGOLA_PROCESS.TILE_CACHE.FILE.SUB_PATH);
            Log.d(TAG, "shell__tegola__start: \tset pb env " + Constants.Strings.TEGOLA_PROCESS.TILE_CACHE.FILE.BASE_PATH_ENV_VAR + " to \"" + pb_env.get(Constants.Strings.TEGOLA_PROCESS.TILE_CACHE.FILE.BASE_PATH_ENV_VAR) + "\"");

            //get gpkg-bundle geopcackages spec from version.props, then confirm existence
            s_prop_val = Utils.getProperty(f_gpkg_bundle_ver_props, Constants.Strings.GPKG_BUNDLE.VERSION_PROPS.PROP.GPKG_FILES);
            String[] s_list_gpkg_files = s_prop_val.split(",");
            s_prop_val = Utils.getProperty(f_gpkg_bundle_ver_props, Constants.Strings.GPKG_BUNDLE.VERSION_PROPS.PROP.GPKG_PATH_ENV_VARS);
            String[] s_list_geopcackage_path_env_vars = s_prop_val.split(",");
            if (s_list_gpkg_files != null && s_list_gpkg_files.length > 0) {
                for (int i = 0; i < s_list_gpkg_files.length; i++) {
                    String
                            s_gpkg_file_remote = s_list_gpkg_files[i],
                            s_gpkg_file_local = s_gpkg_file_remote;
                    Log.d(TAG, "shell__tegola__start: \t" + Constants.Strings.GPKG_BUNDLE.VERSION_PROPS.PROP.GPKG_FILES + "[" + i + "]=\"" + s_gpkg_file_remote + "\"");
                    if (Utils.HTTP.isValidUrl(s_gpkg_file_remote)) {//then retrieve only last part for local file name
                        Log.d(TAG, "shell__tegola__start: \t\t" + Constants.Strings.GPKG_BUNDLE.VERSION_PROPS.PROP.GPKG_FILES + "[" + i + "]:\"" + s_gpkg_file_remote + "\" IS uri");
                        s_gpkg_file_local = s_gpkg_file_remote.substring(s_gpkg_file_remote.lastIndexOf("/") + 1);
                    } else {
                        Log.d(TAG, "shell__tegola__start: \t\t" + Constants.Strings.GPKG_BUNDLE.VERSION_PROPS.PROP.GPKG_FILES + "[" + i + "]:\"" + s_gpkg_file_remote + "\" IS NOT uri");
                    }
                    Log.d(TAG, "shell__tegola__start: \t\tlocal: \"" + s_gpkg_file_local + "\"; remote: \"" + s_gpkg_file_remote + "\"");
                    f_gpkg_bundle__gpkg = new File(f_gpkg_bundle.getPath(), s_gpkg_file_local);
                    Log.d(TAG, "shell__tegola__start: version.properties: " + Constants.Strings.GPKG_BUNDLE.VERSION_PROPS.PROP.GPKG_FILES + "[" + i + "] == " + f_gpkg_bundle__gpkg.getCanonicalPath());
                    if (!f_gpkg_bundle__gpkg.exists())
                        throw new FileNotFoundException("geopcackage-bundle gpkg file " + f_gpkg_bundle__gpkg.getCanonicalPath() + " not found");
                    //set env var to store path to this particular gpkg
                    pb_env.put(s_list_geopcackage_path_env_vars[i], f_gpkg_bundle__gpkg.getCanonicalPath());
                    Log.d(TAG, "shell__tegola__start: \tset pb env " + s_list_geopcackage_path_env_vars[i] + " to \"" + pb_env.get(s_list_geopcackage_path_env_vars[i]) + "\"");
                }
            } else {
                throw new FileNotFoundException("failed to retrieve list of geopackage files from gpkg-bundle version.properties file " + f_gpkg_bundle_ver_props.getCanonicalPath());
            }
        } else if (server_start_spec instanceof MVT_SERVER_START_SPEC__POSTGIS_PROVIDER) {
            final MVT_SERVER_START_SPEC__POSTGIS_PROVIDER server_start_spec_postgis_provider = (MVT_SERVER_START_SPEC__POSTGIS_PROVIDER)server_start_spec;
            if (server_start_spec_postgis_provider.config_toml == null || server_start_spec_postgis_provider.config_toml.isEmpty())
                throw new Exceptions.InvalidTegolaArgumentException("argument \"" + Constants.Strings.TEGOLA_ARG.CONFIG + "\" is null or empty");
            if (server_start_spec_postgis_provider.config_toml__is_remote) {
                Log.d(TAG, "shell__tegola__start: using remote config toml file: " + server_start_spec_postgis_provider.config_toml);
                als_cmd_line.add("--" + Constants.Strings.TEGOLA_ARG.CONFIG);
                als_cmd_line.add(server_start_spec_postgis_provider.config_toml);
            } else {
                final File f_postgis_config_toml = new File(server_start_spec_postgis_provider.config_toml);
                if (f_postgis_config_toml != null && !f_postgis_config_toml.exists())
                    throw new FileNotFoundException("toml file " + f_postgis_config_toml.getCanonicalPath() + " not found");
                Log.d(TAG, "shell__tegola__start: found/using config toml file: " + f_postgis_config_toml.getCanonicalPath());
                als_cmd_line.add("--" + Constants.Strings.TEGOLA_ARG.CONFIG);
                als_cmd_line.add(f_postgis_config_toml.getCanonicalPath());
            }

            //set env var for tegola file cache base path
            pb_env.put(Constants.Strings.TEGOLA_PROCESS.TILE_CACHE.FILE.BASE_PATH_ENV_VAR, pb.directory().getCanonicalPath() + File.separator + Constants.Strings.TEGOLA_PROCESS.TILE_CACHE.FILE.SUB_PATH);
            Log.d(TAG, "shell__tegola__start: \tset pb env " + Constants.Strings.TEGOLA_PROCESS.TILE_CACHE.FILE.BASE_PATH_ENV_VAR + " to \"" + pb_env.get(Constants.Strings.TEGOLA_PROCESS.TILE_CACHE.FILE.BASE_PATH_ENV_VAR) + "\"");
        } else
            throw new UnknownMVTServerStartSpecType(server_start_spec);
        pb = pb.command(als_cmd_line);

        //shell__tegola__stop();

        Log.i(TAG, "shell__tegola__start: starting new tegola server process...");
        //notify br_receivers (if any) server starting
        Intent intent_notify_server_starting = new Intent(Constants.Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.STATE.STARTING.STRING);
        fgs_asn__update(getString(R.string.starting));
        sendBroadcast(intent_notify_server_starting);

        //build and exec tegola cmd line in new process
        StringBuilder sb_cmdline = new StringBuilder();
        if (pb.command() != null) {
            for (int i = 0; i < pb.command().size(); i++) {
                if (i > 0)
                    sb_cmdline.append(" ");
                sb_cmdline.append(pb.command().get(i));
            }
        }
        String s_cmdline = sb_cmdline.toString();
        String s_working_dir = pb.directory().getCanonicalPath();
        Log.d(TAG, "shell__tegola__start: starting tegola server process (cmdline is '" + s_cmdline + "' and will run in " + s_working_dir + ")...");
        m_process_tegola = pb.start();

        //immediately notify br receivers MVT_SERVER_STATE_STOPPED if we fail to create tegola process
        if (m_process_tegola == null) {
            m_thread_tegola_process_stdout_monitor = null;
            m_thread_tegola_process_stderr_monitor = null;
            m_process_tegola = null;
            m_tegola_process_is_running = false;
            Intent intent_notify_server_stopped = new Intent(Constants.Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.STATE.STOPPED.STRING);
            fgs_asn__update(getString(R.string.stopped));
            sendBroadcast(intent_notify_server_stopped);
            return false;
        }

        m_tegola_process_is_running = true;

        //get tegola pid if we can - may not work since "pid" is private field of Process, obtained via reflection...
        m_process_tegola_pid = getPid(m_process_tegola);
        Log.i(TAG, "shell__tegola__start: tegola server process " + (m_process_tegola_pid != -1 ? "(pid " + m_process_tegola_pid + ") ": "") + "started");

        //notify br receivers MVT_SERVER_STATE_RUNNING
        Intent intent_notify_server_started = new Intent(Constants.Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.STATE.RUNNING.STRING);
        if (m_process_tegola_pid != -1)
            intent_notify_server_started.putExtra(Constants.Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.STATE.RUNNING.EXTRA_KEY.PID.STRING, m_process_tegola_pid.intValue());
        fgs_asn__update(getString(R.string.running) + (m_process_tegola_pid != -1 ? ", pid " + m_process_tegola_pid: ""));
        sendBroadcast(intent_notify_server_started);

        //now start tegola logcat monitor specific to tegola process (before stderr and stdout monitors since there is always the possibility tegola could segfault or trigger some other native signal)
        if (m_process_tegola_pid != -1) {
            sb_cmdline = new StringBuilder();
            sb_cmdline.append("logcat");
            sb_cmdline.append(" -v thread");
            s_cmdline = sb_cmdline.toString();

            //the crux: start logcat process!
            Log.d(TAG, "shell__tegola__start: starting logcat process (cmdline: '" + s_cmdline + "')...");
            m_logcat_process = Runtime.getRuntime().exec(s_cmdline);

            if (m_logcat_process != null) {
                final int pid_logcat = getPid(m_logcat_process);
                Log.i(TAG, "shell__tegola__start: logcat process (" + pid_logcat + ") started");

                //now create/start thread to monitor the logcat process itself
                m_thread_logcat_process_monitor = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Thread logcat_inputstream_processor_thread = null;
                        try {
                            Log.i(TAG, "logcat_inputstream_processor_thread: started");

                            //create an additional thread which reads the inputstream of the process and sends content (line by line) back to consumer
                            logcat_inputstream_processor_thread = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    InputStream inputstream_logcat_process = m_logcat_process != null ? m_logcat_process.getInputStream() : null;
                                    if (inputstream_logcat_process != null) {
                                        Log.d(TAG, "logcat_inputstream_processor_thread: got ref to logcat process inputstream");

                                        BufferedReader reader_logcat_process_inputstream = new BufferedReader(new InputStreamReader(inputstream_logcat_process));
                                        String s_line = "";
                                        while  (!Thread.currentThread().isInterrupted()) {
                                            try {
                                                while ((s_line = reader_logcat_process_inputstream.readLine()) != null) {
                                                    if (s_line.contains(m_process_tegola_pid + ":")) {
                                                        Intent intent_notify_server_output_logcat = new Intent(Constants.Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.MONITOR.LOGCAT.OUTPUT.STRING);
                                                        intent_notify_server_output_logcat.putExtra(Constants.Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.MONITOR.LOGCAT.OUTPUT.EXTRA_KEY.LINE.STRING, s_line);
                                                        sendBroadcast(intent_notify_server_output_logcat);
                                                    }
                                                }
                                                Thread.currentThread().sleep(1000);
                                            } catch (InterruptedIOException e1) {
                                                //e1.printStackTrace();
                                                try {
                                                    reader_logcat_process_inputstream.close();
                                                } catch (IOException e2) {
                                                    //e2.printStackTrace();
                                                }
                                                Log.d(TAG, "logcat_inputstream_processor_thread: thread interrupted");
                                                Thread.currentThread().interrupt();
                                            } catch (InterruptedException e1) {
                                                //e1.printStackTrace();
                                                if (reader_logcat_process_inputstream != null) {
                                                    try {
                                                        reader_logcat_process_inputstream.close();
                                                    } catch (IOException e2) {
                                                        //e2.printStackTrace();
                                                    }
                                                }
                                                Log.d(TAG, "logcat_inputstream_processor_thread: thread interrupted");
                                                Thread.currentThread().interrupt();
                                            } catch (IOException e) {
                                                //e.printStackTrace();
                                                if (reader_logcat_process_inputstream != null) {
                                                    try {
                                                        reader_logcat_process_inputstream.close();
                                                    } catch (IOException e2) {
                                                        //e2.printStackTrace();
                                                    }
                                                }
                                                Log.d(TAG, "logcat_inputstream_processor_thread: thread interrupted");
                                            }
                                        }
                                    } else {
                                        Log.e(TAG, "logcat_inputstream_processor_thread: could not get ref to tegola logcat monitor process inputstream!");
                                    }
                                    Log.i(TAG, "logcat_inputstream_processor_thread: thread exiting");
                                }
                            });
                            //Log.i(TAG, "logcat_process_monitor_thread: starting logcat_inputstream_processor_thread thread...");
                            logcat_inputstream_processor_thread.start();
                            if (logcat_inputstream_processor_thread.isAlive())
                                m_logcat_process.waitFor();
                            Log.i(TAG, "logcat_process_monitor_thread: logcat process stopped");
                        } catch (InterruptedException e) {
                            //e.printStackTrace();
                        } finally {
                            //now interrupt inputstream capture thread
                            if (logcat_inputstream_processor_thread != null && logcat_inputstream_processor_thread.isAlive()) {
                                //Log.i(TAG, "logcat_process_monitor_thread: interrupting logcat_inputstream_processor_thread thread...");
                                logcat_inputstream_processor_thread.interrupt();
                                try {
                                    Log.i(TAG, "logcat_process_monitor_thread: interrupted inputstream processor thread and waiting 250 ms for thread to exit");
                                    logcat_inputstream_processor_thread.join(250);
                                } catch (InterruptedException e) {
                                    //e.printStackTrace();
                                }
                                logcat_inputstream_processor_thread = null;
                                Log.i(TAG, "logcat_process_monitor_thread: logcat_inputstream_processor_thread thread stopped or 250 ms wait to stop expired");
                            }
                        }
                    }
                });
                m_thread_logcat_process_monitor.start();
            }
        }

        //start tegola process stderr monitor
        m_thread_tegola_process_stderr_monitor = new Thread(new Runnable() {
            @Override
            public void run() {
                boolean found_listen_port = false;
                String s_key_listen_port = "starting tegola server on port";
                Log.d(TAG, "tegola_stderr_monitor_thread: thread started");
                InputStream input_stream_tegola_process_stderr = m_process_tegola != null ? m_process_tegola.getErrorStream() : null;
                if (input_stream_tegola_process_stderr != null) {
                    Log.d(TAG, "tegola_stderr_monitor_thread: got ref to stderr");
                    BufferedReader reader_tegola_process_stderr = new BufferedReader(new InputStreamReader(input_stream_tegola_process_stderr));
                    String s_line = "";
                    while (!Thread.currentThread().isInterrupted()) {
                        try {
                            while ((s_line = reader_tegola_process_stderr.readLine()) != null) {
                                Log.e(TAG, "tegola_STDERR_output: " + s_line);
                                Intent intent_notify_server_output_stderr = new Intent(Constants.Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.MONITOR.STDERR.OUTPUT.STRING);
                                intent_notify_server_output_stderr.putExtra(Constants.Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.MONITOR.STDERR.OUTPUT.EXTRA_KEY.LINE.STRING, s_line);
                                sendBroadcast(intent_notify_server_output_stderr);

                                if (!found_listen_port) {
                                    if (s_line.contains(s_key_listen_port)) {
                                        found_listen_port = true;
                                        String s_port = s_line.substring(s_line.indexOf(s_key_listen_port) + s_key_listen_port.length());  //now contains only "starting tegola server on port :xxxx"
                                        s_port = s_port.substring(s_port.indexOf(":") + 1).trim();
                                        Log.d(TAG, "tegola_STDERR_output: confirmed tegola process (PID " + m_process_tegola_pid+ ") is listening on localhost:" + s_port);

                                        //notify br receivers MVT_SERVER_STATE_LISTENING
                                        m_i_tegola_listen_port = Integer.valueOf(s_port);
                                        Intent intent_notify_server_listening = new Intent(Constants.Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.STATE.LISTENING.STRING);
                                        intent_notify_server_listening.putExtra(Constants.Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.STATE.LISTENING.EXTRA_KEY.PORT.STRING, m_i_tegola_listen_port.intValue());
                                        fgs_asn__update(
                                                getString(R.string.running)
                                                        + (m_process_tegola_pid != -1 ? ", pid " + m_process_tegola_pid : "")
                                                        + ", listening on port " + m_i_tegola_listen_port.intValue()
                                        );
                                        sendBroadcast(intent_notify_server_listening);
                                    }
                                }
                            }
                            Thread.currentThread().sleep(1000);
                        } catch (IOException e) {
//                            e.printStackTrace();
                            if (reader_tegola_process_stderr != null) {
                                try {
                                    reader_tegola_process_stderr.close();
                                } catch (IOException e2) {
                                    //e2.printStackTrace();
                                }
                            }
                            Log.d(TAG, "tegola_stderr_monitor_thread: thread interrupted");
                            Thread.currentThread().interrupt();
                        } catch (InterruptedException e1) {
                            //e.printStackTrace();
                            if (reader_tegola_process_stderr != null) {
                                try {
                                    reader_tegola_process_stderr.close();
                                } catch (IOException e2) {
                                    //e2.printStackTrace();
                                }
                            }
                            Log.d(TAG, "tegola_stderr_monitor_thread: thread interrupted");
                            Thread.currentThread().interrupt();
                        }
                    }
                } else {
                    Log.e(TAG, "tegola_stderr_monitor_thread: could not get ref to stderr!");
                }
                Log.d(TAG, "tegola_stderr_monitor_thread: thread exiting");
            }
        });
        m_thread_tegola_process_stderr_monitor.start();

        //start tegola process stdout monitor
        m_thread_tegola_process_stdout_monitor = new Thread(new Runnable() {
            @Override
            public void run() {
                boolean found_listen_port = false;
                String s_key_listen_port = "starting tegola server on port";
                Log.d(TAG, "tegola_stdout_monitor_thread: thread started");
                InputStream input_stream_tegola_process_stdout = m_process_tegola != null ? m_process_tegola.getInputStream() : null;
                if (input_stream_tegola_process_stdout != null) {
                    Log.d(TAG, "tegola_stdout_monitor_thread: got ref to stdout");
                    BufferedReader reader_tegola_process_stdout = new BufferedReader(new InputStreamReader(input_stream_tegola_process_stdout));
                    String s_line = "";
                    while (!Thread.currentThread().isInterrupted()) {
                        try {
                            while ((s_line = reader_tegola_process_stdout.readLine()) != null) {
                                Log.d(TAG, "tegola_STDOUT_output: " + s_line);
                                Intent intent_notify_server_output_stdout = new Intent(Constants.Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.MONITOR.STDOUT.OUTPUT.STRING);
                                intent_notify_server_output_stdout.putExtra(Constants.Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.MONITOR.STDOUT.OUTPUT.EXTRA_KEY.LINE.STRING, s_line);
                                sendBroadcast(intent_notify_server_output_stdout);

                                if (!found_listen_port) {
                                    if (s_line.contains(s_key_listen_port)) {
                                        found_listen_port = true;
                                        String s_port = s_line.substring(s_line.indexOf(s_key_listen_port) + s_key_listen_port.length());  //now contains only "starting tegola server on port :xxxx"
                                        s_port = s_port.substring(s_port.indexOf(":") + 1).trim();
                                        Log.d(TAG, "tegola_STDOUT_output: confirmed tegola process (PID " + m_process_tegola_pid+ ") is listening on localhost:" + s_port);

                                        //notify br receivers MVT_SERVER_STATE_LISTENING
                                        m_i_tegola_listen_port = Integer.valueOf(s_port);
                                        Intent intent_notify_server_listening = new Intent(Constants.Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.STATE.LISTENING.STRING);
                                        intent_notify_server_listening.putExtra(Constants.Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.STATE.LISTENING.EXTRA_KEY.PORT.STRING, m_i_tegola_listen_port.intValue());
                                        fgs_asn__update(
                                                getString(R.string.running)
                                                        + (m_process_tegola_pid != -1 ? ", pid " + m_process_tegola_pid : "")
                                                        + ", listening on port " + m_i_tegola_listen_port.intValue()
                                        );
                                        sendBroadcast(intent_notify_server_listening);
                                    }
                                }
                            }
                            Thread.currentThread().sleep(1000);
                        } catch (IOException e) {
//                            e.printStackTrace();
                            if (reader_tegola_process_stdout != null) {
                                try {
                                    reader_tegola_process_stdout.close();
                                } catch (IOException e2) {
                                    //e2.printStackTrace();
                                }
                            }
                            Log.d(TAG, "tegola_stdout_monitor_thread: thread interrupted");
                            Thread.currentThread().interrupt();
                        } catch (InterruptedException e1) {
                            //e.printStackTrace();
                            if (reader_tegola_process_stdout != null) {
                                try {
                                    reader_tegola_process_stdout.close();
                                } catch (IOException e2) {
                                    //e2.printStackTrace();
                                }
                            }
                            Log.d(TAG, "tegola_stdout_monitor_thread: thread interrupted");
                            Thread.currentThread().interrupt();
                        }
                    }
                } else {
                    Log.e(TAG, "tegola_stdout_monitor_thread: could not get ref to stdout!");
                }
                Log.d(TAG, "tegola_stdout_monitor_thread: thread exiting");
            }
        });
        m_thread_tegola_process_stdout_monitor.start();

        m_thread_tegola_process_monitor = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    m_process_tegola.waitFor();
                    Log.i(TAG, "tegola_process_monitor_thread: tegola mvt server process stopped");
                } catch (InterruptedException e) {
                    //e.printStackTrace();
                } finally {
                    int n_thread_cleanup_wait_ms = 250;

                    if (m_thread_tegola_process_stdout_monitor != null && m_thread_tegola_process_stdout_monitor.isAlive()) {
                        m_thread_tegola_process_stdout_monitor.interrupt();
                        try {
                            Log.i(TAG, "tegola_process_monitor_thread: interrupted stdout monitor thread and waiting " + n_thread_cleanup_wait_ms + " ms for thread to exit");
                            m_thread_tegola_process_stdout_monitor.join(n_thread_cleanup_wait_ms);
                        } catch (InterruptedException e) {
                            //e.printStackTrace();
                        }
                    }
                    m_thread_tegola_process_stdout_monitor = null;
                    Log.i(TAG, "tegola_process_monitor_thread: m_thread_tegola_process_stdout_monitor thread stopped or " + n_thread_cleanup_wait_ms + " ms wait to stop expired");

                    if (m_thread_tegola_process_stderr_monitor != null && m_thread_tegola_process_stderr_monitor.isAlive()) {
                        m_thread_tegola_process_stderr_monitor.interrupt();
                        try {
                            Log.i(TAG, "tegola_process_monitor_thread: interrupted stderr monitor thread and waiting " + n_thread_cleanup_wait_ms + " ms for thread to exit");
                            m_thread_tegola_process_stderr_monitor.join(n_thread_cleanup_wait_ms);
                        } catch (InterruptedException e) {
                            //e.printStackTrace();
                        }
                    }
                    m_thread_tegola_process_stderr_monitor = null;
                    Log.i(TAG, "tegola_process_monitor_thread: m_thread_tegola_process_stderr_monitor thread stopped or " + n_thread_cleanup_wait_ms + " ms wait to stop expired");

                    if (m_logcat_process != null)
                        m_logcat_process.destroy();
                    if (m_thread_logcat_process_monitor != null && m_thread_logcat_process_monitor.isAlive()) {
                        m_thread_logcat_process_monitor.interrupt();
                        try {
                            Log.i(TAG, "tegola_process_monitor_thread: destroyed logcat process and waiting " + n_thread_cleanup_wait_ms + " ms for monitor thread to exit");
                            m_thread_logcat_process_monitor.join(n_thread_cleanup_wait_ms);
                        } catch (InterruptedException e) {
                            //e.printStackTrace();
                        }
                    }
                    m_thread_logcat_process_monitor = null;
                    m_logcat_process = null;
                    Log.i(TAG, "tegola_process_monitor_thread: m_thread_logcat_process_monitor thread stopped or " + n_thread_cleanup_wait_ms + " ms wait to stop expired");

                    m_process_tegola_pid = null;
                    m_tegola_process_is_running = false;

                    Intent intent_notify_server_stopped = new Intent(Constants.Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.STATE.STOPPED.STRING);
                    //Log.d(TAG, "tegola_process_monitor_thread: broadcasting intent \"" + intent_notify_server_stopped.getAction() + "\"");
                    sendBroadcast(intent_notify_server_stopped);
                    //Log.d(TAG, "tegola_process_monitor_thread: updating ASNB w/ \"" + getString(R.string.stopped) + "\"");
                    fgs_asn__update(getString(R.string.stopped));
                }
            }
        });
        m_thread_tegola_process_monitor.start();

        return m_tegola_process_is_running;
    }

    private class TegolaJSON {
        public String root_url = "";
        public String json_url_endpoint = "";
        public String json_string = "";
    }
    private void rest_api__tegola__get_json(final String root_url, final String json_url_endpoint, final String purpose) {
        final TegolaJSON tegolaJSON = new TegolaJSON();

        StringBuilder sb_json = new StringBuilder();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        tegolaJSON.root_url = root_url;
        tegolaJSON.json_url_endpoint = json_url_endpoint;

        Utils.HTTP.Get.exec(
            tegolaJSON.root_url + tegolaJSON.json_url_endpoint,
            new Utils.HTTP.Get.ContentHandler() {
                @Override
                public void onStartRead(long n_size) {
                    Log.d(TAG, "rest_api__tegola__get_json: Utils.HTTP.Get.ContentHandler: onStartRead: read " + tegolaJSON.root_url + tegolaJSON.json_url_endpoint + ": content-length: " + n_size);
                }

                @Override
                public void onChunkRead(int n_bytes_read, byte[] bytes_1kb_chunk) {
                    if (n_bytes_read > 0) {
                        baos.write(bytes_1kb_chunk, 0, n_bytes_read);
                        Log.d(TAG, "rest_api__tegola__get_json: Utils.HTTP.Get.ContentHandler: read next " + n_bytes_read + " bytes from " + tegolaJSON.root_url + tegolaJSON.json_url_endpoint + " into byteoutputstream");
                    } else {
                        Log.d(TAG, "rest_api__tegola__get_json: Utils.HTTP.Get.ContentHandler: onChunkRead: skipped writing bytes from " + tegolaJSON.root_url + tegolaJSON.json_url_endpoint + " into byteoutputstream since n_bytes_read <= 0");
                    }
                }

                @Override
                public void onReadError(long n_remaining, Exception e) {
                    Log.d(TAG, "rest_api__tegola__get_json: Utils.HTTP.Get.ContentHandler: onReadError: n_remaining: " + n_remaining + "; error: " + e.getMessage());
                    e.printStackTrace();
                    Intent intent_notify_mvt_server_json_read_failed = new Intent(Constants.Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.HTTP_URL_API.GET_JSON_FAILED.STRING);
                    intent_notify_mvt_server_json_read_failed.putExtra(
                        Constants.Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.HTTP_URL_API.GET_JSON_FAILED.EXTRA_KEY.PURPOSE.STRING,
                        purpose
                    );
                    intent_notify_mvt_server_json_read_failed.putExtra(
                        Constants.Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.HTTP_URL_API.GET_JSON_FAILED.EXTRA_KEY.ROOT_URL.STRING,
                        tegolaJSON.root_url
                    );
                    intent_notify_mvt_server_json_read_failed.putExtra(
                        Constants.Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.HTTP_URL_API.GET_JSON_FAILED.EXTRA_KEY.ENDPOINT.STRING,
                        tegolaJSON.json_url_endpoint
                    );
                    intent_notify_mvt_server_json_read_failed.putExtra(
                        Constants.Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.HTTP_URL_API.GET_JSON_FAILED.EXTRA_KEY.REASON.STRING,
                        e.getMessage()
                    );
                    sendBroadcast(intent_notify_mvt_server_json_read_failed);
                }

                @Override
                public void onReadComplete(long n_read, long n_remaining) {
                    Log.d(TAG, "rest_api__tegola__get_json: Utils.HTTP.Get.ContentHandler: onReadComplete: n_read: " + n_read + "; n_remaining: " + n_remaining);
                    try {
                        tegolaJSON.json_string = baos.toString();
                        Log.d(TAG, "rest_api__tegola__get_json: Utils.HTTP.Get.ContentHandler: onReadComplete: json content is:\n" + tegolaJSON.json_string);
                        baos.close();
                        Intent intent_notify_mvt_server_json_read = new Intent(Constants.Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.HTTP_URL_API.GOT_JSON.STRING);
                        intent_notify_mvt_server_json_read.putExtra(
                            Constants.Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.HTTP_URL_API.GOT_JSON.EXTRA_KEY.PURPOSE.STRING,
                            purpose
                        );
                        intent_notify_mvt_server_json_read.putExtra(
                            Constants.Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.HTTP_URL_API.GOT_JSON.EXTRA_KEY.ROOT_URL.STRING,
                            tegolaJSON.root_url
                        );
                        intent_notify_mvt_server_json_read.putExtra(
                            Constants.Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.HTTP_URL_API.GOT_JSON.EXTRA_KEY.ENDPOINT.STRING,
                            tegolaJSON.json_url_endpoint
                        );
                        intent_notify_mvt_server_json_read.putExtra(
                            Constants.Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.HTTP_URL_API.GOT_JSON.EXTRA_KEY.CONTENT.STRING,
                            tegolaJSON.json_string
                        );
                        sendBroadcast(intent_notify_mvt_server_json_read);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                    }
                }
            }
        );
    }

    private void shell__netstat__parse() {
//                    if (m_process_tegola_pid != - 1) {//get port on which tegola process is listening and notify broaccast receivers
//                        Thread.currentThread().sleep(1000);
//                        int i_port = -1;
//                        String[] s_ary_netstat_output = Utils.Shell.run("netstat -tlnpe", m_process_tegola_pid.toString());
//                        if (s_ary_netstat_output.length > 0) {
//                            String s_tegola_proc_netstat_ifo = s_ary_netstat_output[0];
//                            s_tegola_proc_netstat_ifo = s_tegola_proc_netstat_ifo.replaceAll("\\s+", ",");
//                            Log.i(TAG, "tegola_process_monitor_thread: got netstat ifo for tegola process (PID " + m_process_tegola_pid + "): \n'" + s_tegola_proc_netstat_ifo + "'");
//                            String[] s_netstat_cols = s_tegola_proc_netstat_ifo.split(",");
//                            Log.d(TAG, "tegola_process_monitor_thread: split netstat ifo for tegola process into " + s_netstat_cols.length + " cols, separated by commas");
//                            if (s_netstat_cols.length >= 3) {//we got the port on which tegola is listening and it's in col 3 (zero-based)
//                                String s_port = s_netstat_cols[3];
//                                int i_port_sep = s_port.lastIndexOf(":");
//                                if (i_port_sep > 0 && s_port.length() > (i_port_sep + 1)) {
//                                    s_port = s_port.substring(i_port_sep + 1);
//                                    i_port = Integer.valueOf(s_port);
//                                    Log.d(TAG, "tegola_process_monitor_thread: confirmed tegola process (PID " + m_process_tegola_pid+ ") is listening on localhost:" + i_port);
//                                }
//                            }
//                        }
//                        if (i_port == -1) {
//                            i_port = 8080;  //punt to best known default listen port
//                            Log.w(TAG, "tegola_process_monitor_thread: could not confirm listen port for tegola process (PID " + m_process_tegola_pid.toString() + "); assuming tegola is listening on default port " + i_port + " - note that this could be wrong if tegola startup is delayed");
//                        }
//                        //notify br receivers MVT_SERVER_STATE_LISTENING
//                        Intent intent_notify_server_listening = new Intent(Constants.Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER_STATE_LISTENING);
//                        intent_notify_server_listening.putExtra(Constants.Strings.INTENT.ACTION.NOTIFICATION.EXTRA_KEY.MVT_SERVER__LISTENING__PORT, i_port);
//                        fgs_asn__update(
//                                getString(R.string.started)
//                                        + (m_process_tegola_pid != -1 ? ", pid " + m_process_tegola_pid : "")
//                                        + ", listening on port " + i_port
//                        );
//                        sendBroadcast(intent_notify_server_listening);
//                    }
    }

    private boolean shell__tegola__stop() {
        boolean wasrunning = false;
        if (m_process_tegola != null) {
            Log.i(TAG, "shell__tegola__stop: destroying mvt server tegola process (pid " + m_process_tegola_pid + ")...");
            Intent intent_notify_server_stopping = new Intent(Constants.Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.STATE.STOPPING.STRING);
            fgs_asn__update(getString(R.string.stopping));
            sendBroadcast(intent_notify_server_stopping);
            m_process_tegola.destroy();
            if (m_thread_tegola_process_monitor != null) {
                try {
                    m_thread_tegola_process_monitor.join();
                } catch (InterruptedException e) {
                   // e.printStackTrace();
                }
                m_thread_tegola_process_monitor = null;
            }
            wasrunning = true;
        }
        Log.i(TAG, "shell__tegola__stop: tegola mvt server is not currently running");
        m_tegola_process_is_running = false;
        m_process_tegola = null;;
        return wasrunning;
    }
}
