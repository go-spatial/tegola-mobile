package go_spatial.com.github.tegola.mobile.android.bootstrapper;

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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Set;

import go_spatial.com.github.tegola.mobile.android.controller.Utils;
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
    private ProgressBar m_pb_toml = null;
    private ProgressBar m_pb_gpkg = null;

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

    private class AsyncGetGpkgBundleFileTaskStageHandler extends Utils.HTTP.AsyncGetFileTaskStageHandler {
        private final String TAG = AsyncGetGpkgBundleFileTaskStageHandler.class.getSimpleName();
        private boolean firstUpdate = true;
        private long content_length = 0;
        private long total_bytes_read = 0;
        private String s_url_remote_file = "";
        private FileOutputStream f_outputstream__local_file = null;
        private final ProgressBar m_pb;

        private AsyncGetGpkgBundleFileTaskHandlerState state = AsyncGetGpkgBundleFileTaskHandlerState.NOT_STARTED;
        public AsyncGetGpkgBundleFileTaskHandlerState get_state() {
            return state;
        }

        private Exception exception = null;
        public Exception get_exception() {
            return exception;
        }

        public AsyncGetGpkgBundleFileTaskStageHandler(@NonNull final ProgressBar pb) {
            m_pb = pb;
        }

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
                            Log.d(TAG, "onChunkRead: local file " + get_httpUrl_to_local_file().get_file().getPath() + " already exists");
                            throw new Utils.HTTP.AsyncGetFileTask_StageHandlerOnChunkRead_LocalFileAlreadyExistsException(get_httpUrl_to_local_file());
                        } else {
                            Log.d(TAG, "onChunkRead: local file " + get_httpUrl_to_local_file().get_file().getPath() + " does not exist");
                            if (!get_httpUrl_to_local_file().get_file().getParentFile().exists()) {
                                Log.d(TAG, "onChunkRead: local file directory " + get_httpUrl_to_local_file().get_file().getParentFile() + " does not exist; creating...");
                                get_httpUrl_to_local_file().get_file().getParentFile().mkdirs();
                            }
                            boolean created_file = get_httpUrl_to_local_file().get_file().createNewFile();
                            Log.d(TAG, "onChunkRead: " + (created_file ? "Succcessfully created" : "Failed to create") + " new local file " + get_httpUrl_to_local_file().get_file().getPath() + "; opening outputstream");
                            f_outputstream__local_file = new FileOutputStream(get_httpUrl_to_local_file().get_file());
                        }
                        Log.d(TAG, "onChunkRead: downloading/writing " + get_httpUrl_to_local_file().get_url().toString() + " outputstream to " + get_httpUrl_to_local_file().get_file().getPath() + "...");
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
                            }
                        });
                    } else {
                        Log.d(TAG, "onChunkRead: cannot update interim progress for " + get_httpUrl_to_local_file().get_url().toString() + " to " + get_httpUrl_to_local_file().get_file().getPath() + " download since content_length==" + content_length);
                    }
                } else {//done
                    Log.d(TAG, "onChunkRead: done; wrote: " + total_bytes_read + " bytes to " + get_httpUrl_to_local_file().get_file().getPath());
                    if (f_outputstream__local_file != null) {
                        Log.d(TAG, "onChunkRead: Closing fileoutputstream for " + get_httpUrl_to_local_file().get_file().getPath());
                        f_outputstream__local_file.close();
                    } else {
                        Log.d(TAG, "onChunkRead: Cannot close null fileoutputstream for " + get_httpUrl_to_local_file().get_file().getPath());
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            m_pb.setMax(PROG_MAX);
                            m_pb.setProgress(PROG_MAX);
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
                Iterator<Utils.HTTP.AsyncGetFileTaskExecuteQueueItem> iterator_exec_queue_items = m_asyncgetgpkgbundlefileexecutequeue.iterator();
                if (iterator_exec_queue_items.hasNext()) {
                    AlertDialog alertDialog = new AlertDialog.Builder(InstallGpkgBundleActivity.this).create();
                    StringBuilder sb_alert_msg = new StringBuilder();
                    if (item_excutor_exception_map != null && item_excutor_exception_map.size() > 0) {
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
                    } else {
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
                }
            }
        }
    );

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

    private String build_local_gpkg_bundle_path_string() throws PackageManager.NameNotFoundException {
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

                //toml
                HttpUrl httpurl_toml = HttpUrl.parse(build_remote_gpkg_bundle_file_url_string(getString(R.string.canon_fname__toml)));
                Log.d(TAG, "OnClickListener__m_btn_install_remote_gpkg_bundle.onClick: httpurl_toml ==" + httpurl_toml.toString());
                File localfile_toml = new File(local_gpkg_bundle_path, getString(R.string.canon_fname__toml));
                Log.d(TAG, "OnClickListener__m_btn_install_remote_gpkg_bundle.onClick: localfile_toml path ==" + localfile_toml.getPath());
                m_asyncgetgpkgbundlefileexecutequeue.add(
                        new Utils.HTTP.AsyncGetFileTaskExecuteQueueItem(
                                new Utils.HTTP.AsyncGetFileTaskExecuteQueueItemExecutor(
                                        new AsyncGetGpkgBundleFileTaskStageHandler(m_pb_toml)
                                        , m_asyncgetgpkgbundlefileexecutequeue
                                )
                                , new Utils.HTTP.HttpUrl_To_Local_File(httpurl_toml, localfile_toml))
                );

                //gpkg
                HttpUrl httpurl_gpkg = HttpUrl.parse(build_remote_gpkg_bundle_file_url_string(getString(R.string.canon_fname__gpkg)));
                Log.d(TAG, "OnClickListener__m_btn_install_remote_gpkg_bundle.onClick: httpurl_gpkg ==" + httpurl_gpkg.toString());
                File localfile_gpkg = new File(local_gpkg_bundle_path, getString(R.string.canon_fname__gpkg));
                Log.d(TAG, "OnClickListener__m_btn_install_remote_gpkg_bundle.onClick: localfile_gpkg path ==" + localfile_gpkg.getPath());
                m_asyncgetgpkgbundlefileexecutequeue.add(
                        new Utils.HTTP.AsyncGetFileTaskExecuteQueueItem(
                                new Utils.HTTP.AsyncGetFileTaskExecuteQueueItemExecutor(
                                        new AsyncGetGpkgBundleFileTaskStageHandler(m_pb_gpkg)
                                        , m_asyncgetgpkgbundlefileexecutequeue
                                )
                                , new Utils.HTTP.HttpUrl_To_Local_File(httpurl_gpkg, localfile_gpkg))
                );

                m_asyncgetgpkgbundlefileexecutequeue.execute();
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            } catch (Utils.HTTP.AsyncGetFileTaskExecuteQueueException e) {
                e.printStackTrace();
            }
        }
    };

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
        m_pb_toml = (ProgressBar)findViewById(R.id.pb_toml);
        m_pb_gpkg = (ProgressBar)findViewById(R.id.pb_gpkg);

        m_btn_install_remote_gpkg_bundle__cancel.setOnClickListener(OnClickListener__m_btn_install_remote_gpkg_bundle__cancel);
        m_btn_install_remote_gpkg_bundle.setOnClickListener(OnClickListener__m_btn_install_remote_gpkg_bundle);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        m_edt_remote_gpkg_bundle__root_url__base.setText(getString(R.string.gpkg_bundle__root_url__default));

        m_btn_install_remote_gpkg_bundle.setEnabled(false);

        m_pb_toml.setMax(PROG_MAX);
        m_pb_gpkg.setMax(PROG_MAX);
        m_vw_progress.setVisibility(View.GONE);
    }
}
