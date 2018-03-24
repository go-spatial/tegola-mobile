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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;


public class ControllerFGS extends Service {
    private static final String TAG = ControllerFGS.class.getName();

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
                    startForeground(Constants.ASNB_NOTIFICATIONS.FGS_NB_ID, fgs_asn__prepare(getString(R.string.fgs_asn_title), getString(R.string.stopped)));

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

        return new NotificationCompat.Builder(this)
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
        asn_mgr.notify(Constants.ASNB_NOTIFICATIONS.FGS_NB_ID, fgs_asn__prepare(s_title, s_status));
    }
    private void fgs_asn__update(final String s_status) {
        NotificationManager asn_mgr = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        asn_mgr.notify(Constants.ASNB_NOTIFICATIONS.FGS_NB_ID, fgs_asn__prepare(getString(R.string.fgs_asn_title), s_status));
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

        //grab tegola bin version string from version.properties output from latest bin build
        InputStream f_inputstream_version_props = null;
        try {
            f_inputstream_version_props = getApplicationContext().getAssets().open("version.properties");
            String s_version = Utils.getProperty(f_inputstream_version_props, "TEGOLA_BIN_VER");
            if (s_version != null)
                Constants.Enums.TEGOLA_BIN.set_version_string(s_version);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (f_inputstream_version_props != null) {
                try {
                    f_inputstream_version_props.close();
                } catch (IOException e) {}
            }
        }

        //create geopackage bundle dir
        try {
            File f_gpkg_root_dir = Utils.Files.F_GPKG_DIR.getInstance(getApplicationContext());
            if (!f_gpkg_root_dir.exists()) {
                Log.d(TAG, "init: creating gepackage-bundle root directory " + f_gpkg_root_dir.getPath());
                f_gpkg_root_dir.mkdir();
            }
            Log.d(TAG, "init: gepackage-bundle root directory " + f_gpkg_root_dir.getPath() + " " + (f_gpkg_root_dir.exists() ? "exists" : "does not exist"));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        //set BR to listen for client mvt-server-control-request
        m_filter_br_client_control_request = new IntentFilter();
        m_filter_br_client_control_request.addAction(Constants.Strings.INTENT.ACTION.MVT_SERVER_CONTROL_REQUEST.MVT_SERVER__START);
        m_filter_br_client_control_request.addAction(Constants.Strings.INTENT.ACTION.MVT_SERVER_CONTROL_REQUEST.MVT_SERVER__STOP);
        m_br_client_control_request = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Constants.Enums.E_INTENT_ACTION__MVT_SERVER_CONTROL_REQUEST e_mvt_srvr_ctrl_request = Constants.Enums.E_INTENT_ACTION__MVT_SERVER_CONTROL_REQUEST.fromString(intent != null ? intent.getAction() : null);
                switch (e_mvt_srvr_ctrl_request) {
                    case MVT_SERVER__START: {
                        Log.i(TAG, "Received MVT_SERVER__START request");
                        handle_mvt_server_control_request__start(
                                intent.getStringExtra(Constants.Strings.INTENT.ACTION.MVT_SERVER_CONTROL_REQUEST.EXTRA__KEY.MVT_SERVER__START__CONFIG)
                                , intent.getBooleanExtra(Constants.Strings.INTENT.ACTION.MVT_SERVER_CONTROL_REQUEST.EXTRA__KEY.MVT_SERVER__START__CONFIG__REMOTE, false)
                        );
                        break;
                    }
                    case MVT_SERVER__STOP: {
                        Log.i(TAG, "Received MVT_SERVER__STOP request");
                        handle_mvt_server_control_request__stop();
                        break;
                    }
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
            , m_thread_tegola_process_logcat_monitor = null;
    private volatile boolean m_tegola_process_is_running = false;

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

    private void handle_mvt_server_control_request__start(@NonNull final String s_config_toml, final boolean remote_config) {
        try {
            final Constants.Enums.TEGOLA_BIN e_tegola_bin = Constants.Enums.TEGOLA_BIN.get_for(Constants.Enums.CPU_ABI.fromDevice());
            start_tegola(s_config_toml, remote_config);  //note that this function internally handles sending the MVT_SERVER__STARTING and MVT_SERVER__STARTED notifications - on failure an exception will be thrown on the SEH below will send the failure notification in that case
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
        }
    }

    private void handle_mvt_server_control_request__stop() {
        stop_tegola();
    }

    private boolean start_tegola(final String s_config_toml_path, final boolean remote_config) throws IOException, Exceptions.UnsupportedCPUABIException, Exceptions.InvalidTegolaArgumentException {
        if (s_config_toml_path == null || s_config_toml_path.isEmpty())
            throw new Exceptions.InvalidTegolaArgumentException(Constants.Strings.TEGOLA_ARG.CONFIG + ": is null or empty");
        final Constants.Enums.TEGOLA_BIN e_tegola_bin = Constants.Enums.TEGOLA_BIN.get_for(Constants.Enums.CPU_ABI.fromDevice());
        File
                f_filesDir = getFilesDir()
                , f_tegola_bin_executable = new File(f_filesDir.getPath() + File.separator + e_tegola_bin.name())
                , f_tegola_config_toml = (remote_config ? null : new File(s_config_toml_path));
        final String
                s_tegola_bin_executable_path = f_tegola_bin_executable.getPath()
                , s_tegola_config_toml_path = (remote_config ? s_config_toml_path : f_tegola_config_toml.getPath());
        if (!f_tegola_bin_executable.exists()) {
            throw new FileNotFoundException("tegola bin file " + s_tegola_bin_executable_path + " does not exist");
        } else {
            Log.d(TAG, "start_tegola: found/using tegola bin: " + s_tegola_bin_executable_path);
        }
        if (!(!remote_config && f_tegola_config_toml.exists())) {
            throw new FileNotFoundException("tegola config file " + s_tegola_config_toml_path + " does not exist");
        } else {
            if (remote_config) {
                Log.d(TAG, "start_tegola: start spec requests remote config for postgis provider but this is not currently supported!");
            } else {
                Log.d(TAG, "start_tegola: found/using config toml file: " + s_config_toml_path);
            }
        }

        stop_tegola();

        Log.i(TAG, "start_tegola: starting new tegola server process...");
        //notify br_receivers (if any) server starting
        Intent intent_notify_server_starting = new Intent(Constants.Strings.INTENT.ACTION.CTRLR_NOTIFICATION.MVT_SERVER__STARTING);
        fgs_asn__update(getString(R.string.starting));
        sendBroadcast(intent_notify_server_starting);

        //build and exec tegola cmd line in new process
        StringBuilder sb_cmdline = new StringBuilder();
        sb_cmdline.append(s_tegola_bin_executable_path);
        sb_cmdline.append(" --" + Constants.Strings.TEGOLA_ARG.CONFIG + "=" + s_tegola_config_toml_path);
        String s_cmdline = sb_cmdline.toString();
        Log.d(TAG, "start_tegola: tegola process cmdline is '" + s_cmdline + "'");
        m_process_tegola = Runtime.getRuntime().exec(s_cmdline);

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
        final int pid_process_tegola = getPid(m_process_tegola);
        Log.i(TAG, "start_tegola: server process " + (pid_process_tegola != -1 ? "(pid " + pid_process_tegola + ") ": "") + "started");

        //start tegola process logcat cat monitor and notify br receivers MVT_SERVER__STARTED
        Intent intent_notify_server_started = new Intent(Constants.Strings.INTENT.ACTION.CTRLR_NOTIFICATION.MVT_SERVER__STARTED);
        intent_notify_server_started.putExtra(Constants.Strings.INTENT.ACTION.CTRLR_NOTIFICATION.EXTRA__KEY.MVT_SERVER__STARTED__VERSION, getString(R.string.srvr_ver));
        if (pid_process_tegola != -1)
            intent_notify_server_started.putExtra(Constants.Strings.INTENT.ACTION.CTRLR_NOTIFICATION.EXTRA__KEY.MVT_SERVER__STARTED__PID, pid_process_tegola);
        fgs_asn__update(getString(R.string.started) + (pid_process_tegola != -1 ? ", pid " + pid_process_tegola: ""));
        sendBroadcast(intent_notify_server_started);

        //now start tegola logcat monitor specifc to tegola process (before stderr and stdout monitors since there is always the possibility tegola could segfault or trigger some other native signal)
        if (pid_process_tegola != -1) {
            sb_cmdline = new StringBuilder();
            sb_cmdline.append("logcat");
            sb_cmdline.append(" -v thread");
            s_cmdline = sb_cmdline.toString();
            Log.d(TAG, "start_tegola: tegola process (pid " + pid_process_tegola + ") logcat monitor process cmdline is '" + s_cmdline + "'");
            final Process tegola_logcat_monitor_process = Runtime.getRuntime().exec(s_cmdline);
            Log.i(TAG, "start_tegola: tegola logcat monitor process started");
            if (tegola_logcat_monitor_process != null) {
                m_thread_tegola_process_logcat_monitor = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG, "tegola_process_logcat_monitor_thread: thread started");
                        InputStream input_stream_logcat_monitor_process = tegola_logcat_monitor_process != null ? tegola_logcat_monitor_process.getInputStream() : null;
                        if (input_stream_logcat_monitor_process != null) {
                            Log.d(TAG, "tegola_process_logcat_monitor_thread: got ref to tegola logcat monitor process inputstream");
                            BufferedReader reader_logcat_monitor_process = new BufferedReader(new InputStreamReader(input_stream_logcat_monitor_process));
                            String s_line = "";
                            while (!Thread.currentThread().isInterrupted()) {
                                try {
                                    while ((s_line = reader_logcat_monitor_process.readLine()) != null) {
                                        if (s_line.contains(pid_process_tegola + ":")) {
                                            Intent intent_notify_server_output_logcat = new Intent(Constants.Strings.INTENT.ACTION.CTRLR_NOTIFICATION.MVT_SERVER__OUTPUT__LOGCAT);
                                            intent_notify_server_output_logcat.putExtra(Constants.Strings.INTENT.ACTION.CTRLR_NOTIFICATION.EXTRA__KEY.MVT_SERVER__OUTPUT__LOGCAT__LINE, s_line);
                                            sendBroadcast(intent_notify_server_output_logcat);
                                        }
                                    }
                                    Thread.sleep(100);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                } catch (InterruptedException e1) {
                                    //e.printStackTrace();
                                    if (reader_logcat_monitor_process != null) {
                                        try {
                                            reader_logcat_monitor_process.close();
                                        } catch (IOException e2) {
                                            //e2.printStackTrace();
                                        }
                                    }
                                    Log.d(TAG, "tegola_process_logcat_monitor_thread: thread interrupted");
                                    Thread.currentThread().interrupt();
                                }
                            }
                        } else {
                            Log.e(TAG, "tegola_process_logcat_monitor_thread: could not get ref to tegola logcat monitor process inputstream!");
                        }
                        Log.d(TAG, "tegola_process_logcat_monitor_thread: thread exiting");
                        tegola_logcat_monitor_process.destroy();
                    }
                });
                m_thread_tegola_process_logcat_monitor.start();
            }
        }

        //start tegola process stderr monitor
        m_thread_tegola_process_stderr_monitor = new Thread(new Runnable() {
            @Override
            public void run() {
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
                            }
                            Thread.sleep(100);
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
                            Thread.sleep(100);
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
                    m_process_tegola.waitFor();
                    Log.i(TAG, "tegola_process_monitor_thread: process stopped; interrupting monitor threads...");
                    Thread.sleep(1000); //give a little bit of time to allow any remaining output to be flushed - no synchronization but this should suffice for now
                    m_thread_tegola_process_stdout_monitor.interrupt();
                    m_thread_tegola_process_stderr_monitor.interrupt();
                    m_thread_tegola_process_logcat_monitor.interrupt();
                } catch (InterruptedException e) {
                    //e.printStackTrace();
                } finally {
                    m_thread_tegola_process_stdout_monitor = null;
                    m_thread_tegola_process_stderr_monitor = null;
                    m_thread_tegola_process_logcat_monitor = null;
                    m_process_tegola = null;
                    m_tegola_process_is_running = false;
                    Intent intent_notify_server_stopped = new Intent(Constants.Strings.INTENT.ACTION.CTRLR_NOTIFICATION.MVT_SERVER__STOPPED);
                    fgs_asn__update(getString(R.string.stopped));
                    sendBroadcast(intent_notify_server_stopped);
                }
            }
        });
        m_thread_tegola_process_monitor.start();

        return m_tegola_process_is_running;
    }

    private boolean stop_tegola() {
        if (m_process_tegola != null) {
            Log.i(TAG, "stop_tegola: killing current running instance of tegola mvt server...");
            Intent intent_notify_server_stopping = new Intent(Constants.Strings.INTENT.ACTION.CTRLR_NOTIFICATION.MVT_SERVER__STOPPING);
            fgs_asn__update(getString(R.string.stopping));
            sendBroadcast(intent_notify_server_stopping);
            m_process_tegola.destroy();
            if (m_thread_tegola_process_monitor != null) {
                try {
                    m_thread_tegola_process_monitor.join();
                } catch (InterruptedException e) {
                    //e.printStackTrace();
                }
                m_thread_tegola_process_monitor = null;
            }
            m_process_tegola = null;
            m_tegola_process_is_running = false;
            Intent intent_notify_server_stopped = new Intent(Constants.Strings.INTENT.ACTION.CTRLR_NOTIFICATION.MVT_SERVER__STOPPED);
            fgs_asn__update(getString(R.string.stopped));
            sendBroadcast(intent_notify_server_stopped);
            return true;
        }
        Log.i(TAG, "stop_tegola: tegola mvt server is not currently running");
        return false;
    }
}
