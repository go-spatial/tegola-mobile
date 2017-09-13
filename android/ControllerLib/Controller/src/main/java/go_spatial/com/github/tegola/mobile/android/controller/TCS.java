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
                        start_tegola();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }
                case MVT_SERVER__START: {
                    try {
                        start_tegola();
                    } catch (IOException e) {
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
        //check for existence in app data directory of tegola binary and config.toml
        File
                f_filesDir = getFilesDir()
                , f_tegola_bin_executable = new File(f_filesDir.getPath() + "/" + Constants.Strings.TEGOLA_BIN__NORMALIZED_FNAME)
                , f_tegola_config_toml = new File(f_filesDir.getPath() + "/" + Constants.Strings.TEGOLA_CONFIG_TOML__NORMALIZED_FNAME)
                ;
        if (!f_tegola_bin_executable.exists()) {
            //transfer matching tegola binary from raw resources based on device arch
            Log.d(TAG, "init: creating executable tegola.bin from raw for " + Build.CPU_ABI + " ABI, API Level " + Build.VERSION.SDK_INT + "...");
            boolean btegolaexecutablecreated = false;
            try {
                btegolaexecutablecreated = make_tegola_executable(Constants.Enums.CPU_ABI.fromString(Build.CPU_ABI), Build.VERSION.SDK_INT);
            } catch (Exceptions.UnsupportedArchitectureException e) {
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

    private boolean make_tegola_executable(final Constants.Enums.CPU_ABI eArch, final int api_level) throws Exceptions.UnsupportedArchitectureException, IOException {
        int id__raw_tegola_bin = -1;
        if (eArch == null) throw new Exceptions.UnsupportedArchitectureException("eArch is null");
        switch (eArch) {
            case armeabi:
            case armeabi_v7a: {
                id__raw_tegola_bin = R.raw.tegola_0_4_0_bin__arm_api_15;
                break;
            }
            case arm64_v8a: {
                id__raw_tegola_bin = R.raw.tegola_0_4_0_bin__arm64_api_21;
                break;
            }
            default: throw new Exceptions.UnsupportedArchitectureException(eArch.name() + " does not yet have a corresponding tegola binary for Android");
        }
        InputStream inputstream_raw_tegola_bin = getResources().openRawResource(id__raw_tegola_bin);
        byte[] buf_raw_tegola = new byte[inputstream_raw_tegola_bin.available()];
        inputstream_raw_tegola_bin.read(buf_raw_tegola);
        inputstream_raw_tegola_bin.close();
        FileOutputStream f_outputstream_tegola_bin = openFileOutput(Constants.Strings.TEGOLA_BIN__NORMALIZED_FNAME, Context.MODE_PRIVATE);
        f_outputstream_tegola_bin.write(buf_raw_tegola);
        f_outputstream_tegola_bin.close();
        File f_tegola_bin_executable = new File(getFilesDir().getPath() + "/" + Constants.Strings.TEGOLA_BIN__NORMALIZED_FNAME);
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

    private volatile Process m_process_tegola = null;
    private volatile boolean m_process_tegola_is_running = false;

    private boolean start_tegola() throws IOException {
        File
                f_filesDir = getFilesDir()
                , f_tegola_bin_executable = new File(f_filesDir.getPath() + "/" + Constants.Strings.TEGOLA_BIN__NORMALIZED_FNAME)
                , f_tegola_config_toml = new File(f_filesDir.getPath() + "/" + Constants.Strings.TEGOLA_CONFIG_TOML__NORMALIZED_FNAME);
                ;
        final String
                s_tegola_bin_executable_path = f_tegola_bin_executable.getPath()
                , s_tegola_config_toml_path = f_tegola_config_toml.getPath()
                ;
        if (!(f_tegola_bin_executable.exists() && f_tegola_config_toml.exists())) throw new FileNotFoundException("either " + s_tegola_bin_executable_path + " or " + s_tegola_config_toml_path + " does not exist");
        stop_tegola();
        Log.i(TAG, "start_tegola: starting new tegola server process...");
        Thread thread_tegola_monitor = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Intent intent_notify_server_starting = new Intent(Constants.Strings.CTRLR_INTENT_BR_NOTIFICATIONS.MVT_SERVER__STARTING);
                    sendBroadcast(intent_notify_server_starting);
                    m_process_tegola = Runtime.getRuntime().exec(new String[] {s_tegola_bin_executable_path, "--config=" + s_tegola_config_toml_path});
                    m_process_tegola_is_running = true;
                    Intent intent_notify_server_started = new Intent(Constants.Strings.CTRLR_INTENT_BR_NOTIFICATIONS.MVT_SERVER__STARTED);
                    sendBroadcast(intent_notify_server_started);
                    m_process_tegola.waitFor();
                    m_process_tegola_is_running = false;
                    BufferedReader reader_tegola_process_stdout = new BufferedReader(new InputStreamReader(m_process_tegola.getInputStream()));
                    String s_line = "";
                    while ((s_line = reader_tegola_process_stdout.readLine())!= null) {
                        Log.i(TAG, "tegola_monitor_thread: " + s_line);
                    }
                    Intent intent_notify_server_stopped = new Intent(Constants.Strings.CTRLR_INTENT_BR_NOTIFICATIONS.MVT_SERVER__STOPPED);
                    sendBroadcast(intent_notify_server_stopped);
                } catch (InterruptedException e) {
                    m_process_tegola_is_running = false;
                    e.printStackTrace();
                } catch (IOException e) {
                    m_process_tegola_is_running = false;
                    e.printStackTrace();
                }
            }
        });
        thread_tegola_monitor.start();
        Log.i(TAG, "start_tegola: new tegola server process started: " + m_process_tegola_is_running);
        return m_process_tegola_is_running;
    }

    private boolean stop_tegola() {
        if (m_process_tegola != null) {
            Log.i(TAG, "stop_tegola: killing current running instance of tegola mvt server...");
            Intent intent_notify_server_stopping = new Intent(Constants.Strings.CTRLR_INTENT_BR_NOTIFICATIONS.MVT_SERVER__STOPPING);
            sendBroadcast(intent_notify_server_stopping);
            m_process_tegola.destroy();
            m_process_tegola = null;
            m_process_tegola_is_running = false;
            return true;
        }
        Log.i(TAG, "stop_tegola: tegola mvt server is not currently running");
        return false;
    }
}
