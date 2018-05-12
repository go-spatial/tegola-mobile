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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.BufferedReader;
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
    private static final String TAG = FGS.class.getName();

    private BroadcastReceiver m_br_client_control_request = null;
    private IntentFilter m_filter_br_client_control_request = null;
    private final boolean m_br_client_control_request_onReceive_in_worker_thread = true;  //change to false if you want this to run on the main UI thread (but this is not a good idea as it will slow down the UI since all the work will be done there)
    private HandlerThread m_handlerthread_br_client_control_request = null;
    private Looper m_looper_handler_br_client_control_request = null;
    private Handler m_handler_br_client_control_request = null;


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

    @Override
    public int onStartCommand(Intent intent, /*@IntDef(value = {Service.START_FLAG_REDELIVERY, Service.START_FLAG_RETRY}, flag = true)*/ int flags, int startId) {
        Constants.Enums.E_INTENT_ACTION__FGS_CONTROL_REQUEST e_fgs_ctrl_request = Constants.Enums.E_INTENT_ACTION__FGS_CONTROL_REQUEST.fromString(intent != null ? intent.getAction() : null);
        if (e_fgs_ctrl_request != null) {
            switch (e_fgs_ctrl_request) {
                case FGS__START_FOREGROUND: {
                    String s_class_harness = intent.getStringExtra(Constants.Strings.INTENT.ACTION.FGS_CONTROL_REQUEST.EXTRA__KEY.FGS__START_FOREGROUND__HARNESS);
                    Log.i(TAG, "onStartCommand: Received FGS__START_FOREGROUND request from harness " + s_class_harness);

                    Intent intent_notify_service_starting = new Intent(Constants.Strings.INTENT.ACTION.CTRLR_NOTIFICATION.CONTROLLER__FOREGROUND_STARTING);
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

                    Log.d(TAG, "onStartCommand: ASN dispatched and FGS started - init'ing...");
                    init();

                    Log.d(TAG, "onStartCommand: start sequence complete - notifying harness...");
                    Intent intent_notify_service_started = new Intent(Constants.Strings.INTENT.ACTION.CTRLR_NOTIFICATION.CONTROLLER__FOREGROUND_STARTED);
                    sendBroadcast(intent_notify_service_started);
                    break;
                }
                case FGS__STOP_FOREGROUND: {
                    Log.i(TAG, "Received FGS__STOP_FOREGROUND request");
                    stop_tegola();
                    stopForeground(true);
                    stopSelf();
                }
                default: {
                    break;
                }
            }
        }
        return START_REDELIVER_INTENT;
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
            .setSmallIcon(R.drawable.ic_stat_satellite_black)
            .setLargeIcon(Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher), 128, 128, false))
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
        //check for existence in app private files directory of executable tegola binary for this device ABI
        File f_filesDir = getFilesDir();
        try {
            final Constants.Enums.CPU_ABI e_device_abi = Constants.Enums.CPU_ABI.fromDevice();
            if (e_device_abi == null)
                throw new Exceptions.UnsupportedCPUABIException(Build.CPU_ABI);
            final Constants.Enums.TEGOLA_BIN e_tegola_bin = Constants.Enums.TEGOLA_BIN.get_for(e_device_abi);
            if (e_tegola_bin == null)
                throw new Exceptions.UnsupportedCPUABIException(Build.CPU_ABI);
            Log.d(TAG, "init: tegola bin is " + e_tegola_bin.name() + " for CPU_ABI " + e_device_abi.toString());
            File f_tegola_bin_executable = new File(f_filesDir.getPath() + "/" + e_tegola_bin.name());
            if (!f_tegola_bin_executable.exists()) {
                Log.d(TAG, "init: transferring " + e_tegola_bin.name() + " raw res to private files dir...");
                Utils.Files.copy_raw_res_to_app_file(getApplicationContext(), e_tegola_bin.raw_res_id(), e_tegola_bin.name(), Utils.Files.TM_APP_FILE_TYPE.PRIVATE);
                if (f_tegola_bin_executable.exists() && !f_tegola_bin_executable.setExecutable(true))
                    throw new Exceptions.TegolaBinaryNotExecutableException(e_tegola_bin.name());
            }
            Log.d(TAG, "init: " + e_tegola_bin.name() + " " + (f_tegola_bin_executable.exists() ? "exists" : "transfer to files dir failed!"));
        } catch (Exceptions.UnsupportedCPUABIException e) {
            e.printStackTrace();
        } catch (Exceptions.TegolaBinaryNotExecutableException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        //grab tegola bin version string from tegola_version.properties output from latest bin build
        String s_version = Utils.getAssetProperty(getApplicationContext(), "tegola_version.properties", "TEGOLA_BIN_VER");
        if (s_version != null)
            Constants.Enums.TEGOLA_BIN.set_version_string(s_version);

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
        m_filter_br_client_control_request.addAction(Constants.Strings.INTENT.ACTION.MVT_SERVER_CONTROL_REQUEST.MVT_SERVER__START);
        m_filter_br_client_control_request.addAction(Constants.Strings.INTENT.ACTION.MVT_SERVER_CONTROL_REQUEST.MVT_SERVER__STOP);
        m_br_client_control_request = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent != null) {
                    Log.d(TAG, "m_br_client_control_request received: " + intent.getAction());
                    Constants.Enums.E_INTENT_ACTION__MVT_SERVER_CONTROL_REQUEST e_mvt_srvr_ctrl_request = Constants.Enums.E_INTENT_ACTION__MVT_SERVER_CONTROL_REQUEST.fromString(intent != null ? intent.getAction() : null);
                    switch (e_mvt_srvr_ctrl_request) {
                        case MVT_SERVER__START: {
                            MVT_SERVER_START_SPEC server_start_spec = null;
                            if (intent.getBooleanExtra(Constants.Strings.INTENT.ACTION.MVT_SERVER_CONTROL_REQUEST.EXTRA__KEY.MVT_SERVER__START__PROVIDER__IS_GPKG, false))
                                server_start_spec = new MVT_SERVER_START_SPEC__GPKG_PROVIDER(
                                        intent.getStringExtra(Constants.Strings.INTENT.ACTION.MVT_SERVER_CONTROL_REQUEST.EXTRA__KEY.MVT_SERVER__START__GPKG_PROVIDER__BUNDLE)
                                );
                            else {
                                server_start_spec = new MVT_SERVER_START_SPEC__POSTGIS_PROVIDER(
                                        intent.getBooleanExtra(Constants.Strings.INTENT.ACTION.MVT_SERVER_CONTROL_REQUEST.EXTRA__KEY.MVT_SERVER__START__CONFIG__IS_REMOTE, false)
                                        , intent.getStringExtra(Constants.Strings.INTENT.ACTION.MVT_SERVER_CONTROL_REQUEST.EXTRA__KEY.MVT_SERVER__START__CONFIG__PATH)
                                );
                            }
                            handle_mvt_server_control_request__start(server_start_spec);
                            break;
                        }
                        case MVT_SERVER__STOP: {
                            handle_mvt_server_control_request__stop();
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
        if (m_br_client_control_request_onReceive_in_worker_thread) {
            m_handlerthread_br_client_control_request = new HandlerThread("Thread_BroadcastReceiver_CliCtrlRequest_TCS");
            m_handlerthread_br_client_control_request.start();
            m_looper_handler_br_client_control_request = m_handlerthread_br_client_control_request.getLooper();
        } else
            m_looper_handler_br_client_control_request = getApplicationContext().getMainLooper();  //then this is the looper for the main ui thread - hence onReceive() of broadcast receiver runs in main ui's thread
        m_handler_br_client_control_request = new Handler(m_looper_handler_br_client_control_request);
        getApplicationContext().registerReceiver(m_br_client_control_request, m_filter_br_client_control_request, null, m_handler_br_client_control_request);
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

    private abstract class MVT_SERVER_START_SPEC {
        public final boolean provider__is_gpkg;
        public final boolean config_toml__is_remote;
        private MVT_SERVER_START_SPEC(final boolean provider__is_gpkg, final boolean config_toml__is_remote) {
            this.provider__is_gpkg = provider__is_gpkg;
            this.config_toml__is_remote = config_toml__is_remote;
        }
    }
    private class MVT_SERVER_START_SPEC__POSTGIS_PROVIDER extends MVT_SERVER_START_SPEC {
        public final String config_toml;
        public MVT_SERVER_START_SPEC__POSTGIS_PROVIDER(final boolean config_toml__is_remote, @NonNull final String config_toml) {
            super(false, config_toml__is_remote);
            this.config_toml = config_toml;
        }
    }
    private class MVT_SERVER_START_SPEC__GPKG_PROVIDER extends MVT_SERVER_START_SPEC {
        public final String gpkg_bundle;
        public MVT_SERVER_START_SPEC__GPKG_PROVIDER(final String gpkg_bundle) {
            super(true, false);
            this.gpkg_bundle = gpkg_bundle;
        }
    }
    public class UnknownMVTServerStartSpecType extends Exception {
        public UnknownMVTServerStartSpecType(@NonNull final MVT_SERVER_START_SPEC unknown) {
            super(unknown.getClass().getName());
        }
    }
    private void handle_mvt_server_control_request__start(@NonNull final MVT_SERVER_START_SPEC server_start_spec) {
        try {
            final Constants.Enums.TEGOLA_BIN e_tegola_bin = Constants.Enums.TEGOLA_BIN.get_for(Constants.Enums.CPU_ABI.fromDevice());   //this line just asserts tegola bin supports this device's ABI
            start_tegola(server_start_spec);  //note that this function internally handles sending the MVT_SERVER__STARTING and MVT_SERVER__STARTED notifications - on failure an exception will be thrown on the SEH below will send the failure notification in that case
        } catch (IOException e) {
            e.printStackTrace();
            Intent intent_notify_mvt_server_start_failed = new Intent(Constants.Strings.INTENT.ACTION.CTRLR_NOTIFICATION.MVT_SERVER__START_FAILED);
            intent_notify_mvt_server_start_failed.putExtra(Constants.Strings.INTENT.ACTION.CTRLR_NOTIFICATION.EXTRA__KEY.MVT_SERVER__START_FAILED__REASON, e.getMessage());
            fgs_asn__update(getString(R.string.stopped));
            sendBroadcast(intent_notify_mvt_server_start_failed);
        } catch (Exceptions.UnsupportedCPUABIException e) {
            e.printStackTrace();
            Intent intent_notify_mvt_server_start_failed = new Intent(Constants.Strings.INTENT.ACTION.CTRLR_NOTIFICATION.MVT_SERVER__START_FAILED);
            intent_notify_mvt_server_start_failed.putExtra(Constants.Strings.INTENT.ACTION.CTRLR_NOTIFICATION.EXTRA__KEY.MVT_SERVER__START_FAILED__REASON, e.getMessage());
            fgs_asn__update(getString(R.string.stopped));
            sendBroadcast(intent_notify_mvt_server_start_failed);
        } catch (Exceptions.InvalidTegolaArgumentException e) {
            e.printStackTrace();
            Intent intent_notify_mvt_server_start_failed = new Intent(Constants.Strings.INTENT.ACTION.CTRLR_NOTIFICATION.MVT_SERVER__START_FAILED);
            intent_notify_mvt_server_start_failed.putExtra(Constants.Strings.INTENT.ACTION.CTRLR_NOTIFICATION.EXTRA__KEY.MVT_SERVER__START_FAILED__REASON, e.getMessage());
            fgs_asn__update(getString(R.string.stopped));
            sendBroadcast(intent_notify_mvt_server_start_failed);
        } catch (UnknownMVTServerStartSpecType e) {
            e.printStackTrace();
            Intent intent_notify_mvt_server_start_failed = new Intent(Constants.Strings.INTENT.ACTION.CTRLR_NOTIFICATION.MVT_SERVER__START_FAILED);
            intent_notify_mvt_server_start_failed.putExtra(Constants.Strings.INTENT.ACTION.CTRLR_NOTIFICATION.EXTRA__KEY.MVT_SERVER__START_FAILED__REASON, e.getMessage());
            fgs_asn__update(getString(R.string.stopped));
            sendBroadcast(intent_notify_mvt_server_start_failed);
        }
    }

    private void handle_mvt_server_control_request__stop() {
        stop_tegola();
    }

    private Process m_logcat_process = null;

    private boolean start_tegola(@NonNull final MVT_SERVER_START_SPEC server_start_spec) throws IOException, Exceptions.UnsupportedCPUABIException, Exceptions.InvalidTegolaArgumentException, UnknownMVTServerStartSpecType {
        m_process_tegola_pid = null;
        final Constants.Enums.TEGOLA_BIN e_tegola_bin = Constants.Enums.TEGOLA_BIN.get_for(Constants.Enums.CPU_ABI.fromDevice());
        final File
                f_filesDir = getFilesDir()
                , f_tegola_bin_executable = new File(f_filesDir.getPath() + File.separator + e_tegola_bin.name());
        final String
                s_tegola_bin_executable_path = f_tegola_bin_executable.getCanonicalPath()
                ;
        if (!f_tegola_bin_executable.exists())
            throw new FileNotFoundException("tegola bin file " + s_tegola_bin_executable_path + " does not exist");
        Log.d(TAG, "start_tegola: found/using tegola bin: " + s_tegola_bin_executable_path);
        ArrayList<String> als_cmd_line = new ArrayList<String>();
        als_cmd_line.add("./" + f_tegola_bin_executable.getName());
        als_cmd_line.add("serve");
        ProcessBuilder pb = new ProcessBuilder();
        pb = pb.directory(f_tegola_bin_executable.getParentFile());

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
            Log.d(TAG, "start_tegola: found/using gpkg-bundle: " + f_gpkg_bundle.getName());

            //process version.properties file for this gpk-bundle
            File f_gpkg_bundle_ver_props = new File(f_gpkg_bundle.getPath(), Constants.Strings.GPKG_BUNDLE_VERSION_PROPS__FNAME);
            if (!f_gpkg_bundle_ver_props.exists())
                throw new FileNotFoundException("geopcackage-bundle version.properties file " + f_gpkg_bundle_ver_props.getCanonicalPath() + " not found");
            Log.d(TAG, "start_tegola: found/using gpkg-bundle version.properties: " + f_gpkg_bundle_ver_props.getCanonicalPath());
            //get gpkg-bundle toml file spec from version.props, confirm existence, then build "--config" arg for tegola commandline
            String s_gpkg_bundle__toml = Utils.getProperty(f_gpkg_bundle_ver_props, Constants.Strings.GPKG_BUNDLE_VERSION_PROPS_PROP_NAME__TOML_FILE);
            f_gpkg_bundle__toml = new File(f_gpkg_bundle.getPath(), s_gpkg_bundle__toml);
            Log.d(TAG, "start_tegola: version.properties: " + Constants.Strings.GPKG_BUNDLE_VERSION_PROPS_PROP_NAME__TOML_FILE + " == " + f_gpkg_bundle__toml.getCanonicalPath());
            if (!f_gpkg_bundle__toml.exists())
                throw new FileNotFoundException("geopcackage-bundle toml file " + f_gpkg_bundle__toml.getCanonicalPath() + " not found");
            Log.d(TAG, "start_tegola: \tfound/using gpkg-bundle toml file: " + f_gpkg_bundle__toml.getCanonicalPath());
            als_cmd_line.add("--" + Constants.Strings.TEGOLA_ARG.CONFIG);
            als_cmd_line.add(f_gpkg_bundle__toml.getCanonicalPath());

            //get gpkg-bundle geopcackages spec from version.props, then confirm existence
            String[] s_list_geopcackage_files = Utils.getProperty(f_gpkg_bundle_ver_props, Constants.Strings.GPKG_BUNDLE_VERSION_PROPS_PROP_NAME__GPKG_FILES).split(",");
            String[] s_list_geopcackage_path_env_vars = Utils.getProperty(f_gpkg_bundle_ver_props, Constants.Strings.GPKG_BUNDLE_VERSION_PROPS_PROP_NAME__GPKG_PATH_ENV_VARS).split(",");
            if (s_list_geopcackage_files != null && s_list_geopcackage_files.length > 0) {
                Map<String, String> pb_env = pb.environment();
                for (int i = 0; i < s_list_geopcackage_files.length; i++) {
                    String s_gpkg_file = s_list_geopcackage_files[i];
                    f_gpkg_bundle__gpkg = new File(f_gpkg_bundle.getPath(), s_gpkg_file);
                    Log.d(TAG, "start_tegola: version.properties: " + Constants.Strings.GPKG_BUNDLE_VERSION_PROPS_PROP_NAME__GPKG_FILES + "[" + i + "] == " + f_gpkg_bundle__gpkg.getCanonicalPath());
                    if (!f_gpkg_bundle__gpkg.exists())
                        throw new FileNotFoundException("geopcackage-bundle gpkg file " + f_gpkg_bundle__gpkg.getCanonicalPath() + " not found");
                    pb_env.put(s_list_geopcackage_path_env_vars[i], f_gpkg_bundle__gpkg.getCanonicalPath());
                    Log.d(TAG, "start_tegola: \tset pb env " + s_list_geopcackage_path_env_vars[i] + " to \"" + pb_env.get(s_list_geopcackage_path_env_vars[i]) + "\"");
                }
            } else {
                throw new FileNotFoundException("failed to retrieve list of geopackage files from gpkg-bundle version.properties file " + f_gpkg_bundle_ver_props.getCanonicalPath());
            }
        } else if (server_start_spec instanceof MVT_SERVER_START_SPEC__POSTGIS_PROVIDER) {
            final MVT_SERVER_START_SPEC__POSTGIS_PROVIDER server_start_spec_postgis_provider = (MVT_SERVER_START_SPEC__POSTGIS_PROVIDER)server_start_spec;
            if (server_start_spec_postgis_provider.config_toml__is_remote)
                throw new Exceptions.InvalidTegolaArgumentException("start spec requests remote config \"" + server_start_spec_postgis_provider.config_toml + "\"for postgis provider but this is temporarily not supported in Tegola Mobile");
            else {
                if (server_start_spec_postgis_provider.config_toml == null || server_start_spec_postgis_provider.config_toml.isEmpty())
                    throw new Exceptions.InvalidTegolaArgumentException("argument \"" + Constants.Strings.TEGOLA_ARG.CONFIG + "\" is null or empty");
                final File f_postgis_config_toml = new File(server_start_spec_postgis_provider.config_toml);
                if (f_postgis_config_toml != null && !f_postgis_config_toml.exists())
                    throw new FileNotFoundException("toml file " + f_postgis_config_toml.getCanonicalPath() + " not found");
                Log.d(TAG, "start_tegola: found/using config toml file: " + f_postgis_config_toml.getCanonicalPath());
                als_cmd_line.add("--" + Constants.Strings.TEGOLA_ARG.CONFIG);
                als_cmd_line.add(f_postgis_config_toml.getCanonicalPath());
            }
        } else
            throw new UnknownMVTServerStartSpecType(server_start_spec);
        pb = pb.command(als_cmd_line);

        stop_tegola();

        Log.i(TAG, "start_tegola: starting new tegola server process...");
        //notify br_receivers (if any) server starting
        Intent intent_notify_server_starting = new Intent(Constants.Strings.INTENT.ACTION.CTRLR_NOTIFICATION.MVT_SERVER__STARTING);
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
        Log.d(TAG, "start_tegola: starting tegola server process (cmdline is '" + s_cmdline + "' and will run in " + s_working_dir + ")...");
        m_process_tegola = pb.start();

        //immediately notify br receivers MVT_SERVER__STOPPED if we fail to create tegola process
        if (m_process_tegola == null) {
            m_thread_tegola_process_stdout_monitor = null;
            m_thread_tegola_process_stderr_monitor = null;
            m_process_tegola = null;
            m_tegola_process_is_running = false;
            Intent intent_notify_server_stopped = new Intent(Constants.Strings.INTENT.ACTION.CTRLR_NOTIFICATION.MVT_SERVER__STOPPED);
            fgs_asn__update(getString(R.string.stopped));
            sendBroadcast(intent_notify_server_stopped);
            return false;
        }

        m_tegola_process_is_running = true;

        //get tegola pid if we can - may not work since "pid" is private field of Process, obtained via reflection...
        m_process_tegola_pid = getPid(m_process_tegola);
        Log.i(TAG, "start_tegola: tegola server process " + (m_process_tegola_pid != -1 ? "(pid " + m_process_tegola_pid + ") ": "") + "started");

        //notify br receivers MVT_SERVER__STARTED
        Intent intent_notify_server_started = new Intent(Constants.Strings.INTENT.ACTION.CTRLR_NOTIFICATION.MVT_SERVER__STARTED);
        intent_notify_server_started.putExtra(Constants.Strings.INTENT.ACTION.CTRLR_NOTIFICATION.EXTRA__KEY.MVT_SERVER__STARTED__VERSION, getString(R.string.srvr_ver));
        if (m_process_tegola_pid != -1)
            intent_notify_server_started.putExtra(Constants.Strings.INTENT.ACTION.CTRLR_NOTIFICATION.EXTRA__KEY.MVT_SERVER__STARTED__PID, m_process_tegola_pid.intValue());
        fgs_asn__update(getString(R.string.started) + (m_process_tegola_pid != -1 ? ", pid " + m_process_tegola_pid: ""));
        sendBroadcast(intent_notify_server_started);

        //now start tegola logcat monitor specific to tegola process (before stderr and stdout monitors since there is always the possibility tegola could segfault or trigger some other native signal)
        if (m_process_tegola_pid != -1) {
            sb_cmdline = new StringBuilder();
            sb_cmdline.append("logcat");
            sb_cmdline.append(" -v thread");
            s_cmdline = sb_cmdline.toString();

            //the crux: start logcat process!
            Log.d(TAG, "start_tegola: starting logcat process (cmdline: '" + s_cmdline + "')...");
            m_logcat_process = Runtime.getRuntime().exec(s_cmdline);

            if (m_logcat_process != null) {
                final int pid_logcat = getPid(m_logcat_process);
                Log.i(TAG, "start_tegola: logcat process (" + pid_logcat + ") started");

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
                                                        Intent intent_notify_server_output_logcat = new Intent(Constants.Strings.INTENT.ACTION.CTRLR_NOTIFICATION.MVT_SERVER__OUTPUT__LOGCAT);
                                                        intent_notify_server_output_logcat.putExtra(Constants.Strings.INTENT.ACTION.CTRLR_NOTIFICATION.EXTRA__KEY.MVT_SERVER__OUTPUT__LOGCAT__LINE, s_line);
                                                        sendBroadcast(intent_notify_server_output_logcat);
                                                    }
                                                }
                                                Thread.currentThread().sleep(100);
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
                                                e.printStackTrace();
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
                                Intent intent_notify_server_output_stderr = new Intent(Constants.Strings.INTENT.ACTION.CTRLR_NOTIFICATION.MVT_SERVER__OUTPUT__STDERR);
                                intent_notify_server_output_stderr.putExtra(Constants.Strings.INTENT.ACTION.CTRLR_NOTIFICATION.EXTRA__KEY.MVT_SERVER__OUTPUT__STDERR__LINE, s_line);
                                sendBroadcast(intent_notify_server_output_stderr);

                                if (!found_listen_port) {
                                    if (s_line.contains(s_key_listen_port)) {
                                        found_listen_port = true;
                                        String s_port = s_line.substring(s_line.indexOf(s_key_listen_port) + s_key_listen_port.length());  //now contains only "starting tegola server on port :xxxx"
                                        s_port = s_port.substring(s_port.indexOf(":") + 1).trim();
                                        Log.d(TAG, "tegola_STDERR_output: confirmed tegola process (PID " + m_process_tegola_pid+ ") is listening on localhost:" + s_port);

                                        //notify br receivers MVT_SERVER__LISTENING
                                        int i_port = Integer.valueOf(s_port);
                                        Intent intent_notify_server_listening = new Intent(Constants.Strings.INTENT.ACTION.CTRLR_NOTIFICATION.MVT_SERVER__LISTENING);
                                        intent_notify_server_listening.putExtra(Constants.Strings.INTENT.ACTION.CTRLR_NOTIFICATION.EXTRA__KEY.MVT_SERVER__LISTENING__PORT, i_port);
                                        fgs_asn__update(
                                                getString(R.string.started)
                                                        + (m_process_tegola_pid != -1 ? ", pid " + m_process_tegola_pid : "")
                                                        + ", listening on port " + i_port
                                        );
                                        sendBroadcast(intent_notify_server_listening);
                                    }
                                }
                            }
                            Thread.currentThread().sleep(100);
                        } catch (IOException e) {
//                            e.printStackTrace();
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
                                Intent intent_notify_server_output_stdout = new Intent(Constants.Strings.INTENT.ACTION.CTRLR_NOTIFICATION.MVT_SERVER__OUTPUT__STDOUT);
                                intent_notify_server_output_stdout.putExtra(Constants.Strings.INTENT.ACTION.CTRLR_NOTIFICATION.EXTRA__KEY.MVT_SERVER__OUTPUT__STDOUT__LINE, s_line);
                                sendBroadcast(intent_notify_server_output_stdout);
                            }
                            Thread.currentThread().sleep(100);
                        } catch (IOException e) {
//                            e.printStackTrace();
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
//                        //notify br receivers MVT_SERVER__LISTENING
//                        Intent intent_notify_server_listening = new Intent(Constants.Strings.INTENT.ACTION.CTRLR_NOTIFICATION.MVT_SERVER__LISTENING);
//                        intent_notify_server_listening.putExtra(Constants.Strings.INTENT.ACTION.CTRLR_NOTIFICATION.EXTRA__KEY.MVT_SERVER__LISTENING__PORT, i_port);
//                        fgs_asn__update(
//                                getString(R.string.started)
//                                        + (m_process_tegola_pid != -1 ? ", pid " + m_process_tegola_pid : "")
//                                        + ", listening on port " + i_port
//                        );
//                        sendBroadcast(intent_notify_server_listening);
//                    }
                    m_process_tegola.waitFor();
                    Log.i(TAG, "tegola_process_monitor_thread: tegola mvt server process stopped");
                } catch (InterruptedException e) {
                    //e.printStackTrace();
                } finally {
                    m_process_tegola = null;
                    m_process_tegola_pid = null;
                    m_tegola_process_is_running = false;

                    int n_thread_cleanup_wait_ms = 250;

                    m_thread_tegola_process_stdout_monitor.interrupt();
                    try {
                        Log.i(TAG, "tegola_process_monitor_thread: interrupted stdout monitor thread and waiting " + n_thread_cleanup_wait_ms + " ms for thread to exit");
                        m_thread_tegola_process_stdout_monitor.join(n_thread_cleanup_wait_ms);
                    } catch (InterruptedException e) {
                        //e.printStackTrace();
                    }
                    m_thread_tegola_process_stdout_monitor = null;
                    Log.i(TAG, "tegola_process_monitor_thread: m_thread_tegola_process_stdout_monitor thread stopped or " + n_thread_cleanup_wait_ms + " ms wait to stop expired");

                    m_thread_tegola_process_stderr_monitor.interrupt();
                    try {
                        Log.i(TAG, "tegola_process_monitor_thread: interrupted stderr monitor thread and waiting " + n_thread_cleanup_wait_ms + " ms for thread to exit");
                        m_thread_tegola_process_stderr_monitor.join(n_thread_cleanup_wait_ms);
                    } catch (InterruptedException e) {
                        //e.printStackTrace();
                    }
                    m_thread_tegola_process_stderr_monitor = null;
                    Log.i(TAG, "tegola_process_monitor_thread: m_thread_tegola_process_stderr_monitor thread stopped or " + n_thread_cleanup_wait_ms + " ms wait to stop expired");

                    m_logcat_process.destroy();
                    try {
                        Log.i(TAG, "tegola_process_monitor_thread: destroyed logcat process and waiting " + n_thread_cleanup_wait_ms + " ms for monitor thread to exit");
                        m_thread_logcat_process_monitor.join(n_thread_cleanup_wait_ms);
                    } catch (InterruptedException e) {
                        //e.printStackTrace();
                    }
                    m_thread_logcat_process_monitor = null;
                    m_logcat_process = null;
                    Log.i(TAG, "tegola_process_monitor_thread: m_thread_logcat_process_monitor thread stopped or " + n_thread_cleanup_wait_ms + " ms wait to stop expired");

                    Intent intent_notify_server_stopped = new Intent(Constants.Strings.INTENT.ACTION.CTRLR_NOTIFICATION.MVT_SERVER__STOPPED);
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

    private boolean stop_tegola() {
        if (m_process_tegola != null) {
            Log.i(TAG, "stop_tegola: destroying mvt server tegola process (pid " + m_process_tegola_pid + ")...");
            Intent intent_notify_server_stopping = new Intent(Constants.Strings.INTENT.ACTION.CTRLR_NOTIFICATION.MVT_SERVER__STOPPING);
            fgs_asn__update(getString(R.string.stopping));
            sendBroadcast(intent_notify_server_stopping);
            m_process_tegola.destroy();
            if (m_thread_tegola_process_monitor != null) {
                try {
                    m_thread_tegola_process_monitor.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                m_thread_tegola_process_monitor = null;
                m_tegola_process_is_running = false;
            }
            return true;
        }
        Log.i(TAG, "stop_tegola: tegola mvt server is not currently running");
        m_tegola_process_is_running = false;
        return false;
    }
}
