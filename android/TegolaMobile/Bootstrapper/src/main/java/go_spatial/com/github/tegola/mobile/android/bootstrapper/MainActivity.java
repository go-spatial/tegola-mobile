package go_spatial.com.github.tegola.mobile.android.bootstrapper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import go_spatial.com.github.tegola.mobile.android.controller.Constants;
import go_spatial.com.github.tegola.mobile.android.controller.TCS;

public class MainActivity extends AppCompatActivity {
    private TextView m_tv_val_anddro_device_info = null;
    private TextView m_tv_val_ctrlr_status = null;
    private TextView m_tv_val_srvr_status = null;
    private ScrollView m_scrvw_tegola_console_output = null;
    private TextView m_tv_tegola_console_output = null;

    private BroadcastReceiver m_rcvr_ctrlr_notifications = null;
    private static IntentFilter m_ctrlr_notifications_filter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle(getString(R.string.app_name) + " - build " + BuildConfig.VERSION_NAME);

        m_tv_val_anddro_device_info = (TextView)findViewById(R.id.tv_val_andro_device_info);
        StringBuilder sb_andro_dev_nfo = new StringBuilder();
        sb_andro_dev_nfo.append("CPU_ABI " + Constants.Enums.CPU_ABI.fromDevice().toString());
        sb_andro_dev_nfo.append(", API " + Build.VERSION.SDK_INT /*+ " \"" + Build.VERSION.CODENAME + "\""*/);
        m_tv_val_anddro_device_info.setText(sb_andro_dev_nfo.toString());

        m_tv_val_ctrlr_status = (TextView)findViewById(R.id.tv_val_tegola_ctrlr_status);
        m_tv_val_srvr_status = (TextView)findViewById(R.id.tv_val_tegola_srvr_status);
        m_scrvw_tegola_console_output = (ScrollView)findViewById(R.id.scrvw_tegola_console_output);
        m_tv_tegola_console_output = (TextView)findViewById(R.id.tv_tegola_console_output);

        onMVTServerStopped();
        onControllerStopped();

        m_ctrlr_notifications_filter = new IntentFilter();
        m_ctrlr_notifications_filter.addAction(Constants.Strings.CTRLR_INTENT_BR_NOTIFICATIONS.CONTROLLER__FOREGROUND_STARTING);
        m_ctrlr_notifications_filter.addAction(Constants.Strings.CTRLR_INTENT_BR_NOTIFICATIONS.CONTROLLER__FOREGROUND_STARTED);
        m_ctrlr_notifications_filter.addAction(Constants.Strings.CTRLR_INTENT_BR_NOTIFICATIONS.CONTROLLER__FOREGROUND_STOPPING);
        m_ctrlr_notifications_filter.addAction(Constants.Strings.CTRLR_INTENT_BR_NOTIFICATIONS.CONTROLLER__FOREGROUND_STOPPED);
        m_ctrlr_notifications_filter.addAction(Constants.Strings.CTRLR_INTENT_BR_NOTIFICATIONS.MVT_SERVER__STARTING);
        m_ctrlr_notifications_filter.addAction(Constants.Strings.CTRLR_INTENT_BR_NOTIFICATIONS.MVT_SERVER__STARTED);
        m_ctrlr_notifications_filter.addAction(Constants.Strings.CTRLR_INTENT_BR_NOTIFICATIONS.MVT_SERVER__OUTPUT__LOGCAT);
        m_ctrlr_notifications_filter.addAction(Constants.Strings.CTRLR_INTENT_BR_NOTIFICATIONS.MVT_SERVER__OUTPUT__STDERR);
        m_ctrlr_notifications_filter.addAction(Constants.Strings.CTRLR_INTENT_BR_NOTIFICATIONS.MVT_SERVER__OUTPUT__STDOUT);
        m_ctrlr_notifications_filter.addAction(Constants.Strings.CTRLR_INTENT_BR_NOTIFICATIONS.MVT_SERVER__STOPPING);
        m_ctrlr_notifications_filter.addAction(Constants.Strings.CTRLR_INTENT_BR_NOTIFICATIONS.MVT_SERVER__STOPPED);
        m_rcvr_ctrlr_notifications = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Constants.Enums.E_CTRLR_BR_NOTIFICATIONS e_ctrlr_br_notification = Constants.Enums.E_CTRLR_BR_NOTIFICATIONS.fromString(intent != null ? intent.getAction() : null);
                switch (e_ctrlr_br_notification) {
                    case CONTROLLER_FOREGROUND_STARTING: {
                        onControllerStarting();
                        break;
                    }
                    case CONTROLLER_FOREGROUND_STARTED: {
                        onControllerStarted();
                        break;
                    }
                    case CONTROLLER_FOREGROUND_STOPPING: {
                        onControllerStopping();
                        break;
                    }
                    case CONTROLLER_FOREGROUND_STOPPED: {
                        onControllerStopped();
                        break;
                    }
                    case MVT_SERVER__STARTING: {
                        onMVTServerStarting();
                        break;
                    }
                    case MVT_SERVER__STARTED: {
                        onMVTServerStarted(
                            intent.getStringExtra(Constants.Strings.CTRLR_INTENT_BR_NOTIFICATIONS.MVT_SERVER__STARTED__VERSION)
                            , intent.getIntExtra(Constants.Strings.CTRLR_INTENT_BR_NOTIFICATIONS.MVT_SERVER__STARTED__PID, -1)
                        );
                        break;
                    }
                    case MVT_SERVER__OUTPUT__LOGCAT: {
                        onMVTServerOutputLogcat(intent.getStringExtra(Constants.Strings.CTRLR_INTENT_BR_NOTIFICATIONS.MVT_SERVER__OUTPUT__LOGCAT__LINE));
                        break;
                    }
                    case MVT_SERVER__OUTPUT__STDERR: {
                        onMVTServerOutputStdErr(intent.getStringExtra(Constants.Strings.CTRLR_INTENT_BR_NOTIFICATIONS.MVT_SERVER__OUTPUT__STDERR__LINE));
                        break;
                    }
                    case MVT_SERVER__OUTPUT__STDOUT: {
                        onMVTServerOutputStdOut(intent.getStringExtra(Constants.Strings.CTRLR_INTENT_BR_NOTIFICATIONS.MVT_SERVER__OUTPUT__STDOUT__LINE));
                        break;
                    }
                    case MVT_SERVER__STOPPING: {
                        onMVTServerStopping();
                        break;
                    }
                    case MVT_SERVER__STOPPED: {
                        onMVTServerStopped();
                        break;
                    }
                }
            }
        };
        registerReceiver(m_rcvr_ctrlr_notifications, m_ctrlr_notifications_filter, null, new Handler(getMainLooper()));
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        onControllerStarting();
        start_controller(); //auto-start controller instead of via button click
    }

    private void onControllerStarting() {
        m_tv_val_ctrlr_status.setText(getString(R.string.starting));
    }

    private void onControllerStarted() {
        m_tv_val_ctrlr_status.setText(getString(R.string.started));
    }

    private void onControllerStopping() {
        m_tv_val_ctrlr_status.setText(getString(R.string.stopping));
    }

    private void onControllerStopped() {
        m_tv_val_ctrlr_status.setText(getString(R.string.stopped));
    }

    private void onMVTServerStarting() {
        m_tv_val_srvr_status.setText(getString(R.string.starting));
    }

    private void onMVTServerStarted(final String version, final int pid) {
        final StringBuilder sb_srvr_status = new StringBuilder();
        sb_srvr_status.append(getString(R.string.started));
        if (version != null && !version.isEmpty())
            sb_srvr_status.append(" version " + version);
        if (pid != -1)
            sb_srvr_status.append(" , pid " + pid + "");
        m_tv_val_srvr_status.setText(sb_srvr_status.toString());
    }

    private void onMVTServerOutputLogcat(final String logcat_line) {
        sv_append_mvt_server_console_output("<LOGCAT> " + logcat_line);
    }

    private void onMVTServerOutputStdErr(final String stderr_line) {
        sv_append_mvt_server_console_output("<STDERR> " + stderr_line);
    }

    private void onMVTServerOutputStdOut(final String stdout_line) {
        sv_append_mvt_server_console_output("<STDOUT> " + stdout_line);
    }

    private void sv_append_mvt_server_console_output(final String s) {
        m_tv_tegola_console_output.append(s + "\n");
        m_scrvw_tegola_console_output.postDelayed(new Runnable() {
            public void run() {
                m_scrvw_tegola_console_output.fullScroll(View.FOCUS_DOWN);
            }
        }, 250);
    }

    private void onMVTServerStopping() {
        m_tv_val_srvr_status.setText(getString(R.string.stopping));
    }

    private void onMVTServerStopped() {
        m_tv_val_srvr_status.setText(getString(R.string.stopped));
    }


    private void start_controller() {
        m_tv_val_ctrlr_status.setText(getString(R.string.starting));
        Intent intent_start_tegola_fgs = new Intent(MainActivity.this, TCS.class);
        intent_start_tegola_fgs.setAction(Constants.Strings.CTRLR_INTENT_ACTION.CONTROLLER__START_FOREGROUND);
        startService(intent_start_tegola_fgs);
    }

    private void stop_controller() {
        m_tv_val_ctrlr_status.setText(getString(R.string.stopping));
        Intent intent_stop_tegola_fgs = new Intent(MainActivity.this, TCS.class);
        intent_stop_tegola_fgs.setAction(Constants.Strings.CTRLR_INTENT_ACTION.CONTROLLER__STOP_FOREGROUND);
        stopService(intent_stop_tegola_fgs);
    }
}
