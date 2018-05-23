package go_spatial.com.github.tegola.mobile.android.ux;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.ContextThemeWrapper;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.aakira.expandablelayout.ExpandableRelativeLayout;
import com.ipaulpro.afilechooser.utils.FileUtils;
import com.mapbox.mapboxsdk.exceptions.MapboxConfigurationException;

/*
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.OpenFileActivityBuilder;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.SearchableField;
*/

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;

import go_spatial.com.github.tegola.mobile.android.ux.Constants.REQUEST_CODES;
import go_spatial.com.github.tegola.mobile.android.ux.Constants.Strings;
import go_spatial.com.github.tegola.mobile.android.controller.Constants;
import go_spatial.com.github.tegola.mobile.android.controller.FGS;
import go_spatial.com.github.tegola.mobile.android.controller.Utils;

public class MainActivity extends AppCompatActivity implements TegolaMBGLFragment.OnFragmentInteractionListener {
    private static final String TAG = MainActivity.class.getName();

    private DrawerLayout m_drawerlayout = null;
    private LinearLayout m_drawerlayout_content__main = null;
    private LinearLayout m_drawerlayout_content__drawer = null;
    private DrawerHandle m_drawer_handle = null;
    private ActionBarDrawerToggle m_drawerlayout_main__DrawerToggle = null;

    private ScrollView m_scvw_main = null;

    //andro_dev info - UI objects
    private Button m_btn_sect__andro_dev_nfo__expand = null;
    private ExpandableRelativeLayout m_vw_sect_content__andro_dev_nfo = null;
    private TextView m_tv_val_CPU_ABI = null;
    private TextView m_tv_val_API_level = null;

    //ctrlr info - UI objects
    private Button m_btn_sect__ctrlr_nfo__expand = null;
    private ExpandableRelativeLayout m_vw_sect_content__ctrlr_nfo = null;
    private TextView m_tv_val_ctrlr_status = null;

    //srvr info - version - UI objects
    private TextView m_tv_val_bin_ver = null;

    //srvr info - provider sel postgis - UI objects
    private RadioButton m_rb_val_provider_type_sel__postgis = null;
    private View m_sect__postgis_provider_spec = null;

    //srvr info - config sel local - UI objects
    private RadioButton m_rb_val_config_type_sel__local = null;
    private TextView m_tv_lbl_config_type_sel__local__manage_files = null;
    private View m_vw_config_sel_container__local = null;
    private CustomSpinner m_spinner_val_config_sel_local = null;
    private final ArrayList<String> m_spinner_val_config_sel_local__items = new ArrayList<String>();
    private ArrayAdapter<String> m_spinner_val_config_sel_local__dataadapter = null;
    private ImageButton m_btn_config_sel_local__edit_file = null;
//    private ImageButton m_btn_config_sel_local_import__googledrive = null;
    private ImageButton m_btn_config_sel_local_import__sdcard = null;

    //srvr info - config sel remote - UI objects
    private RadioButton m_rb_val_config_type_sel__remote = null;
    private View m_vw_config_sel_container__remote = null;
    private EditText m_edt_val_config_sel__remote;
    private Button m_btn_config_sel_remote_apply_changes = null;

    //srvr info - provider sel gpkg - UI objects
    private RadioButton m_rb_val_provider_type_sel__gpkg = null;
    private View m_sect__gpkg_provider_spec = null;
    private CustomSpinner m_spinner_val_gpkg_bundle_sel = null;
    private final ArrayList<String> m_spinner_val_gpkg_bundle_sel__items = new ArrayList<String>();
    private ArrayAdapter<String> m_spinner_val_gpkg_bundle_sel__dataadapter = null;
    private CustomSpinner m_spinner_val_gpkg_bundle_props_sel = null;
    private final ArrayList<String> m_spinner_val_gpkg_bundle_props_sel__items = new ArrayList<String>();
    private ArrayAdapter<String> m_spinner_val_gpkg_bundle_props_sel__dataadapter = null;

    //srvr info - status - UI objects
    private TextView m_tv_val_srvr_status = null;
    private Button m_btn_srvr_ctrl = null;

    //srvr info - console output - UI objects
    private View m_sect_content__item__srvr_console_output = null;
    private TextView m_tv_tegola_console_output = null;

    private final TegolaMBGLFragment mb_frag = new TegolaMBGLFragment();


    private BroadcastReceiver m_br_ctrlr_notifications = null;
    private IntentFilter m_br_ctrlr_notifications_filter = null;
    private boolean m_controller_running = false;

//    private DriveId m_google_drive_id;
//    private final class MyGoogleApiClientCallbacks implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
//        private final String TAG = MyGoogleApiClientCallbacks.class.getName();
//
//        //GoogleApiClient override
//        @Override
//        public void onConnected(@Nullable Bundle bundle) {
//            Drawable drawable_cloud_download = ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_cloud_download_black_24dp);
//            int h = drawable_cloud_download.getIntrinsicHeight();
//            int w = drawable_cloud_download.getIntrinsicWidth();
//            drawable_cloud_download.setBounds(0, 0, w, h);
//            m_btn_config_sel_local_import__googledrive.setImageDrawable(drawable_cloud_download);
//            m_btn_config_sel_local_import__googledrive.setBackgroundColor(ContextCompat.getColor(MainActivity.this, android.R.color.holo_green_light));
//            Log.i(TAG, "onConnected: GoogleApiClient flow handler: connection success");
//            Toast.makeText(getApplicationContext(), "GoogleApiClient successfully connected", Toast.LENGTH_LONG).show();
//        }
//
//        //GoogleApiClient override
//        @Override
//        public void onConnectionSuspended(int i) {
//            Drawable drawable_cloud_disconnected = ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_cloud_off_black_24dp);
//            int h = drawable_cloud_disconnected.getIntrinsicHeight();
//            int w = drawable_cloud_disconnected.getIntrinsicWidth();
//            drawable_cloud_disconnected.setBounds(0, 0, w, h);
//            m_btn_config_sel_local_import__googledrive.setImageDrawable(drawable_cloud_disconnected);
//            m_btn_config_sel_local_import__googledrive.setBackgroundColor(ContextCompat.getColor(MainActivity.this, android.R.color.holo_red_dark));
//            Log.i(TAG, "onConnectionSuspended: GoogleApiClient flow handler: connection suspended");
//            Toast.makeText(getApplicationContext(), "GoogleApiClient connection suspended", Toast.LENGTH_LONG).show();
//        }
//
//        //GoogleApiClient override
//        @Override
//        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
//            m_btn_config_sel_local_import__googledrive.setBackgroundColor(ContextCompat.getColor(MainActivity.this, android.R.color.holo_red_dark));
//            if (!connectionResult.hasResolution()) {
//                Log.e(TAG, "onConnectionFailed: GoogleApiClient connection failed: " + connectionResult.toString() + " -- flow control handler: abnormal termination :(");
//                Toast.makeText(getApplicationContext(), "GoogleApiClient connection failed with no reported resolution!", Toast.LENGTH_LONG).show();
//                GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, connectionResult.getErrorCode(), 0).show();
//                return;
//            }
//            Log.i(TAG, "onConnectionFailed: GoogleApiClient connection failed: " + connectionResult.toString() + " -- flow control handler: starting GoogleApiClient connection resolution for this result...");
//            Toast.makeText(getApplicationContext(), "GoogleApiClient connection failed -- starting resolution flow...", Toast.LENGTH_LONG).show();
//            try {
//                connectionResult.startResolutionForResult(MainActivity.this, REQUEST_CODES.REQUEST_CODE__GOOGLEAPICLIENT__RESOLVE_CONNECTION_FAILURE);
//            } catch (IntentSender.SendIntentException e) {
//                Log.e(TAG, "onConnectionFailed: GoogleApiClient connection failed: " + connectionResult.toString() + " -- flow control handler: IntentSender failed to send intent; abnormal termination :(", e);
//                Toast.makeText(getApplicationContext(), "GoogleApiClient connection-failure resolution flow abnormally terminated!", Toast.LENGTH_LONG).show();
//            }
//        }
//    }
//    private MyGoogleApiClientCallbacks m_google_api_callbacks = null;


    private final String SAVE_INSTANCE_ARG__CTRLR_RUNNING = "SAVE_INSTANCE_ARG__CTRLR_RUNNING";
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "onSaveInstanceState: outState.putBoolean(SAVE_INSTANCE_ARG__CTRLR_RUNNING, " + m_controller_running + ")");
        outState.putBoolean(SAVE_INSTANCE_ARG__CTRLR_RUNNING, m_controller_running);
        super.onSaveInstanceState(outState);
    }

    //credit to: https://stackoverflow.com/questions/16754305/full-width-navigation-drawer?utm_medium=organic&utm_source=google_rich_qa&utm_campaign=google_rich_qa
    private void fixMinDrawerMargin(DrawerLayout drawerLayout) {
        try {
            Field f = DrawerLayout.class.getDeclaredField("mMinDrawerMargin");
            f.setAccessible(true);
            f.set(drawerLayout, 0);

            drawerLayout.requestLayout();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: entered");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //map UI objects to UI resources
        m_drawerlayout = (DrawerLayout)findViewById(R.id.drawerlayout);
        fixMinDrawerMargin(m_drawerlayout);
        m_drawerlayout_content__main = (LinearLayout)findViewById(R.id.drawerlayout_content__main);
        m_drawerlayout_content__drawer = findViewById(R.id.drawerlayout_content__drawer);
        m_drawerlayout_main__DrawerToggle = new ActionBarDrawerToggle(this, m_drawerlayout, R.string.open, R.string.close) {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                float slideX = drawerView.getWidth() * slideOffset;
                m_drawerlayout_content__main.setTranslationX(slideX);
                m_drawerlayout_content__main.setScaleX(1 - slideOffset);
                m_drawerlayout_content__main.setScaleY(1 - slideOffset);
            }
        };

        m_scvw_main = (ScrollView)findViewById(R.id.sv_main);

        m_btn_sect__andro_dev_nfo__expand = (Button)findViewById(R.id.btn_sect__andro_dev_nfo__expand);
        m_vw_sect_content__andro_dev_nfo = (ExpandableRelativeLayout)findViewById(R.id.sect_content__andro_dev_nfo);
        m_tv_val_CPU_ABI = (TextView)findViewById(R.id.tv_val_CPU_ABI);
        m_tv_val_API_level = (TextView)findViewById(R.id.tv_val_API_level);

        m_btn_sect__ctrlr_nfo__expand = (Button)findViewById(R.id.btn_sect__ctrlr_nfo__expand);
        m_vw_sect_content__ctrlr_nfo = (ExpandableRelativeLayout)findViewById(R.id.sect_content__ctrlr_nfo);
        m_tv_val_ctrlr_status = (TextView)findViewById(R.id.tv_val_tegola_ctrlr_status);

        m_tv_val_bin_ver = (TextView)findViewById(R.id.tv_val_bin_ver);

        m_rb_val_provider_type_sel__postgis = (RadioButton)findViewById(R.id.rb_val_provider_type_sel__postgis);
        m_rb_val_provider_type_sel__gpkg = (RadioButton)findViewById(R.id.rb_val_provider_type_sel__gpkg);

        m_sect__postgis_provider_spec = (View)findViewById(R.id.sect__postgis_provider_spec);
        m_rb_val_config_type_sel__local = (RadioButton)findViewById(R.id.rb_val_config_type_sel__local);
        m_tv_lbl_config_type_sel__local__manage_files = (TextView)findViewById(R.id.tv_lbl_gpkg_provider_type_sel__manage_bundles);
        m_rb_val_config_type_sel__remote = (RadioButton)findViewById(R.id.rb_val_config_type_sel__remote);
        m_vw_config_sel_container__local = findViewById(R.id.postgis_provider_config_sel__local__container);
        m_spinner_val_config_sel_local = (CustomSpinner)findViewById(R.id.spinner_val_postgis_provider_config_sel__local);
        m_btn_config_sel_local__edit_file = (ImageButton)findViewById(R.id.btn_postgis_provider_config_sel_local__edit_file);
        m_btn_config_sel_local_import__sdcard = (ImageButton)findViewById(R.id.btn_postgis_provider_config_sel_local_import__sdcard);
//        m_btn_config_sel_local_import__googledrive = (ImageButton)findViewById(R.id.btn_postgis_provider_config_sel_local_import__googledrive);
        m_vw_config_sel_container__remote = findViewById(R.id.postgis_provider_config_sel__remote__container);
        m_edt_val_config_sel__remote = (EditText)findViewById(R.id.edt_val_postgis_provider_config_sel__remote);
        m_btn_config_sel_remote_apply_changes = (Button)findViewById(R.id.btn_postgis_provider_config_sel_remote_apply_changes);

        m_sect__gpkg_provider_spec = (View)findViewById(R.id.sect__gpkg_provider_spec);
        m_spinner_val_gpkg_bundle_sel = (CustomSpinner)findViewById(R.id.spinner_val_gpkg_provider_bundle_sel);
        m_spinner_val_gpkg_bundle_props_sel = (CustomSpinner)findViewById(R.id.spinner_val_gpkg_provider_bundle_props_sel);

        m_tv_val_srvr_status = (TextView)findViewById(R.id.tv_val_srvr_status);
        m_btn_srvr_ctrl = (Button)findViewById(R.id.btn_srvr_ctrl);
        m_sect_content__item__srvr_console_output = findViewById(R.id.sect_content__item__srvr_console_output);
        m_tv_tegola_console_output = (TextView)findViewById(R.id.tv_tegola_console_output);

        //set up associated UI objects auxiliary objects if any - e.g. TAGs and data adapters
        m_spinner_val_config_sel_local__dataadapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, m_spinner_val_config_sel_local__items);
        m_spinner_val_config_sel_local__dataadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        m_spinner_val_config_sel_local.setAdapter(m_spinner_val_config_sel_local__dataadapter);
        m_spinner_val_gpkg_bundle_sel__dataadapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, m_spinner_val_gpkg_bundle_sel__items);
        m_spinner_val_gpkg_bundle_sel__dataadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        m_spinner_val_gpkg_bundle_sel.setAdapter(m_spinner_val_gpkg_bundle_sel__dataadapter);
        m_spinner_val_gpkg_bundle_props_sel__dataadapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, m_spinner_val_gpkg_bundle_props_sel__items);
        m_spinner_val_gpkg_bundle_props_sel__dataadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        m_spinner_val_gpkg_bundle_props_sel.setAdapter(m_spinner_val_gpkg_bundle_props_sel__dataadapter);

        m_btn_srvr_ctrl.setTag(R.id.TAG__SRVR_RUNNING, false);

        //associate listeners for user-UI-interaction
        m_btn_sect__andro_dev_nfo__expand.setOnClickListener(OnClickListener__btn_expandable_section);
        m_btn_sect__ctrlr_nfo__expand.setOnClickListener(OnClickListener__btn_expandable_section);
        m_rb_val_provider_type_sel__postgis.setOnCheckedChangeListener(OnCheckedChangeListener__m_rb_val_provider_type_sel__postgis);
        m_rb_val_provider_type_sel__gpkg.setOnCheckedChangeListener(OnCheckedChangeListener__m_rb_val_provider_type_sel__gpkg);
        m_rb_val_config_type_sel__local.setOnCheckedChangeListener(OnCheckedChangeListener__m_rb_val_config_type_sel__local);
        m_tv_lbl_config_type_sel__local__manage_files.setMovementMethod(LinkMovementMethod.getInstance());
        Spannable span__clickable_text__m_tv_lbl_config_type_sel__local__manage_files = (Spannable)m_tv_lbl_config_type_sel__local__manage_files.getText();
        span__clickable_text__m_tv_lbl_config_type_sel__local__manage_files.setSpan(ClickableSpan____m_tv_lbl_config_type_sel__local__manage_files, 0, span__clickable_text__m_tv_lbl_config_type_sel__local__manage_files.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        m_spinner_val_config_sel_local.setOnItemSelectedListener(OnItemSelectedListener__m_spinner_val_config_sel_local);
        m_btn_config_sel_local__edit_file.setOnClickListener(OnClickListener__m_btn_config_sel_local__edit_file);
        m_btn_config_sel_local_import__sdcard.setOnClickListener(OnClickListener__m_btn_config_sel_local_import__sdcard);
//        m_btn_config_sel_local_import__googledrive.setOnClickListener(OnClickListener__m_btn_config_sel_local_import__googledrive);
        m_rb_val_config_type_sel__remote.setOnCheckedChangeListener(OnCheckedChangeListener__m_rb_val_config_type_sel__remote);
        m_edt_val_config_sel__remote.setOnEditorActionListener(OnEditorActionListener__m_edt_val_config_sel__remote);
        m_edt_val_config_sel__remote.setOnFocusChangeListener(OnFocusChangeListener__m_edt_val_config_sel__remote);
        m_btn_config_sel_remote_apply_changes.setOnClickListener(OnClickListener__m_btn_config_sel_remote_apply_changes);
        m_spinner_val_gpkg_bundle_sel.setOnItemSelectedListener(OnItemSelectedListener__m_spinner_val_gpkg_bundle_sel);
        m_spinner_val_gpkg_bundle_props_sel.setOnItemSelectedListener(OnItemSelectedListener__m_spinner_val_gpkg_bundle_config_sel);
        m_btn_srvr_ctrl.setOnClickListener(OnClickListener__m_btn_srvr_ctrl);


        //instantiate PersistentConfigSettingsManager singleton
        SharedPrefsManager.newInstance(this);

//        m_google_api_callbacks = new MyGoogleApiClientCallbacks();


        //set up BR to listen to notifications from ControllerLib
        m_br_ctrlr_notifications_filter = new IntentFilter();
        m_br_ctrlr_notifications_filter.addAction(Constants.Strings.INTENT.ACTION.CTRLR_NOTIFICATION.CONTROLLER__FOREGROUND_STARTING);
        m_br_ctrlr_notifications_filter.addAction(Constants.Strings.INTENT.ACTION.CTRLR_NOTIFICATION.CONTROLLER__FOREGROUND_RUNNING);
        m_br_ctrlr_notifications_filter.addAction(Constants.Strings.INTENT.ACTION.CTRLR_NOTIFICATION.CONTROLLER__FOREGROUND_STOPPING);
        m_br_ctrlr_notifications_filter.addAction(Constants.Strings.INTENT.ACTION.CTRLR_NOTIFICATION.CONTROLLER__FOREGROUND_STOPPED);
        m_br_ctrlr_notifications_filter.addAction(Constants.Strings.INTENT.ACTION.CTRLR_NOTIFICATION.MVT_SERVER__STARTING);
        m_br_ctrlr_notifications_filter.addAction(Constants.Strings.INTENT.ACTION.CTRLR_NOTIFICATION.MVT_SERVER__START_FAILED);
        m_br_ctrlr_notifications_filter.addAction(Constants.Strings.INTENT.ACTION.CTRLR_NOTIFICATION.MVT_SERVER__RUNNING);
        m_br_ctrlr_notifications_filter.addAction(Constants.Strings.INTENT.ACTION.CTRLR_NOTIFICATION.MVT_SERVER__LISTENING);
        m_br_ctrlr_notifications_filter.addAction(Constants.Strings.INTENT.ACTION.CTRLR_NOTIFICATION.MVT_SERVER__OUTPUT__LOGCAT);
        m_br_ctrlr_notifications_filter.addAction(Constants.Strings.INTENT.ACTION.CTRLR_NOTIFICATION.MVT_SERVER__OUTPUT__STDERR);
        m_br_ctrlr_notifications_filter.addAction(Constants.Strings.INTENT.ACTION.CTRLR_NOTIFICATION.MVT_SERVER__OUTPUT__STDOUT);
        m_br_ctrlr_notifications_filter.addAction(Constants.Strings.INTENT.ACTION.CTRLR_NOTIFICATION.MVT_SERVER__JSON_READ);
        m_br_ctrlr_notifications_filter.addAction(Constants.Strings.INTENT.ACTION.CTRLR_NOTIFICATION.MVT_SERVER__STOPPING);
        m_br_ctrlr_notifications_filter.addAction(Constants.Strings.INTENT.ACTION.CTRLR_NOTIFICATION.MVT_SERVER__STOPPED);
        m_br_ctrlr_notifications = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent != null) {
                    Log.d(TAG, "m_br_ctrlr_notifications received: " + intent.getAction());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Constants.Enums.E_INTENT_ACTION__CTRLR_NOTIFICATION e_ctrlr_notification = Constants.Enums.E_INTENT_ACTION__CTRLR_NOTIFICATION.fromString(intent != null ? intent.getAction() : null);
                            switch (e_ctrlr_notification) {
                                case CONTROLLER_FOREGROUND_STARTING: {
                                    OnControllerStarting();
                                    break;
                                }
                                case CONTROLLER_FOREGROUND_STARTED: {
                                    OnControllerRunning();
                                    break;
                                }
                                case CONTROLLER_FOREGROUND_STOPPING: {
                                    OnControllerStopping();
                                    break;
                                }
                                case CONTROLLER_FOREGROUND_STOPPED: {
                                    OnControllerStopped();
                                    break;
                                }
                                case MVT_SERVER__STARTING: {
                                    OnMVTServerStarting();
                                    break;
                                }
                                case MVT_SERVER__START_FAILED: {
                                    OnMVTServerStartFailed(intent.getStringExtra(Constants.Strings.INTENT.ACTION.CTRLR_NOTIFICATION.EXTRA__KEY.MVT_SERVER__START_FAILED__REASON));
                                    break;
                                }
                                case MVT_SERVER__RUNNING: {
                                    OnMVTServerRunning(intent.getIntExtra(Constants.Strings.INTENT.ACTION.CTRLR_NOTIFICATION.EXTRA__KEY.MVT_SERVER__STARTED__PID, -1));
                                    break;
                                }
                                case MVT_SERVER__LISTENING: {
                                    OnMVTServerListening(intent.getIntExtra(Constants.Strings.INTENT.ACTION.CTRLR_NOTIFICATION.EXTRA__KEY.MVT_SERVER__LISTENING__PORT, 8080));
                                    break;
                                }
                                case MVT_SERVER__OUTPUT__LOGCAT: {
                                    OnMVTServerOutputLogcat(intent.getStringExtra(Constants.Strings.INTENT.ACTION.CTRLR_NOTIFICATION.EXTRA__KEY.MVT_SERVER__OUTPUT__LOGCAT__LINE));
                                    break;
                                }
                                case MVT_SERVER__OUTPUT__STDERR: {
                                    OnMVTServerOutputStdErr(intent.getStringExtra(Constants.Strings.INTENT.ACTION.CTRLR_NOTIFICATION.EXTRA__KEY.MVT_SERVER__OUTPUT__STDERR__LINE));
                                    break;
                                }
                                case MVT_SERVER__OUTPUT__STDOUT: {
                                    OnMVTServerOutputStdOut(intent.getStringExtra(Constants.Strings.INTENT.ACTION.CTRLR_NOTIFICATION.EXTRA__KEY.MVT_SERVER__OUTPUT__STDOUT__LINE));
                                    break;
                                }
                                case MVT_SERVER__JSON_READ: {
                                    OnMVTServerJSONRead(
                                        intent.getStringExtra(Constants.Strings.INTENT.ACTION.CTRLR_NOTIFICATION.EXTRA__KEY.MVT_SERVER__JSON_READ__ROOT_URL),
                                        intent.getStringExtra(Constants.Strings.INTENT.ACTION.CTRLR_NOTIFICATION.EXTRA__KEY.MVT_SERVER__JSON_READ__JSON_ENDPOINT),
                                        intent.getStringExtra(Constants.Strings.INTENT.ACTION.CTRLR_NOTIFICATION.EXTRA__KEY.MVT_SERVER__JSON_READ__JSON),
                                        intent.getStringExtra(Constants.Strings.INTENT.ACTION.CTRLR_NOTIFICATION.EXTRA__KEY.MVT_SERVER__JSON_READ__PURPOSE)
                                    );
                                    break;
                                }
                                case MVT_SERVER__STOPPING: {
                                    OnMVTServerStopping();
                                    break;
                                }
                                case MVT_SERVER__STOPPED: {
                                    OnMVTServerStopped();
                                    break;
                                }
                                default: {
                                    Log.e(TAG, "m_br_ctrlr_notifications received: " + intent.getAction() + " but NO HANDLER IS DEFINED!");
                                    break;
                                }
                            }
                        }
                    });
                } else {
                    Log.e(TAG, "m_br_ctrlr_notifications received null intent!");
                }
            }
        };
        registerReceiver(m_br_ctrlr_notifications, m_br_ctrlr_notifications_filter, null, new Handler(getMainLooper()));
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy: unregisterReceiver(m_br_ctrlr_notifications)");
        unregisterReceiver(m_br_ctrlr_notifications);
        super.onDestroy();
    }

    final String FRAG_DRAWER_CONTENT = "FRAG_DRAWER_CONTENT";

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onPostCreate: entered - savedInstanceState is " + (savedInstanceState != null ? "NOT " : "") + "null");
        if (savedInstanceState != null) {
            Log.d(TAG, "onPostCreate: savedInstanceState.getBoolean(SAVE_INSTANCE_ARG__CTRLR_RUNNING, false)==" + savedInstanceState.getBoolean(SAVE_INSTANCE_ARG__CTRLR_RUNNING, false));
        }

        //set title to build version
        setTitle(getString(R.string.app_name) + " - build " + BuildConfig.VERSION_NAME);

        //set andro_dev info fixed val content
        m_tv_val_CPU_ABI.setText(Constants.Enums.CPU_ABI.fromDevice().toString());
        m_tv_val_API_level.setText(Build.VERSION.SDK);

        m_drawerlayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        OnMVTServerStopped();
        OnControllerStopped();

        //set expandable sections UI initial "expanded" state
        m_vw_sect_content__andro_dev_nfo.collapse();
        m_vw_sect_content__andro_dev_nfo.setExpanded(false);
        m_vw_sect_content__ctrlr_nfo.expand();
        m_vw_sect_content__ctrlr_nfo.setExpanded(true);

        m_sect_content__item__srvr_console_output.setVisibility(View.GONE);
        m_tv_tegola_console_output.setMovementMethod(new ScrollingMovementMethod());

        //now queue up initial automated UI actions
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (savedInstanceState == null || !savedInstanceState.getBoolean(SAVE_INSTANCE_ARG__CTRLR_RUNNING, false))
                    start_controller_fgs();
                Intent intent_query_mvt_server_is_running = new Intent(Constants.Strings.INTENT.ACTION.MVT_SERVER_STATE_QUERY.IS_RUNNING);
                sendBroadcast(intent_query_mvt_server_is_running);
                Intent intent_query_mvt_server_listen_port = new Intent(Constants.Strings.INTENT.ACTION.MVT_SERVER_STATE_QUERY.LISTEN_PORT);
                sendBroadcast(intent_query_mvt_server_listen_port);

                //reconcile expandable sections UI with initial "expanded" state
                m_vw_sect_content__andro_dev_nfo.callOnClick();
                m_vw_sect_content__ctrlr_nfo.callOnClick();

                m_tv_tegola_console_output__scroll_max();

                //adjust main scroll view (since expandable sections may or may not have been expanded/collapsed based on initial settings)
                m_scvw_main__scroll_max();
            }
        }, 50);

//        if (BuildConfig.mbgl_test_style_json) {
//            Log.d(TAG, "onPostCreate: BuildConfig.mbgl_test_style_json==true --> starting mbgl mapview with test mbgl_style_url " + BuildConfig.mbgl_test_style_json_url);
//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    mbgl_map_start(BuildConfig.mbgl_test_style_json_url);
//                }
//            }, 50);
//        }

        super.onPostCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        Log.e(TAG, "onResume: entered");
        //now queue up initial automated UI actions
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //set srvr provider type (postGIS/geopackage) based on PersistentConfigSettingsManager.TM_PROVIDER__IS_GEOPACKAGE val
                if (SharedPrefsManager.BOOLEAN_SHARED_PREF.TM_PROVIDER__IS_GEOPACKAGE.getValue() == true) {
                    m_rb_val_provider_type_sel__gpkg.setChecked(true);
                } else {
                    m_rb_val_provider_type_sel__postgis.setChecked(true);
                }

                //set srvr config selection type (local/remote) based on PersistentConfigSettingsManager.TM_CONFIG_TOML__IS_REMOTE val
                if (SharedPrefsManager.BOOLEAN_SHARED_PREF.TM_CONFIG_TOML__IS_REMOTE.getValue() == true) {
                    m_rb_val_config_type_sel__remote.setChecked(true);
                } else {
                    m_rb_val_config_type_sel__local.setChecked(true);
                }

                m_tv_tegola_console_output__scroll_max();

                //adjust main scroll view (since expandable sections may or may not have been expanded/collapsed based on initial settings)
                m_scvw_main__scroll_max();
            }
        }, 50);

        super.onResume();
    }

    @Override
    protected void onStop() {
        Log.e(TAG, "onStop: entered");
        super.onStop();
//        GoogleDriveFileDownloadManager.getInstance().disconnect_api_client();
        //super.onPause();
    }


    //user-UI-interaction listeners...
    //reaction to toggling expandable section
    private final View.OnClickListener OnClickListener__btn_expandable_section = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ExpandableRelativeLayout expandable_section = null;
            switch (v.getId()) {
                case R.id.btn_sect__andro_dev_nfo__expand:
                    expandable_section = m_vw_sect_content__andro_dev_nfo;
                    break;
                case R.id.btn_sect__ctrlr_nfo__expand:
                    expandable_section = m_vw_sect_content__ctrlr_nfo;
                    break;
                default: return;
            }
            final ExpandableRelativeLayout final_expandable_section = expandable_section;
            if (final_expandable_section.isExpanded()) {
                final_expandable_section.collapse();
                final_expandable_section.setExpanded(false);
            } else {
                final_expandable_section.expand();
                final_expandable_section.setExpanded(true);
            }
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    reconcile_expandable_section(final_expandable_section);
                }
            }, 50);
        }
    };

    //reaction to postGIS provider-type selection - display all postGIS config selection UI and update shared prefs to reflect selection
    private final CompoundButton.OnCheckedChangeListener OnCheckedChangeListener__m_rb_val_provider_type_sel__postgis = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
            if (checked) {
                m_sect__gpkg_provider_spec.setVisibility(View.GONE);
                m_sect__postgis_provider_spec.setVisibility(View.VISIBLE);
                SharedPrefsManager.BOOLEAN_SHARED_PREF.TM_PROVIDER__IS_GEOPACKAGE.setValue(false);
            }
        }
    };

    //reaction to gpkg provider-type selection - display all gpkg bundle selection UI and update shared prefs to reflect selection
    private final CompoundButton.OnCheckedChangeListener OnCheckedChangeListener__m_rb_val_provider_type_sel__gpkg = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
            if (checked) {
                m_sect__postgis_provider_spec.setVisibility(View.GONE);
                m_sect__gpkg_provider_spec.setVisibility(View.VISIBLE);
                SharedPrefsManager.BOOLEAN_SHARED_PREF.TM_PROVIDER__IS_GEOPACKAGE.setValue(true);
                synchronize_spinner_val_gpkg_bundle_sel();
                synchronize_spinner_val_gpkg_bundle_props_sel();
            }
        }
    };

    private void synchronize_spinner_val_gpkg_bundle_sel() {
        //1. enumerate geopackage-bundles and display results in spinner (drop-down)
        File f_gpkg_bundles_root_dir = null;
        try {
            f_gpkg_bundles_root_dir = Utils.GPKG.Local.F_GPKG_DIR.getInstance(getApplicationContext());
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return;
        } catch (IOException e) {
            e.printStackTrace();
        }
        File[] f_gpkg_bundles_root_dir_files = f_gpkg_bundles_root_dir.listFiles();

        //2.1 remove current entries from m_spinner_val_gpkg_bundle_sel dataAdapter
        Log.d(TAG, "synchronize_spinner_val_gpkg_bundle_sel: clearing spinner items");
        m_spinner_val_gpkg_bundle_sel__items.clear();

        if (f_gpkg_bundles_root_dir_files.length > 0) {//found gpkg bundles
            //2.2 add found geopackage bundle names into m_spinner_val_gpkg_bundle_sel dataAdapter
            for (int i = 0; i < f_gpkg_bundles_root_dir_files.length; i++) {
                File f_gpkg_bundle_candidate = f_gpkg_bundles_root_dir_files[i];
                final String name = f_gpkg_bundle_candidate.getName();
                if (f_gpkg_bundle_candidate.isDirectory()) {
                    Log.d(TAG, "synchronize_spinner_val_gpkg_bundle_sel: found geopackage-bundle \"" + name + "\" - adding it to spinner items");
                    m_spinner_val_gpkg_bundle_sel__items.add(name);
                } else {
                    Log.d(TAG, "synchronize_spinner_val_gpkg_bundle_sel: found errant file \"" + name + "\" in root geopackage-bundle directory - note that there should be no errant files here");
                }
            }
        } else {//no geopacklage bundles found
            //2.2 add "not found" item @ position 0
            String s_no_geopackage_bundles_installed = getString(R.string.srvr_provider_type__gpkg__no_geopackage_bundles_installed);
            Log.d(TAG, "synchronize_spinner_val_gpkg_bundle_sel: no geopackage bundles installed! adding \"" + s_no_geopackage_bundles_installed + "\" to spinner items");
            m_spinner_val_gpkg_bundle_sel__items.add(s_no_geopackage_bundles_installed);
        }

        //3. reconcile ConfigSettings.STRING_CONFIG_SETTING.TM_PROVIDER__GPKG_BUNDLE__SELECTION setting with m_spinner_val_gpkg_bundle_sel__items selection and update selection as necessary
        int i_sel_pos = m_spinner_val_gpkg_bundle_sel__dataadapter.getPosition(SharedPrefsManager.STRING_SHARED_PREF.TM_PROVIDER__GPKG_BUNDLE__SELECTION.getValue());
        if (i_sel_pos != -1) {
            Log.d(TAG, "synchronize_spinner_val_gpkg_bundle_sel: synchronizing shared pref setting " + SharedPrefsManager.STRING_SHARED_PREF.TM_PROVIDER__GPKG_BUNDLE__SELECTION.toString() + " current value \"" + SharedPrefsManager.STRING_SHARED_PREF.TM_PROVIDER__GPKG_BUNDLE__SELECTION.getValue() + "\" spinner item selection to existing item position " + i_sel_pos);
        } else {
            //note that we must reset i_sel_pos to 0 here since it will be assigned -1 if we are here
            i_sel_pos = 0;
            Log.d(TAG,
                    "synchronize_spinner_val_gpkg_bundle_sel: cannot synchronize shared prefs setting " + SharedPrefsManager.STRING_SHARED_PREF.TM_PROVIDER__GPKG_BUNDLE__SELECTION.toString() + " current value \"" + SharedPrefsManager.STRING_SHARED_PREF.TM_PROVIDER__GPKG_BUNDLE__SELECTION.getValue()
                            + "\" to spinner item selection since spinner does not currently have a selectable item with that value; setting spinner selected item position to " + i_sel_pos + " for value \"" + m_spinner_val_gpkg_bundle_sel__items.get(i_sel_pos) + "\"");
        }

        //4. commit changes to spinner to allow for listener to react
        m_spinner_val_gpkg_bundle_sel.setSelection(i_sel_pos);
        m_spinner_val_gpkg_bundle_sel__dataadapter.notifyDataSetChanged();
    }

    private void synchronize_spinner_val_gpkg_bundle_props_sel() {
        //1. enumerate geopackage-bundle config files and display results in spinner (drop-down)
        File f_gpkg_bundle_dir = null;
        try {
            f_gpkg_bundle_dir = new File(Utils.GPKG.Local.F_GPKG_DIR.getInstance(getApplicationContext()).getPath(), SharedPrefsManager.STRING_SHARED_PREF.TM_PROVIDER__GPKG_BUNDLE__SELECTION.getValue());
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return;
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!f_gpkg_bundle_dir.exists()) {
            Log.d(TAG, "synchronize_spinner_val_gpkg_bundle_props_sel: gpkg-bundle does not exist - exiting");
            return;
        }
        File[] f_gpkg_bundle_props_files = f_gpkg_bundle_dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".properties");
            }
        });

        //2.1 remove current entries from synchronize_spinner_val_gpkg_bundle_props_sel dataAdapter
        Log.d(TAG, "synchronize_spinner_val_gpkg_bundle_props_sel: clearing spinner items");
        m_spinner_val_gpkg_bundle_props_sel__items.clear();

        if (f_gpkg_bundle_props_files.length > 0) {//found props files
            //2.2 add found geopackage bundle config file names into synchronize_spinner_val_gpkg_bundle_props_sel dataAdapter
            for (int i = 0; i < f_gpkg_bundle_props_files.length; i++) {
                File f_gpkg_bundle_config_candidate = f_gpkg_bundle_props_files[i];
                final String name = f_gpkg_bundle_config_candidate.getName();
                if (!f_gpkg_bundle_config_candidate.isDirectory()) {
                    Log.d(TAG, "synchronize_spinner_val_gpkg_bundle_props_sel: found geopackage-bundle config file \"" + name + "\" - adding it to spinner items");
                    m_spinner_val_gpkg_bundle_props_sel__items.add(name);
                } else {
                    Log.d(TAG, "synchronize_spinner_val_gpkg_bundle_props_sel: \"" + name + "\" is a directory");
                }
            }
        } else {//no geopacklage bundle config files found
            //2.2 add "not found" item @ position 0
            String s_no_geopackage_bundle_props_files_installed = getString(R.string.srvr_provider_type__gpkg__no_geopackage_bundle_props_installed);
            Log.d(TAG, "synchronize_spinner_val_gpkg_bundle_props_sel: no geopackage bundle configs installed! adding \"" + s_no_geopackage_bundle_props_files_installed + "\" to spinner items");
            m_spinner_val_gpkg_bundle_props_sel__items.add(s_no_geopackage_bundle_props_files_installed);
        }

        //3. reconcile ConfigSettings.STRING_CONFIG_SETTING.TM_PROVIDER__GPKG_BUNDLE_PROPS__SELECTION setting with m_spinner_val_gpkg_bundle_props_sel__items selection and update selection as necessary
        int i_sel_pos = m_spinner_val_gpkg_bundle_props_sel__dataadapter.getPosition(SharedPrefsManager.STRING_SHARED_PREF.TM_PROVIDER__GPKG_BUNDLE_PROPS__SELECTION.getValue());
        if (i_sel_pos != -1) {
            Log.d(TAG, "synchronize_spinner_val_gpkg_bundle_props_sel: synchronizing shared pref setting " + SharedPrefsManager.STRING_SHARED_PREF.TM_PROVIDER__GPKG_BUNDLE_PROPS__SELECTION.toString() + " current value \"" + SharedPrefsManager.STRING_SHARED_PREF.TM_PROVIDER__GPKG_BUNDLE_PROPS__SELECTION.getValue() + "\" spinner item selection to existing item position " + i_sel_pos);
        } else {
            //note that we must reset i_sel_pos to 0 here since it will be assigned -1 if we are here
            i_sel_pos = 0;
            Log.d(TAG,
                    "synchronize_spinner_val_gpkg_bundle_props_sel: cannot synchronize shared prefs setting " + SharedPrefsManager.STRING_SHARED_PREF.TM_PROVIDER__GPKG_BUNDLE_PROPS__SELECTION.toString() + " current value \"" + SharedPrefsManager.STRING_SHARED_PREF.TM_PROVIDER__GPKG_BUNDLE_PROPS__SELECTION.getValue()
                            + "\" to spinner item selection since spinner does not currently have a selectable item with that value; setting spinner selected item position to " + i_sel_pos + " for value \"" + m_spinner_val_gpkg_bundle_sel__items.get(i_sel_pos) + "\"");
        }

        //4. commit changes to spinner to allow for listener to react
        m_spinner_val_gpkg_bundle_props_sel.setSelection(i_sel_pos);
        m_spinner_val_gpkg_bundle_props_sel__dataadapter.notifyDataSetChanged();
    }

    //reaction to local config-type selection - display all local config selection UI and update shared prefs to reflect selection
    private final CompoundButton.OnCheckedChangeListener OnCheckedChangeListener__m_rb_val_config_type_sel__local = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
            if (checked) {
                boolean sdcardmounted = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
                m_btn_config_sel_local_import__sdcard.setBackgroundColor(sdcardmounted ? ContextCompat.getColor(getApplicationContext(), android.R.color.holo_green_light) : ContextCompat.getColor(getApplicationContext(), android.R.color.holo_red_dark));
                m_btn_config_sel_local_import__sdcard.setEnabled(sdcardmounted);
                m_vw_config_sel_container__remote.setVisibility(View.GONE);
                m_vw_config_sel_container__local.setVisibility(View.VISIBLE);
                SharedPrefsManager.BOOLEAN_SHARED_PREF.TM_CONFIG_TOML__IS_REMOTE.setValue(false);
                synchronize_spinner_val_config_sel_local();
            }
        }
    };

    private final ClickableSpan ClickableSpan____m_tv_lbl_config_type_sel__local__manage_files = new ClickableSpan() {
        @Override
        public void onClick(View widget) {
            startActivityForResult(new Intent(MainActivity.this, ManageGpkgBundlesActivity.class), REQUEST_CODES.REQUEST_CODE__MANAGE_GPKG_BUNDLES);
        }
    };

    //user selects a local config file selection from spinner - synchronizes selection with shared prefs
    private final AdapterView.OnItemSelectedListener OnItemSelectedListener__m_spinner_val_config_sel_local = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapter, View view, int position, long id) {
            String s_sel_val = adapter.getItemAtPosition(position).toString();
            Log.d(TAG, "OnItemSelectedListener__m_spinner_val_config_sel_local.onItemSelected: triggered item selection @ position " + position + " with value " + (s_sel_val == null ? "null" : "\"" + s_sel_val + "\""));

            String s_cached_config_sel__local_val = SharedPrefsManager.STRING_SHARED_PREF.TM_CONFIG_TOML__LOCAL__SELECTION.getValue();
            Log.d(TAG, "OnItemSelectedListener__m_spinner_val_config_sel_local.onItemSelected: shared pref setting " + SharedPrefsManager.STRING_SHARED_PREF.TM_CONFIG_TOML__LOCAL__SELECTION.toString() + " current value is \"" + s_cached_config_sel__local_val + "\"");

            boolean no_config_files = (s_sel_val == null || s_sel_val.compareTo(getString(R.string.srvr_config_type__local__no_config_files_found)) == 0);
            if (no_config_files) {
                Log.d(TAG, "OnItemSelectedListener__m_spinner_val_config_sel_local.onItemSelected: no-config-files condition!");
                if (!s_cached_config_sel__local_val.isEmpty()) {
                    Log.d(TAG, "OnItemSelectedListener__m_spinner_val_config_sel_local.onItemSelected: clearing shared pref setting " + SharedPrefsManager.STRING_SHARED_PREF.TM_CONFIG_TOML__LOCAL__SELECTION.toString() + " value (currently \"" + s_cached_config_sel__local_val + "\")");
                    Toast.makeText(getApplicationContext(), "Clearing setting value for local config toml file selection since there are none available", Toast.LENGTH_LONG).show();
                    SharedPrefsManager.STRING_SHARED_PREF.TM_CONFIG_TOML__LOCAL__SELECTION.setValue("");
                } else {
                    Log.d(TAG, "OnItemSelectedListener__m_spinner_val_config_sel_local.onItemSelected: skipping change to shared pref setting " + SharedPrefsManager.STRING_SHARED_PREF.TM_CONFIG_TOML__LOCAL__SELECTION.toString() + " since it is already cleared (value is \"" + s_cached_config_sel__local_val + "\")");
                }

                //edit button obviously not applicable in this case
                m_btn_config_sel_local__edit_file.setVisibility(View.GONE);
                m_btn_config_sel_local__edit_file.setEnabled(false);
                //and neither is MVT srvr control (start/stop) button
                m_btn_srvr_ctrl.setEnabled(false);

                //finally display alertdialog notifying user that tegola cannot be used until a config file is imported
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(new ContextThemeWrapper(MainActivity.this, R.style.alert_dialog));
                alertDialogBuilder.setTitle(getString(R.string.srvr_config_type__local__no_config_files_found));
                alertDialogBuilder
                        .setMessage(getString(R.string.srvr_config_type__local__no_config_files_found__alert_msg))
                        .setCancelable(false)
                        .setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            } else {
                //first, update shared pref val as necessary - does sel value differ from cached?
                if (s_cached_config_sel__local_val.compareTo(s_sel_val) != 0) {
                    Toast.makeText(getApplicationContext(), "Saving new setting value for local config toml file \"" + s_sel_val + "\" selection", Toast.LENGTH_LONG).show();
                    //now update shared pref
                    SharedPrefsManager.STRING_SHARED_PREF.TM_CONFIG_TOML__LOCAL__SELECTION.setValue(s_sel_val);
                    Log.d(TAG, "OnItemSelectedListener__m_spinner_val_config_sel_local.onItemSelected: changed setting " + SharedPrefsManager.STRING_SHARED_PREF.TM_CONFIG_TOML__LOCAL__SELECTION.toString() + " value from \"" + s_cached_config_sel__local_val + "\" to \"" + SharedPrefsManager.STRING_SHARED_PREF.TM_CONFIG_TOML__LOCAL__SELECTION.getValue() + "\"");
                } else {
                    //no change to shared pref val
                    Log.d(TAG, "OnItemSelectedListener__m_spinner_val_config_sel_local.onItemSelected: skipping change to shared pref setting " + SharedPrefsManager.STRING_SHARED_PREF.TM_CONFIG_TOML__LOCAL__SELECTION.toString() + " value (\"" + SharedPrefsManager.STRING_SHARED_PREF.TM_CONFIG_TOML__LOCAL__SELECTION.getValue() + "\") since new value (\"" + s_sel_val + "\") is no different");
                }

                //now update m_btn_config_sel_local__edit_file UI based on existence of current local config toml file selection setting (SharedPrefsManager.STRING_SHARED_PREF.TM_CONFIG_TOML__LOCAL__SELECTION.getValue())
                File file = new File(getFilesDir().getPath() + "/" + SharedPrefsManager.STRING_SHARED_PREF.TM_CONFIG_TOML__LOCAL__SELECTION.getValue());
                m_btn_config_sel_local__edit_file.setVisibility(file.exists() ? View.VISIBLE : View.GONE);
                m_btn_config_sel_local__edit_file.setEnabled(file.exists());
                //and same MVT srvr control (start/stop) button
                m_btn_srvr_ctrl.setEnabled(file.exists());
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapter) {
            Toast.makeText(getApplicationContext(), "Cleared local config toml file selection", Toast.LENGTH_LONG).show();
            //disable/hide m_btn_config_sel_local__edit_file
            m_btn_config_sel_local__edit_file.setVisibility(View.GONE);
            m_btn_config_sel_local__edit_file.setEnabled(false);
        }
    };

    //user clicks button to edit/open/view selected local config file selection
    private final View.OnClickListener OnClickListener__m_btn_config_sel_local__edit_file = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            edit_local_config_file(SharedPrefsManager.STRING_SHARED_PREF.TM_CONFIG_TOML__LOCAL__SELECTION.getValue());
        }
    };

    private void edit_local_config_file(@NonNull final String config_filename) {
        File f_config_toml = new File(getFilesDir().getPath() + "/" + config_filename);
        if (f_config_toml.exists()) {
            try {
                Log.d(TAG, "edit_local_config_file: " + f_config_toml.getPath() + " exists; starting ConfigFileEditorActivity for result...");
                Intent intent_edit_config_toml = new Intent(getApplicationContext(), ConfigFileEditorActivity.class);
                intent_edit_config_toml.putExtra(Strings.EDITOR_INTENT_EXTRAS.FILENAME, config_filename);
                startActivityForResult(intent_edit_config_toml, REQUEST_CODES.REQUEST_CODE__EDIT_TOML_FILE);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to start config file viewer/editor!", Toast.LENGTH_LONG).show();
            }
        } else {
            String s_err = f_config_toml.getPath() + " does not exist! nothing to edit/view";
            Log.e(TAG, "edit_local_config_file: " + s_err);
            Toast.makeText(this, s_err, Toast.LENGTH_LONG).show();
        }
    }

    //user clicks button to initiate import config toml files from sdcard
    private final View.OnClickListener OnClickListener__m_btn_config_sel_local_import__sdcard = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            import_config_toml__from_sdcard();
        }
    };
    private void import_config_toml__from_sdcard() {
        try {
            Intent intent_get_file_content = new Intent(Intent.ACTION_GET_CONTENT);
            intent_get_file_content.setType("text/plain" /*first preferred mime-type*/);
            intent_get_file_content.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{FileUtils.MIME_TYPE_TEXT /*second preferred mime-type*/, "application/octet-stream" /*third preferred and catch-all mime-type*/});
            intent_get_file_content.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(Intent.createChooser(intent_get_file_content, "Select Tegola config TOML file"), REQUEST_CODES.REQUEST_CODE__SELECT_TOML_FILES_FOR_IMPORT__LOCAL_STORAGE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //user clicks button to initiate import config toml files from google drive
//    private final View.OnClickListener OnClickListener__m_btn_config_sel_local_import__googledrive = new View.OnClickListener() {
//        @Override
//        public void onClick(View view) {
//            import_config_toml__from_google_drive();
//        }
//    };
//    private void import_config_toml__from_google_drive() {
//        GoogleDriveFileDownloadManager.getInstance().validate_init_api_client(this, m_google_api_callbacks, m_google_api_callbacks);
//        if (GoogleDriveFileDownloadManager.getInstance().validate_connect_api_client(this)) {
//            Log.i(TAG, "import_config_toml__from_google_drive: calling select_and_download_files() for Filter Filters.contains(SearchableField.TITLE, \".toml\")...");
//            GoogleDriveFileDownloadManager.getInstance().select_and_download_files(this, Filters.contains(SearchableField.TITLE, ".toml"), REQUEST_CODES.REQUEST_CODE__SELECT_TOML_FILES_FOR_IMPORT__GOOGLEDRIVE);
//        } else {
//            Log.i(TAG, "import_config_toml__from_google_drive: GoogleApiClient was not connected -- flow control was transferred to appropriate handler");
//        }
//    }


    //reaction to srvr remote config-type selection - display all remote config selection UI and update shared prefs to reflect selection
    private final CompoundButton.OnCheckedChangeListener OnCheckedChangeListener__m_rb_val_config_type_sel__remote = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
            if (checked) {
                m_vw_config_sel_container__local.setVisibility(View.GONE);
                m_vw_config_sel_container__remote.setVisibility(View.VISIBLE);
                SharedPrefsManager.BOOLEAN_SHARED_PREF.TM_CONFIG_TOML__IS_REMOTE.setValue(true);
                synchronize_edittext_val_config_sel_remote();
            }
        }
    };

    //reaction to changing remote config URL value - user must press enter in editor or switch focus to another control to register to app that a pending change has occurred
    private final TextView.OnEditorActionListener OnEditorActionListener__m_edt_val_config_sel__remote = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_NULL) {
                validate__m_edt_val_config_sel__remote();
                return true;
            } else
                return false;
        }
    };
    private final TextView.OnFocusChangeListener OnFocusChangeListener__m_edt_val_config_sel__remote = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (!hasFocus)
                validate__m_edt_val_config_sel__remote();
        }
    };

    //reaction to when user applies changes to remote config URL
    private final View.OnClickListener OnClickListener__m_btn_config_sel_remote_apply_changes = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String s_remote_config_toml_sel_normalized = m_edt_val_config_sel__remote.getText().toString();
            Log.d(TAG, "m_btn_config_sel_remote_apply_changes.setOnClickListener: triggered remote config change with value " + (s_remote_config_toml_sel_normalized == null ? "null" : "\"" + s_remote_config_toml_sel_normalized + "\""));
            if (s_remote_config_toml_sel_normalized == null) {
                Log.d(TAG, "m_btn_config_sel_remote_apply_changes.setOnClickListener: normalizing remote config change (null) to \"\"");
                s_remote_config_toml_sel_normalized = "";
            }
            String s_old_config_sel__remote_val = SharedPrefsManager.STRING_SHARED_PREF.TM_CONFIG_TOML__REMOTE__SELECTION.getValue();
            Log.d(TAG, "m_btn_config_sel_remote_apply_changes.setOnClickListener: shared pref setting " + SharedPrefsManager.STRING_SHARED_PREF.TM_CONFIG_TOML__REMOTE__SELECTION.toString() + " current value is \"" + SharedPrefsManager.STRING_SHARED_PREF.TM_CONFIG_TOML__REMOTE__SELECTION.getValue() + "\"");
            if (s_old_config_sel__remote_val.compareTo(s_remote_config_toml_sel_normalized) != 0) {
                if (s_remote_config_toml_sel_normalized.isEmpty())
                    Toast.makeText(getApplicationContext(), "Clearing remote config toml file selection", Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(getApplicationContext(), "Saving new setting value for remote config toml file https://" + s_remote_config_toml_sel_normalized, Toast.LENGTH_LONG).show();
                SharedPrefsManager.STRING_SHARED_PREF.TM_CONFIG_TOML__REMOTE__SELECTION.setValue(s_remote_config_toml_sel_normalized);
                Log.d(TAG, "m_btn_config_sel_remote_apply_changes.setOnClickListener: changed setting " + SharedPrefsManager.STRING_SHARED_PREF.TM_CONFIG_TOML__REMOTE__SELECTION.toString() + " value from \"" + s_old_config_sel__remote_val + "\" to \"" + SharedPrefsManager.STRING_SHARED_PREF.TM_CONFIG_TOML__REMOTE__SELECTION.getValue() + "\"");
                m_btn_config_sel_remote_apply_changes.setEnabled(false);
            } else {
                //no change to share pref val - do nothing other than log
                Log.d(TAG, "m_btn_config_sel_remote_apply_changes.setOnClickListener: skipping change to shared pref setting " + SharedPrefsManager.STRING_SHARED_PREF.TM_CONFIG_TOML__REMOTE__SELECTION.toString() + " value (\"" + SharedPrefsManager.STRING_SHARED_PREF.TM_CONFIG_TOML__REMOTE__SELECTION.getValue() + "\") since normalized new value (\"" + s_remote_config_toml_sel_normalized + "\") is no different");
            }
            synchronize_edittext_val_config_sel_remote();
        }
    };

    private final View.OnClickListener OnClickListener__m_btn_srvr_ctrl = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Button btn_srvr_ctrl = (Button)v;
            Boolean srvr_started = (Boolean)btn_srvr_ctrl.getTag(R.id.TAG__SRVR_RUNNING);
            if (srvr_started == null || !srvr_started) {
                start_mvt_server();
            } else {
                stop_mvt_server();
            }
        }
    };

    //user selects a geopackage-bundle from spinner - synchronizes selection with shared prefs
    private final AdapterView.OnItemSelectedListener OnItemSelectedListener__m_spinner_val_gpkg_bundle_sel = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapter, View view, int position, long id) {
            String s_sel_val = adapter.getItemAtPosition(position).toString();
            Log.d(TAG, "OnItemSelectedListener__m_spinner_val_gpkg_bundle_sel.onItemSelected: triggered item selection @ position " + position + " with value " + (s_sel_val == null ? "null" : "\"" + s_sel_val + "\""));

            String s_cached_gpkg_bundle_val = SharedPrefsManager.STRING_SHARED_PREF.TM_PROVIDER__GPKG_BUNDLE__SELECTION.getValue();
            Log.d(TAG, "OnItemSelectedListener__m_spinner_val_gpkg_bundle_sel.onItemSelected: shared pref setting " + SharedPrefsManager.STRING_SHARED_PREF.TM_PROVIDER__GPKG_BUNDLE__SELECTION.toString() + " current value is \"" + s_cached_gpkg_bundle_val + "\"");

            boolean no_gpkg_bundles = (s_sel_val == null || s_sel_val.compareTo(getString(R.string.srvr_provider_type__gpkg__no_geopackage_bundles_installed)) == 0);
            if (no_gpkg_bundles) {
                Log.d(TAG, "OnItemSelectedListener__m_spinner_val_gpkg_bundle_sel.onItemSelected: no-gpkg-bundles condition!");
                if (!s_cached_gpkg_bundle_val.isEmpty()) {
                    Log.d(TAG, "OnItemSelectedListener__m_spinner_val_gpkg_bundle_sel.onItemSelected: clearing shared pref setting " + SharedPrefsManager.STRING_SHARED_PREF.TM_PROVIDER__GPKG_BUNDLE__SELECTION.toString() + " value (currently \"" + s_cached_gpkg_bundle_val + "\")");
                    Toast.makeText(getApplicationContext(), "Clearing setting value for geopackage-bundle selection since there are none installed", Toast.LENGTH_LONG).show();
                    SharedPrefsManager.STRING_SHARED_PREF.TM_PROVIDER__GPKG_BUNDLE__SELECTION.setValue("");
                } else {
                    Log.d(TAG, "OnItemSelectedListener__m_spinner_val_gpkg_bundle_sel.onItemSelected: skipping change to shared pref setting " + SharedPrefsManager.STRING_SHARED_PREF.TM_PROVIDER__GPKG_BUNDLE__SELECTION.toString() + " since it is already cleared (value is \"" + s_cached_gpkg_bundle_val + "\")");
                }

                m_btn_srvr_ctrl.setEnabled(false);

                //finally display alertdialog notifying user that tegola cannot be used until a gpkg-bundle is installed
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(new ContextThemeWrapper(MainActivity.this, R.style.alert_dialog));
                alertDialogBuilder.setTitle(getString(R.string.srvr_provider_type__gpkg__no_geopackage_bundles_installed));
                alertDialogBuilder
                        .setMessage(getString(R.string.srvr_provider_type__gpkg__no_geopackage_bundles_installed__alert_msg))
                        .setCancelable(false)
                        .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        })
                        .setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                startActivityForResult(new Intent(MainActivity.this, InstallGpkgBundleActivity.class), REQUEST_CODES.REQUEST_CODE__INSTALL_GPKG_BUNDLE);
                                dialog.dismiss();
                            }
                        });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            } else {
                //first, update shared pref val as necessary - does sel value differ from cached?
                if (s_cached_gpkg_bundle_val.compareTo(s_sel_val) != 0) {
                    Toast.makeText(getApplicationContext(), "Saving new setting value for geopackage-bundle \"" + s_sel_val + "\" selection", Toast.LENGTH_LONG).show();
                    //now update shared pref
                    SharedPrefsManager.STRING_SHARED_PREF.TM_PROVIDER__GPKG_BUNDLE__SELECTION.setValue(s_sel_val);
                    Log.d(TAG, "OnItemSelectedListener__m_spinner_val_gpkg_bundle_sel.onItemSelected: changed setting " + SharedPrefsManager.STRING_SHARED_PREF.TM_PROVIDER__GPKG_BUNDLE__SELECTION.toString() + " value from \"" + s_cached_gpkg_bundle_val + "\" to \"" + SharedPrefsManager.STRING_SHARED_PREF.TM_PROVIDER__GPKG_BUNDLE__SELECTION.getValue() + "\"");
                } else {
                    //no change to shared pref val
                    Log.d(TAG, "OnItemSelectedListener__m_spinner_val_gpkg_bundle_sel.onItemSelected: skipping change to shared pref setting " + SharedPrefsManager.STRING_SHARED_PREF.TM_PROVIDER__GPKG_BUNDLE__SELECTION.toString() + " value (\"" + SharedPrefsManager.STRING_SHARED_PREF.TM_PROVIDER__GPKG_BUNDLE__SELECTION.getValue() + "\") since new value (\"" + s_sel_val + "\") is no different");
                }

                //now update UI based on existence of current local geopackage-bundle selection setting (SharedPrefsManager.STRING_SHARED_PREF.TM_PROVIDER__GPKG_BUNDLE__SELECTION.getValue())
                File f_gpkg_bundles_root_dir = null;
                try {
                    f_gpkg_bundles_root_dir = Utils.GPKG.Local.F_GPKG_DIR.getInstance(getApplicationContext());
                    File f_gpkg_bundle = new File(f_gpkg_bundles_root_dir, SharedPrefsManager.STRING_SHARED_PREF.TM_PROVIDER__GPKG_BUNDLE__SELECTION.getValue());
                    //and same MVT srvr control (start/stop) button
                    m_btn_srvr_ctrl.setEnabled(f_gpkg_bundle.exists());
                    if (f_gpkg_bundle.exists())
                        synchronize_spinner_val_gpkg_bundle_props_sel();
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapter) {
            Toast.makeText(getApplicationContext(), "Cleared local config toml file selection", Toast.LENGTH_LONG).show();
            //disable/hide m_btn_config_sel_local__edit_file
            m_btn_config_sel_local__edit_file.setVisibility(View.GONE);
            m_btn_config_sel_local__edit_file.setEnabled(false);
        }
    };

    //user selects a geopackage-bundle config from spinner - synchronizes selection with shared prefs
    private final AdapterView.OnItemSelectedListener OnItemSelectedListener__m_spinner_val_gpkg_bundle_config_sel = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapter, View view, int position, long id) {
            String s_sel_val = adapter.getItemAtPosition(position).toString();
            Log.d(TAG, "OnItemSelectedListener__m_spinner_val_gpkg_bundle_config_sel.onItemSelected: triggered item selection @ position " + position + " with value " + (s_sel_val == null ? "null" : "\"" + s_sel_val + "\""));

            String s_cached_gpkg_bundle_config_val = SharedPrefsManager.STRING_SHARED_PREF.TM_PROVIDER__GPKG_BUNDLE_PROPS__SELECTION.getValue();
            Log.d(TAG, "OnItemSelectedListener__m_spinner_val_gpkg_bundle_config_sel.onItemSelected: shared pref setting " + SharedPrefsManager.STRING_SHARED_PREF.TM_PROVIDER__GPKG_BUNDLE_PROPS__SELECTION.toString() + " current value is \"" + s_cached_gpkg_bundle_config_val + "\"");

            boolean no_gpkg_bundle_cfg = (s_sel_val == null);
            if (no_gpkg_bundle_cfg) {
                Log.d(TAG, "OnItemSelectedListener__m_spinner_val_gpkg_bundle_config_sel.onItemSelected: no-gpkg-no_gpkg_bundle_cfg condition!");
                if (!s_cached_gpkg_bundle_config_val.isEmpty()) {
                    Log.d(TAG, "OnItemSelectedListener__m_spinner_val_gpkg_bundle_config_sel.onItemSelected: clearing shared pref setting " + SharedPrefsManager.STRING_SHARED_PREF.TM_PROVIDER__GPKG_BUNDLE_PROPS__SELECTION.toString() + " value (currently \"" + s_cached_gpkg_bundle_config_val + "\")");
                    Toast.makeText(getApplicationContext(), "Clearing setting value for geopackage-bundle config selection since there are none installed", Toast.LENGTH_LONG).show();
                    SharedPrefsManager.STRING_SHARED_PREF.TM_PROVIDER__GPKG_BUNDLE_PROPS__SELECTION.setValue("");
                } else {
                    Log.d(TAG, "OnItemSelectedListener__m_spinner_val_gpkg_bundle_config_sel.onItemSelected: skipping change to shared pref setting " + SharedPrefsManager.STRING_SHARED_PREF.TM_PROVIDER__GPKG_BUNDLE_PROPS__SELECTION.toString() + " since it is already cleared (value is \"" + s_cached_gpkg_bundle_config_val + "\")");
                }

                m_btn_srvr_ctrl.setEnabled(false);

                //finally display alertdialog notifying user that tegola cannot be used until a gpkg-bundle is installed
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(new ContextThemeWrapper(MainActivity.this, R.style.alert_dialog));
                alertDialogBuilder.setTitle(getString(R.string.srvr_provider_type__gpkg__no_geopackage_bundle_props_installed));
                alertDialogBuilder
                        .setMessage(getString(R.string.srvr_provider_type__gpkg__no_geopackage_bundle_configs_installed__alert_msg))
                        .setCancelable(false)
                        .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        })
                        .setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                startActivityForResult(new Intent(MainActivity.this, InstallGpkgBundleActivity.class), REQUEST_CODES.REQUEST_CODE__INSTALL_GPKG_BUNDLE);
                                dialog.dismiss();
                            }
                        });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            } else {
                //first, update shared pref val as necessary - does sel value differ from cached?
                if (s_cached_gpkg_bundle_config_val.compareTo(s_sel_val) != 0) {
                    Toast.makeText(getApplicationContext(), "Saving new setting value for geopackage-bundle config \"" + s_sel_val + "\" selection", Toast.LENGTH_LONG).show();
                    //now update shared pref
                    SharedPrefsManager.STRING_SHARED_PREF.TM_PROVIDER__GPKG_BUNDLE_PROPS__SELECTION.setValue(s_sel_val);
                    Log.d(TAG, "OnItemSelectedListener__m_spinner_val_gpkg_bundle_config_sel.onItemSelected: changed setting " + SharedPrefsManager.STRING_SHARED_PREF.TM_PROVIDER__GPKG_BUNDLE_PROPS__SELECTION.toString() + " value from \"" + s_cached_gpkg_bundle_config_val + "\" to \"" + SharedPrefsManager.STRING_SHARED_PREF.TM_PROVIDER__GPKG_BUNDLE_PROPS__SELECTION.getValue() + "\"");
                } else {
                    //no change to shared pref val
                    Log.d(TAG, "OnItemSelectedListener__m_spinner_val_gpkg_bundle_config_sel.onItemSelected: skipping change to shared pref setting " + SharedPrefsManager.STRING_SHARED_PREF.TM_PROVIDER__GPKG_BUNDLE_PROPS__SELECTION.toString() + " value (\"" + SharedPrefsManager.STRING_SHARED_PREF.TM_PROVIDER__GPKG_BUNDLE_PROPS__SELECTION.getValue() + "\") since new value (\"" + s_sel_val + "\") is no different");
                }

                //now update UI based on existence of current local geopackage-bundle config selection setting (SharedPrefsManager.STRING_SHARED_PREF.TM_PROVIDER__GPKG_BUNDLE_PROPS__SELECTION.getValue())
                File f_gpkg_bundle_dir = null;
                try {
                    f_gpkg_bundle_dir = new File(Utils.GPKG.Local.F_GPKG_DIR.getInstance(getApplicationContext()), SharedPrefsManager.STRING_SHARED_PREF.TM_PROVIDER__GPKG_BUNDLE__SELECTION.getValue());
                    File[] f_gpkg_bundle_props = f_gpkg_bundle_dir.listFiles(new FilenameFilter() {
                        @Override
                        public boolean accept(File dir, String name) {
                            return name.endsWith(".properties");
                        }
                    });
                    //and same MVT srvr control (start/stop) button
                    m_btn_srvr_ctrl.setEnabled(f_gpkg_bundle_props.length > 0);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapter) {
            Toast.makeText(getApplicationContext(), "Cleared local config toml file selection", Toast.LENGTH_LONG).show();
            //disable/hide m_btn_config_sel_local__edit_file
            m_btn_config_sel_local__edit_file.setVisibility(View.GONE);
            m_btn_config_sel_local__edit_file.setEnabled(false);
        }
    };

    //TegolaMBGLFragment overrides
    @Override
    public void onFragmentInteraction(E_MBGL_FRAG_ACTION e_mbgl_frag_action) {
        switch (e_mbgl_frag_action) {
            case HIDE: {
                if (m_drawer_handle != null)
                    m_drawer_handle.closerDrawer();
                break;
            }
            default: {
                //no-op
            }
        }
    }



    //auxiliary UI helper functions...
    private void reconcile_expandable_section(@NonNull final ExpandableRelativeLayout expandable_section) {
        Button btn_toggle = null;
        switch (expandable_section.getId()) {
            case R.id.sect_content__andro_dev_nfo: {
                btn_toggle = m_btn_sect__andro_dev_nfo__expand;
                break;
            }
            case R.id.sect_content__ctrlr_nfo: {
                btn_toggle = m_btn_sect__ctrlr_nfo__expand;
                break;
            }
        }
        boolean currently_expanded = expandable_section.isExpanded();
        Drawable drawable_arrow = ContextCompat.getDrawable(this, currently_expanded ? android.R.drawable.arrow_up_float : android.R.drawable.arrow_down_float);
        int h = drawable_arrow.getIntrinsicHeight();
        int w = drawable_arrow.getIntrinsicWidth();
        drawable_arrow.setBounds(0, 0, w, h);
        btn_toggle.setCompoundDrawables(null, null, drawable_arrow, null);
    }

    private void m_scvw_main__scroll_max() {
        m_scvw_main.postDelayed(new Runnable() {
            public void run() {
                m_scvw_main.fullScroll(View.FOCUS_DOWN);
            }
        }, 50);
    }

    private void synchronize_spinner_val_config_sel_local() {
        //1. enumerate local config.toml files and display results in spinner (drop-down)
        File f_filesDir = getFilesDir();
        File[] config_toml_files = f_filesDir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".toml");
            }
        });

        //2.1 remove current entries from m_spinner_val_config_sel_local dataAdapter
        Log.d(TAG, "synchronize_spinner_val_config_sel_local: clearing spinner items");
        m_spinner_val_config_sel_local__items.clear();

        if (config_toml_files.length > 0) {//found local config.toml files
            //2.2 add found config.toml filenames into m_spinner_val_config_sel_local dataAdapter
            for (int i = 0; i < config_toml_files.length; i++) {
                final String config_toml_filename = config_toml_files[i].getName();
                Log.d(TAG, "synchronize_spinner_val_config_sel_local: found local config '.toml' file: " + config_toml_filename + " - adding it to spinner items");
                //add this config.toml filename to spinner (drop-down) for local config file selection
                m_spinner_val_config_sel_local__items.add(config_toml_filename);
            }
        } else {//no local config.toml files found
            //2.2 add "not found" item @ position 0
            String s_config_sel__local_val__no_config_files_found = getString(R.string.srvr_config_type__local__no_config_files_found);
            Log.d(TAG, "synchronize_spinner_val_config_sel_local: no local config '.toml' files found! adding \"" + s_config_sel__local_val__no_config_files_found + "\" to spinner items");
            m_spinner_val_config_sel_local__items.add(s_config_sel__local_val__no_config_files_found);
        }

        //3. reconcile ConfigSettings.STRING_CONFIG_SETTING.TM_CONFIG_TOML__LOCAL__SELECTION setting with m_spinner_val_config_sel_local__dataadapter items and update selection as necessary
        int i_sel_pos = m_spinner_val_config_sel_local__dataadapter.getPosition(SharedPrefsManager.STRING_SHARED_PREF.TM_CONFIG_TOML__LOCAL__SELECTION.getValue());
        if (i_sel_pos != -1) {
            Log.d(TAG, "synchronize_spinner_val_config_sel_local: synchronizing shared pref setting " + SharedPrefsManager.STRING_SHARED_PREF.TM_CONFIG_TOML__LOCAL__SELECTION.toString() + " current value \"" + SharedPrefsManager.STRING_SHARED_PREF.TM_CONFIG_TOML__LOCAL__SELECTION.getValue() + "\" spinner item selection to existing item position " + i_sel_pos);
        } else {
            //note that we must reset i_sel_pos to 0 here since it will be assigned -1 if we are here
            i_sel_pos = 0;
            Log.d(TAG,
                    "synchronize_spinner_val_config_sel_local: cannot synchronize shared prefs setting " + SharedPrefsManager.STRING_SHARED_PREF.TM_CONFIG_TOML__LOCAL__SELECTION.toString() + " current value \"" + SharedPrefsManager.STRING_SHARED_PREF.TM_CONFIG_TOML__LOCAL__SELECTION.getValue()
                    + "\" to spinner item selection since spinner does not currently have a selectable item with that value; setting spinner selected item position to " + i_sel_pos + " for value \"" + m_spinner_val_config_sel_local__items.get(i_sel_pos) + "\"");
        }

        //4. commit changes to spinner to allow for listener to react
        m_spinner_val_config_sel_local.setSelection(i_sel_pos);
        m_spinner_val_config_sel_local__dataadapter.notifyDataSetChanged();
    }

    private void synchronize_edittext_val_config_sel_remote() {
        m_edt_val_config_sel__remote.setText(SharedPrefsManager.STRING_SHARED_PREF.TM_CONFIG_TOML__REMOTE__SELECTION.getValue());
        m_btn_config_sel_remote_apply_changes.setEnabled(false);
        if (SharedPrefsManager.STRING_SHARED_PREF.TM_CONFIG_TOML__REMOTE__SELECTION.getValue().isEmpty()) {
            m_btn_srvr_ctrl.setEnabled(false);
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(new ContextThemeWrapper(MainActivity.this, R.style.alert_dialog));
            alertDialogBuilder.setTitle(getString(R.string.srvr_config_type__remote__no_url_specified));
            alertDialogBuilder
                    .setMessage(getString(R.string.srvr_config_type__remote__no_url_specified__alert_msg))
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        } else
            m_btn_srvr_ctrl.setEnabled(true);
    }

    private void validate__m_edt_val_config_sel__remote() {
        String s_old_config_sel__remote_val = SharedPrefsManager.STRING_SHARED_PREF.TM_CONFIG_TOML__REMOTE__SELECTION.getValue();
        String s_config_sel__remote_val__proposted = m_edt_val_config_sel__remote.getText().toString();
        Log.d(TAG, "validate__m_edt_val_config_sel__remote: shared pref setting " + SharedPrefsManager.STRING_SHARED_PREF.TM_CONFIG_TOML__REMOTE__SELECTION.toString() + " current value is \"" + s_old_config_sel__remote_val + "\"");
        if (s_old_config_sel__remote_val.compareTo(s_config_sel__remote_val__proposted) == 0) {
            Log.d(TAG, "validate__m_edt_val_config_sel__remote: m_edt_val_config_sel__remote value is no different than shared pref setting " + SharedPrefsManager.STRING_SHARED_PREF.TM_CONFIG_TOML__REMOTE__SELECTION.toString() + " current value \"" + s_old_config_sel__remote_val + "\"");
            m_btn_config_sel_remote_apply_changes.setEnabled(false);
        } else {
            Log.d(TAG, "validate__m_edt_val_config_sel__remote: m_edt_val_config_sel__remote proposed value \"" + s_config_sel__remote_val__proposted + "\" differs from shared pref setting " + SharedPrefsManager.STRING_SHARED_PREF.TM_CONFIG_TOML__REMOTE__SELECTION.toString() + " current value \"" + s_old_config_sel__remote_val + "\"");
            m_btn_config_sel_remote_apply_changes.setEnabled(true);
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(m_edt_val_config_sel__remote.getWindowToken(), 0);
        }
    }


    //result handler for both SD-card and GoogleDrive Tegola config TOML file selection (and any other requests to be handled via startActivityForResult)...
    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case REQUEST_CODES.REQUEST_CODE__SELECT_TOML_FILES_FOR_IMPORT__LOCAL_STORAGE: {
                switch (resultCode) {
                    case RESULT_OK: {
                        final Uri file_uri = data.getData();
                        if (file_uri != null) {
                            Log.d(TAG, "onActivityResult: requestCode: REQUEST_CODE__SELECT_TOML_FILES_FOR_IMPORT__LOCAL_STORAGE | resultCode: RESULT_OK -- selected local storage file uri \"" + file_uri + "\"; calling local__file__import() for this file uri...");
                            try {
                                final local__file__import__result result = local__file__import(file_uri);
                                final String s_result_msg = (result.succeeded ? "Successfully imported" : "Failed to import") + " local storage file \"" + result.src_name + "\"";
                                Log.d(TAG, "onActivityResult: requestCode: REQUEST_CODE__SELECT_TOML_FILES_FOR_IMPORT__LOCAL_STORAGE | resultCode: RESULT_OK -- " + s_result_msg);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getApplicationContext(), s_result_msg, Toast.LENGTH_LONG).show();
                                        if (result.succeeded)
                                            synchronize_spinner_val_config_sel_local();
                                    }
                                });
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            Log.d(TAG, "onActivityResult: requestCode: REQUEST_CODE__SELECT_TOML_FILES_FOR_IMPORT__LOCAL_STORAGE | resultCode: RESULT_OK -- but selected local storage file uri is null; aborting import");
                        }
                        break;
                    }
                    case RESULT_CANCELED: {
                        Log.i(TAG, "onActivityResult: requestCode: REQUEST_CODE__SELECT_TOML_FILES_FOR_IMPORT__LOCAL_STORAGE | resultCode: RESULT_CANCELED");
                        super.onActivityResult(requestCode, resultCode, data);
                        break;
                    }
                    default: {
                        Log.d(TAG, "onActivityResult: requestCode " + requestCode + ", resultCode " + resultCode);
                        super.onActivityResult(requestCode, resultCode, data);
                    }
                }
                break;
            }
//            case REQUEST_CODES.REQUEST_CODE__SELECT_TOML_FILES_FOR_IMPORT__GOOGLEDRIVE: {
//                switch (resultCode) {
//                    case RESULT_OK: {
//                        m_google_drive_id = (DriveId)data.getParcelableExtra(OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);
//                        Log.i(TAG, "onActivityResult: requestCode: REQUEST_CODE__SELECT_TOML_FILES_FOR_IMPORT__GOOGLEDRIVE | resultCode: RESULT_OK -- flow control handler: selected Google Drive file id " + m_google_drive_id.getResourceId() + "; calling GoogleDriveFileDownloadManager.download_file_contents() for this file...");
//                        GoogleDriveFileDownloadManager.getInstance().download_file_contents(this, m_google_drive_id, DriveFile.MODE_READ_ONLY, new GoogleDriveFileDownloadManager.FileContentsHandler() {
//                            @Override
//                            public void onProgress(long bytesDownloaded, long bytesExpected) {
//                            }
//
//                            @Override
//                            public void OnFileContentsDownloaded(final DriveContents google_drive_file_contents, final Metadata google_drive_file_metadata, final DriveFile google_drive_file) {
//                                final String
//                                    s_gd_id = google_drive_file_contents.getDriveId().encodeToString()
//                                    , s_gd_filename = google_drive_file_metadata.getOriginalFilename();
//                                Log.i(TAG, "inline GoogleDriveDownloadedFileContentsHandler.OnFileContentsDownloaded: triggered from PendingResult from call to google_drive__download_file_contents() -- successfully downloaded google drive file \"" + s_gd_filename + "\" contents from: id " + s_gd_id);
//                                try {
//                                    Log.i(TAG, "inline GoogleDriveDownloadedFileContentsHandler.OnFileContentsDownloaded: importing contents from google drive file \"" + s_gd_filename + " (id \"" + s_gd_id + ")\"");
//                                    final boolean succeeded = GoogleDriveFileDownloadManager.getInstance().import_file(MainActivity.this, google_drive_file_contents, google_drive_file_metadata, google_drive_file);
//                                    final String s_result_msg = (succeeded ? "Successfully imported" : "Failed to import") + " google drive file \"" + s_gd_filename + "\" (id " + s_gd_id + ")";
//                                    Log.i(TAG, "inline GoogleDriveDownloadedFileContentsHandler.OnFileContentsDownloaded: " + s_result_msg);
//                                    runOnUiThread(new Runnable() {
//                                        @Override
//                                        public void run() {
//                                            Toast.makeText(getApplicationContext(), s_result_msg, Toast.LENGTH_LONG).show();
//                                            if (succeeded)
//                                                synchronize_spinner_val_config_sel_local();
//                                        }
//                                    });
//                                } catch (IOException e) {
//                                    e.printStackTrace();
//                                }
//                            }
//                        });
//                        break;
//                    }
//                    case RESULT_CANCELED: {
//                        Log.i(TAG, "onActivityResult: requestCode: REQUEST_CODE__SELECT_TOML_FILES_FOR_IMPORT__GOOGLEDRIVE | resultCode: RESULT_CANCELED -- flow control handler: user canceled -- normal flow termination");
//                        super.onActivityResult(requestCode, resultCode, data);
//                        break;
//                    }
//                    default: {
//                        Log.d(TAG, "onActivityResult: default case: requestCode " + requestCode + ", resultCode " + resultCode);
//                        super.onActivityResult(requestCode, resultCode, data);
//                        break;
//                    }
//                }
//                break;
//            }
//            case REQUEST_CODES.REQUEST_CODE__GOOGLEAPICLIENT__RESOLVE_CONNECTION_FAILURE: {
//                switch (resultCode) {
//                    case RESULT_OK: {
//                        Log.i(TAG, "onActivityResult: requestCode: REQUEST_CODE__GOOGLEAPICLIENT__RESOLVE_CONNECTION_FAILURE | resultCode: RESULT_OK -- flow control handler: validating GoogleApiClient connection...");
//                        GoogleDriveFileDownloadManager.getInstance().validate_connect_api_client(this);
//                        break;
//                    }
//                    case RESULT_CANCELED: {
//                        Log.i(TAG, "onActivityResult: requestCode: REQUEST_CODE__GOOGLEAPICLIENT__RESOLVE_CONNECTION_FAILURE | resultCode: RESULT_CANCELED -- flow control handler: abnormal flow termination :(");
//                        super.onActivityResult(requestCode, resultCode, data);
//                        break;
//                    }
//                    default: {
//                        Log.d(TAG, "onActivityResult: default case: requestCode " + requestCode + ", resultCode " + resultCode);
//                        super.onActivityResult(requestCode, resultCode, data);
//                        break;
//                    }
//                }
//                break;
//            }
//            case REQUEST_CODES.REQUEST_CODE__EDIT_TOML_FILE: {
//                switch (resultCode) {
//                    case RESULT_OK: {
//                        break;
//                    }
//                    case RESULT_CANCELED: {
//                        break;
//                    }
//                }
//                break;
//            }
            case REQUEST_CODES.REQUEST_CODE__MANAGE_GPKG_BUNDLES: {
                switch (resultCode) {
                    case ManageGpkgBundlesActivity.MNG_GPKG_BUNDLES_RESULT__CHANGED: {
                        Log.i(TAG, "onActivityResult: requestCode: REQUEST_CODE__MANAGE_GPKG_BUNDLES | resultCode: MNG_GPKG_BUNDLES_RESULT__CHANGED");
                        synchronize_spinner_val_gpkg_bundle_sel();
                        synchronize_spinner_val_gpkg_bundle_props_sel();
                        break;
                    }
                    case ManageGpkgBundlesActivity.MNG_GPKG_BUNDLES_RESULT__UNCHANGED: {
                        Log.i(TAG, "onActivityResult: requestCode: REQUEST_CODE__MANAGE_GPKG_BUNDLES | resultCode: MNG_GPKG_BUNDLES_RESULT__UNCHANGED");
                        super.onActivityResult(requestCode, resultCode, data);
                        break;
                    }
                    default: {
                        Log.d(TAG, "onActivityResult: default case: requestCode " + requestCode + ", resultCode " + resultCode);
                        super.onActivityResult(requestCode, resultCode, data);
                        break;
                    }
                }
                break;
            }
            case REQUEST_CODES.REQUEST_CODE__INSTALL_GPKG_BUNDLE: {
                switch (resultCode) {
                    case InstallGpkgBundleActivity.INSTALL_GPKG_BUNDLE_RESULT__SUCCESSFUL: {
                        Log.i(TAG, "onActivityResult: requestCode: REQUEST_CODE__INSTALL_GPKG_BUNDLE | resultCode: INSTALL_GPKG_BUNDLE_RESULT__SUCCESSFUL");
                        synchronize_spinner_val_gpkg_bundle_sel();
                        synchronize_spinner_val_gpkg_bundle_props_sel();
                        break;
                    }
                    case InstallGpkgBundleActivity.INSTALL_GPKG_BUNDLE_RESULT__CANCELLED: {
                        Log.i(TAG, "onActivityResult: requestCode: REQUEST_CODE__INSTALL_GPKG_BUNDLE | resultCode: INSTALL_GPKG_BUNDLE_RESULT__CANCELLED");
                        super.onActivityResult(requestCode, resultCode, data);
                        break;
                    }
                    case InstallGpkgBundleActivity.INSTALL_GPKG_BUNDLE_RESULT__FAILED: {
                        Log.i(TAG, "onActivityResult: requestCode: REQUEST_CODE__INSTALL_GPKG_BUNDLE | resultCode: INSTALL_GPKG_BUNDLE_RESULT__FAILED");
                        super.onActivityResult(requestCode, resultCode, data);
                        break;
                    }
                    default: {
                        Log.d(TAG, "onActivityResult: default case: requestCode " + requestCode + ", resultCode " + resultCode);
                        super.onActivityResult(requestCode, resultCode, data);
                        break;
                    }
                }
                break;
            }
            default: {
                Log.d(TAG, "onActivityResult: default case: requestCode " + requestCode + ", resultCode " + resultCode);
                super.onActivityResult(requestCode, resultCode, data);
                break;
            }
        }
    }

    //supporting functions for above result handler
    private class local__file__import__result {
        public String src_name = "";
        public String src_path = "";
        public boolean succeeded = false;
    }
    private local__file__import__result local__file__import(final Uri local_file_uri) throws IOException {
        local__file__import__result result = new local__file__import__result();
        result.src_path = local_file_uri.getPath();
        InputStream inputstream_config_toml = null;
        boolean uselocalstorageprovider = getResources().getBoolean(R.bool.use_provider);
        if (uselocalstorageprovider) {
            Log.d(TAG, "local__file__import: using storage access framework since API level (" + Build.VERSION.SDK_INT + ") of device >= 19");
            Cursor cursor = this.getContentResolver().query(local_file_uri, null, null, null, null, null);
            try {
                if (cursor == null) {
                    Log.d(TAG, "local__file__import: getContentResolver().query() returned null cursor for uri " + local_file_uri.toString());
                } else {
                    // moveToFirst() returns false if the cursor has 0 rows.  Very handy for
                    // "if there's anything to look at, look at it" conditionals.
                    if (cursor.moveToFirst()) {
                        // Note it's called "Display Name".  This is
                        // provider-specific, and might not necessarily be the file name.
                        result.src_name = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                        Log.d(TAG, "local__file__import: cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME) returns display (file) name: " + result.src_name);
                    } else {
                        Log.d(TAG, "local__file__import: cursor.moveToFirst() failed for uri " + local_file_uri.toString());
                    }
                }
            } finally {
                if (cursor != null)
                    cursor.close();
            }
            if (result.src_name.isEmpty()) {
                Log.d(TAG, "local__file__import: result.src_name is empty; parsing result.src_name manually from uri path " + result.src_path);
                int i = result.src_path.lastIndexOf("/");
                if (i == -1)
                    i = 0;
                else
                    i += 1;
                result.src_name = result.src_path.substring(i);
            }
            Log.d(TAG, "local__file__import: using storage access framework content resolver to open inputstream from " + result.src_path + "...");
            inputstream_config_toml = getContentResolver().openInputStream(local_file_uri);
        } else {
            Log.d(TAG, "local__file__import: not using storage access framework since API level (" + Build.VERSION.SDK_INT + ") of device < 19");
            File f_local = new File(result.src_path);
            result.src_name = f_local.getName();
            Log.d(TAG, "local__file__import: opening inputstream from " + result.src_path + "...");
            inputstream_config_toml = new FileInputStream(f_local);
        }
        if (inputstream_config_toml == null )
            throw new IOException("Failed to open inputstream to " + result.src_path);
        final int file_size_in_bytes = inputstream_config_toml.available();
        byte[] buf_raw_config_toml = new byte[file_size_in_bytes];
        Log.d(TAG, "local__file__import: input file size is " + file_size_in_bytes + " bytes; reading...");
        inputstream_config_toml.read(buf_raw_config_toml);
        inputstream_config_toml.close();
        Log.d(TAG, "local__file__import: writing " + file_size_in_bytes + " bytes to new file (result.src_name \"" + result.src_name + "\") in app files directory...");
        FileOutputStream f_outputstream_new_tegola_config_toml = openFileOutput(result.src_name, Context.MODE_PRIVATE);
        f_outputstream_new_tegola_config_toml.write(buf_raw_config_toml);
        f_outputstream_new_tegola_config_toml.close();
        File f_new_tegola_config_toml = new File(getFilesDir().getPath() + "/" + result.src_name);
        result.succeeded = f_new_tegola_config_toml.exists();
        Log.d(TAG, "local__file__import: all done - " + (result.succeeded ? "successfully copied" : "failed to copy") + " " + result.src_name + " to app files directory");
        return result;
    }




    //ControllerLib-related stuff
    private void OnControllerStarting() {
        m_tv_val_ctrlr_status.setText(getString(R.string.starting));
    }

    private void OnControllerRunning() {
        m_controller_running = true;
        Log.d(TAG, "OnControllerRunning: set m_controller_running == " + m_controller_running);
        m_tv_val_ctrlr_status.setText(getString(R.string.running));
        m_tv_val_bin_ver.setText(Constants.Enums.TEGOLA_BIN.get_version_string());
    }

    private void OnControllerStopping() {
        m_tv_val_ctrlr_status.setText(getString(R.string.stopping));
    }

    private void OnControllerStopped() {
        m_tv_val_ctrlr_status.setText(getString(R.string.stopped));
    }

    private void OnMVTServerStarting() {
        m_tv_val_srvr_status.setText(getString(R.string.starting));
        m_sect_content__item__srvr_console_output.setVisibility(View.VISIBLE);
    }

    private void OnMVTServerStartFailed(final String reason) {
        OnMVTServerStopped();
    }

    private void textview_setColorizedText(TextView view, String fulltext, String subtext, int color) {
        view.setText(fulltext, TextView.BufferType.SPANNABLE);
        Spannable str = (Spannable)view.getText();
        int i = fulltext.indexOf(subtext);
        str.setSpan(new ForegroundColorSpan(color), i, i + subtext.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private void OnMVTServerRunning(final int pid) {
        final StringBuilder sb_srvr_status = new StringBuilder();
        sb_srvr_status.append(getString(R.string.running));
        if (pid != -1)
            sb_srvr_status.append(" (pid " + pid + ")");
        textview_setColorizedText(m_tv_val_srvr_status, sb_srvr_status.toString(), getString(R.string.running), Color.GREEN);
        m_btn_srvr_ctrl.setTag(R.id.TAG__SRVR_RUNNING, true);
        m_btn_srvr_ctrl.setText(getString(R.string.stop));
        //now disable edit-config button
        m_btn_config_sel_local__edit_file.setEnabled(false);
        m_tv_tegola_console_output.setText("");
    }

    //process stream-output (STDOUT/STDERR) and logcat-output helper functions associated w/ server-started state
    private void OnMVTServerOutputLogcat(final String logcat_line) {
        sv_append_mvt_server_console_output("LOGCAT> " + logcat_line);
    }
    private void OnMVTServerOutputStdErr(final String stderr_line) {
        sv_append_mvt_server_console_output("STDERR> " + stderr_line);
    }
    private void OnMVTServerOutputStdOut(final String stdout_line) {
        sv_append_mvt_server_console_output("STDOUT> " + stdout_line);
    }
    private void m_tv_tegola_console_output__scroll_max() {
        m_tv_tegola_console_output.postDelayed(new Runnable() {
            public void run() {
                if (m_tv_tegola_console_output != null && m_tv_tegola_console_output.getLayout() != null) {
                    final int scrollAmount = m_tv_tegola_console_output.getLayout().getLineTop(m_tv_tegola_console_output.getLineCount()) - m_tv_tegola_console_output.getHeight();
                    // if there is no need to scroll, scrollAmount will be <=0
                    if (scrollAmount > 0)
                        m_tv_tegola_console_output.scrollTo(0, scrollAmount);
                    else
                        m_tv_tegola_console_output.scrollTo(0, 0);
                }
            }
        }, 50);
    }
    private void sv_append_mvt_server_console_output(final String s) {
        m_tv_tegola_console_output.append(s + "\n");
        m_tv_tegola_console_output__scroll_max();
        m_scvw_main__scroll_max();
    }

    private void OnMVTServerListening(final int port) {
        Boolean srvr_started = (Boolean)m_btn_srvr_ctrl.getTag(R.id.TAG__SRVR_RUNNING);
        if (srvr_started != null && srvr_started == true) {
            String s_srvr_status = m_tv_val_srvr_status.getText().toString() + "\n\t\tlistening on port " + port;
            textview_setColorizedText(m_tv_val_srvr_status, s_srvr_status, getString(R.string.running), Color.GREEN);
            mbgl_map_start__tegola();
        }
    }

    private void mbgl_map_start__tegola() {
        Intent intent_mvt_server_read_json = new Intent(Constants.Strings.INTENT.ACTION.MVT_SERVER_HTTP_URL_API.READ_JSON);
        intent_mvt_server_read_json.putExtra(Constants.Strings.INTENT.ACTION.MVT_SERVER_HTTP_URL_API.EXTRA_KEY.MVT_SERVER__READ_JSON__PURPOSE, Constants.Strings.INTENT.ACTION.MVT_SERVER_HTTP_URL_API.EXTRA_KEY.READ_JSON__PURPOSE__VALUE.LOAD_MAP);
        sendBroadcast(intent_mvt_server_read_json);
    }

    private TegolaCapabilities parse_tegola_capabilities_json(final String s_tegola_tile_server_url__root, final String json) {
        final TegolaCapabilities tegolaCapabilities = new TegolaCapabilities();
        try {
            JSONTokener jsonTokener = new JSONTokener(json);
            tegolaCapabilities.root_json_object = new JSONObject(jsonTokener);
            Log.d(TAG, "parse_tegola_capabilities_json: json content is content is:\n" + tegolaCapabilities.root_json_object.toString());
            tegolaCapabilities.version = tegolaCapabilities.root_json_object.getString("version");
            if (tegolaCapabilities.version != null) {
                Log.d(TAG, "parse_tegola_capabilities_json: got \"version\" == \"" + tegolaCapabilities.version + "\"");
            } else
                tegolaCapabilities.version = "";
            JSONArray json_maps = tegolaCapabilities.root_json_object.getJSONArray("maps");
            if (json_maps != null) {
                ArrayList<TegolaCapabilities.Parsed.Map> al_maps = null;
                Log.d(TAG, "parse_tegola_capabilities_json: got \"maps\" JSONArray - contains " + json_maps.length() + " \"map\" JSON objects");
                if (json_maps.length() > 0) {
                    al_maps = new ArrayList<TegolaCapabilities.Parsed.Map>();
                    for (int i = 0; i < json_maps.length(); i++) {
                        JSONObject json_map = json_maps.getJSONObject(i);
                        if (json_map != null) {
                            Log.d(TAG, "parse_tegola_capabilities_json: got JSONObject for \"maps\"[" + i + "]");
                            TegolaCapabilities.Parsed.Map map = new TegolaCapabilities.Parsed.Map();
                            map.name = json_map.getString("name");
                            Log.d(TAG, "parse_tegola_capabilities_json: \"maps\"[" + i + "].\"name\" == \"" + map.name + "\"");
                            map.attribution = json_map.getString("attribution");
                            Log.d(TAG, "parse_tegola_capabilities_json: \"maps\"[" + i + "].\"attribution\" == \"" + map.attribution + "\"");
                            map.mbgl_style_json_url = s_tegola_tile_server_url__root + "/maps/" + map.name + "/style.json";
                            Log.d(TAG, "parse_tegola_capabilities_json: mbgl_style url for map \"" + map.name + "\" is " + map.mbgl_style_json_url);
                            JSONArray jsonarray_map_center = json_map.getJSONArray("center");
                            if (jsonarray_map_center != null) {
                                Log.d(TAG, "parse_tegola_capabilities_json: got \"center\" JSONArray - contains " + jsonarray_map_center.length() + " values");
                                if (jsonarray_map_center.length() == 3) {
                                    map.center.latitude = jsonarray_map_center.getDouble(1);
                                    Log.d(TAG, "parse_tegola_capabilities_json: got \"center\" latitude (pos 1) value: " + map.center.latitude);
                                    map.center.longitude = jsonarray_map_center.getDouble(0);
                                    Log.d(TAG, "parse_tegola_capabilities_json: got \"center\" longitude (pos 0) value: " + map.center.longitude);
                                    map.center.zoom = jsonarray_map_center.getDouble(2);
                                    Log.d(TAG, "parse_tegola_capabilities_json: got \"center\" zoom (pos 2) value: " + map.center.zoom);
                                } else {
                                    Log.e(TAG, "parse_tegola_capabilities_json: \"center\" JSONArray contains " + jsonarray_map_center.length() + " values but 3 are required!");
                                }
                            } else {
                                Log.w(TAG, "parse_tegola_capabilities_json: tegola capabilities json map \"" + map.name + "\" does not contain \"center\" json object");
                            }
                            JSONArray jsonarray_map_layers = json_map.getJSONArray("layers");
                            if (jsonarray_map_layers != null) {
                                Log.d(TAG, "parse_tegola_capabilities_json: got \"layers\" JSONArray - contains " + jsonarray_map_layers.length() + " objects");
                                if (jsonarray_map_layers.length() > 0) {
                                    ArrayList<TegolaCapabilities.Parsed.Map.Layer> al_layers = new ArrayList<TegolaCapabilities.Parsed.Map.Layer>();
                                    Log.d(TAG, "parse_tegola_capabilities_json: collating layers objects for inf(minzoom) and sup(maxzoom)...");
                                    for (int j = 0; j < jsonarray_map_layers.length(); j++) {
                                        JSONObject json_layer = jsonarray_map_layers.getJSONObject(j);
                                        if (json_layer != null) {
                                            Log.d(TAG, "parse_tegola_capabilities_json: got JSONObject for \"layers\"[" + j + "]");
                                            TegolaCapabilities.Parsed.Map.Layer layer = new TegolaCapabilities.Parsed.Map.Layer();
                                            layer.name = json_layer.getString("name");
                                            Log.d(TAG, "parse_tegola_capabilities_json: \"layers\"[" + j + "].\"name\" == \"" + layer.name + "\"");
                                            layer.minzoom = json_layer.getDouble("minzoom");
                                            Log.d(TAG, "parse_tegola_capabilities_json: \"layers\"[" + j + "].\"minzoom\" == \"" + layer.minzoom + "\"");
                                            if (tegolaCapabilities.parsed.maps_layers_inf_minzoom == -1.0 || tegolaCapabilities.parsed.maps_layers_inf_minzoom < layer.minzoom) {
                                                Log.d(TAG, "parse_tegola_capabilities_json: found new inf(minzoom) == " + layer.minzoom);
                                                tegolaCapabilities.parsed.maps_layers_inf_minzoom = layer.minzoom;
                                            }
                                            layer.maxzoom = json_layer.getDouble("maxzoom");
                                            Log.d(TAG, "parse_tegola_capabilities_json: \"layers\"[" + j + "].\"maxzoom\" == \"" + layer.maxzoom + "\"");
                                            if (tegolaCapabilities.parsed.maps_layers_sup_maxzoom == -1.0 || tegolaCapabilities.parsed.maps_layers_sup_maxzoom > layer.maxzoom) {
                                                Log.d(TAG, "parse_tegola_capabilities_json: found new sup(maxzoom) == " + layer.maxzoom);
                                                tegolaCapabilities.parsed.maps_layers_sup_maxzoom = layer.maxzoom;
                                            }
                                            al_layers.add(layer);
                                        }
                                    }
                                    map.layers = al_maps.toArray(new TegolaCapabilities.Parsed.Map.Layer[al_maps.size()]);
                                }
                            }
                            al_maps.add(map);
                        } else {
                            Log.e(TAG, "parse_tegola_capabilities_json: tegola capabilities json does not have a map json object at index " + i + " of \"maps\" json array");
                        }
                    }
                } else {
                    Log.e(TAG, "parse_tegola_capabilities_json: tegola capabilities json \"maps\" json array does not contain any elements!");
                }
                tegolaCapabilities.parsed.maps = al_maps.toArray(new TegolaCapabilities.Parsed.Map[al_maps.size()]);
                Log.d(TAG, "parse_tegola_capabilities_json: post-parse: tegolaCapabilities.parsed.maps contains " + tegolaCapabilities.parsed.maps.length + " elements");
                Log.d(TAG, "parse_tegola_capabilities_json: post-parse: tegolaCapabilities.parsed.maps_layers_inf_minzoom == " + tegolaCapabilities.parsed.maps_layers_inf_minzoom);
                Log.d(TAG, "parse_tegola_capabilities_json: post-parse: tegolaCapabilities.parsed.maps_layers_sup_maxzoom == " + tegolaCapabilities.parsed.maps_layers_sup_maxzoom);
            } else {
                Log.e(TAG, "parse_tegola_capabilities_json: tegola capabilities json does not contain \"maps\" json array!");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return tegolaCapabilities;
    }

    private void mbgl_map_start(@NonNull final TegolaCapabilities tegolaCapabilities) throws MapboxConfigurationException {
        if (tegolaCapabilities != null) {
            mbgl_map_stop();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "mbgl_map_start: swapping drawer content to TegolaMBGLFragment");
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(
                                    R.id.drawerlayout_content__drawer__frag_container,
                                    TegolaMBGLFragment.newInstance(tegolaCapabilities, BuildConfig.mbgl_debug_active),
                                    FRAG_DRAWER_CONTENT
                            )
                            .commit();
                    Log.d(TAG, "mbgl_map_start: adding drawerlistener m_drawerlayout_main__DrawerToggle to m_drawerlayout");
                    m_drawerlayout.addDrawerListener(m_drawerlayout_main__DrawerToggle);
                    Log.d(TAG, "mbgl_map_start: attaching drawerhandle R.layout.drawer_handle to m_drawerlayout_content__drawer");
                    m_drawer_handle = DrawerHandle.attach(m_drawerlayout_content__drawer, R.layout.drawer_handle, 0.45f);
                    Log.d(TAG, "mbgl_map_start: unlocking drawer");
                    m_drawerlayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                    m_drawer_handle.openDrawer();
                }
            });
        } else {
            throw new MapboxConfigurationException();
        }
    }

    private void OnMVTServerJSONRead(final String s_tegola_url_root, final String json_url_endpoint, final String json, final String purpose) {
        Log.d(TAG, "OnMVTServerJSONRead: s_tegola_url_root: " + s_tegola_url_root + "; json_url_endpoint: " + json_url_endpoint + "; purpose: " + purpose);
        switch (json_url_endpoint) {
            case "/capabilities": {
                final TegolaCapabilities tegolaCapabilities = parse_tegola_capabilities_json(s_tegola_url_root, json);
                switch (purpose) {
                    case Constants.Strings.INTENT.ACTION.CTRLR_NOTIFICATION.EXTRA__KEY.JSON_READ__PURPOSE__VALUE.LOAD_MAP: {
                        if (tegolaCapabilities.parsed.maps.length > 0)
                            mbgl_map_start(tegolaCapabilities);
                        break;
                    }
                }
                break;
            }
            default: {

            }
        }
    }

    private void OnMVTServerStopping() {
        textview_setColorizedText(m_tv_val_srvr_status, getString(R.string.stopping), getString(R.string.stopping), Color.YELLOW);
    }

    private void mbgl_map_stop() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "mbgl_map_stop: locking drawer closed");
                if (m_drawer_handle != null) {
                    m_drawer_handle.closerDrawer();
                    Log.d(TAG, "mbgl_map_stop: detaching drawer handle");
                    m_drawer_handle.detach();
                    m_drawer_handle = null;
                } else {
                    Log.d(TAG, "mbgl_map_stop: no (null) drawerhandle to detach!");
                }
                m_drawerlayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                Log.d(TAG, "mbgl_map_stop: removing drawer listener: m_drawerlayout_main__DrawerToggle");
                m_drawerlayout.removeDrawerListener(m_drawerlayout_main__DrawerToggle);
                Fragment frag_current = getSupportFragmentManager().findFragmentByTag(FRAG_DRAWER_CONTENT);
                if (frag_current != null) {
                    Log.d(TAG, "mbgl_map_stop: removing MapFragment from drawer");
                    getSupportFragmentManager()
                        .beginTransaction()
                        .remove(frag_current)
                        .commit();
                }
            }
        });
    }

    private void OnMVTServerStopped() {
        mbgl_map_stop();

        Log.d(TAG, "OnMVTServerStopped: updating status-related UX");
        textview_setColorizedText(m_tv_val_srvr_status, getString(R.string.stopped), getString(R.string.stopped), Color.RED);
        m_btn_srvr_ctrl.setTag(R.id.TAG__SRVR_RUNNING, false);
        m_btn_srvr_ctrl.setText(getString(R.string.start));
        m_btn_config_sel_local__edit_file.setEnabled(true);
        m_tv_tegola_console_output.setText("");
        m_sect_content__item__srvr_console_output.setVisibility(View.GONE);
    }

    private void start_controller_fgs() {
        m_tv_val_ctrlr_status.setText(getString(R.string.starting));
        Intent intent_start_controller_fgs = new Intent(MainActivity.this, FGS.class);
        intent_start_controller_fgs.setAction(Constants.Strings.INTENT.ACTION.FGS_COMMAND_REQUEST.START);
        intent_start_controller_fgs.putExtra(Constants.Strings.INTENT.ACTION.FGS_COMMAND_REQUEST.EXTRA__KEY.FGS__START_FOREGROUND__HARNESS, MainActivity.class.getName());
        startService(intent_start_controller_fgs);
    }

    private void stop_controller_fgs() {
        m_tv_val_ctrlr_status.setText(getString(R.string.stopping));
        Intent intent_stop_controller_fgs = new Intent(MainActivity.this, FGS.class);
        intent_stop_controller_fgs.setAction(Constants.Strings.INTENT.ACTION.FGS_COMMAND_REQUEST.STOP);
        stopService(intent_stop_controller_fgs);
    }

    private void start_mvt_server() {
        Intent intent_start_mvt_server = new Intent(Constants.Strings.INTENT.ACTION.MVT_SERVER_CONTROL_REQUEST.START);
        String s_config_toml = null;
        boolean gpkg_provider = SharedPrefsManager.BOOLEAN_SHARED_PREF.TM_PROVIDER__IS_GEOPACKAGE.getValue();
        intent_start_mvt_server.putExtra(Constants.Strings.INTENT.ACTION.MVT_SERVER_CONTROL_REQUEST.EXTRA__KEY.MVT_SERVER__START__PROVIDER__IS_GPKG, gpkg_provider);
        if (gpkg_provider) {
            intent_start_mvt_server.putExtra(
                    Constants.Strings.INTENT.ACTION.MVT_SERVER_CONTROL_REQUEST.EXTRA__KEY.MVT_SERVER__START__GPKG_PROVIDER__BUNDLE,
                    SharedPrefsManager.STRING_SHARED_PREF.TM_PROVIDER__GPKG_BUNDLE__SELECTION.getValue()
            );
            intent_start_mvt_server.putExtra(
                    Constants.Strings.INTENT.ACTION.MVT_SERVER_CONTROL_REQUEST.EXTRA__KEY.MVT_SERVER__START__GPKG_PROVIDER__BUNDLE__PROPS,
                    SharedPrefsManager.STRING_SHARED_PREF.TM_PROVIDER__GPKG_BUNDLE_PROPS__SELECTION.getValue()
            );
        } else {
            boolean remote_config = SharedPrefsManager.BOOLEAN_SHARED_PREF.TM_CONFIG_TOML__IS_REMOTE.getValue();
            intent_start_mvt_server.putExtra(Constants.Strings.INTENT.ACTION.MVT_SERVER_CONTROL_REQUEST.EXTRA__KEY.MVT_SERVER__START__CONFIG__IS_REMOTE, remote_config);
            if (!remote_config) {
                File
                        f_filesDir = getFilesDir()
                        , f_postgis_toml = new File(f_filesDir.getPath(), SharedPrefsManager.STRING_SHARED_PREF.TM_CONFIG_TOML__LOCAL__SELECTION.getValue());
                s_config_toml = f_postgis_toml.getPath();
                if (!f_postgis_toml.exists()) {
                    Log.e(TAG, "start_mvt_server: failed to start mvt server for provider type postgis since toml file " + s_config_toml + " does not exist!");
                    return;
                }
            } else
                s_config_toml = SharedPrefsManager.STRING_SHARED_PREF.TM_CONFIG_TOML__REMOTE__SELECTION.getValue();
        }
        intent_start_mvt_server.putExtra(Constants.Strings.INTENT.ACTION.MVT_SERVER_CONTROL_REQUEST.EXTRA__KEY.MVT_SERVER__START__CONFIG__PATH, s_config_toml);
        sendBroadcast(intent_start_mvt_server);
    }

    private void stop_mvt_server() {
        mbgl_map_stop();
        Intent intent_stop_mvt_server = new Intent(Constants.Strings.INTENT.ACTION.MVT_SERVER_CONTROL_REQUEST.STOP);
        sendBroadcast(intent_stop_mvt_server);
    }
}