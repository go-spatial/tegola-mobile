package go_spatial.com.github.tegola.mobile.android.ux;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.concurrent.Semaphore;

import go_spatial.com.github.tegola.mobile.android.controller.*;
import go_spatial.com.github.tegola.mobile.android.controller.Constants;
import go_spatial.com.github.tegola.mobile.android.controller.utils.Files;
import go_spatial.com.github.tegola.mobile.android.controller.utils.HTTP;
import okhttp3.HttpUrl;
import okio.Buffer;
import timber.log.Timber;

public class InstallGpkgBundleActivity extends AppCompatActivity {
    private final static String TAG = InstallGpkgBundleActivity.class.getCanonicalName();

    private ImageButton m_btn_install_remote_gpkg_bundle__cancel = null;
    private TextView m_tv_lbl_remote_gpkg_bundle__root_url__http_proto_prefix = null;
    private EditText m_edt_remote_gpkg_bundle__root_url = null;
    private EditText m_edt_remote_gpkg_bundle__name = null;
    private ImageButton m_ibtn_help_gpkg_name = null;
    private EditText m_edt_remote_gpkg_bundle__ver_props = null;
    private ImageButton m_ibtn_help_gpkg_props_file = null;
    private EditText m_edt_local_gpkg_bundle__name = null;
    private Button m_btn_install_remote_gpkg_bundle = null;
    private View m_vw_progress = null;
    private TextView m_tv_val__install_gpkg_bundle__file_download_progress = null;
    private ProgressBar m_pb = null;
    private TextView m_tv_val__install_gpkg_bundle__file_download_progress__bytes = null;

    private final int PROG_MAX = 100;

    public static final int INSTALL_GPKG_BUNDLE_RESULT__SUCCESSFUL = 0;
    public static final int INSTALL_GPKG_BUNDLE_RESULT__CANCELLED = -1;
    public static final int INSTALL_GPKG_BUNDLE_RESULT__FAILED = -2;

    public enum AsyncGetGpkgBundleFileTaskHandlerState {
        NOT_STARTED, STARTING, IN_PROGRESS, CANCELLED, DONE_WITH_EXCEPTION, DONE;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_install_gpkg_bundle);

        m_btn_install_remote_gpkg_bundle__cancel = (ImageButton)findViewById(R.id.btn_install_remote_geopackage_bundle__cancel);
        m_tv_lbl_remote_gpkg_bundle__root_url__http_proto_prefix = (TextView)findViewById(R.id.tv_lbl_remote_gpkg_bundle__root_url__http_proto_prefix);
        m_edt_remote_gpkg_bundle__root_url = (EditText) findViewById(R.id.edt_remote_gpkg_bundle__root_url);

        m_edt_remote_gpkg_bundle__name = (EditText) findViewById(R.id.edt_remote_gpkg_bundle__name);
        m_edt_remote_gpkg_bundle__name.setOnKeyListener(OnKeyListener__disable_install);
        m_edt_remote_gpkg_bundle__name.setOnEditorActionListener(OnEditorActionListener__validate_enable_install);
        m_edt_remote_gpkg_bundle__name.setOnFocusChangeListener(OnFocusChangeListener__validate_enable_install);

        m_ibtn_help_gpkg_name = (ImageButton)findViewById(R.id.ibtn_help_gpkg_name);
        m_ibtn_help_gpkg_name.setOnClickListener(onHelpButtonClickLister);

        m_edt_remote_gpkg_bundle__ver_props = (EditText)findViewById(R.id.edt_remote_gpkg_bundle__ver_props);
        m_edt_remote_gpkg_bundle__ver_props.setOnKeyListener(OnKeyListener__disable_install);
        m_edt_remote_gpkg_bundle__ver_props.setOnEditorActionListener(OnEditorActionListener__validate_enable_install);
        m_edt_remote_gpkg_bundle__ver_props.setOnFocusChangeListener(OnFocusChangeListener__validate_enable_install);

        m_ibtn_help_gpkg_props_file = (ImageButton)findViewById(R.id.ibtn_help_gpkg_props_file);
        m_ibtn_help_gpkg_props_file.setOnClickListener(onHelpButtonClickLister);

        m_edt_local_gpkg_bundle__name = (EditText)findViewById(R.id.edt_local_gpkg_bundle__name);
        m_edt_local_gpkg_bundle__name.setOnKeyListener(OnKeyListener__disable_install);
        m_edt_local_gpkg_bundle__name.setOnEditorActionListener(OnEditorActionListener__validate_enable_install);
        m_edt_local_gpkg_bundle__name.setOnFocusChangeListener(OnFocusChangeListener__validate_enable_install);

        m_btn_install_remote_gpkg_bundle = (Button)findViewById(R.id.btn_install_remote_gpkg_bundle);
        m_vw_progress = findViewById(R.id.sect_content__item__install_remote_gpkg_bundle__progress);
        m_tv_val__install_gpkg_bundle__file_download_progress = (TextView)findViewById(R.id.tv_val__install_gpkg_bundle__file_download_progress);
        m_pb = (ProgressBar)findViewById(R.id.pb);
        m_tv_val__install_gpkg_bundle__file_download_progress__bytes = (TextView)findViewById(R.id.tv_val__install_gpkg_bundle__file_download_progress__bytes);

        m_btn_install_remote_gpkg_bundle__cancel.setOnClickListener(OnClickListener__m_btn_install_remote_gpkg_bundle__cancel);
        m_btn_install_remote_gpkg_bundle.setOnClickListener(OnClickListener__m_btn_install_remote_gpkg_bundle);
    }

    private LinkedHashMap<String, ArrayList<String>> m_gpkg_bundles_available = null;
    private void getGpkgBundlesAvailable() {
        m_gpkg_bundles_available = new LinkedHashMap<String, ArrayList<String>>();
        final String[] sary_gpkg_bundles_available = getResources().getStringArray(R.array.gpkg_bundles__available);
        for (String s_gpkg_bundle : sary_gpkg_bundles_available) {
            String[] sary_gpkg_bundle_spec = s_gpkg_bundle.split(":");
            String s_gpkg_name = sary_gpkg_bundle_spec[0].trim();
            String[] sary_gpkg_bundle_props_files = sary_gpkg_bundle_spec[1].split(",");
            ArrayList<String> sal_gpkg_bundle_props_files = new ArrayList<String>();
            for (String s_gpkg_props_file : sary_gpkg_bundle_props_files)
                sal_gpkg_bundle_props_files.add(s_gpkg_props_file.trim());
            m_gpkg_bundles_available.put(s_gpkg_name, sal_gpkg_bundle_props_files);
        }
    }

    private final View.OnClickListener onHelpButtonClickLister = view -> {
        switch (view.getId()) {
            case R.id.ibtn_help_gpkg_name: {
                StringBuilder sb_msg = new StringBuilder("Available GeoPackage-Bundles (names):\n\n");
                for (String s_gpkg_bundle_name : m_gpkg_bundles_available.keySet())
                    sb_msg.append(String.format("\t- \"%s\"\n", s_gpkg_bundle_name));
                sb_msg.append("\n");
                AlertDialog alertDialog = new AlertDialog.Builder(InstallGpkgBundleActivity.this)
                    .setTitle("Help: GeoPackage-Bundle Name")
                    .setMessage(sb_msg.toString())
                    .setPositiveButton(
                        "OK",
                        (dialog, which) -> dialog.dismiss()
                    )
                    .setCancelable(false)
                    .create();
                alertDialog.show();
                break;
            }
            case R.id.ibtn_help_gpkg_props_file: {
                StringBuilder sb_msg = null;
                String s_gpkg_bundle_name = m_edt_remote_gpkg_bundle__name.getText().toString().trim();
                if (m_gpkg_bundles_available.containsKey(s_gpkg_bundle_name)) {
                    sb_msg = new StringBuilder(String.format("Available Props Files for GeoPackage-Bundle \"%s\":\n\n", s_gpkg_bundle_name));
                    ArrayList<String> sal_gpkg_bundle_props_files = m_gpkg_bundles_available.get(s_gpkg_bundle_name);
                    for (String s_gpkg_bundle_props_file : sal_gpkg_bundle_props_files)
                        sb_msg.append(String.format("\t- \"%s\"\n", s_gpkg_bundle_props_file));
                } else {
                    sb_msg = s_gpkg_bundle_name.isEmpty()
                        ? new StringBuilder("No Geopackage-Bundle is named/selected!\n\nPlease name/select an avaialble Geopackage-Bundle.")
                        : new StringBuilder(String.format("\"%s\" does not name an available Geopackage-Bundle!", s_gpkg_bundle_name));
                }
                sb_msg.append("\n");
                AlertDialog alertDialog = new AlertDialog.Builder(InstallGpkgBundleActivity.this)
                    .setTitle("Help: GeoPackage-Bundle Props File")
                    .setMessage(sb_msg.toString())
                    .setPositiveButton(
                        "OK",
                        (dialog, which) -> dialog.dismiss()
                    )
                    .setCancelable(false)
                    .create();
                alertDialog.show();
                break;
            }
        }
    };

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        m_edt_remote_gpkg_bundle__root_url.setText(
            new StringBuilder()
                .append(getString(R.string.gpkg_bundle__root_url__default))
                .append(getString(R.string.fixed__remote_gpkg_bundle__root_url__tail))
                .toString()
        );

        m_edt_local_gpkg_bundle__name.setEnabled(false);
        m_btn_install_remote_gpkg_bundle.setEnabled(false);

        m_pb.setMax(PROG_MAX);
        m_vw_progress.setVisibility(View.GONE);

        m_edt_remote_gpkg_bundle__name.requestFocus();

        getGpkgBundlesAvailable();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        m_gpkg_bundle_download_boostrapper.shutdown();
        m_gpkg_bundle_file_downloader.shutdown();
    }

    private String build_remote_gpkg_bundle_file_url_string(String s_gremote_gpkg_fname) {
        return new
            StringBuilder(
                GPKG.Remote.build_root_url_string(
                    m_tv_lbl_remote_gpkg_bundle__root_url__http_proto_prefix.getText().toString(),
                    m_edt_remote_gpkg_bundle__root_url.getText().toString(),
                    m_edt_remote_gpkg_bundle__name.getText().toString()
                )
            )
            .append("/")
            .append(s_gremote_gpkg_fname)
            .toString();
    }

    private String build_local_gpkg_bundle_path_string() throws PackageManager.NameNotFoundException, IOException {
        return new StringBuilder()
            .append(GPKG.Local.F_GPKG_BUNDLE_ROOT_DIR.getInstance(InstallGpkgBundleActivity.this.getApplicationContext()).getPath())
            .append(File.separator)
            .append(m_edt_local_gpkg_bundle__name.getText())
            .toString();
    }

    private final View.OnClickListener OnClickListener__m_btn_install_remote_gpkg_bundle__cancel = v -> {
        InstallGpkgBundleActivity.this.setResult(INSTALL_GPKG_BUNDLE_RESULT__CANCELLED);
        InstallGpkgBundleActivity.this.finish();
    };

    private TextView.OnKeyListener OnKeyListener__disable_install = (v, keyCode, event) -> {
        m_edt_local_gpkg_bundle__name.setText("");
        m_btn_install_remote_gpkg_bundle.setEnabled(false);
        return false;
    };

    private void validate_enable_install_button() {
        String s_remote_gpkg_bundle__name = m_edt_remote_gpkg_bundle__name.getText().toString().trim().replace("/", "");
        String s_remote_gpkg_bundle__ver_props = m_edt_remote_gpkg_bundle__ver_props.getText().toString().trim().replace("/", "");

        m_btn_install_remote_gpkg_bundle.setEnabled(
            !(
                s_remote_gpkg_bundle__name.isEmpty()
                || s_remote_gpkg_bundle__ver_props.isEmpty()
            )
        );
        if (m_btn_install_remote_gpkg_bundle.isEnabled()) {
            m_edt_local_gpkg_bundle__name.setText(s_remote_gpkg_bundle__name);
        }
    }

    //reaction to changing remote gpkg bundle name - user must press enter in editor or switch focus to another control to register to app that a pending change has occurred
    private final TextView.OnEditorActionListener OnEditorActionListener__validate_enable_install = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_NULL) {
                validate_enable_install_button();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(textView.getWindowToken(), 0);
                return true;
            } else {
                return false;
            }
        }
    };
    private final TextView.OnFocusChangeListener OnFocusChangeListener__validate_enable_install = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (!hasFocus) {
                validate_enable_install_button();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        }
    };

    final class ShowReplaceGPKGBundleFileAlertDialog implements Runnable {
        private final String TAG = ShowReplaceGPKGBundleFileAlertDialog.class.getCanonicalName();

        final private Context context;
        private Semaphore permit;
        final private File f_gpkg_bundle_file;
        final private Bundle bundle_result;

        public ShowReplaceGPKGBundleFileAlertDialog(final Context context, final File f_gpkg_bundle_file, final Bundle bundle_result) {
            this.context = context;
            this.f_gpkg_bundle_file = f_gpkg_bundle_file;
            this.bundle_result = bundle_result;
        }

        private ShowReplaceGPKGBundleFileAlertDialog() {
            this.context = null;
            this.f_gpkg_bundle_file = null;
            this.bundle_result = null;
        }

        public final static String ARG_REPLACE_GPKG_BUNDLE_FILE = "REPLACE_GPKG_BUNDLE_FILE";

        @Override
        public void run() {
            Log.d(TAG, "run (working thread): entered");
            permit = new Semaphore(1, true);
            permit.drainPermits();
            Log.d(TAG, "run (working thread): created semaphore");
            runOnUiThread(
                () -> {
                    Log.d(TAG, "run (on ui thread): creating alert dialog");
                    AlertDialog alertDialog = new AlertDialog.Builder(context)
                        .setTitle("GeoPackage-Bundle \"" + f_gpkg_bundle_file.getParentFile().getName() + "\" file \"" + f_gpkg_bundle_file.getName() + "\" already exists!")
                        .setMessage("Would you like to REPLACE, or SKIP \"" + f_gpkg_bundle_file.getParentFile().getName() + "\" geopackage bundle file \"" + f_gpkg_bundle_file.getName() + "\"?")
                        .setPositiveButton(
                            "REPLACE",
                            (dialog, which) -> {
                                f_gpkg_bundle_file.delete();
                                bundle_result.putBoolean(ARG_REPLACE_GPKG_BUNDLE_FILE, true);
                                permit.release();
                            }
                        )
                        .setNegativeButton(
                            "SKIP",
                            (dialog, which) -> {
                                bundle_result.putBoolean(ARG_REPLACE_GPKG_BUNDLE_FILE, false);
                                permit.release();
                            }
                        )
                        .setCancelable(false)
                        .create();
                    Log.d(TAG, "run (on ui thread): alert dialog created, showing dialog...");
                    alertDialog.show();
                }
            );

            try {
                Thread.sleep(50);
                Log.d(TAG, "run (working thread): acquiring semaphore permit");
                permit.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Log.d(TAG, "run (working thread): exiting");
        }
    };

    private final View.OnClickListener OnClickListener__m_btn_install_remote_gpkg_bundle = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            m_vw_progress.setVisibility(View.VISIBLE);

            try {
                String local_gpkg_bundle_path = build_local_gpkg_bundle_path_string();
                Log.d(TAG, "OnClickListener__m_btn_install_remote_gpkg_bundle.onClick: local_gpkg_bundle_path==" + local_gpkg_bundle_path);

                //queue up task to get remote version.properties - this file contains the download-spec for the selected gpkg-bundle
                // we need to process this first, to find out how many and which components to download that comprise this gpkg-bundle
                String s_gpkg_version_props_fname = m_edt_remote_gpkg_bundle__ver_props.getText().toString().trim();
                String s_req_suffix = ".properties";
                if (!s_gpkg_version_props_fname.endsWith(s_req_suffix)) {
                    Log.d(TAG, "OnClickListener__m_btn_install_remote_gpkg_bundle.onClick: normalizing ver props fname \"" + s_gpkg_version_props_fname + "\" to \"" + s_gpkg_version_props_fname + s_req_suffix + "\"");
                    s_gpkg_version_props_fname += s_req_suffix;
                }
                HttpUrl httpurl_version_props = HttpUrl.parse(build_remote_gpkg_bundle_file_url_string(s_gpkg_version_props_fname));
                Log.d(TAG, "OnClickListener__m_btn_install_remote_gpkg_bundle.onClick: FNAME ==" + httpurl_version_props.toString());
                File localfile_version_props = new File(local_gpkg_bundle_path, s_gpkg_version_props_fname);
                Log.d(TAG, "OnClickListener__m_btn_install_remote_gpkg_bundle.onClick: localfile_version_props path ==" + localfile_version_props.getCanonicalPath());
                Bundle bundle = new Bundle();
                bundle.putBoolean(ShowReplaceGPKGBundleFileAlertDialog.ARG_REPLACE_GPKG_BUNDLE_FILE, true);
                if (localfile_version_props.exists())
                    localfile_version_props.delete();
                m_gpkg_bundle_download_boostrapper.submit(
                    new HTTP.AsyncGet.CallableTask(
                        new HTTP.AsyncGet.HttpUrl_To_Local_File(httpurl_version_props, localfile_version_props),
                        new AsyncGetGpkgBundleFileTaskStageHandler()
                    )
                );
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
//            catch (HTTP.AsyncGet.TaskExecuteQueueException e) {
//                e.printStackTrace();
//            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    private enum E_INSTALLATION_STATE {
        IDLE,
        IN_PROGRESS
    }
    private void OnInstallationStateChanged(@NonNull final E_INSTALLATION_STATE installation_state) {
        Log.d(TAG, "OnInstallationStateChanged: state==" + installation_state.name());
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (installation_state) {
                    case IDLE: {
                        m_edt_remote_gpkg_bundle__root_url.setEnabled(true);
                        m_edt_remote_gpkg_bundle__name.setEnabled(true);
                        m_edt_remote_gpkg_bundle__ver_props.setEnabled(true);
                        m_btn_install_remote_gpkg_bundle.setEnabled(true);
                        break;
                    }
                    case IN_PROGRESS: {
                        m_edt_remote_gpkg_bundle__root_url.setEnabled(false);
                        m_edt_remote_gpkg_bundle__name.setEnabled(false);
                        m_edt_remote_gpkg_bundle__ver_props.setEnabled(false);
                        m_btn_install_remote_gpkg_bundle.setEnabled(false);
                        break;
                    }
                }
            }
        });
    }


    private class AsyncGetGpkgBundleFileTaskStageHandler extends HTTP.AsyncGet.TaskStageHandler {
        private final String TAG = InstallGpkgBundleActivity.class.getSimpleName() + "." + AsyncGetGpkgBundleFileTaskStageHandler.class.getSimpleName();

        private volatile boolean firstUpdate = true;
        private volatile long content_length = 0;
        private volatile long total_bytes_read = 0;
        private volatile HTTP.AsyncGet.HttpUrl_To_Local_File httpUrl_to_local_file = null;
        private volatile String s_url_remote_file = "<failed to retrieve remote file url>";
        private volatile File local_file = null;
        private volatile String s_local_file = "<failed to retrieve local file path>";
        private volatile FileOutputStream f_outputstream__local_file = null;

        private AsyncGetGpkgBundleFileTaskHandlerState state = AsyncGetGpkgBundleFileTaskHandlerState.NOT_STARTED;

        public AsyncGetGpkgBundleFileTaskHandlerState get_state() {
            return state;
        }

        private Exception exception = null;

        public Exception get_exception() {
            return exception;
        }

        public AsyncGetGpkgBundleFileTaskStageHandler() {}

        @Override
        public void onPreExecute() {
            Log.d(TAG, "onPreExecute - resetting state vars...");

            state = AsyncGetGpkgBundleFileTaskHandlerState.STARTING;

            firstUpdate = true;
            content_length = 0;
            total_bytes_read = 0;
            httpUrl_to_local_file = get_httpUrl_to_local_file();
            f_outputstream__local_file = null;

            if (httpUrl_to_local_file != null) {
                s_url_remote_file = httpUrl_to_local_file.get_url().toString();
                local_file = httpUrl_to_local_file.get_file();
                if (local_file != null) {
                    try {
                        s_local_file = local_file.getCanonicalPath();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            Log.d(TAG, String.format("onPreExecute - starting download of %s to %s", s_url_remote_file, s_local_file));
        }

        @Override
        public void onChunkRead(Buffer sink, long bytesRead, long contentLength, boolean done) throws IOException {
            try {
                if (!done) {
                    if (firstUpdate) {
                        Timber.d("onChunkRead: firstUpdate==true");
                        firstUpdate = false;
                        state = AsyncGetGpkgBundleFileTaskHandlerState.IN_PROGRESS;

                        if (httpUrl_to_local_file == null) {
                            httpUrl_to_local_file = get_httpUrl_to_local_file();
                            s_url_remote_file = httpUrl_to_local_file.get_url().toString();
                            local_file = httpUrl_to_local_file.get_file();
                            if (local_file != null) {
                                try {
                                    s_local_file = local_file.getCanonicalPath();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        if (contentLength < 1) {
                            Timber.d("onChunkRead: *** WARNING!!! *** - contentLength < 1");
                        }
                        content_length = contentLength;
                        Log.d(TAG, String.format("onChunkRead: content_length==%d", content_length));
                        if (local_file.exists()) {
                            Log.d(TAG, String.format("onChunkRead: local file %s already exists", s_local_file));
                            throw new HTTP.AsyncGet.StageHandlerOnChunkRead_LocalFileAlreadyExistsException(httpUrl_to_local_file);
                        } else {
                            Log.d(TAG, String.format("onChunkRead: local file %s does not exist", s_local_file));
                            if (!local_file.getParentFile().exists()) {
                                Log.d(TAG, String.format("onChunkRead: local file directory %s does not exist; creating...", local_file.getParentFile().getCanonicalPath()));
                                local_file.getParentFile().mkdirs();
                            }
                            boolean created_file = local_file.createNewFile();
                            Log.d(TAG, String.format("onChunkRead: %s new local file %s; opening outputstream", (created_file ? "Succcessfully created" : "Failed to create"), s_local_file));
                            f_outputstream__local_file = new FileOutputStream(local_file);
                        }
                        Log.d(TAG, String.format("onChunkRead: downloading/writing %s outputstream to %s...", s_url_remote_file, s_local_file));

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                m_tv_val__install_gpkg_bundle__file_download_progress.setText(get_httpUrl_to_local_file().get_file().getName());
                            }
                        });
                    }

                    //write bytes to outputstream
                    //Log.d(TAG, "onChunkRead: sink.size()==" + sink.size());
                    sink.copyTo(f_outputstream__local_file);
                    f_outputstream__local_file.flush();

                    total_bytes_read += bytesRead;
                    //Log.d(TAG, "onChunkRead: total_bytes_read==" + total_bytes_read);

                    //now update progress
                    if (content_length > 0) {
                        final double dbl_progress = ((double) total_bytes_read * 1.0) / ((double) content_length * 1.0);
                        //Log.d(TAG, "onChunkRead: (double) progress ratio: " + dbl_progress);
                        final int i_progress = (int) (dbl_progress * 100.0);
                        //Log.d(TAG, "onChunkRead: updating progress to " + i_progress + "%");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                m_pb.setMax(PROG_MAX);
                                m_pb.setProgress(i_progress);
                                m_tv_val__install_gpkg_bundle__file_download_progress__bytes.setText(String.format("%d of %d bytes", total_bytes_read, content_length));
                            }
                        });
                    } else {
                        Log.d(TAG, String.format("onChunkRead: cannot update interim progress for %s to %s download since content_length==%d", s_url_remote_file, s_local_file, content_length));
                    }
                } else {//done
                    Log.d(TAG, String.format("onChunkRead: done; wrote: %d bytes to %s", total_bytes_read, s_local_file));
                    if (f_outputstream__local_file != null) {
                        Log.d(TAG, String.format("onChunkRead: Closing fileoutputstream for %s", s_local_file));
                        f_outputstream__local_file.close();
                    } else {
                        Log.d(TAG, String.format("onChunkRead: Cannot close null fileoutputstream for %s", s_local_file));
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            m_pb.setMax(PROG_MAX);
                            m_pb.setProgress(PROG_MAX);
                            m_tv_val__install_gpkg_bundle__file_download_progress__bytes.setText(content_length + " of " + content_length + " bytes");
                        }
                    });
                }
            } catch (HTTP.AsyncGet.StageHandlerOnChunkRead_LocalFileAlreadyExistsException e) {
                this.exception = e;
                throw e;
            } catch (FileNotFoundException e) {
                HTTP.AsyncGet.StageHandlerOnChunkRead_LocalFileCreateException lfce = new HTTP.AsyncGet.StageHandlerOnChunkRead_LocalFileCreateException(httpUrl_to_local_file, e.getMessage());
                this.exception = lfce;
                throw lfce;
            } catch (IOException e) {
                HTTP.AsyncGet.StageHandlerOnChunkRead_GeneralIOException gioe = new HTTP.AsyncGet.StageHandlerOnChunkRead_GeneralIOException(httpUrl_to_local_file, e.getMessage());
                this.exception = gioe;
                throw gioe;
            }
        }

        @Override
        public void onCancelled(Exception exception) {
            if (exception != null) {
                this.exception = exception;
            }
            if (this.exception != null) {
                Log.e(TAG, "onCancelled: cancelled with exception: " + this.exception.toString() + ": " + this.exception.getMessage());
            }
            state = AsyncGetGpkgBundleFileTaskHandlerState.CANCELLED;
            Log.d(TAG, String.format("onCancelled - download of %s to %s cancelled", s_url_remote_file, s_local_file));
        }

        @Override
        public void onPostExecute(Exception exception) {
            if (exception != null) {
                state = AsyncGetGpkgBundleFileTaskHandlerState.DONE_WITH_EXCEPTION;
                this.exception = exception;
                Log.d(TAG, "onPostExecute: exception during download: " + this.exception.getMessage());
                return;
            }
            state = AsyncGetGpkgBundleFileTaskHandlerState.DONE;
            Log.d(TAG,String.format("onPostExecute - download of %s to %s complete", s_url_remote_file, s_local_file));
        }
    }

    private final class GpkgBundlePropertiesDownloadQueueObserver extends HTTP.AsyncGet.ExecutorService.DownloadQueueObserver {
        protected String getTag() {
            return GpkgBundlePropertiesDownloadQueueObserver.class.getSimpleName();
        }

        @Override
        public void onDownloadQueued(HTTP.AsyncGet.HttpUrl_To_Local_File httpUrl_to_local_file, int n_pending) {
            super.onDownloadQueued(httpUrl_to_local_file, n_pending);
            OnInstallationStateChanged(E_INSTALLATION_STATE.IN_PROGRESS);
        }

        @Override
        public void onAllDownloadsFinished() {
            int
                n_succeeded = getSuccessfulDownloads().size(),
                n_failed = getFailedDownloadExceptions().size();
            Log.d(getTag(), String.format("onAllDownloadsFinished: all queued downloads have finished -%s%s", n_succeeded > 0 ? " " + n_succeeded + " succeeded" : "", n_failed > 0 ? " " + n_failed + " failed (with exceptions) " : " "));

            if (n_failed > 0) {
                if (n_succeeded > 0) {
                    //detect partial installations
                    HashMap<String, File>
                            partially_installed_gpkg_bundles = new HashMap<>();
                    for (HTTP.AsyncGet.CallableTaskException callableTaskException : getFailedDownloadExceptions()) {
                        File f_local_file__failed_download = callableTaskException.get_httpUrl_to_local_file().get_file();
                        File dir_gpkg_bundle__failed_download = f_local_file__failed_download.getParentFile();
                        boolean partially_installed = false;
                        if (!partially_installed_gpkg_bundles.containsKey(dir_gpkg_bundle__failed_download.getName())) {
                            Log.d(getTag(), String.format("onAllDownloadsFinished: failed download of %s - running partial-installation detection routine for associated geopackage-bundle \"%s\"...", callableTaskException.get_httpUrl_to_local_file().get_url().toString(), dir_gpkg_bundle__failed_download.getName()));
                            for (HTTP.AsyncGet.HttpUrl_To_Local_File httpUrl_to_local_file : getSuccessfulDownloads()) {
                                File f_local_file__successful_download = httpUrl_to_local_file.get_file();
                                File dir_gpkg_bundle__successful_download = f_local_file__successful_download.getParentFile();
                                try {
                                    if (dir_gpkg_bundle__failed_download.getCanonicalPath().compareTo(dir_gpkg_bundle__successful_download.getCanonicalPath()) == 0) {
                                        Log.d(getTag(), String.format("onAllDownloadsFinished: partial-installation of geopackage-bundle \"%s\" detected!", dir_gpkg_bundle__failed_download.getName()));
                                        partially_installed_gpkg_bundles.put(dir_gpkg_bundle__failed_download.getName(), dir_gpkg_bundle__failed_download);
                                        partially_installed = true;
                                        break;
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        } else
                            partially_installed = true;
                        if (!partially_installed) {
                            Log.d(getTag(), String.format("onAllDownloadsFinished: geopackage-bundle \"%s\" was not only partially installed (already existed)", dir_gpkg_bundle__failed_download.getName()));
                        }
                    }
                    for (String s_partially_installed_gpkg_bundle : partially_installed_gpkg_bundles.keySet()) {
                        Log.d(getTag(), String.format("onAllDownloadsFinished: removing partially installed geopackage: %s...", s_partially_installed_gpkg_bundle));
                        File dir_partially_installed_gpkg_bundle = partially_installed_gpkg_bundles.get(s_partially_installed_gpkg_bundle);
                        try {
                            Files.delete(dir_partially_installed_gpkg_bundle);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                StringBuilder sb_alert_msg = new StringBuilder();
                String title, alert_msg;
                int result_code = INSTALL_GPKG_BUNDLE_RESULT__SUCCESSFUL;
                title = "GeoPackage-Bundle Installation FAILED!";
                for (HTTP.AsyncGet.CallableTaskException callableTaskException : getFailedDownloadExceptions()) {
                    Exception exception = (Exception)callableTaskException.getCause();
                    if (exception instanceof HTTP.AsyncGet.RemoteFileInvalidParameterException) {
                        HTTP.AsyncGet.RemoteFileInvalidParameterException typed_exception = (HTTP.AsyncGet.RemoteFileInvalidParameterException) exception;
                        sb_alert_msg.append(typed_exception.getMessage());
                    } else if (exception instanceof HTTP.AsyncGet.RemoteFile_SizeException) {
                        HTTP.AsyncGet.RemoteFile_SizeException typed_exception = (HTTP.AsyncGet.RemoteFile_SizeException) exception;
                        sb_alert_msg.append(String.format("Failed to retrieve file size of %s - %s", typed_exception.get_httpurl().uri().toString(), typed_exception.getMessage()));
                    } else if (exception instanceof HTTP.AsyncGet.StageHandlerOnChunkRead_LocalFileAlreadyExistsException) {
                        HTTP.AsyncGet.StageHandlerOnChunkRead_LocalFileAlreadyExistsException typed_exception = (HTTP.AsyncGet.StageHandlerOnChunkRead_LocalFileAlreadyExistsException)exception;
                        sb_alert_msg.append(String.format("Local geopackage-bundle file %s/%s already exists!\n\n", m_edt_local_gpkg_bundle__name.getText().toString(), typed_exception.get_httpUrl_to_local_file().get_file().getName()));
                        sb_alert_msg.append(String.format("If you wish to reinstall or update local geopackage-bundle \"%s\", please uninstall it first!", m_edt_local_gpkg_bundle__name.getText().toString()));
                    } else {
                        sb_alert_msg.append(String.format("Exception: %s", exception.getMessage()));
                    }
                    sb_alert_msg.append("\n\n\n\n");
                }
                alert_msg = sb_alert_msg.toString();
                result_code = INSTALL_GPKG_BUNDLE_RESULT__FAILED;
                final int final_result_code = result_code;
                runOnUiThread(
                    () -> {
                        AlertDialog alertDialog = new AlertDialog.Builder(InstallGpkgBundleActivity.this).create();
                        alertDialog.setTitle(title);
                        alertDialog.setMessage(alert_msg);
                        alertDialog.setButton(
                            AlertDialog.BUTTON_NEUTRAL,
                            "OK",
                            (dialog, which) -> {
                                dialog.dismiss();
                                InstallGpkgBundleActivity.this.setResult(final_result_code);
                                InstallGpkgBundleActivity.this.finish();
                            }
                        );
                        alertDialog.show();
                    }
                );
            } else {//then all tasks were successfully executed - i.e. all canonical "bootstrap" files were successfully downloaded, these provide a "spec" for downloading all files that comprise this geopackage bundle
                int index = -1;
                Iterator<HTTP.AsyncGet.HttpUrl_To_Local_File> httpUrl_to_local_fileIterator = getSuccessfulDownloads().iterator();
                while (httpUrl_to_local_fileIterator.hasNext()) {
                    index++;
                    HTTP.AsyncGet.HttpUrl_To_Local_File httpUrl_to_local_file = httpUrl_to_local_fileIterator.next();
                    File file = httpUrl_to_local_file.get_file();
                    HttpUrl httpUrl = httpUrl_to_local_file.get_url();
                    String s_remote_gpkg_bundle = httpUrl.url().toString().substring(0, httpUrl.url().toString().lastIndexOf("/"));

                    //we expect one and only one ".properties" download task!
                    if (index == 0) {
                        //now parse/process ".properties" and proceed to download gpkg-bundle components based on its TOML_FILE and GPKG_FILES values
                        File
                            f_gpkg_bundle = new File(file.getParentFile().getPath()),
                            f_gpkg_bundle_ver_props = file;

                        //process version.properties file for this gpk-bundle
                        FileInputStream f_inputstream_gpkg_bundle_ver_props = null;
                        String s_prop_val = "";
                        final Bundle bundle = new Bundle();

                        try {
                            //build gpkg-bundle toml file-download spec from version.props and queue it up
                            s_prop_val = Files.getPropsFileProperty(f_gpkg_bundle_ver_props, Constants.Strings.GPKG_BUNDLE.VERSION_PROPS.PROP.TOML_FILE);
                            Log.d(getTag(), String.format("onAllDownloadsFinished: version prope (file %s): %s = \"%s\"", f_gpkg_bundle_ver_props.getCanonicalPath(), Constants.Strings.GPKG_BUNDLE.VERSION_PROPS.PROP.TOML_FILE, s_prop_val));
                            String
                                s_toml_file_remote = s_prop_val,
                                s_toml_file_local = s_toml_file_remote;
                            if (HTTP.isValidUrl(s_toml_file_remote)) {//then retrieve only last part for local file name
                                Log.d(getTag(), String.format("onAllDownloadsFinished: \t\t%s:\"%s\" is a valid uri", Constants.Strings.GPKG_BUNDLE.VERSION_PROPS.PROP.TOML_FILE, s_toml_file_remote));
                                s_toml_file_local = s_toml_file_remote.substring(s_toml_file_remote.lastIndexOf("/") + 1);
                            } else {
                                Log.d(getTag(), String.format("onAllDownloadsFinished: \t\t%s:\"%s\" is NOT a valid uri", Constants.Strings.GPKG_BUNDLE.VERSION_PROPS.PROP.TOML_FILE, s_toml_file_remote));
                                s_toml_file_remote = s_remote_gpkg_bundle + "/" + s_toml_file_remote;
                            }
                            Log.d(getTag(), String.format("onAllDownloadsFinished: \t\tlocal: \"%s\"; remote: \"%s\"", s_toml_file_local, s_toml_file_remote));
                            final File f_gpkg_bundle__toml = new File(f_gpkg_bundle.getPath(), s_toml_file_local);
                            if (f_gpkg_bundle__toml.exists())
                                f_gpkg_bundle__toml.delete();

                            m_gpkg_bundle_file_downloader.submit(
                                new HTTP.AsyncGet.CallableTask(
                                    new HTTP.AsyncGet.HttpUrl_To_Local_File(HttpUrl.parse(s_toml_file_remote), f_gpkg_bundle__toml),
                                    new AsyncGetGpkgBundleFileTaskStageHandler()
                                )
                            );

                            //build gpkg-bundle geopcackage file-download specs from version.props and queue them up
                            bundle.putBoolean("DOWNLOAD", true);
                            s_prop_val = Files.getPropsFileProperty(f_gpkg_bundle_ver_props, Constants.Strings.GPKG_BUNDLE.VERSION_PROPS.PROP.GPKG_FILES);
                            Log.d(getTag(), String.format("onAllDownloadsFinished: version props (file %s): %s = \"%s\"", f_gpkg_bundle_ver_props.getCanonicalPath(), Constants.Strings.GPKG_BUNDLE.VERSION_PROPS.PROP.GPKG_FILES, s_prop_val));
                            String[] s_list_gpkg_files = s_prop_val.split(",");
                            if (s_list_gpkg_files != null && s_list_gpkg_files.length > 0) {
                                for (int i = 0; i < s_list_gpkg_files.length; i++) {
                                    String
                                        s_gpkg_file_remote = s_list_gpkg_files[i],
                                        s_gpkg_file_local = s_gpkg_file_remote;
                                    Log.d(getTag(), String.format("onAllDownloadsFinished: \t%s[%d]=\"%s\"", Constants.Strings.GPKG_BUNDLE.VERSION_PROPS.PROP.GPKG_FILES, i, s_gpkg_file_remote));
                                    if (HTTP.isValidUrl(s_gpkg_file_remote)) {//then retrieve only last part for local file name
                                        Log.d(getTag(), String.format("onAllDownloadsFinished: \t\t%s[%d]:\"%s\" is a valid uri", Constants.Strings.GPKG_BUNDLE.VERSION_PROPS.PROP.GPKG_FILES, i, s_gpkg_file_remote));
                                        s_gpkg_file_local = s_gpkg_file_remote.substring(s_gpkg_file_remote.lastIndexOf("/") + 1);
                                    } else {
                                        Log.w(getTag(), String.format("onAllDownloadsFinished: \t\t%s[%d]:\"%s\" is NOT a valid uri", Constants.Strings.GPKG_BUNDLE.VERSION_PROPS.PROP.GPKG_FILES, i, s_gpkg_file_remote));
                                        s_gpkg_file_remote = s_remote_gpkg_bundle + "/" + s_gpkg_file_remote;
                                    }
                                    Log.d(getTag(), String.format("onAllDownloadsFinished: \t\tlocal: \"%s\"; remote: \"%s\"", s_gpkg_file_local, s_gpkg_file_remote));
                                    final File f_gpkg_bundle__gpkg = new File(f_gpkg_bundle.getPath(), s_gpkg_file_local);
                                    if (f_gpkg_bundle__gpkg.exists())
                                        f_gpkg_bundle__gpkg.delete();

                                    m_gpkg_bundle_file_downloader.submit(
                                        new HTTP.AsyncGet.CallableTask(
                                            new HTTP.AsyncGet.HttpUrl_To_Local_File(HttpUrl.parse(s_gpkg_file_remote), f_gpkg_bundle__gpkg),
                                            new AsyncGetGpkgBundleFileTaskStageHandler()
                                        )
                                    );
                                }
                            } else
                                throw new FileNotFoundException(String.format("failed to retrieve list of geopackage files from gpkg-bundle version.properties file %s", f_gpkg_bundle_ver_props.getCanonicalPath()));
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                            runOnUiThread(
                                () -> {
                                    AlertDialog alertDialog = new AlertDialog.Builder(InstallGpkgBundleActivity.this).create();
                                    StringBuilder sb_alert_msg = new StringBuilder();
                                    alertDialog.setTitle("GeoPackage-Bundle Installation FAILED!");
                                    sb_alert_msg.append(e.getMessage() + "\n\n");
                                    sb_alert_msg.append("Please notify developer.\n\n");
                                    sb_alert_msg.append("GeoPackage-Bundle installation cannot continue.");
                                    alertDialog.setMessage(sb_alert_msg.toString());
                                    alertDialog.setButton(
                                        AlertDialog.BUTTON_NEUTRAL,
                                        "OK",
                                        (dialog, which) -> {
                                            dialog.dismiss();
                                            InstallGpkgBundleActivity.this.setResult(INSTALL_GPKG_BUNDLE_RESULT__FAILED);
                                            InstallGpkgBundleActivity.this.finish();
                                        }
                                    );
                                    alertDialog.show();
                                }
                            );
                        } catch (IOException e) {
                            e.printStackTrace();
                            runOnUiThread(
                                () -> {
                                    AlertDialog alertDialog = new AlertDialog.Builder(InstallGpkgBundleActivity.this).create();
                                    StringBuilder sb_alert_msg = new StringBuilder();
                                    alertDialog.setTitle("GeoPackage-Bundle Installation FAILED!");
                                    sb_alert_msg.append(e.getMessage() + "\n\n");
                                    sb_alert_msg.append("Please notify developer.\n\n");
                                    sb_alert_msg.append("GeoPackage-Bundle installation cannot continue.");
                                    alertDialog.setMessage(sb_alert_msg.toString());
                                    alertDialog.setButton(
                                        AlertDialog.BUTTON_NEUTRAL,
                                        "OK",
                                        (dialog, which) -> {
                                            dialog.dismiss();
                                            InstallGpkgBundleActivity.this.setResult(INSTALL_GPKG_BUNDLE_RESULT__FAILED);
                                            InstallGpkgBundleActivity.this.finish();
                                        }
                                    );
                                    alertDialog.show();
                                }
                            );
                        }
//                        catch (HTTP.AsyncGet.TaskExecuteQueueException e) {
//                            e.printStackTrace();
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    AlertDialog alertDialog = alertDialog = new AlertDialog.Builder(InstallGpkgBundleActivity.this).create();
//                                    StringBuilder sb_alert_msg = new StringBuilder();
//                                    alertDialog.setTitle("GeoPackage-Bundle Installation FAILED!");
//                                    sb_alert_msg.append(e.getMessage() + "\n\n");
//                                    sb_alert_msg.append("Please notify developer.\n\n");
//                                    sb_alert_msg.append("GeoPackage-Bundle installation cannot continue.");
//                                    alertDialog.setMessage(sb_alert_msg.toString());
//                                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
//                                            new DialogInterface.OnClickListener() {
//                                                public void onClick(DialogInterface dialog, int which) {
//                                                    dialog.dismiss();
//                                                    InstallGpkgBundleActivity.this.setResult(INSTALL_GPKG_BUNDLE_RESULT__FAILED);
//                                                    InstallGpkgBundleActivity.this.finish();
//                                                }
//                                            });
//                                    alertDialog.show();
//                                }
//                            });
//                        }
                        finally {
                            if (f_inputstream_gpkg_bundle_ver_props != null) {
                                try {
                                    f_inputstream_gpkg_bundle_ver_props.close();
                                } catch (IOException ioexception) {
                                    ioexception.printStackTrace();
                                }
                            }
                        }
                    } else {//gpkg-bundle installation-bootstrap spec has more than one download task!
                        runOnUiThread(
                            () -> {
                                AlertDialog alertDialog = new AlertDialog.Builder(InstallGpkgBundleActivity.this).create();
                                StringBuilder sb_alert_msg = new StringBuilder();
                                alertDialog.setTitle("GeoPackage-Bundle Installation FAILED!");
                                sb_alert_msg.append(
                                    String.format(
                                        "Installation of geopckage-bundle %s must be bootstrapped by first downloading its %s file, but you have an encountered a bug wherein source-code is attempting to download %s first!\n\n",
                                            s_remote_gpkg_bundle,
                                            Constants.Strings.GPKG_BUNDLE.VERSION_PROPS.FNAME,
                                            httpUrl.url().toString()
                                    )
                                );
                                sb_alert_msg.append("Please notify developer.\n\n");
                                sb_alert_msg.append("GeoPackage-Bundle installation cannot continue.");
                                alertDialog.setMessage(sb_alert_msg.toString());
                                alertDialog.setButton(
                                    AlertDialog.BUTTON_NEUTRAL,
                                    "OK",
                                    (dialog, which) -> {
                                        dialog.dismiss();
                                        InstallGpkgBundleActivity.this.setResult(INSTALL_GPKG_BUNDLE_RESULT__FAILED);
                                        InstallGpkgBundleActivity.this.finish();
                                    }
                                );
                                alertDialog.show();
                            }
                        );
                    }
                }
            }

            super.onAllDownloadsFinished();
        }
    }
    private final HTTP.AsyncGet.ExecutorService m_gpkg_bundle_download_boostrapper = new HTTP.AsyncGet.ExecutorService(
        (runnable, threadPoolExecutor) -> {
            Log.d(threadPoolExecutor.getClass().getCanonicalName(), String.format("rejectedExecution - %s", runnable.getClass().getCanonicalName()));
        },
        new GpkgBundlePropertiesDownloadQueueObserver()
    );

    private final class GpkgBundleFileDownloadQueueObserver extends HTTP.AsyncGet.ExecutorService.DownloadQueueObserver {
        @Override
        protected String getTag() {
            return GpkgBundleFileDownloadQueueObserver.class.getSimpleName();
        }

        @Override
        public void onDownloadQueued(HTTP.AsyncGet.HttpUrl_To_Local_File httpUrl_to_local_file, int n_pending) {
            super.onDownloadQueued(httpUrl_to_local_file, n_pending);
            OnInstallationStateChanged(E_INSTALLATION_STATE.IN_PROGRESS);
        }

        @Override
        public void onAllDownloadsFinished() {
            int
                n_succeeded = getSuccessfulDownloads().size(),
                n_failed = getFailedDownloadExceptions().size();
            Log.d(getTag(), String.format("onAllDownloadsFinished: all queued downloads have finished -%s%s", n_succeeded > 0 ? " " + n_succeeded + " succeeded" : "", n_failed > 0 ? " " + n_failed + " failed (with exceptions) " : " "));

            try {
                StringBuilder sb_alert_msg = new StringBuilder();
                String title, alert_msg;
                int result_code = INSTALL_GPKG_BUNDLE_RESULT__SUCCESSFUL;
                if (n_failed > 0) {
                    title = "GeoPackage-Bundle Installation FAILED!";
                    for (HTTP.AsyncGet.CallableTaskException callableTaskException : getFailedDownloadExceptions()) {
                        Exception exception = (Exception)callableTaskException.getCause();
                        if (exception instanceof HTTP.AsyncGet.RemoteFileInvalidParameterException) {
                            HTTP.AsyncGet.RemoteFileInvalidParameterException typed_exception = (HTTP.AsyncGet.RemoteFileInvalidParameterException)exception;
                            sb_alert_msg.append(typed_exception.getMessage());
                        } else if (exception instanceof HTTP.AsyncGet.RemoteFile_SizeException) {
                            HTTP.AsyncGet.RemoteFile_SizeException typed_exception = (HTTP.AsyncGet.RemoteFile_SizeException) exception;
                            sb_alert_msg.append(String.format("Failed to retrieve file size of %s - %s", typed_exception.get_httpurl().uri().toString(), typed_exception.getMessage()));
                        } else if (exception instanceof HTTP.AsyncGet.StageHandlerOnChunkRead_LocalFileAlreadyExistsException) {
                            sb_alert_msg.append("Local geopackage-bundle file ");
                            HTTP.AsyncGet.StageHandlerOnChunkRead_LocalFileAlreadyExistsException typed_exception = (HTTP.AsyncGet.StageHandlerOnChunkRead_LocalFileAlreadyExistsException)exception;
                            sb_alert_msg.append(String.format(File.separator + m_edt_local_gpkg_bundle__name.getText().toString() + File.separator + typed_exception.get_httpUrl_to_local_file().get_file().getName()));
                            sb_alert_msg.append(" already exists!\n\n");
                            sb_alert_msg.append(String.format("If you wish to reinstall or update local geopackage-bundle \"%s\", please uninstall it first!", m_edt_local_gpkg_bundle__name.getText().toString()));
                        } else
                            sb_alert_msg.append(String.format("Exception: %s", exception.getMessage()));
                        sb_alert_msg.append("\n\n\n\n");
                    }
                    alert_msg = sb_alert_msg.toString();
                    result_code = INSTALL_GPKG_BUNDLE_RESULT__FAILED;
                } else {
                    title = "GeoPackage-Bundle Installation SUCCESSFUL!";
                    for (HTTP.AsyncGet.HttpUrl_To_Local_File httpUrl_to_local_file : getSuccessfulDownloads()) {
                        File file = httpUrl_to_local_file.get_file();
                        sb_alert_msg
                            .append("Geopackage-bundle file ")
                            .append(httpUrl_to_local_file.get_url().toString())
                            .append(" has been successfully installed to: \n\n")
                            .append("\t" + File.separator + m_edt_local_gpkg_bundle__name.getText().toString() + File.separator + file.getName())
                            .append("\n\n\n");
                    }
                    alert_msg = sb_alert_msg.toString();
                    result_code = INSTALL_GPKG_BUNDLE_RESULT__SUCCESSFUL;
                }
                final int final_result_code = result_code;
                runOnUiThread(
                    () -> {
                        AlertDialog alertDialog = new AlertDialog.Builder(InstallGpkgBundleActivity.this).create();
                        alertDialog.setTitle(title);
                        alertDialog.setMessage(alert_msg);
                        alertDialog.setButton(
                            AlertDialog.BUTTON_NEUTRAL,
                            "OK",
                            (dialog, which) -> {
                                dialog.dismiss();
                                InstallGpkgBundleActivity.this.setResult(final_result_code);
                                InstallGpkgBundleActivity.this.finish();
                            }
                        );
                        alertDialog.show();
                    }
                );
            } finally {
                OnInstallationStateChanged(E_INSTALLATION_STATE.IDLE);
                super.onAllDownloadsFinished();
            }
        }
    }

    private final HTTP.AsyncGet.ExecutorService m_gpkg_bundle_file_downloader = new HTTP.AsyncGet.ExecutorService(
        (runnable, threadPoolExecutor) -> {
            Log.d(threadPoolExecutor.getClass().getCanonicalName(), String.format("rejectedExecution - %s", runnable.getClass().getCanonicalName()));
        },
        new GpkgBundleFileDownloadQueueObserver()
    );
}
