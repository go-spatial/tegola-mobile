package go_spatial.com.github.tegola.mobile.android.bootstrapper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import go_spatial.com.github.tegola.mobile.android.controller.Constants;
import go_spatial.com.github.tegola.mobile.android.controller.TCS;

public class MainActivity extends AppCompatActivity {

    private Button m_btn_go = null;
    private TextView m_tv_val_ctrlr_status = null;
    private TextView m_tv_val_srvr_status = null;
    private TextView m_tv_val_uri__static = null;
    private EditText m_et_val_uri__dynamic = null;
    private WebView m_wv_mvt_renderer = null;
    private TextView m_tv_val_got_uri = null;

    private BroadcastReceiver m_rcvr_ctrlr_notifications = null;
    private static IntentFilter m_ctrlr_notifications_filter = null;


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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setTitle(getString(R.string.app_name) + " - bin ver. " + getString(R.string.tegola_bin_ver));

        m_tv_val_ctrlr_status = (TextView)findViewById(R.id.tv_val_ctrlr_status);
        m_tv_val_srvr_status = (TextView)findViewById(R.id.tv_val_srvr_status);
        m_tv_val_uri__static = (TextView)findViewById(R.id.tv_val_uri__static);
        m_et_val_uri__dynamic = (EditText)findViewById(R.id.edt_val_uri__dynamic);
        m_btn_go = (Button)findViewById(R.id.btn_go);
        m_wv_mvt_renderer = (WebView)findViewById(R.id.wv_mvt_renderer);
        m_wv_mvt_renderer.getSettings().setJavaScriptEnabled(true);
        m_tv_val_got_uri = (TextView)findViewById(R.id.tv_val_got_uri);
        m_tv_val_got_uri.setText("");
        m_btn_go.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StringBuilder sb = new StringBuilder();
                sb.append(m_tv_val_uri__static.getText());
                sb.append(m_et_val_uri__dynamic.getText());
                m_wv_mvt_renderer.loadUrl(sb.toString());
                m_tv_val_got_uri.setText(m_wv_mvt_renderer.getUrl());
            }
        });

        m_ctrlr_notifications_filter = new IntentFilter();
        m_ctrlr_notifications_filter.addAction(Constants.Strings.CTRLR_INTENT_BR_NOTIFICATIONS.CONTROLLER__FOREGROUND_STARTING);
        m_ctrlr_notifications_filter.addAction(Constants.Strings.CTRLR_INTENT_BR_NOTIFICATIONS.CONTROLLER__FOREGROUND_STARTED);
        m_ctrlr_notifications_filter.addAction(Constants.Strings.CTRLR_INTENT_BR_NOTIFICATIONS.CONTROLLER__FOREGROUND_STOPPING);
        m_ctrlr_notifications_filter.addAction(Constants.Strings.CTRLR_INTENT_BR_NOTIFICATIONS.CONTROLLER__FOREGROUND_STOPPED);
        m_ctrlr_notifications_filter.addAction(Constants.Strings.CTRLR_INTENT_BR_NOTIFICATIONS.MVT_SERVER__STARTING);
        m_ctrlr_notifications_filter.addAction(Constants.Strings.CTRLR_INTENT_BR_NOTIFICATIONS.MVT_SERVER__STARTED);
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
                        onMVTServerStarted();
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
        start_controller(); //auto-start controller instead of via button click
        super.onPostCreate(savedInstanceState);
    }

    public void onControllerStarting() {
        m_tv_val_ctrlr_status.setText(getString(R.string.starting));
    }

    public void onControllerStarted() {
        m_tv_val_ctrlr_status.setText(getString(R.string.started));
    }

    public void onControllerStopping() {
        m_tv_val_ctrlr_status.setText(getString(R.string.stopping));
    }

    public void onControllerStopped() {
        m_tv_val_ctrlr_status.setText(getString(R.string.stopped));
    }

    public void onMVTServerStarting() {
        m_tv_val_srvr_status.setText(getString(R.string.starting));
    }

    public void onMVTServerStarted() {
        m_tv_val_srvr_status.setText(getString(R.string.started));
    }

    public void onMVTServerStopping() {
        m_tv_val_srvr_status.setText(getString(R.string.stopping));
    }

    public void onMVTServerStopped() {
        m_tv_val_srvr_status.setText(getString(R.string.stopped));
    }
}
