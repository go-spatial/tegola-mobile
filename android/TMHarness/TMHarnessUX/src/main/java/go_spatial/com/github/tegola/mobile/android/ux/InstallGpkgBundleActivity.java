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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

import go_spatial.com.github.tegola.mobile.android.controller.*;
import go_spatial.com.github.tegola.mobile.android.controller.Constants;
import okhttp3.HttpUrl;
import okio.Buffer;

public class InstallGpkgBundleActivity extends AppCompatActivity {
    private final static String TAG = InstallGpkgBundleActivity.class.getSimpleName();

    private ImageButton m_btn_install_remote_gpkg_bundle__cancel = null;
    private TextView m_tv_lbl_remote_gpkg_bundle__root_url__http_proto_prefix = null;
    private EditText m_edt_remote_gpkg_bundle__root_url__base = null;
    private TextView m_tv_lbl_remote_gpkg_bundle__root_url___canon_tail = null;
    private EditText m_edt_remote_gpkg_bundle__name = null;
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
        NOT_STARTED
        , STARTING
        , IN_PROGRESS
        , CANCELLED
        , DONE_WITH_EXCEPTION
        , DONE
        ;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_install_gpkg_bundle);

        m_btn_install_remote_gpkg_bundle__cancel = (ImageButton)findViewById(R.id.btn_install_remote_geopackage_bundle__cancel);
        m_tv_lbl_remote_gpkg_bundle__root_url__http_proto_prefix = (TextView)findViewById(R.id.tv_lbl_remote_gpkg_bundle__root_url__http_proto_prefix);
        m_edt_remote_gpkg_bundle__root_url__base = (EditText)findViewById(R.id.edt_remote_gpkg_bundle__root_url__base);
        m_tv_lbl_remote_gpkg_bundle__root_url___canon_tail = (TextView)findViewById(R.id.tv_lbl_remote_gpkg_bundle__root_url___canon_tail);
        m_edt_remote_gpkg_bundle__name = (EditText)findViewById(R.id.edt_remote_gpkg_bundle__name);
        m_edt_remote_gpkg_bundle__name.setOnEditorActionListener(OnEditorActionListener__m_edt_remote_gpkg_bundle__name);
        m_edt_remote_gpkg_bundle__name.setOnFocusChangeListener(OnFocusChangeListener__m_edt_remote_gpkg_bundle__name);
        m_edt_local_gpkg_bundle__name = (EditText)findViewById(R.id.edt_local_gpkg_bundle__name);
        m_edt_local_gpkg_bundle__name.setOnEditorActionListener(OnEditorActionListener__m_edt_local_gpkg_bundle__name);
        m_edt_local_gpkg_bundle__name.setOnFocusChangeListener(OnFocusChangeListener__m_edt_local_gpkg_bundle__name);
        m_btn_install_remote_gpkg_bundle = (Button)findViewById(R.id.btn_install_remote_gpkg_bundle);
        m_vw_progress = findViewById(R.id.sect_content__item__install_remote_gpkg_bundle__progress);
        m_tv_val__install_gpkg_bundle__file_download_progress = (TextView)findViewById(R.id.tv_val__install_gpkg_bundle__file_download_progress);
        m_pb = (ProgressBar)findViewById(R.id.pb);
        m_tv_val__install_gpkg_bundle__file_download_progress__bytes = (TextView)findViewById(R.id.tv_val__install_gpkg_bundle__file_download_progress__bytes);

        m_btn_install_remote_gpkg_bundle__cancel.setOnClickListener(OnClickListener__m_btn_install_remote_gpkg_bundle__cancel);
        m_btn_install_remote_gpkg_bundle.setOnClickListener(OnClickListener__m_btn_install_remote_gpkg_bundle);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        m_edt_remote_gpkg_bundle__root_url__base.setText(getString(R.string.gpkg_bundle__root_url__default));

        m_edt_local_gpkg_bundle__name.setEnabled(false);
        m_btn_install_remote_gpkg_bundle.setEnabled(false);

        m_pb.setMax(PROG_MAX);
        m_vw_progress.setVisibility(View.GONE);
    }


    private String build_remote_gpkg_bundle_root_url_string() {
        return new StringBuilder()
                .append(m_tv_lbl_remote_gpkg_bundle__root_url__http_proto_prefix.getText())
                .append(m_edt_remote_gpkg_bundle__root_url__base.getText())
                .append(m_tv_lbl_remote_gpkg_bundle__root_url___canon_tail.getText())
                .append("/")
                .append(m_edt_remote_gpkg_bundle__name.getText())
                .toString();
    }

    private String build_remote_gpkg_bundle_file_url_string(String s_gremote_gpkg_fname) {
        return new StringBuilder(build_remote_gpkg_bundle_root_url_string())
                .append("/")
                .append(s_gremote_gpkg_fname)
                .toString();
    }

    private String build_local_gpkg_bundle_path_string() throws PackageManager.NameNotFoundException, IOException {
        return new StringBuilder()
                .append(Utils.Files.F_GPKG_DIR.getInstance(InstallGpkgBundleActivity.this.getApplicationContext()).getPath())
                .append(File.separator)
                .append(m_edt_local_gpkg_bundle__name.getText())
                .toString();
    }

    private final View.OnClickListener OnClickListener__m_btn_install_remote_gpkg_bundle__cancel = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            InstallGpkgBundleActivity.this.setResult(INSTALL_GPKG_BUNDLE_RESULT__CANCELLED);
            InstallGpkgBundleActivity.this.finish();
        }
    };

    private void check_enable_install_button() {
        String s_remote_gpkg_bundle__name = m_edt_remote_gpkg_bundle__name.getText().toString().trim();
        String s_local_gpkg_bundle__name = m_edt_local_gpkg_bundle__name.getText().toString().trim();
        m_btn_install_remote_gpkg_bundle.setEnabled(!(s_remote_gpkg_bundle__name.isEmpty() || s_local_gpkg_bundle__name.isEmpty()));
    }

    //reaction to changing remote gpkg bundle name - user must press enter in editor or switch focus to another control to register to app that a pending change has occurred
    private final TextView.OnEditorActionListener OnEditorActionListener__m_edt_remote_gpkg_bundle__name = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_NULL) {
                m_edt_local_gpkg_bundle__name.setText(m_edt_remote_gpkg_bundle__name.getText());
                check_enable_install_button();
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(m_edt_remote_gpkg_bundle__name.getWindowToken(), 0);
                return true;
            } else
                return false;
        }
    };
    private final TextView.OnFocusChangeListener OnFocusChangeListener__m_edt_remote_gpkg_bundle__name = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (!hasFocus) {
                m_edt_local_gpkg_bundle__name.setText(m_edt_remote_gpkg_bundle__name.getText());
                check_enable_install_button();
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(m_edt_remote_gpkg_bundle__name.getWindowToken(), 0);
            }
        }
    };

    //reaction to changing local gpkg bundle name - user must press enter in editor or switch focus to another control to register to app that a pending change has occurred
    private final TextView.OnEditorActionListener OnEditorActionListener__m_edt_local_gpkg_bundle__name = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_NULL) {
                check_enable_install_button();
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(m_edt_local_gpkg_bundle__name.getWindowToken(), 0);
                return true;
            } else
                return false;
        }
    };
    private final TextView.OnFocusChangeListener OnFocusChangeListener__m_edt_local_gpkg_bundle__name = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (!hasFocus) {
                check_enable_install_button();
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(m_edt_local_gpkg_bundle__name.getWindowToken(), 0);
            }
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
                HttpUrl httpurl_version_props = HttpUrl.parse(build_remote_gpkg_bundle_file_url_string(Constants.Strings.GPKG_BUNDLE_VERSION_PROPS__FNAME));
                Log.d(TAG, "OnClickListener__m_btn_install_remote_gpkg_bundle.onClick: GPKG_BUNDLE_VERSION_PROPS__FNAME ==" + httpurl_version_props.toString());
                File localfile_version_props = new File(local_gpkg_bundle_path, Constants.Strings.GPKG_BUNDLE_VERSION_PROPS__FNAME);
                Log.d(TAG, "OnClickListener__m_btn_install_remote_gpkg_bundle.onClick: localfile_version_props path ==" + localfile_version_props.getCanonicalPath());
                m_asyncgetgpkgbundlefiles_bootstrapper.add(
                        new Utils.HTTP.AsyncGetFileTaskExecuteQueueItem(
                                new Utils.HTTP.AsyncGetFileTaskExecuteQueueItemExecutor(
                                        new AsyncGetGpkgBundleFileTaskStageHandler()
                                        , m_asyncgetgpkgbundlefiles_bootstrapper
                                )
                                , new Utils.HTTP.HttpUrl_To_Local_File(httpurl_version_props, localfile_version_props))
                );

                m_asyncgetgpkgbundlefiles_bootstrapper.execute();
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            } catch (Utils.HTTP.AsyncGetFileTaskExecuteQueueException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };


    private class AsyncGetGpkgBundleFileTaskStageHandler extends Utils.HTTP.AsyncGetFileTaskStageHandler {
        private final String TAG = AsyncGetGpkgBundleFileTaskStageHandler.class.getSimpleName();
        private boolean firstUpdate = true;
        private long content_length = 0;
        private long total_bytes_read = 0;
        private String s_url_remote_file = "";
        private FileOutputStream f_outputstream__local_file = null;

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
            state = AsyncGetGpkgBundleFileTaskHandlerState.STARTING;
        }

        @Override
        public void onChunkRead(Buffer sink, long bytesRead, long contentLength, boolean done) throws IOException {
            try {
                if (!done) {
                    if (firstUpdate) {
                        Log.d(TAG, "onChunkRead: firstUpdate==true");
                        firstUpdate = false;
                        state = AsyncGetGpkgBundleFileTaskHandlerState.IN_PROGRESS;
                        if (contentLength < 1) {
                            Log.d(TAG, "onChunkRead: *** WARNING!!! *** - contentLength < 1");
                        }
                        content_length = contentLength;
                        Log.d(TAG, "onChunkRead: content_length==" + content_length);
                        s_url_remote_file = get_httpUrl_to_local_file().get_url().toString();
                        if (get_httpUrl_to_local_file().get_file().exists()) {
                            Log.d(TAG, "onChunkRead: local file " + get_httpUrl_to_local_file().get_file().getCanonicalPath() + " already exists");
                            throw new Utils.HTTP.AsyncGetFileTask_StageHandlerOnChunkRead_LocalFileAlreadyExistsException(get_httpUrl_to_local_file());
                        } else {
                            Log.d(TAG, "onChunkRead: local file " + get_httpUrl_to_local_file().get_file().getCanonicalPath() + " does not exist");
                            if (!get_httpUrl_to_local_file().get_file().getParentFile().exists()) {
                                Log.d(TAG, "onChunkRead: local file directory " + get_httpUrl_to_local_file().get_file().getParentFile().getCanonicalPath() + " does not exist; creating...");
                                get_httpUrl_to_local_file().get_file().getParentFile().mkdirs();
                            }
                            boolean created_file = get_httpUrl_to_local_file().get_file().createNewFile();
                            Log.d(TAG, "onChunkRead: " + (created_file ? "Succcessfully created" : "Failed to create") + " new local file " + get_httpUrl_to_local_file().get_file().getCanonicalPath() + "; opening outputstream");
                            f_outputstream__local_file = new FileOutputStream(get_httpUrl_to_local_file().get_file());
                        }
                        Log.d(TAG, "onChunkRead: downloading/writing " + get_httpUrl_to_local_file().get_url().toString() + " outputstream to " + get_httpUrl_to_local_file().get_file().getCanonicalPath() + "...");
                        m_tv_val__install_gpkg_bundle__file_download_progress.setText(get_httpUrl_to_local_file().get_file().getName());
                    }

                    //write bytes to outputstream
                    //Log.d(TAG, "onChunkRead: sink.size()==" + sink.size());
                    sink.copyTo(f_outputstream__local_file);
                    sink.flush();
                    f_outputstream__local_file.flush();

                    total_bytes_read += bytesRead;
                    //Log.d(TAG, "onChunkRead: total_bytes_read==" + total_bytes_read);

                    //now update progress
                    if (content_length > 0) {
                        final double dbl_progress = ((double)total_bytes_read*1.0)/((double)content_length*1.0);
                        //Log.d(TAG, "onChunkRead: (double) progress ratio: " + dbl_progress);
                        final int i_progress = (int)(dbl_progress * 100.0);
                        //Log.d(TAG, "onChunkRead: updating progress to " + i_progress + "%");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                m_pb.setMax(PROG_MAX);
                                m_pb.setProgress(i_progress);
                                m_tv_val__install_gpkg_bundle__file_download_progress__bytes.setText(total_bytes_read + " of " + content_length + " bytes");
                            }
                        });
                    } else {
                        Log.d(TAG, "onChunkRead: cannot update interim progress for " + get_httpUrl_to_local_file().get_url().toString() + " to " + get_httpUrl_to_local_file().get_file().getCanonicalPath() + " download since content_length==" + content_length);
                    }
                } else {//done
                    Log.d(TAG, "onChunkRead: done; wrote: " + total_bytes_read + " bytes to " + get_httpUrl_to_local_file().get_file().getCanonicalPath());
                    if (f_outputstream__local_file != null) {
                        Log.d(TAG, "onChunkRead: Closing fileoutputstream for " + get_httpUrl_to_local_file().get_file().getCanonicalPath());
                        f_outputstream__local_file.close();
                    } else {
                        Log.d(TAG, "onChunkRead: Cannot close null fileoutputstream for " + get_httpUrl_to_local_file().get_file().getCanonicalPath());
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
            }
            catch (Utils.HTTP.AsyncGetFileTask_StageHandlerOnChunkRead_LocalFileAlreadyExistsException e) {
                this.exception = e;
                throw e;
            }
            catch (FileNotFoundException e) {
                Utils.HTTP.AsyncGetFileTask_StageHandlerOnChunkRead_LocalFileCreateException lfce = new Utils.HTTP.AsyncGetFileTask_StageHandlerOnChunkRead_LocalFileCreateException(get_httpUrl_to_local_file(), e.getMessage());
                this.exception = lfce;
                throw lfce;
            }
            catch (IOException e) {
                Utils.HTTP.AsyncGetFileTask_StageHandlerOnChunkRead_GeneralIOException gioe = new Utils.HTTP.AsyncGetFileTask_StageHandlerOnChunkRead_GeneralIOException(get_httpUrl_to_local_file(), e.getMessage());
                this.exception = gioe;
                throw gioe;
            }
        }

        @Override
        public void onCancelled(Exception exception) {
            if (exception != null)
                this.exception = exception;
            if (this.exception != null) {
                Log.e(TAG, "onCancelled: cancelled with exception: " + this.exception.toString() + ": " + this.exception.getMessage());
            }
            state = AsyncGetGpkgBundleFileTaskHandlerState.CANCELLED;
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
        }
    }

    private final Utils.HTTP.AsyncGetFileTaskExecuteQueue m_asyncgetgpkgbundlefiles_bootstrapper = new Utils.HTTP.AsyncGetFileTaskExecuteQueue(
            new Utils.HTTP.AsyncGetFileTaskExecuteQueueListener() {
                @Override
                public void onItemExecutor_PostExecute(Utils.HTTP.AsyncGetFileTaskExecuteQueueItemExecutor executor) {
                }

                @Override
                public void onItemExecutor_Cancelled(Utils.HTTP.AsyncGetFileTaskExecuteQueueItemExecutor executor) {
                }

                @Override
                public void onCancelled() {
                }

                @Override
                public void onPostExecute(LinkedHashMap<Utils.HTTP.AsyncGetFileTaskExecuteQueueItemExecutor, Exception> item_excutor_exception_map) {
                    AlertDialog alertDialog = null;
                    StringBuilder sb_alert_msg = null;
                    Iterator<Utils.HTTP.AsyncGetFileTaskExecuteQueueItem> iterator_exec_queue_items = m_asyncgetgpkgbundlefiles_bootstrapper.iterator();
                    if (iterator_exec_queue_items.hasNext()) {//validate there is at least one actual task that was placed in the queue
                        if (item_excutor_exception_map != null && item_excutor_exception_map.size() > 0) {//handle any exceptions that occurred, if any - note that if any have occurred this means that not all of the files that were wanted were successfully downloaded!
                            alertDialog = new AlertDialog.Builder(InstallGpkgBundleActivity.this).create();
                            sb_alert_msg = new StringBuilder();
                            alertDialog.setTitle("GeoPackage-Bundle Installation FAILED!");
                            Set<Utils.HTTP.AsyncGetFileTaskExecuteQueueItemExecutor> set_executors = item_excutor_exception_map.keySet();
                            for (Utils.HTTP.AsyncGetFileTaskExecuteQueueItemExecutor executor : set_executors) {
                                Exception exception = item_excutor_exception_map.get(executor);
                                if (exception instanceof Utils.HTTP.AsyncGetFileTask_RemoteFileInvalidParameterException) {
                                    Utils.HTTP.AsyncGetFileTask_RemoteFileInvalidParameterException typed_exception = (Utils.HTTP.AsyncGetFileTask_RemoteFileInvalidParameterException)exception;
                                    sb_alert_msg.append(typed_exception.getMessage());
                                } else if (exception instanceof Utils.HTTP.AsyncGetFileTask_RemoteFile_SizeException) {
                                    Utils.HTTP.AsyncGetFileTask_RemoteFile_SizeException typed_exception = (Utils.HTTP.AsyncGetFileTask_RemoteFile_SizeException)exception;
                                    sb_alert_msg.append("Failed to retrieve file size of " + typed_exception.get_httpurl().uri().toString() + " - " + typed_exception.getMessage());
                                } else if (exception instanceof Utils.HTTP.AsyncGetFileTask_StageHandlerOnChunkRead_LocalFileAlreadyExistsException) {
                                    sb_alert_msg.append("Local geopackage-bundle file ");
                                    Utils.HTTP.AsyncGetFileTask_StageHandlerOnChunkRead_LocalFileAlreadyExistsException typed_exception = (Utils.HTTP.AsyncGetFileTask_StageHandlerOnChunkRead_LocalFileAlreadyExistsException)exception;
                                    sb_alert_msg.append(File.separator + m_edt_local_gpkg_bundle__name.getText().toString() + File.separator + typed_exception.get_httpUrl_to_local_file().get_file().getName());
                                    sb_alert_msg.append(" already exists!\n\n");
                                    sb_alert_msg.append("If you wish to reinstall or update local geopackage-bundle \"" + m_edt_local_gpkg_bundle__name.getText().toString() + "\", please uninstall it first!");
                                } else
                                    sb_alert_msg.append("Exception: " + exception.getMessage());
                                sb_alert_msg.append("\n\n\n\n");
                            }
                            alertDialog.setMessage(sb_alert_msg.toString());
                            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            InstallGpkgBundleActivity.this.setResult(INSTALL_GPKG_BUNDLE_RESULT__FAILED);
                                            InstallGpkgBundleActivity.this.finish();
                                        }
                                    });
                            alertDialog.show();
                        } else {//then all tasks were successfully executed - i.e. all files were successfully downloaded
                            int index = -1;
                            while (iterator_exec_queue_items.hasNext()) {
                                index++;
                                Utils.HTTP.AsyncGetFileTaskExecuteQueueItem exec_queue_item = iterator_exec_queue_items.next();
                                Utils.HTTP.HttpUrl_To_Local_File httpUrl_to_local_file = exec_queue_item.get_httpUrl_to_local_file();
                                File file = httpUrl_to_local_file.get_file();
                                HttpUrl httpUrl = httpUrl_to_local_file.get_url();
                                String s_remote_gpkg_bundle = httpUrl.url().toString().substring(0, httpUrl.url().toString().lastIndexOf("/"));

                                //we expect one and only one version.properties download task!
                                if (index == 0) {
                                    if (file.getName().compareTo(Constants.Strings.GPKG_BUNDLE_VERSION_PROPS__FNAME) != 0) {
                                        alertDialog = new AlertDialog.Builder(InstallGpkgBundleActivity.this).create();
                                        sb_alert_msg = new StringBuilder();
                                        alertDialog.setTitle("GeoPackage-Bundle Installation FAILED!");
                                        sb_alert_msg.append("Installation of geopckage-bundle " + s_remote_gpkg_bundle + " must be bootstrapped by first downloading its " + Constants.Strings.GPKG_BUNDLE_VERSION_PROPS__FNAME
                                                + " file, but you have an encountered a bug wherein source-code is attempting to download " + httpUrl.url().toString() + " first!\n\n");
                                        sb_alert_msg.append("Please notify developer.\n\n");
                                        sb_alert_msg.append("GeoPackage-Bundle installation cannot continue.");
                                        alertDialog.setMessage(sb_alert_msg.toString());
                                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        dialog.dismiss();
                                                        InstallGpkgBundleActivity.this.setResult(INSTALL_GPKG_BUNDLE_RESULT__FAILED);
                                                        InstallGpkgBundleActivity.this.finish();
                                                    }
                                                });
                                        alertDialog.show();
                                    } else {//now process version.properties and proceed to download gpkg-bundle components based on its GPKG_BUNDLE_VERSION_PROPS_PROP_NAME__TOML_FILE and GPKG_BUNDLE_VERSION_PROPS_PROP_NAME__GPKG_FILES values
                                        File
                                                f_gpkg_bundle = new File(file.getParentFile().getPath())
                                                , f_gpkg_bundle_ver_props = file
                                                , f_gpkg_bundle__toml = null
                                                , f_gpkg_bundle__gpkg = null;

                                        //process version.properties file for this gpk-bundle
                                        FileInputStream f_inputstream_gpkg_bundle_ver_props = null;
                                        try {
                                            //build gpkg-bundle toml file-download spec from version.props and queue it up
                                            String s_gpkg_bundle__toml = Utils.getProperty(f_gpkg_bundle_ver_props, Constants.Strings.GPKG_BUNDLE_VERSION_PROPS_PROP_NAME__TOML_FILE);
                                            f_gpkg_bundle__toml = new File(f_gpkg_bundle.getPath(), s_gpkg_bundle__toml);
                                            m_asyncgetgpkgbundlefileexecutequeue.add(
                                                    new Utils.HTTP.AsyncGetFileTaskExecuteQueueItem(
                                                            new Utils.HTTP.AsyncGetFileTaskExecuteQueueItemExecutor(
                                                                    new AsyncGetGpkgBundleFileTaskStageHandler()
                                                                    , m_asyncgetgpkgbundlefileexecutequeue
                                                            )
                                                            , new Utils.HTTP.HttpUrl_To_Local_File(HttpUrl.parse(s_remote_gpkg_bundle + "/" + s_gpkg_bundle__toml), f_gpkg_bundle__toml)
                                                    )
                                            );

                                            //build gpkg-bundle geopcackage file-download specs from version.props and queue them up
                                            String[] s_list_geopcackage_names = Utils.getProperty(f_gpkg_bundle_ver_props, Constants.Strings.GPKG_BUNDLE_VERSION_PROPS_PROP_NAME__GPKG_FILES).split(",");
                                            if (s_list_geopcackage_names != null && s_list_geopcackage_names.length > 0) {
                                                for (int i = 0; i < s_list_geopcackage_names.length; i++) {
                                                    String s_gpkg_name = s_list_geopcackage_names[i];
                                                    f_gpkg_bundle__gpkg = new File(f_gpkg_bundle.getPath(), s_gpkg_name);
                                                    m_asyncgetgpkgbundlefileexecutequeue.add(
                                                            new Utils.HTTP.AsyncGetFileTaskExecuteQueueItem(
                                                                    new Utils.HTTP.AsyncGetFileTaskExecuteQueueItemExecutor(
                                                                            new AsyncGetGpkgBundleFileTaskStageHandler()
                                                                            , m_asyncgetgpkgbundlefileexecutequeue
                                                                    )
                                                                    , new Utils.HTTP.HttpUrl_To_Local_File(HttpUrl.parse(s_remote_gpkg_bundle + "/" + s_gpkg_name), f_gpkg_bundle__gpkg)
                                                            )
                                                    );
                                                }
                                            } else {
                                                throw new FileNotFoundException("failed to retrieve list of geopackage files from gpkg-bundle version.properties file " + f_gpkg_bundle_ver_props.getCanonicalPath());
                                            }

                                            m_asyncgetgpkgbundlefileexecutequeue.execute();
                                        } catch (FileNotFoundException e) {
                                            e.printStackTrace();
                                            alertDialog = new AlertDialog.Builder(InstallGpkgBundleActivity.this).create();
                                            sb_alert_msg = new StringBuilder();
                                            alertDialog.setTitle("GeoPackage-Bundle Installation FAILED!");
                                            sb_alert_msg.append(e.getMessage() + "\n\n");
                                            sb_alert_msg.append("Please notify developer.\n\n");
                                            sb_alert_msg.append("GeoPackage-Bundle installation cannot continue.");
                                            alertDialog.setMessage(sb_alert_msg.toString());
                                            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            dialog.dismiss();
                                                            InstallGpkgBundleActivity.this.setResult(INSTALL_GPKG_BUNDLE_RESULT__FAILED);
                                                            InstallGpkgBundleActivity.this.finish();
                                                        }
                                                    });
                                            alertDialog.show();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                            alertDialog = new AlertDialog.Builder(InstallGpkgBundleActivity.this).create();
                                            sb_alert_msg = new StringBuilder();
                                            alertDialog.setTitle("GeoPackage-Bundle Installation FAILED!");
                                            sb_alert_msg.append(e.getMessage() + "\n\n");
                                            sb_alert_msg.append("Please notify developer.\n\n");
                                            sb_alert_msg.append("GeoPackage-Bundle installation cannot continue.");
                                            alertDialog.setMessage(sb_alert_msg.toString());
                                            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            dialog.dismiss();
                                                            InstallGpkgBundleActivity.this.setResult(INSTALL_GPKG_BUNDLE_RESULT__FAILED);
                                                            InstallGpkgBundleActivity.this.finish();
                                                        }
                                                    });
                                            alertDialog.show();
                                        } catch (Utils.HTTP.AsyncGetFileTaskExecuteQueueException e) {
                                            e.printStackTrace();
                                            alertDialog = new AlertDialog.Builder(InstallGpkgBundleActivity.this).create();
                                            sb_alert_msg = new StringBuilder();
                                            alertDialog.setTitle("GeoPackage-Bundle Installation FAILED!");
                                            sb_alert_msg.append(e.getMessage() + "\n\n");
                                            sb_alert_msg.append("Please notify developer.\n\n");
                                            sb_alert_msg.append("GeoPackage-Bundle installation cannot continue.");
                                            alertDialog.setMessage(sb_alert_msg.toString());
                                            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                                    new DialogInterface.OnClickListener() {
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            dialog.dismiss();
                                                            InstallGpkgBundleActivity.this.setResult(INSTALL_GPKG_BUNDLE_RESULT__FAILED);
                                                            InstallGpkgBundleActivity.this.finish();
                                                        }
                                                    });
                                            alertDialog.show();
                                        } finally {
                                            if (f_inputstream_gpkg_bundle_ver_props != null) {
                                                try {
                                                    f_inputstream_gpkg_bundle_ver_props.close();
                                                } catch (IOException ioexception) {
                                                    ioexception.printStackTrace();
                                                }
                                            }
                                        }
                                    }
                                } else {//gpkg-bundle installation-bootstrap spec has more than one download task!
                                    alertDialog = new AlertDialog.Builder(InstallGpkgBundleActivity.this).create();
                                    sb_alert_msg = new StringBuilder();
                                    alertDialog.setTitle("GeoPackage-Bundle Installation FAILED!");
                                    sb_alert_msg.append("Installation of geopckage-bundle " + s_remote_gpkg_bundle + " must be bootstrapped by first downloading its " + Constants.Strings.GPKG_BUNDLE_VERSION_PROPS__FNAME
                                            + " file, but you have an encountered a bug wherein source-code is attempting to download " + httpUrl.url().toString() + " first!\n\n");
                                    sb_alert_msg.append("Please notify developer.\n\n");
                                    sb_alert_msg.append("GeoPackage-Bundle installation cannot continue.");
                                    alertDialog.setMessage(sb_alert_msg.toString());
                                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                    InstallGpkgBundleActivity.this.setResult(INSTALL_GPKG_BUNDLE_RESULT__FAILED);
                                                    InstallGpkgBundleActivity.this.finish();
                                                }
                                            });
                                    alertDialog.show();
                                }
                            }
                        }
                    } else {//gpkg-bundle installation-bootstrap spec does not have any tasks at all!
                        alertDialog = new AlertDialog.Builder(InstallGpkgBundleActivity.this).create();
                        sb_alert_msg = new StringBuilder();
                        alertDialog.setTitle("GeoPackage-Bundle Installation FAILED!");
                        sb_alert_msg.append("You have an encountered a bug! GeoPackage-Bundle Installation bootstrap-spec does not contain any tasks!\n\n");
                        sb_alert_msg.append("Please notify developer.\n\n");
                        sb_alert_msg.append("GeoPackage-Bundle installation cannot continue.");
                        alertDialog.setMessage(sb_alert_msg.toString());
                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        InstallGpkgBundleActivity.this.setResult(INSTALL_GPKG_BUNDLE_RESULT__FAILED);
                                        InstallGpkgBundleActivity.this.finish();
                                    }
                                });
                        alertDialog.show();
                    }
                }
            }
    );

    private final Utils.HTTP.AsyncGetFileTaskExecuteQueue m_asyncgetgpkgbundlefileexecutequeue = new Utils.HTTP.AsyncGetFileTaskExecuteQueue(
            new Utils.HTTP.AsyncGetFileTaskExecuteQueueListener() {
                @Override
                public void onItemExecutor_PostExecute(Utils.HTTP.AsyncGetFileTaskExecuteQueueItemExecutor executor) {
                }

                @Override
                public void onItemExecutor_Cancelled(Utils.HTTP.AsyncGetFileTaskExecuteQueueItemExecutor executor) {
                }

                @Override
                public void onCancelled() {
                }

                @Override
                public void onPostExecute(final LinkedHashMap<Utils.HTTP.AsyncGetFileTaskExecuteQueueItemExecutor, Exception> item_excutor_exception_map) {
                    AlertDialog alertDialog = new AlertDialog.Builder(InstallGpkgBundleActivity.this).create();
                    StringBuilder sb_alert_msg = new StringBuilder();
                    Iterator<Utils.HTTP.AsyncGetFileTaskExecuteQueueItem> iterator_exec_queue_items = m_asyncgetgpkgbundlefileexecutequeue.iterator();
                    if (iterator_exec_queue_items.hasNext()) {//validate there is at least one actual task that was placed in the queue
                        if (item_excutor_exception_map != null && item_excutor_exception_map.size() > 0) {//handle any exceptions that occurred, if any - note that if any have occurred this means that not all of the files that were wanted to successfully downloaded!
                            alertDialog.setTitle("GeoPackage-Bundle Installation FAILED!");
                            Set<Utils.HTTP.AsyncGetFileTaskExecuteQueueItemExecutor> set_executors = item_excutor_exception_map.keySet();
                            for (Utils.HTTP.AsyncGetFileTaskExecuteQueueItemExecutor executor : set_executors) {
                                Exception exception = item_excutor_exception_map.get(executor);
                                if (exception instanceof Utils.HTTP.AsyncGetFileTask_RemoteFileInvalidParameterException) {
                                    Utils.HTTP.AsyncGetFileTask_RemoteFileInvalidParameterException typed_exception = (Utils.HTTP.AsyncGetFileTask_RemoteFileInvalidParameterException)exception;
                                    sb_alert_msg.append(typed_exception.getMessage());
                                } else if (exception instanceof Utils.HTTP.AsyncGetFileTask_RemoteFile_SizeException) {
                                    Utils.HTTP.AsyncGetFileTask_RemoteFile_SizeException typed_exception = (Utils.HTTP.AsyncGetFileTask_RemoteFile_SizeException)exception;
                                    sb_alert_msg.append("Failed to retrieve file size of " + typed_exception.get_httpurl().uri().toString() + " - " + typed_exception.getMessage());
                                } else if (exception instanceof Utils.HTTP.AsyncGetFileTask_StageHandlerOnChunkRead_LocalFileAlreadyExistsException) {
                                    sb_alert_msg.append("Local geopackage-bundle file ");
                                    Utils.HTTP.AsyncGetFileTask_StageHandlerOnChunkRead_LocalFileAlreadyExistsException typed_exception = (Utils.HTTP.AsyncGetFileTask_StageHandlerOnChunkRead_LocalFileAlreadyExistsException)exception;
                                    sb_alert_msg.append(File.separator + m_edt_local_gpkg_bundle__name.getText().toString() + File.separator + typed_exception.get_httpUrl_to_local_file().get_file().getName());
                                    sb_alert_msg.append(" already exists!\n\n");
                                    sb_alert_msg.append("If you wish to reinstall or update local geopackage-bundle \"" + m_edt_local_gpkg_bundle__name.getText().toString() + "\", please uninstall it first!");
                                } else
                                    sb_alert_msg.append("Exception: " + exception.getMessage());
                                sb_alert_msg.append("\n\n\n\n");
                            }
                            alertDialog.setMessage(sb_alert_msg.toString());
                            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            InstallGpkgBundleActivity.this.setResult(INSTALL_GPKG_BUNDLE_RESULT__FAILED);
                                            InstallGpkgBundleActivity.this.finish();
                                        }
                                    });
                        } else {//then all tasks were successfully executed - i.e. all files were successfully downloaded
                            alertDialog.setTitle("GeoPackage-Bundle Installation SUCCESSFUL!");
                            while (iterator_exec_queue_items.hasNext()) {
                                Utils.HTTP.AsyncGetFileTaskExecuteQueueItem exec_queue_item = iterator_exec_queue_items.next();
                                Utils.HTTP.HttpUrl_To_Local_File httpUrl_to_local_file = exec_queue_item.get_httpUrl_to_local_file();
                                File file = httpUrl_to_local_file.get_file();
                                sb_alert_msg
                                        .append("Remote geopackage-bundle file ")
                                        .append(httpUrl_to_local_file.get_url().toString())
                                        .append(" has been successfully installed to: \n")
                                        .append("\t" + File.separator + m_edt_local_gpkg_bundle__name.getText().toString() + File.separator + file.getName())
                                        .append("\n\n");
                            }
                            alertDialog.setMessage(sb_alert_msg.toString());
                            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            InstallGpkgBundleActivity.this.setResult(INSTALL_GPKG_BUNDLE_RESULT__SUCCESSFUL);
                                            InstallGpkgBundleActivity.this.finish();
                                        }
                                    });
                        }
                        alertDialog.show();
                    } else {
                        alertDialog.setTitle("GeoPackage-Bundle Installation FAILED!");
                        sb_alert_msg.append("You have an encountered a bug! GeoPackage-Bundle Installation bootstrap-spec does not contain any tasks!\n\n");
                        sb_alert_msg.append("Please notify developer.\n\n");
                        sb_alert_msg.append("GeoPackage-Bundle installation cannot continue.");
                        alertDialog.setMessage(sb_alert_msg.toString());
                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        InstallGpkgBundleActivity.this.setResult(INSTALL_GPKG_BUNDLE_RESULT__FAILED);
                                        InstallGpkgBundleActivity.this.finish();
                                    }
                                });
                        alertDialog.show();
                    }
                }
            }
    );
}
