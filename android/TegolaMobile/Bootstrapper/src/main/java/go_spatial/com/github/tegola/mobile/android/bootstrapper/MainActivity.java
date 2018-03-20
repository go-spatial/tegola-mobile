package go_spatial.com.github.tegola.mobile.android.bootstrapper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.ContextThemeWrapper;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
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
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.aakira.expandablelayout.ExpandableRelativeLayout;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResource;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.OpenFileActivityBuilder;
import com.google.android.gms.drive.query.Filter;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.SearchableField;
import com.ipaulpro.afilechooser.utils.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import go_spatial.com.github.tegola.mobile.android.bootstrapper.Constants.REQUEST_CODES;
import go_spatial.com.github.tegola.mobile.android.bootstrapper.Constants.Strings;
import go_spatial.com.github.tegola.mobile.android.controller.Constants;
import go_spatial.com.github.tegola.mobile.android.controller.ControllerFGS;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = MainActivity.class.getName();

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

    //srvr info - config sel local - UI objects
    private RadioButton m_rb_val_config_type_sel__local = null;
    private TextView m_tv_lbl_config_type_sel__local__manage_files = null;
    private View m_vw_config_sel_container__local = null;
    private CustomSpinner m_spinner_val_config_sel_local = null;
    private final ArrayList<String> m_spinner_val_config_sel_local__items = new ArrayList<String>();
    private ArrayAdapter<String> m_spinner_val_config_sel_local__dataadapter = null;
    private ImageButton m_btn_config_sel_local__edit_file = null;
    private ImageButton m_btn_config_sel_local_import__googledrive = null;
    private ImageButton m_btn_config_sel_local_import__sdcard = null;

    //srvr info - config sel remote - UI objects
    private RadioButton m_rb_val_config_type_sel__remote = null;
    private View m_vw_config_sel_container__remote = null;
    private EditText m_edt_val_config_sel__remote;
    private Button m_btn_config_sel_remote_apply_changes = null;

    //srvr info - status - UI objects
    private TextView m_tv_val_srvr_status = null;
    private Button m_btn_srvr_ctrl = null;

    //srvr info - console output - UI objects
    private ScrollView m_scrvw_tegola_console_output = null;
    private TextView m_tv_tegola_console_output = null;


    private BroadcastReceiver m_br_ctrlr_notifications = null;
    private IntentFilter m_br_ctrlr_notifications_filter = null;

    private GoogleApiClient m_google_api_client = null;
    private DriveId m_google_drive_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //map UI objects to UI resources
        m_scvw_main = (ScrollView)findViewById(R.id.sv_main);
        m_btn_sect__andro_dev_nfo__expand = (Button)findViewById(R.id.btn_sect__andro_dev_nfo__expand);
        m_vw_sect_content__andro_dev_nfo = (ExpandableRelativeLayout)findViewById(R.id.sect_content__andro_dev_nfo);
        m_tv_val_CPU_ABI = (TextView)findViewById(R.id.tv_val_CPU_ABI);
        m_tv_val_API_level = (TextView)findViewById(R.id.tv_val_API_level);
        m_btn_sect__ctrlr_nfo__expand = (Button)findViewById(R.id.btn_sect__ctrlr_nfo__expand);
        m_vw_sect_content__ctrlr_nfo = (ExpandableRelativeLayout)findViewById(R.id.sect_content__ctrlr_nfo);
        m_tv_val_ctrlr_status = (TextView)findViewById(R.id.tv_val_tegola_ctrlr_status);
        m_tv_val_bin_ver = (TextView)findViewById(R.id.tv_val_bin_ver);
        m_rb_val_config_type_sel__local = (RadioButton)findViewById(R.id.rb_val_config_type_sel__local);
        m_tv_lbl_config_type_sel__local__manage_files = (TextView)findViewById(R.id.tv_lbl_config_type_sel__local__manage_files);
        m_rb_val_config_type_sel__remote = (RadioButton)findViewById(R.id.rb_val_config_type_sel__remote);
        m_vw_config_sel_container__local = findViewById(R.id.config_sel_container__local);
        m_spinner_val_config_sel_local = (CustomSpinner)findViewById(R.id.spinner_val_config_sel__local);
        m_btn_config_sel_local__edit_file = (ImageButton)findViewById(R.id.btn_config_sel_local__edit_file);
        m_btn_config_sel_local_import__sdcard = (ImageButton)findViewById(R.id.btn_config_sel_local_import__sdcard);
        m_btn_config_sel_local_import__googledrive = (ImageButton)findViewById(R.id.btn_config_sel_local_import__googledrive);
        m_vw_config_sel_container__remote = findViewById(R.id.config_sel_container__remote);
        m_edt_val_config_sel__remote = (EditText)findViewById(R.id.edt_val_config_sel__remote);
        m_btn_config_sel_remote_apply_changes = (Button)findViewById(R.id.btn_config_sel_remote_apply_changes);
        m_tv_val_srvr_status = (TextView)findViewById(R.id.tv_val_srvr_status);
        m_btn_srvr_ctrl = (Button)findViewById(R.id.btn_srvr_ctrl);
        m_scrvw_tegola_console_output = (ScrollView)findViewById(R.id.scrvw_tegola_console_output);
        m_tv_tegola_console_output = (TextView)findViewById(R.id.tv_tegola_console_output);

        //set up associated UI objects auxiliary objects if any - e.g. TAGs and data adapters
        m_spinner_val_config_sel_local__dataadapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, m_spinner_val_config_sel_local__items);
        m_spinner_val_config_sel_local__dataadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        m_spinner_val_config_sel_local.setAdapter(m_spinner_val_config_sel_local__dataadapter);
        m_btn_srvr_ctrl.setTag(R.id.TAG__SRVR_STARTED, false);

        //associate listeners for user-UI-interaction
        m_btn_sect__andro_dev_nfo__expand.setOnClickListener(OnClickListener__btn_expandable_section);
        m_btn_sect__ctrlr_nfo__expand.setOnClickListener(OnClickListener__btn_expandable_section);
        m_rb_val_config_type_sel__local.setOnCheckedChangeListener(OnCheckedChangeListener__m_rb_val_config_type_sel__local);
        m_tv_lbl_config_type_sel__local__manage_files.setMovementMethod(LinkMovementMethod.getInstance());
        Spannable span__clickable_text__m_tv_lbl_config_type_sel__local__manage_files = (Spannable)m_tv_lbl_config_type_sel__local__manage_files.getText();
        span__clickable_text__m_tv_lbl_config_type_sel__local__manage_files.setSpan(ClickableSpan____m_tv_lbl_config_type_sel__local__manage_files, 0, span__clickable_text__m_tv_lbl_config_type_sel__local__manage_files.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        m_spinner_val_config_sel_local.setOnItemSelectedListener(OnItemSelectedListener__m_spinner_val_config_sel_local);
        m_btn_config_sel_local__edit_file.setOnClickListener(OnClickListener__m_btn_config_sel_local__edit_file);
        m_btn_config_sel_local_import__sdcard.setOnClickListener(OnClickListener__m_btn_config_sel_local_import__sdcard);
        m_btn_config_sel_local_import__googledrive.setOnClickListener(OnClickListener__m_btn_config_sel_local_import__googledrive);
        m_rb_val_config_type_sel__remote.setOnCheckedChangeListener(OnCheckedChangeListener__m_rb_val_config_type_sel__remote);
        m_edt_val_config_sel__remote.setOnEditorActionListener(OnEditorActionListener__m_edt_val_config_sel__remote);
        m_edt_val_config_sel__remote.setOnFocusChangeListener(OnFocusChangeListener__m_edt_val_config_sel__remote);
        m_btn_config_sel_remote_apply_changes.setOnClickListener(OnClickListener__m_btn_config_sel_remote_apply_changes);
        m_btn_srvr_ctrl.setOnClickListener(OnClickListener__m_btn_srvr_ctrl);

        //instantiate PersistentConfigSettingsManager singleton
        SharedPrefsManager.newInstance(this);

        //set up BR to listen to notifications from ControllerLib
        m_br_ctrlr_notifications_filter = new IntentFilter();
        m_br_ctrlr_notifications_filter.addAction(Constants.Strings.INTENT.ACTION.CTRLR_NOTIFICATION.CONTROLLER__FOREGROUND_STARTING);
        m_br_ctrlr_notifications_filter.addAction(Constants.Strings.INTENT.ACTION.CTRLR_NOTIFICATION.CONTROLLER__FOREGROUND_STARTED);
        m_br_ctrlr_notifications_filter.addAction(Constants.Strings.INTENT.ACTION.CTRLR_NOTIFICATION.CONTROLLER__FOREGROUND_STOPPING);
        m_br_ctrlr_notifications_filter.addAction(Constants.Strings.INTENT.ACTION.CTRLR_NOTIFICATION.CONTROLLER__FOREGROUND_STOPPED);
        m_br_ctrlr_notifications_filter.addAction(Constants.Strings.INTENT.ACTION.CTRLR_NOTIFICATION.MVT_SERVER__STARTING);
        m_br_ctrlr_notifications_filter.addAction(Constants.Strings.INTENT.ACTION.CTRLR_NOTIFICATION.MVT_SERVER__START_FAILED);
        m_br_ctrlr_notifications_filter.addAction(Constants.Strings.INTENT.ACTION.CTRLR_NOTIFICATION.MVT_SERVER__STARTED);
        m_br_ctrlr_notifications_filter.addAction(Constants.Strings.INTENT.ACTION.CTRLR_NOTIFICATION.MVT_SERVER__OUTPUT__LOGCAT);
        m_br_ctrlr_notifications_filter.addAction(Constants.Strings.INTENT.ACTION.CTRLR_NOTIFICATION.MVT_SERVER__OUTPUT__STDERR);
        m_br_ctrlr_notifications_filter.addAction(Constants.Strings.INTENT.ACTION.CTRLR_NOTIFICATION.MVT_SERVER__OUTPUT__STDOUT);
        m_br_ctrlr_notifications_filter.addAction(Constants.Strings.INTENT.ACTION.CTRLR_NOTIFICATION.MVT_SERVER__STOPPING);
        m_br_ctrlr_notifications_filter.addAction(Constants.Strings.INTENT.ACTION.CTRLR_NOTIFICATION.MVT_SERVER__STOPPED);
        m_br_ctrlr_notifications = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Constants.Enums.E_INTENT_ACTION__CTRLR_NOTIFICATION e_ctrlr_notification = Constants.Enums.E_INTENT_ACTION__CTRLR_NOTIFICATION.fromString(intent != null ? intent.getAction() : null);
                switch (e_ctrlr_notification) {
                    case CONTROLLER_FOREGROUND_STARTING: {
                        OnControllerStarting();
                        break;
                    }
                    case CONTROLLER_FOREGROUND_STARTED: {
                        OnControllerStarted();
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
                    case MVT_SERVER__STARTED: {
                        OnMVTServerStarted(
                            intent.getStringExtra(Constants.Strings.INTENT.ACTION.CTRLR_NOTIFICATION.EXTRA__KEY.MVT_SERVER__STARTED__VERSION)
                            , intent.getIntExtra(Constants.Strings.INTENT.ACTION.CTRLR_NOTIFICATION.EXTRA__KEY.MVT_SERVER__STARTED__PID, -1)
                        );
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
                    case MVT_SERVER__STOPPING: {
                        OnMVTServerStopping();
                        break;
                    }
                    case MVT_SERVER__STOPPED: {
                        OnMVTServerStopped();
                        break;
                    }
                }
            }
        };
        registerReceiver(m_br_ctrlr_notifications, m_br_ctrlr_notifications_filter, null, new Handler(getMainLooper()));
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        //set title to build version
        setTitle(getString(R.string.app_name) + " - build " + BuildConfig.VERSION_NAME);

        //set andro_dev info fixed val content
        m_tv_val_CPU_ABI.setText(Constants.Enums.CPU_ABI.fromDevice().toString());
        m_tv_val_API_level.setText(Build.VERSION.SDK);

        OnMVTServerStopped();
        OnControllerStopped();

        //set expandable sections UI initial "expanded" state
        m_vw_sect_content__andro_dev_nfo.collapse();
        m_vw_sect_content__andro_dev_nfo.setExpanded(false);
        m_vw_sect_content__ctrlr_nfo.expand();
        m_vw_sect_content__ctrlr_nfo.setExpanded(true);

        //now queue up initial automated UI actions
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //reconcile expandable sections UI with initial "expanded" state
                m_vw_sect_content__andro_dev_nfo.callOnClick();
                m_vw_sect_content__ctrlr_nfo.callOnClick();

                //set srvr config selection type (local/remote) based on PersistentConfigSettingsManager.TM_CONFIG_TYPE_SEL__REMOTE val
                if (SharedPrefsManager.BOOLEAN_SHARED_PREF.TM_CONFIG_TYPE_SEL__REMOTE.getValue() == true) {
                    m_rb_val_config_type_sel__remote.setChecked(true);
                } else {
                    m_rb_val_config_type_sel__local.setChecked(true);
                }

                //adjust main scroll view (since expandable sections may or may not have been expanded/collapsed based on initial settings)
                m_scvw_main__scroll_max();

                //finally, start the controller foreground service
                start_controller_fgs();
            }
        }, 50);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (m_google_api_client != null) {
            // disconnect Google Android Drive API connection.
            m_google_api_client.disconnect();
        }
        super.onPause();
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

    //reaction to local config-type selection - display all local config selection UI and update shared prefs to reflect selection
    private final CompoundButton.OnCheckedChangeListener OnCheckedChangeListener__m_rb_val_config_type_sel__local = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
            if (checked) {
                boolean sdcardmounted = android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
                m_btn_config_sel_local_import__sdcard.setBackgroundColor(sdcardmounted ? ContextCompat.getColor(getApplicationContext(), android.R.color.holo_green_light) : ContextCompat.getColor(getApplicationContext(), android.R.color.holo_red_dark));
                m_btn_config_sel_local_import__sdcard.setEnabled(sdcardmounted);
                m_vw_config_sel_container__remote.setVisibility(View.GONE);
                m_vw_config_sel_container__local.setVisibility(View.VISIBLE);
                SharedPrefsManager.BOOLEAN_SHARED_PREF.TM_CONFIG_TYPE_SEL__REMOTE.setValue(false);
                synchronize_spinner_val_config_sel_local();
            }
        }
    };

    private final ClickableSpan ClickableSpan____m_tv_lbl_config_type_sel__local__manage_files = new ClickableSpan() {
        @Override
        public void onClick(View widget) {
            startActivity(new Intent(MainActivity.this, ManageFilesActivity.class));
        }
    };

    //user selects a local config file selection from spinner - synchronizes selection with shared prefs
    private final AdapterView.OnItemSelectedListener OnItemSelectedListener__m_spinner_val_config_sel_local = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapter, View view, int position, long id) {
            String s_sel_val = adapter.getItemAtPosition(position).toString();
            Log.d(TAG, "OnItemSelectedListener__m_spinner_val_config_sel_local.onItemSelected: triggered item selection @ position " + position + " with value " + (s_sel_val == null ? "null" : "\"" + s_sel_val + "\""));

            String s_cached_config_sel__local_val = SharedPrefsManager.STRING_SHARED_PREF.TM_CONFIG_TYPE_SEL__LOCAL__VAL.getValue();
            Log.d(TAG, "OnItemSelectedListener__m_spinner_val_config_sel_local.onItemSelected: shared pref setting " + SharedPrefsManager.STRING_SHARED_PREF.TM_CONFIG_TYPE_SEL__LOCAL__VAL.toString() + " current value is \"" + s_cached_config_sel__local_val + "\"");

            boolean no_config_files = (s_sel_val == null || s_sel_val.compareTo(getString(R.string.srvr_config_type__local__no_config_files_found)) == 0);
            if (no_config_files) {
                Log.d(TAG, "OnItemSelectedListener__m_spinner_val_config_sel_local.onItemSelected: no-config-files condition!");
                if (!s_cached_config_sel__local_val.isEmpty()) {
                    Log.d(TAG, "OnItemSelectedListener__m_spinner_val_config_sel_local.onItemSelected: clearing shared pref setting " + SharedPrefsManager.STRING_SHARED_PREF.TM_CONFIG_TYPE_SEL__LOCAL__VAL.toString() + " value (currently \"" + s_cached_config_sel__local_val + "\")");
                    Toast.makeText(getApplicationContext(), "Clearing setting value for local config toml file selection since there are none available", Toast.LENGTH_LONG).show();
                    SharedPrefsManager.STRING_SHARED_PREF.TM_CONFIG_TYPE_SEL__LOCAL__VAL.setValue("");
                } else {
                    Log.d(TAG, "OnItemSelectedListener__m_spinner_val_config_sel_local.onItemSelected: skipping change to shared pref setting " + SharedPrefsManager.STRING_SHARED_PREF.TM_CONFIG_TYPE_SEL__LOCAL__VAL.toString() + " since it is already cleared (value is \"" + s_cached_config_sel__local_val + "\")");
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
                                dialog.cancel();
                            }
                        });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            } else {
                //first, update shared pref val as necessary - does sel value differ from cached?
                if (s_cached_config_sel__local_val.compareTo(s_sel_val) != 0) {
                    Toast.makeText(getApplicationContext(), "Saving new setting value for local config toml file \"" + s_sel_val + "\" selection", Toast.LENGTH_LONG).show();
                    //now update shared pref
                    SharedPrefsManager.STRING_SHARED_PREF.TM_CONFIG_TYPE_SEL__LOCAL__VAL.setValue(s_sel_val);
                    Log.d(TAG, "OnItemSelectedListener__m_spinner_val_config_sel_local.onItemSelected: changed setting " + SharedPrefsManager.STRING_SHARED_PREF.TM_CONFIG_TYPE_SEL__LOCAL__VAL.toString() + " value from \"" + s_cached_config_sel__local_val + "\" to \"" + SharedPrefsManager.STRING_SHARED_PREF.TM_CONFIG_TYPE_SEL__LOCAL__VAL.getValue() + "\"");
                } else {
                    //no change to shared pref val
                    Log.d(TAG, "OnItemSelectedListener__m_spinner_val_config_sel_local.onItemSelected: skipping change to shared pref setting " + SharedPrefsManager.STRING_SHARED_PREF.TM_CONFIG_TYPE_SEL__LOCAL__VAL.toString() + " value (\"" + SharedPrefsManager.STRING_SHARED_PREF.TM_CONFIG_TYPE_SEL__LOCAL__VAL.getValue() + "\") since new value (\"" + s_sel_val + "\") is no different");
                }

                //now update m_btn_config_sel_local__edit_file UI based on existence of current local config toml file selection setting (SharedPrefsManager.STRING_SHARED_PREF.TM_CONFIG_TYPE_SEL__LOCAL__VAL.getValue())
                File file = new File(getFilesDir().getPath() + "/" + SharedPrefsManager.STRING_SHARED_PREF.TM_CONFIG_TYPE_SEL__LOCAL__VAL.getValue());
                m_btn_config_sel_local__edit_file.setVisibility(file.exists() ? View.VISIBLE : View.GONE);
                m_btn_config_sel_local__edit_file.setEnabled(file.exists());
                //and same MVT srvr control (start/stop) button
                m_btn_srvr_ctrl.setEnabled(file.exists());
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapter) {
            Toast.makeText(getApplicationContext(), "Cleared local config toml file selection", Toast.LENGTH_LONG).show();
            //disable/hid m_btn_config_sel_local__edit_file
            m_btn_config_sel_local__edit_file.setVisibility(View.GONE);
            m_btn_config_sel_local__edit_file.setEnabled(false);
        }
    };

    //user clicks button to edit/open/view selected local config file selection
    private final View.OnClickListener OnClickListener__m_btn_config_sel_local__edit_file = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            edit_local_config_file(SharedPrefsManager.STRING_SHARED_PREF.TM_CONFIG_TYPE_SEL__LOCAL__VAL.getValue());
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
    private final View.OnClickListener OnClickListener__m_btn_config_sel_local_import__googledrive = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            import_config_toml__from_google_drive();
        }
    };

    private void import_config_toml__from_google_drive() {
        google_api_client__validate_init();
        if (google_api_client__validate_connect()) {
            Log.i(TAG, "import_config_toml__from_google_drive: calling google_drive__select_and_download_files() for Filter Filters.contains(SearchableField.TITLE, \".toml\")...");
            google_drive__select_and_download_files(Filters.contains(SearchableField.TITLE, ".toml"), REQUEST_CODES.REQUEST_CODE__SELECT_TOML_FILES_FOR_IMPORT__GOOGLEDRIVE);
        } else {
            Log.i(TAG, "import_config_toml__from_google_drive: GoogleApiClient was not connected -- flow control was transferred to appropriate handler");
        }
    }


    //reaction to srvr remote config-type selection - display all remote config selection UI and update shared prefs to reflect selection
    private final CompoundButton.OnCheckedChangeListener OnCheckedChangeListener__m_rb_val_config_type_sel__remote = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
            if (checked) {
                m_vw_config_sel_container__local.setVisibility(View.GONE);
                m_vw_config_sel_container__remote.setVisibility(View.VISIBLE);
                SharedPrefsManager.BOOLEAN_SHARED_PREF.TM_CONFIG_TYPE_SEL__REMOTE.setValue(true);
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
            String s_old_config_sel__remote_val = SharedPrefsManager.STRING_SHARED_PREF.TM_CONFIG_TYPE_SEL__REMOTE__VAL.getValue();
            Log.d(TAG, "m_btn_config_sel_remote_apply_changes.setOnClickListener: shared pref setting " + SharedPrefsManager.STRING_SHARED_PREF.TM_CONFIG_TYPE_SEL__REMOTE__VAL.toString() + " current value is \"" + SharedPrefsManager.STRING_SHARED_PREF.TM_CONFIG_TYPE_SEL__REMOTE__VAL.getValue() + "\"");
            if (s_old_config_sel__remote_val.compareTo(s_remote_config_toml_sel_normalized) != 0) {
                if (s_remote_config_toml_sel_normalized.isEmpty())
                    Toast.makeText(getApplicationContext(), "Clearing remote config toml file selection", Toast.LENGTH_LONG).show();
                else
                    Toast.makeText(getApplicationContext(), "Saving new setting value for remote config toml file https://" + s_remote_config_toml_sel_normalized, Toast.LENGTH_LONG).show();
                SharedPrefsManager.STRING_SHARED_PREF.TM_CONFIG_TYPE_SEL__REMOTE__VAL.setValue(s_remote_config_toml_sel_normalized);
                Log.d(TAG, "m_btn_config_sel_remote_apply_changes.setOnClickListener: changed setting " + SharedPrefsManager.STRING_SHARED_PREF.TM_CONFIG_TYPE_SEL__REMOTE__VAL.toString() + " value from \"" + s_old_config_sel__remote_val + "\" to \"" + SharedPrefsManager.STRING_SHARED_PREF.TM_CONFIG_TYPE_SEL__REMOTE__VAL.getValue() + "\"");
                m_btn_config_sel_remote_apply_changes.setEnabled(false);
            } else {
                //no change to share pref val - do nothing other than log
                Log.d(TAG, "m_btn_config_sel_remote_apply_changes.setOnClickListener: skipping change to shared pref setting " + SharedPrefsManager.STRING_SHARED_PREF.TM_CONFIG_TYPE_SEL__REMOTE__VAL.toString() + " value (\"" + SharedPrefsManager.STRING_SHARED_PREF.TM_CONFIG_TYPE_SEL__REMOTE__VAL.getValue() + "\") since normalized new value (\"" + s_remote_config_toml_sel_normalized + "\") is no different");
            }
            synchronize_edittext_val_config_sel_remote();
        }
    };

    private final View.OnClickListener OnClickListener__m_btn_srvr_ctrl = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Button btn_srvr_ctrl = (Button)v;
            Boolean srvr_started = (Boolean)btn_srvr_ctrl.getTag(R.id.TAG__SRVR_STARTED);
            if (srvr_started == null || !srvr_started) {
                start_mvt_server();
            } else {
                stop_mvt_server();
            }
        }
    };



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

        //3. reconcile ConfigSettings.STRING_CONFIG_SETTING.TM_CONFIG_TYPE_SEL__LOCAL__VAL setting with m_spinner_val_config_sel_local__dataadapter items and update selection as necessary
        int i_sel_pos = m_spinner_val_config_sel_local__dataadapter.getPosition(SharedPrefsManager.STRING_SHARED_PREF.TM_CONFIG_TYPE_SEL__LOCAL__VAL.getValue());
        if (i_sel_pos != -1) {
            Log.d(TAG, "synchronize_spinner_val_config_sel_local: synchronizing shared pref setting " + SharedPrefsManager.STRING_SHARED_PREF.TM_CONFIG_TYPE_SEL__LOCAL__VAL.toString() + " current value \"" + SharedPrefsManager.STRING_SHARED_PREF.TM_CONFIG_TYPE_SEL__LOCAL__VAL.getValue() + "\" spinner item selection to existing item position " + i_sel_pos);
        } else {
            //note that we must reset i_sel_pos to 0 here since it will be assigned -1 if we are here
            i_sel_pos = 0;
            Log.d(TAG,
                    "synchronize_spinner_val_config_sel_local: cannot synchronize shared prefs setting " + SharedPrefsManager.STRING_SHARED_PREF.TM_CONFIG_TYPE_SEL__LOCAL__VAL.toString() + " current value \"" + SharedPrefsManager.STRING_SHARED_PREF.TM_CONFIG_TYPE_SEL__LOCAL__VAL.getValue()
                    + "\" to spinner item selection since spinner does not currently have a selectable item with that value; setting spinner selected item position to " + i_sel_pos + " for value \"" + m_spinner_val_config_sel_local__items.get(i_sel_pos) + "\"");
        }

        //4. commit changes to spinner to allow for listener to react
        m_spinner_val_config_sel_local.setSelection(i_sel_pos);
        m_spinner_val_config_sel_local__dataadapter.notifyDataSetChanged();
    }

    private void synchronize_edittext_val_config_sel_remote() {
        m_edt_val_config_sel__remote.setText(SharedPrefsManager.STRING_SHARED_PREF.TM_CONFIG_TYPE_SEL__REMOTE__VAL.getValue());
        m_btn_config_sel_remote_apply_changes.setEnabled(false);
        if (SharedPrefsManager.STRING_SHARED_PREF.TM_CONFIG_TYPE_SEL__REMOTE__VAL.getValue().isEmpty()) {
            m_btn_srvr_ctrl.setEnabled(false);
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(new ContextThemeWrapper(MainActivity.this, R.style.alert_dialog));
            alertDialogBuilder.setTitle(getString(R.string.srvr_config_type__remote__no_url_specified));
            alertDialogBuilder
                    .setMessage(getString(R.string.srvr_config_type__remote__no_url_specified__alert_msg))
                    .setCancelable(false)
                    .setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        } else
            m_btn_srvr_ctrl.setEnabled(true);
    }

    private void validate__m_edt_val_config_sel__remote() {
        String s_old_config_sel__remote_val = SharedPrefsManager.STRING_SHARED_PREF.TM_CONFIG_TYPE_SEL__REMOTE__VAL.getValue();
        String s_config_sel__remote_val__proposted = m_edt_val_config_sel__remote.getText().toString();
        Log.d(TAG, "validate__m_edt_val_config_sel__remote: shared pref setting " + SharedPrefsManager.STRING_SHARED_PREF.TM_CONFIG_TYPE_SEL__REMOTE__VAL.toString() + " current value is \"" + s_old_config_sel__remote_val + "\"");
        if (s_old_config_sel__remote_val.compareTo(s_config_sel__remote_val__proposted) == 0) {
            Log.d(TAG, "validate__m_edt_val_config_sel__remote: m_edt_val_config_sel__remote value is no different than shared pref setting " + SharedPrefsManager.STRING_SHARED_PREF.TM_CONFIG_TYPE_SEL__REMOTE__VAL.toString() + " current value \"" + s_old_config_sel__remote_val + "\"");
            m_btn_config_sel_remote_apply_changes.setEnabled(false);
        } else {
            Log.d(TAG, "validate__m_edt_val_config_sel__remote: m_edt_val_config_sel__remote proposed value \"" + s_config_sel__remote_val__proposted + "\" differs from shared pref setting " + SharedPrefsManager.STRING_SHARED_PREF.TM_CONFIG_TYPE_SEL__REMOTE__VAL.toString() + " current value \"" + s_old_config_sel__remote_val + "\"");
            m_btn_config_sel_remote_apply_changes.setEnabled(true);
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(m_edt_val_config_sel__remote.getWindowToken(), 0);
        }
    }



    //Google API Client stuff...
    private boolean google_api_client__validate_init() {
        if (m_google_api_client == null) {
            Log.i(TAG, "google_api_client__validate_init: GoogleApiClient flow handler: building new GoogleApiClient instance...");
            Toast.makeText(getApplicationContext(), "Initializing GoogleApiClient", Toast.LENGTH_LONG).show();
            m_google_api_client = new GoogleApiClient.Builder(MainActivity.this)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addConnectionCallbacks(MainActivity.this)
                    .addOnConnectionFailedListener(MainActivity.this)
                    .build();
            return false;
        }
        Log.i(TAG, "google_api_client__validate_init: GoogleApiClient flow handler: valid GoogleApiClient is instantiated");
        return true;
    }

    private boolean google_api_client__validate_connect() {
        if (!m_google_api_client.isConnected()) {
            Log.i(TAG, "google_api_client__validate_connect: GoogleApiClient flow handler: GoogleApiClient is not connected -- starting connection...");
            Toast.makeText(getApplicationContext(), "Connecting GoogleApiClient...", Toast.LENGTH_LONG).show();
            m_google_api_client.connect();
            return false;
        }
        Log.i(TAG, "google_api_client__validate_connect: GoogleApiClient flow handler: GoogleApiClient connection is valid");
        return true;
    }

    //GoogleApiClient override
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Drawable drawable_cloud_download = ContextCompat.getDrawable(this, R.drawable.ic_cloud_download_black_24dp);
        int h = drawable_cloud_download.getIntrinsicHeight();
        int w = drawable_cloud_download.getIntrinsicWidth();
        drawable_cloud_download.setBounds(0, 0, w, h);
        m_btn_config_sel_local_import__googledrive.setImageDrawable(drawable_cloud_download);
        m_btn_config_sel_local_import__googledrive.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_green_light));
        Log.i(TAG, "onConnected: GoogleApiClient flow handler: connection success");
        Toast.makeText(getApplicationContext(), "GoogleApiClient successfully connected", Toast.LENGTH_LONG).show();
    }

    //GoogleApiClient override
    @Override
    public void onConnectionSuspended(int i) {
        Drawable drawable_cloud_disconnected = ContextCompat.getDrawable(this, R.drawable.ic_cloud_off_black_24dp);
        int h = drawable_cloud_disconnected.getIntrinsicHeight();
        int w = drawable_cloud_disconnected.getIntrinsicWidth();
        drawable_cloud_disconnected.setBounds(0, 0, w, h);
        m_btn_config_sel_local_import__googledrive.setImageDrawable(drawable_cloud_disconnected);
        m_btn_config_sel_local_import__googledrive.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
        Log.i(TAG, "onConnectionSuspended: GoogleApiClient flow handler: connection suspended");
        Toast.makeText(getApplicationContext(), "GoogleApiClient connection suspended", Toast.LENGTH_LONG).show();
    }

    //GoogleApiClient override
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        m_btn_config_sel_local_import__googledrive.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_red_dark));
        if (!connectionResult.hasResolution()) {
            Log.e(TAG, "onConnectionFailed: GoogleApiClient connection failed: " + connectionResult.toString() + " -- flow control handler: abnormal termination :(");
            Toast.makeText(getApplicationContext(), "GoogleApiClient connection failed with no reported resolution!", Toast.LENGTH_LONG).show();
            GoogleApiAvailability.getInstance().getErrorDialog(this, connectionResult.getErrorCode(), 0).show();
            return;
        }
        Log.i(TAG, "onConnectionFailed: GoogleApiClient connection failed: " + connectionResult.toString() + " -- flow control handler: starting GoogleApiClient connection resolution for this result...");
        //Toast.makeText(getApplicationContext(), "GoogleApiClient connection failed -- starting resolution flow...", Toast.LENGTH_LONG).show();
        try {
            connectionResult.startResolutionForResult(this, REQUEST_CODES.REQUEST_CODE__GOOGLEAPICLIENT__RESOLVE_CONNECTION_FAILURE);
        } catch (IntentSender.SendIntentException e) {
            Log.e(TAG, "onConnectionFailed: GoogleApiClient connection failed: " + connectionResult.toString() + " -- flow control handler: IntentSender failed to send intent; abnormal termination :(", e);
            Toast.makeText(getApplicationContext(), "GoogleApiClient connection-failure resolution flow abnormally terminated!", Toast.LENGTH_LONG).show();
        }
    }

    protected void google_drive__select_and_download_files(final int request_id) {
        Log.i(TAG, "google_drive__select_and_download_files: attempting to select/download files from connected google drive...");
        Drive.DriveApi.newDriveContents(m_google_api_client).setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
            @Override
            public void onResult(@NonNull DriveApi.DriveContentsResult driveContentsResult) {
                if (driveContentsResult.getStatus().isSuccess()) {
                    IntentSender google_drive_file_open_intentSender = Drive.DriveApi
                            .newOpenFileActivityBuilder()
                            .build(m_google_api_client);
                    try {
                        startIntentSenderForResult(google_drive_file_open_intentSender, request_id, null, 0, 0, 0);
                    } catch (IntentSender.SendIntentException e) {
                        Log.e(TAG, "google_drive__select_files: Failed to select/download Google Drive file contents -- IntentSender failure!", e);
                        Toast.makeText(getApplicationContext(), "Failed to select/download Google Drive file contents -- IntentSender failure!", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Failed to select/download Google Drive file contents", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
    protected void google_drive__select_and_download_files(@NonNull final Filter file_selection_filter, final int request_id) {
        Log.i(TAG, "google_drive__select_and_download_files: attempting to select/download files from connected google drive...");
        Drive.DriveApi.newDriveContents(m_google_api_client).setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
            @Override
            public void onResult(@NonNull DriveApi.DriveContentsResult driveContentsResult) {
                if (driveContentsResult.getStatus().isSuccess()) {
                    IntentSender google_drive_file_open_intentSender = Drive.DriveApi
                            .newOpenFileActivityBuilder()
                            .setSelectionFilter(file_selection_filter)
                            .build(m_google_api_client);
                    try {
                        startIntentSenderForResult(google_drive_file_open_intentSender, request_id, null, 0, 0, 0);
                    } catch (IntentSender.SendIntentException e) {
                        Log.e(TAG, "google_drive__select_files: Failed to select/download Google Drive file contents -- IntentSender failure!", e);
                        Toast.makeText(getApplicationContext(), "Failed to select/download Google Drive file contents -- IntentSender failure!", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Failed to select/download Google Drive file contents", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
    protected void google_drive__select_and_download_files(final String[] mime_types, final int request_id) {
        Log.i(TAG, "google_drive__select_files: attempting to select/download files from connected google drive...");
        Drive.DriveApi.newDriveContents(m_google_api_client).setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
            @Override
            public void onResult(@NonNull DriveApi.DriveContentsResult driveContentsResult) {
                if (driveContentsResult.getStatus().isSuccess()) {
                    IntentSender google_drive_file_open_intentSender = Drive.DriveApi
                        .newOpenFileActivityBuilder()
                        .setMimeType(mime_types)
                        .build(m_google_api_client);
                    try {
                        startIntentSenderForResult(google_drive_file_open_intentSender, request_id, null, 0, 0, 0);
                    } catch (IntentSender.SendIntentException e) {
                        Log.e(TAG, "google_drive__select_files: Failed to select/download Google Drive file contents -- IntentSender failure!", e);
                        Toast.makeText(getApplicationContext(), "Failed to select/download Google Drive file contents -- IntentSender failure!", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Failed to select/download Google Drive file contents", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    interface GoogleDriveDownloadedFileContentsHandler {
        void OnFileContentsDownloaded(final DriveContents google_drive_file_contents, final Metadata google_drive_file_metadata, final DriveFile google_drive_file);
    }
    private void google_drive__file_contents__download(final DriveId google_drive_id, final int mode, final GoogleDriveDownloadedFileContentsHandler googleDriveOpenedContentsHandler) {
        //download file
        final DriveFile google_drive_file = Drive.DriveApi.getFile(m_google_api_client, google_drive_id);
        PendingResult<DriveApi.DriveContentsResult> pendingResult = google_drive_file.open(m_google_api_client, mode, new DriveFile.DownloadProgressListener() {
            @Override
            public void onProgress(long bytesDownloaded, long bytesExpected) {
                //TODO: manip UI to display progress
            }
        });
        pendingResult.setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
            @Override
            public void onResult(final DriveApi.DriveContentsResult driveContentsResult) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        DriveResource.MetadataResult metadataresult = google_drive_file.getMetadata(m_google_api_client).await(1000, TimeUnit.MILLISECONDS);
                        Metadata google_drive_file_metadata = metadataresult.getMetadata();
                        String gd_filename = null;
                        try { gd_filename = google_drive_file_metadata.getOriginalFilename(); } catch (Exception e) {}
                        if (!driveContentsResult.getStatus().isSuccess()) {
                            final String s_err = "Failed to download contents of Google Drive file" + (gd_filename != null ? " \"" + gd_filename + "\"" : "");
                            Log.e(TAG, "google_drive__download_file_contents--PendingResult.ResultCallback.onResult: " + s_err);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), s_err, Toast.LENGTH_LONG).show();
                                }
                            });
                            return;
                        }
                        final DriveContents google_drive_contents = driveContentsResult.getDriveContents();
                        googleDriveOpenedContentsHandler.OnFileContentsDownloaded(google_drive_contents, google_drive_file_metadata, google_drive_file);
                        google_drive_contents.discard(m_google_api_client); //closes google drive file
                    }
                }).start();
            }
        });
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
                        break;
                    }
                    default: {
                        Log.d(TAG, "onActivityResult: requestCode " + requestCode + ", resultCode " + resultCode);
                        super.onActivityResult(requestCode, resultCode, data);
                    }
                }
                break;
            }
            case REQUEST_CODES.REQUEST_CODE__SELECT_TOML_FILES_FOR_IMPORT__GOOGLEDRIVE: {
                switch (resultCode) {
                    case RESULT_OK: {
                        m_google_drive_id = (DriveId)data.getParcelableExtra(OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);
                        Log.i(TAG, "onActivityResult: requestCode: REQUEST_CODE__SELECT_TOML_FILES_FOR_IMPORT__GOOGLEDRIVE | resultCode: RESULT_OK -- flow control handler: selected Google Drive file id " + m_google_drive_id.getResourceId() + "; calling google_drive__download_file_contents() for this file...");
                        google_drive__file_contents__download(m_google_drive_id, DriveFile.MODE_READ_ONLY, new GoogleDriveDownloadedFileContentsHandler() {
                            @Override
                            public void OnFileContentsDownloaded(final DriveContents google_drive_file_contents, final Metadata google_drive_file_metadata, final DriveFile google_drive_file) {
                                final String
                                    s_gd_id = google_drive_file_contents.getDriveId().encodeToString()
                                    , s_gd_filename = google_drive_file_metadata.getOriginalFilename();
                                Log.i(TAG, "inline GoogleDriveDownloadedFileContentsHandler.OnFileContentsDownloaded: triggered from PendingResult from call to google_drive__download_file_contents() -- successfully downloaded google drive file \"" + s_gd_filename + "\" contents from: id " + s_gd_id);
                                try {
                                    Log.i(TAG, "inline GoogleDriveDownloadedFileContentsHandler.OnFileContentsDownloaded: importing contents from google drive file \"" + s_gd_filename + " (id \"" + s_gd_id + ")\"");
                                    final boolean succeeded = google_drive__file__import(google_drive_file_contents, google_drive_file_metadata, google_drive_file);
                                    final String s_result_msg = (succeeded ? "Successfully imported" : "Failed to import") + " google drive file \"" + s_gd_filename + "\" (id " + s_gd_id + ")";
                                    Log.i(TAG, "inline GoogleDriveDownloadedFileContentsHandler.OnFileContentsDownloaded: " + s_result_msg);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            Toast.makeText(getApplicationContext(), s_result_msg, Toast.LENGTH_LONG).show();
                                            if (succeeded)
                                                synchronize_spinner_val_config_sel_local();
                                        }
                                    });
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        break;
                    }
                    case RESULT_CANCELED: {
                        Log.i(TAG, "onActivityResult: requestCode: REQUEST_CODE__SELECT_TOML_FILES_FOR_IMPORT__GOOGLEDRIVE | resultCode: RESULT_CANCELED -- flow control handler: user canceled -- normal flow termination");
                        break;
                    }
                }
                break;
            }
            case REQUEST_CODES.REQUEST_CODE__GOOGLEAPICLIENT__RESOLVE_CONNECTION_FAILURE: {
                switch (resultCode) {
                    case RESULT_OK: {
                        Log.i(TAG, "onActivityResult: requestCode: REQUEST_CODE__GOOGLEAPICLIENT__RESOLVE_CONNECTION_FAILURE | resultCode: RESULT_OK -- flow control handler: validating GoogleApiClient connection...");
                        google_api_client__validate_connect();
                        break;
                    }
                    case RESULT_CANCELED: {
                        Log.i(TAG, "onActivityResult: requestCode: REQUEST_CODE__GOOGLEAPICLIENT__RESOLVE_CONNECTION_FAILURE | resultCode: RESULT_CANCELED -- flow control handler: abnormal flow termination :(");
                        break;
                    }
                }
                break;
            }
            /*
            case REQUEST_CODES.REQUEST_CODE__EDIT_TOML_FILE: {
                switch (resultCode) {
                    case RESULT_OK: {
                        break;
                    }
                    case RESULT_CANCELED: {
                        break;
                    }
                }
                break;
            }
            */
            default: {
                Log.d(TAG, "onActivityResult: requestCode " + requestCode + ", resultCode " + resultCode);
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

    private boolean google_drive__file__import(final DriveContents google_drive_file_contents, final Metadata google_drive_file_metadata, final DriveFile google_drive_file) throws IOException {
        String gd_file_name = google_drive_file_metadata.getOriginalFilename();
        InputStream inputstream_gd_config_toml = google_drive_file_contents.getInputStream();
        byte[] buf_raw_config_toml = new byte[inputstream_gd_config_toml.available()];
        inputstream_gd_config_toml.read(buf_raw_config_toml);
        inputstream_gd_config_toml.close();
        FileOutputStream f_outputstream_new_tegola_config_toml = openFileOutput(gd_file_name, Context.MODE_PRIVATE);
        f_outputstream_new_tegola_config_toml.write(buf_raw_config_toml);
        f_outputstream_new_tegola_config_toml.close();
        File f_new_tegola_config_toml = new File(getFilesDir().getPath() + "/" + gd_file_name);
        return f_new_tegola_config_toml.exists();
    }




    //ControllerLib-related stuff
    private void OnControllerStarting() {
        m_tv_val_ctrlr_status.setText(getString(R.string.starting));
    }

    private void OnControllerStarted() {
        m_tv_val_ctrlr_status.setText(getString(R.string.started));
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
    }

    private void OnMVTServerStartFailed(final String reason) {
        OnMVTServerStopped();
    }

    private void OnMVTServerStarted(final String version, final int pid) {
        final StringBuilder sb_srvr_status = new StringBuilder();
        sb_srvr_status.append(getString(R.string.started));
//        if (version != null && !version.isEmpty())
//            sb_srvr_status.append(" version " + version);
        if (pid != -1)
            sb_srvr_status.append(" , pid " + pid + "");
        m_tv_val_srvr_status.setText(sb_srvr_status.toString());
        m_btn_srvr_ctrl.setTag(R.id.TAG__SRVR_STARTED, true);
        m_btn_srvr_ctrl.setText(getString(R.string.stop));
        //now disable edit-config button
        m_btn_config_sel_local__edit_file.setEnabled(false);
    }

    private void OnMVTServerOutputLogcat(final String logcat_line) {
        sv_append_mvt_server_console_output("<LOGCAT> " + logcat_line);
    }

    private void OnMVTServerOutputStdErr(final String stderr_line) {
        sv_append_mvt_server_console_output("<STDERR> " + stderr_line);
    }

    private void OnMVTServerOutputStdOut(final String stdout_line) {
        sv_append_mvt_server_console_output("<STDOUT> " + stdout_line);
    }

    private void m_scrvw_tegola_console_output__scroll_max() {
        m_scrvw_tegola_console_output.postDelayed(new Runnable() {
            public void run() {
                m_scrvw_tegola_console_output.fullScroll(View.FOCUS_DOWN);
            }
        }, 50);
    }

    private void sv_append_mvt_server_console_output(final String s) {
        m_tv_tegola_console_output.append(s + "\n");
        m_scrvw_tegola_console_output__scroll_max();
        m_scvw_main__scroll_max();
    }

    private void OnMVTServerStopping() {
        m_tv_val_srvr_status.setText(getString(R.string.stopping));
    }

    private void OnMVTServerStopped() {
        m_tv_val_srvr_status.setText(getString(R.string.stopped));
        m_btn_srvr_ctrl.setTag(R.id.TAG__SRVR_STARTED, false);
        m_btn_srvr_ctrl.setText(getString(R.string.start));
        m_btn_config_sel_local__edit_file.setEnabled(true);
    }

    private void start_controller_fgs() {
        m_tv_val_ctrlr_status.setText(getString(R.string.starting));
        Intent intent_start_controller_fgs = new Intent(MainActivity.this, ControllerFGS.class);
        intent_start_controller_fgs.setAction(Constants.Strings.INTENT.ACTION.FGS_CONTROL_REQUEST.FGS__START_FOREGROUND);
        intent_start_controller_fgs.putExtra(Constants.Strings.INTENT.ACTION.FGS_CONTROL_REQUEST.EXTRA__KEY.FGS__START_FOREGROUND__HARNESS, MainActivity.class.getName());
        startService(intent_start_controller_fgs);
    }

    private void stop_controller_fgs() {
        m_tv_val_ctrlr_status.setText(getString(R.string.stopping));
        Intent intent_stop_controller_fgs = new Intent(MainActivity.this, ControllerFGS.class);
        intent_stop_controller_fgs.setAction(Constants.Strings.INTENT.ACTION.FGS_CONTROL_REQUEST.FGS__STOP_FOREGROUND);
        stopService(intent_stop_controller_fgs);
    }

    private void start_mvt_server() {
        Intent intent_start_mvt_server = new Intent(Constants.Strings.INTENT.ACTION.MVT_SERVER_CONTROL_REQUEST.MVT_SERVER__START);
        boolean remote_config = SharedPrefsManager.BOOLEAN_SHARED_PREF.TM_CONFIG_TYPE_SEL__REMOTE.getValue();
        String s_config_toml = (remote_config ? SharedPrefsManager.STRING_SHARED_PREF.TM_CONFIG_TYPE_SEL__REMOTE__VAL.getValue() : SharedPrefsManager.STRING_SHARED_PREF.TM_CONFIG_TYPE_SEL__LOCAL__VAL.getValue());
        intent_start_mvt_server.putExtra(Constants.Strings.INTENT.ACTION.MVT_SERVER_CONTROL_REQUEST.EXTRA__KEY.MVT_SERVER__START__CONFIG__REMOTE, remote_config);
        intent_start_mvt_server.putExtra(Constants.Strings.INTENT.ACTION.MVT_SERVER_CONTROL_REQUEST.EXTRA__KEY.MVT_SERVER__START__CONFIG, s_config_toml);
        sendBroadcast(intent_start_mvt_server);
    }

    private void stop_mvt_server() {
        Intent intent_stop_mvt_server = new Intent(Constants.Strings.INTENT.ACTION.MVT_SERVER_CONTROL_REQUEST.MVT_SERVER__STOP);
        sendBroadcast(intent_stop_mvt_server);
    }
}