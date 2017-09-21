package go_spatial.com.github.tegola.mobile.android.controller;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class TCS extends Service {
    private static final String TAG = TCS.class.getName();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }



    @Override
    public int onStartCommand(Intent intent, /*@IntDef(value = {Service.START_FLAG_REDELIVERY, Service.START_FLAG_RETRY}, flag = true)*/ int flags, int startId) {
        Constants.Enums.E_CTRLR_INTENT_ACTION eIntent = Constants.Enums.E_CTRLR_INTENT_ACTION.fromString(intent != null ? intent.getAction() : null);
        if (eIntent != null) {
            switch (eIntent) {
                case CONTROLLER__START_FOREGROUND: {
                    Log.i(TAG, "Received MVT Controller Start Foreground Intent");
                    Intent notificationIntent = new Intent(this, ASNBContentActivity.class);
                    notificationIntent.setAction(Constants.Strings.CTRLR_INTENT_ACTION.MVT_SERVER__START);
                    notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

                    Intent startServerIntent = new Intent(this, TCS.class);
                    startServerIntent.setAction(Constants.Strings.CTRLR_INTENT_ACTION.MVT_SERVER__START);
                    PendingIntent pstartServerIntent = PendingIntent.getService(this, 0, startServerIntent, 0);

                    Intent stopServerIntent = new Intent(this, TCS.class);
                    stopServerIntent.setAction(Constants.Strings.CTRLR_INTENT_ACTION.MVT_SERVER__STOP);
                    PendingIntent pstopServerIntent = PendingIntent.getService(this, 0, stopServerIntent, 0);

                    Bitmap icon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);

                    Notification notification = new NotificationCompat.Builder(this)
                            .setContentTitle("Tegola MVT Server")
                            .setTicker("Tegola MVT Server")
                            .setContentText("My MVTs")
                            .setSmallIcon(R.drawable.ic_stat_satellite_black)
                            .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                            .setContentIntent(pendingIntent)
                            .setOngoing(true)
                            .addAction(android.R.drawable.ic_media_play, getString(R.string.start_server), pstartServerIntent)
                            .addAction(android.R.drawable.ic_media_pause, getString(R.string.stop_server), pstopServerIntent).build();
                    startForeground(Constants.ASNB_NOTIFICATIONS.FGS_NB_ID, notification);
                    Intent intent_notify_service_started = new Intent(Constants.Strings.CTRLR_INTENT_BR_NOTIFICATIONS.CONTROLLER__FOREGROUND_STARTED);
                    sendBroadcast(intent_notify_service_started);
                    init();
                    try {
                        start_tegola(Constants.Strings.TEGOLA_CONFIG_TOML__NORMALIZED_FNAME);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (Exceptions.UnsupportedCPUABIException e) {
                        e.printStackTrace();
                    } catch (Exceptions.InvalidTegolaArgumentException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                case MVT_SERVER__START: {
                    try {
                        start_tegola(Constants.Strings.TEGOLA_CONFIG_TOML__NORMALIZED_FNAME);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (Exceptions.UnsupportedCPUABIException e) {
                        e.printStackTrace();
                    } catch (Exceptions.InvalidTegolaArgumentException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                case MVT_SERVER__STOP: {
                    stop_tegola();
                    break;
                }
                case CONTROLLER__STOP_FOREGROUND: {
                    Log.i(TAG, "Received MVT Controller Stop Foreground Intent");
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

    private void init() {
        final Constants.Enums.TEGOLA_BIN e_tegola_bin = Constants.Enums.TEGOLA_BIN.get_for(Constants.Enums.CPU_ABI.fromDevice());
        //check for existence in app data files directory of tegola binary and config.toml
        File
                f_filesDir = getFilesDir()
                , f_tegola_bin_executable = new File(f_filesDir.getPath() + "/" + e_tegola_bin.name())
                , f_tegola_config_toml = new File(f_filesDir.getPath() + "/" + Constants.Strings.TEGOLA_CONFIG_TOML__NORMALIZED_FNAME)
                ;
        if (!f_tegola_bin_executable.exists()) {
            //transfer matching tegola binary from raw resources based on device arch
            Log.d(TAG, "init: creating executable tegola.bin from raw for " + Build.CPU_ABI + " ABI...");
            boolean btegolaexecutablecreated = false;
            try {
                btegolaexecutablecreated = make_tegola_executable();
            } catch (Exceptions.UnsupportedCPUABIException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.d(TAG, "init: make_tegola_executable() returned --> " + btegolaexecutablecreated);
        }
        Log.d(TAG, "init: " + f_tegola_bin_executable.getPath() + " exists --> " + f_tegola_bin_executable.exists());
        if (!f_tegola_config_toml.exists()) {
            //transfer default config.toml from raw resources
            Log.d(TAG, "init: transferring default config.toml from raw...");
            boolean btegoladefaultconfigtransfered = false;
            try {
                btegoladefaultconfigtransfered = transfer_tegola_default_config_toml();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.d(TAG, "init: transfer_tegola_default_config_toml() returned: " + btegoladefaultconfigtransfered);
        }
        Log.d(TAG, "init: " + f_tegola_config_toml.getPath() + " exists --> " + f_tegola_config_toml.exists());

    }

    private boolean make_tegola_executable() throws Exceptions.UnsupportedCPUABIException, IOException {
        final Constants.Enums.CPU_ABI e_device_abi = Constants.Enums.CPU_ABI.fromDevice();
        final Constants.Enums.TEGOLA_BIN e_tegola_bin = Constants.Enums.TEGOLA_BIN.get_for(e_device_abi);
        if (e_device_abi == null || e_tegola_bin == null)
            throw new Exceptions.UnsupportedCPUABIException(Build.CPU_ABI);
        Log.d(TAG, "make_tegola_executable: bin is " + e_tegola_bin.name() + " for CPU_ABI " + Build.CPU_ABI);
        InputStream inputstream_raw_tegola_bin = getResources().openRawResource(e_tegola_bin.raw_res_id());
        byte[] buf_raw_tegola = new byte[inputstream_raw_tegola_bin.available()];
        inputstream_raw_tegola_bin.read(buf_raw_tegola);
        inputstream_raw_tegola_bin.close();
        FileOutputStream f_outputstream_tegola_bin = openFileOutput(e_tegola_bin.name(), Context.MODE_PRIVATE);
        f_outputstream_tegola_bin.write(buf_raw_tegola);
        f_outputstream_tegola_bin.close();
        File f_tegola_bin_executable = new File(getFilesDir().getPath() + "/" + e_tegola_bin.name());
        return f_tegola_bin_executable.setExecutable(true);
    }

    private boolean transfer_tegola_default_config_toml() throws IOException {
        InputStream inputstream_raw_config_toml = getResources().openRawResource(R.raw.config_toml__osm);
        byte[] buf_raw_config_toml = new byte[inputstream_raw_config_toml.available()];
        inputstream_raw_config_toml.read(buf_raw_config_toml);
        inputstream_raw_config_toml.close();
        FileOutputStream f_outputstream_tegola_default_config_toml = openFileOutput(Constants.Strings.TEGOLA_CONFIG_TOML__NORMALIZED_FNAME, Context.MODE_PRIVATE);
        f_outputstream_tegola_default_config_toml.write(buf_raw_config_toml);
        f_outputstream_tegola_default_config_toml.close();
        File f_tegola_config_toml = new File(getFilesDir().getPath() + "/" + Constants.Strings.TEGOLA_CONFIG_TOML__NORMALIZED_FNAME);
        return f_tegola_config_toml.exists();
    }

    private Process m_process_tegola = null;
    private Thread
            m_thread_tegola_process_monitor = null
            , m_thread_tegola_process_stdout_monitor = null
            , m_thread_tegola_process_stderr_monitor = null;
    private volatile boolean m_tegola_process_is_running = false;

    private boolean start_tegola(final String s_fname_config) throws IOException, Exceptions.UnsupportedCPUABIException, Exceptions.InvalidTegolaArgumentException {
        final Constants.Enums.TEGOLA_BIN e_tegola_bin = Constants.Enums.TEGOLA_BIN.get_for(Constants.Enums.CPU_ABI.fromDevice());
        if (e_tegola_bin == null)
            throw new Exceptions.UnsupportedCPUABIException(Build.CPU_ABI);
        if (s_fname_config == null || s_fname_config.isEmpty())
            throw new Exceptions.InvalidTegolaArgumentException(Constants.Strings.TEGOLA_ARG.CONFIG + ": is null or empty");
        File
                f_filesDir = getFilesDir()
                , f_tegola_bin_executable = new File(f_filesDir.getPath() + "/" + e_tegola_bin.name())
                , f_tegola_config_toml = new File(f_filesDir.getPath() + "/" + s_fname_config);
        final String
                s_tegola_bin_executable_path = f_tegola_bin_executable.getPath()
                , s_tegola_config_toml_path = f_tegola_config_toml.getPath();
        if (!f_tegola_bin_executable.exists())
            throw new FileNotFoundException("tegola bin file " + s_tegola_bin_executable_path + " does not exist");
        if (!f_tegola_config_toml.exists())
            throw new FileNotFoundException("tegola config file " + s_tegola_config_toml_path + " does not exist");

        stop_tegola();

        Log.i(TAG, "start_tegola: starting new tegola server process...");
        Intent intent_notify_server_starting = new Intent(Constants.Strings.CTRLR_INTENT_BR_NOTIFICATIONS.MVT_SERVER__STARTING);
        sendBroadcast(intent_notify_server_starting);
        String[] s_ary_cmdline = new String[]
            {
                s_tegola_bin_executable_path
                , "--" + Constants.Strings.TEGOLA_ARG.CONFIG + "=" + s_tegola_config_toml_path
            };
        StringBuilder sb_cmdline = new StringBuilder();
        for (int i = 0; i < s_ary_cmdline.length; i++) {
            sb_cmdline.append(s_ary_cmdline[i]);
            if (i < s_ary_cmdline.length - 1)
                sb_cmdline.append(" ");
        }
        String s_cmdline = sb_cmdline.toString();
        Log.d(TAG, "start_tegola: cmdline is '" + s_cmdline + "'");
        m_process_tegola = Runtime.getRuntime().exec(s_cmdline);
        Log.i(TAG, "start_tegola: process started");
        Intent intent_notify_server_started = new Intent(Constants.Strings.CTRLR_INTENT_BR_NOTIFICATIONS.MVT_SERVER__STARTED);
        sendBroadcast(intent_notify_server_started);
        m_tegola_process_is_running = true;

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
                                Intent intent_notify_server_output_stderr = new Intent(Constants.Strings.CTRLR_INTENT_BR_NOTIFICATIONS.MVT_SERVER__OUTPUT__STDERR);
                                intent_notify_server_output_stderr.putExtra(Constants.Strings.CTRLR_INTENT_BR_NOTIFICATIONS.MVT_SERVER__OUTPUT__STDERR__LINE, s_line);
                                sendBroadcast(intent_notify_server_output_stderr);
                            }
                            Thread.sleep(100);
                        } catch (IOException e) {
                            e.printStackTrace();
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
                                Intent intent_notify_server_output_stdout = new Intent(Constants.Strings.CTRLR_INTENT_BR_NOTIFICATIONS.MVT_SERVER__OUTPUT__STDOUT);
                                intent_notify_server_output_stdout.putExtra(Constants.Strings.CTRLR_INTENT_BR_NOTIFICATIONS.MVT_SERVER__OUTPUT__STDOUT__LINE, s_line);
                                sendBroadcast(intent_notify_server_output_stdout);
                            }
                            Thread.sleep(100);
                        } catch (IOException e) {
                            e.printStackTrace();
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
                    Log.i(TAG, "tegola_process_monitor_thread: process stopped");
                    m_thread_tegola_process_stderr_monitor.interrupt();
                    m_thread_tegola_process_stdout_monitor.interrupt();
                } catch (InterruptedException e) {
                    //e.printStackTrace();
                } finally {
                    m_thread_tegola_process_stdout_monitor = null;
                    m_thread_tegola_process_stderr_monitor = null;
                    m_process_tegola = null;
                    m_tegola_process_is_running = false;
                    Intent intent_notify_server_stopped = new Intent(Constants.Strings.CTRLR_INTENT_BR_NOTIFICATIONS.MVT_SERVER__STOPPED);
                    sendBroadcast(intent_notify_server_stopped);
                }
            }
        });
        m_thread_tegola_process_monitor.start();

        //Log.i(TAG, "start_tegola: new tegola server process started: " + m_tegola_process_is_running);
        return m_tegola_process_is_running;
    }

    private boolean stop_tegola() {
        if (m_process_tegola != null) {
            Log.i(TAG, "stop_tegola: killing current running instance of tegola mvt server...");
            Intent intent_notify_server_stopping = new Intent(Constants.Strings.CTRLR_INTENT_BR_NOTIFICATIONS.MVT_SERVER__STOPPING);
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
            Intent intent_notify_server_stopped = new Intent(Constants.Strings.CTRLR_INTENT_BR_NOTIFICATIONS.MVT_SERVER__STOPPED);
            sendBroadcast(intent_notify_server_stopped);
            return true;
        }
        Log.i(TAG, "stop_tegola: tegola mvt server is not currently running");
        return false;
    }
}
