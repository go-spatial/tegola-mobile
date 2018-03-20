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

import go_spatial.com.github.tegola.mobile.android.controller.Utils;
import okhttp3.HttpUrl;
import okio.Buffer;

public class InstallGpkgActivity extends AppCompatActivity {
    private final static String TAG = InstallGpkgActivity.class.getSimpleName();

    private ImageButton m_btn_gpkg_bundle__close = null;
    private TextView m_tv_lbl_repo_container_url__http_proto_prefix = null;
    private EditText m_edt_gpkg_bundle__repo_container_url__root = null;
    private TextView m_tv_lbl_gpkg_bundle__repo_container_url__root___canon_tail = null;
    private EditText m_edt_gpkg_bundle__name = null;
    private Button m_btn_gpkg_bundle__install = null;
    private View m_vw_progress = null;
    private ProgressBar m_pb_toml = null;
    private ProgressBar m_pb_gpkg = null;

    private final int PROG_MAX = 100;
    private final int ACTIVITY_RESULT__INSTALLATION_CANCELLED = -1;
    private final int ACTIVITY_RESULT__INSTALLATION_SUCCESSFUL = 0;

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
        public void onChunkRead(Buffer sink, long bytesRead, long contentLength, boolean done) {
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
                        Log.d(TAG, "onChunkRead: download " + get_httpUrl_to_local_file().get_url().toString() + " to " + get_httpUrl_to_local_file().get_file().getPath() + " in progress");
                        if (get_httpUrl_to_local_file().get_file().exists()) {
                            throw new Utils.HTTP.AsyncGetFileAlreadyExistsException(get_httpUrl_to_local_file().get_file().getPath());
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
                    if (f_outputstream__local_file != null)
                        f_outputstream__local_file.close();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            m_pb.setMax(PROG_MAX);
                            m_pb.setProgress(PROG_MAX);
                        }
                    });
                }
            } catch (Utils.HTTP.AsyncGetFileAlreadyExistsException e) {
                onCancelled(e);
                return;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFileAlreadyExists() {
            //for now we just cancel
        }

        @Override
        public void onCancelled(Exception exception) {
            state = AsyncGetGpkgBundleFileTaskHandlerState.CANCELLED;
            this.exception = exception;
        }

        @Override
        public void onPostExecute(Exception exception) {
            if (exception != null) {
                state = AsyncGetGpkgBundleFileTaskHandlerState.DONE_WITH_EXCEPTION;
                this.exception = exception;
                Log.d(TAG, "onPostExecute: exception during download: " + get_exception().getMessage());
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
            public void onCancelled() {
            }

            @Override
            public void onPostExecute() {
                Iterator<Utils.HTTP.AsyncGetFileTaskExecuteQueueItem> iterator_exec_queue_items = m_asyncgetgpkgbundlefileexecutequeue.iterator();
                if (iterator_exec_queue_items.hasNext()) {
                    AlertDialog alertDialog = new AlertDialog.Builder(InstallGpkgActivity.this).create();
                    alertDialog.setTitle("Success!");
                    StringBuilder sb_msg = new StringBuilder();
                    File[] f_gpkg_files = null;
                    while (iterator_exec_queue_items.hasNext()) {
                        Utils.HTTP.AsyncGetFileTaskExecuteQueueItem exec_queue_item = iterator_exec_queue_items.next();
                        Utils.HTTP.HttpUrl_To_Local_File httpUrl_to_local_file = exec_queue_item.get_httpUrl_to_local_file();
                        File file = httpUrl_to_local_file.get_file();
                        sb_msg
                                .append("Remote geopackage file ")
                                .append(httpUrl_to_local_file.get_url().toString())
                                .append(" has been successfully installed to: ")
                                .append(file.getParentFile().getPath())
                                .append("\n\n");
                        if (f_gpkg_files == null)
                            f_gpkg_files = file.getParentFile().listFiles();
                    }
                    if (f_gpkg_files.length > 0) {
                        sb_msg.append("\n\n");
                        sb_msg.append("Files:\n");
                        for (File f_gpkg_file : f_gpkg_files) {
                            if (!f_gpkg_file.isDirectory()) {
                                sb_msg.append("\t" + f_gpkg_file.getName());
                                sb_msg.append("\n");
                            }
                        }
                    }
                    alertDialog.setMessage(sb_msg.toString());
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    InstallGpkgActivity.this.setResult(ACTIVITY_RESULT__INSTALLATION_SUCCESSFUL);
                                    InstallGpkgActivity.this.finish();
                                }
                            });
                    alertDialog.show();
                }
            }
        }
    );

    private String build_gpkg_bundle_remote_file_url_string(String s_gremote_gpkg_fname) {
        StringBuilder sb_gpkg_bundle_remote_container_url = new StringBuilder()
                .append(m_tv_lbl_repo_container_url__http_proto_prefix.getText())
                .append(m_edt_gpkg_bundle__repo_container_url__root.getText())
                .append(m_tv_lbl_gpkg_bundle__repo_container_url__root___canon_tail.getText())
                .append(m_edt_gpkg_bundle__name.getText());

        return new StringBuilder(sb_gpkg_bundle_remote_container_url)
                .append("/")
                .append(s_gremote_gpkg_fname)
                .toString();
    }

    private String build_gpkg_bundle_local_path_string() throws PackageManager.NameNotFoundException {
        return new StringBuilder()
                .append(Utils.Files.F_GPKG_DIR.getInstance(InstallGpkgActivity.this).getPath())
                .append(File.separator)
                .append(m_edt_gpkg_bundle__name.getText())
                .toString();
    }

    private final View.OnClickListener OnClickListener__m_btn_install__geopackage_bundle__close = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            InstallGpkgActivity.this.setResult(ACTIVITY_RESULT__INSTALLATION_CANCELLED);
            InstallGpkgActivity.this.finish();
        }
    };

    private final View.OnClickListener OnClickListener__m_btn_gpkg_bundle__install = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            m_vw_progress.setVisibility(View.VISIBLE);

            try {
                String gpkg_bundle_local_dir = build_gpkg_bundle_local_path_string();
                Log.d(TAG, "OnClickListener__m_btn_gpkg_bundle__install.onClick: gpkg_bundle_local_dir==" + gpkg_bundle_local_dir);

                //toml
                HttpUrl httpurl_toml = HttpUrl.parse(build_gpkg_bundle_remote_file_url_string(getString(R.string.canon_fname__toml)));
                Log.d(TAG, "OnClickListener__m_btn_gpkg_bundle__install.onClick: httpurl_toml ==" + httpurl_toml.toString());
                File localfile_toml = new File(gpkg_bundle_local_dir, getString(R.string.canon_fname__toml));
                Log.d(TAG, "OnClickListener__m_btn_gpkg_bundle__install.onClick: localfile_toml path ==" + localfile_toml.getPath());
                m_asyncgetgpkgbundlefileexecutequeue.add(
                        new Utils.HTTP.AsyncGetFileTaskExecuteQueueItem(
                                new Utils.HTTP.AsyncGetFileTaskExecuteQueueItemExecutor(
                                        new AsyncGetGpkgBundleFileTaskStageHandler(m_pb_toml)
                                        , m_asyncgetgpkgbundlefileexecutequeue
                                )
                                , new Utils.HTTP.HttpUrl_To_Local_File(httpurl_toml, localfile_toml))
                );

                //gpkg
                HttpUrl httpurl_gpkg = HttpUrl.parse(build_gpkg_bundle_remote_file_url_string(getString(R.string.canon_fname__gpkg)));
                Log.d(TAG, "OnClickListener__m_btn_gpkg_bundle__install.onClick: httpurl_gpkg ==" + httpurl_gpkg.toString());
                File localfile_gpkg = new File(gpkg_bundle_local_dir, getString(R.string.canon_fname__gpkg));
                Log.d(TAG, "OnClickListener__m_btn_gpkg_bundle__install.onClick: localfile_gpkg path ==" + localfile_gpkg.getPath());
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

    private void validate__m_edt_gpkg_bundle__name() {
        m_btn_gpkg_bundle__install.setEnabled(true);
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(m_edt_gpkg_bundle__name.getWindowToken(), 0);
    }

    //reaction to changing remote config URL value - user must press enter in editor or switch focus to another control to register to app that a pending change has occurred
    private final TextView.OnEditorActionListener OnEditorActionListener__m_edt_gpkg_bundle__name = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_NULL) {
                validate__m_edt_gpkg_bundle__name();
                return true;
            } else
                return false;
        }
    };
    private final TextView.OnFocusChangeListener OnFocusChangeListener__m_edt_gpkg_bundle__name = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (!hasFocus)
                validate__m_edt_gpkg_bundle__name();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_install_gpkg);

        m_btn_gpkg_bundle__close = (ImageButton)findViewById(R.id.btn_install__geopackage_bundle__close);
        m_tv_lbl_repo_container_url__http_proto_prefix = (TextView)findViewById(R.id.tv_lbl_repo_container_url__http_proto_prefix);
        m_edt_gpkg_bundle__repo_container_url__root = (EditText)findViewById(R.id.edt_gpkg_bundle__repo_container_url__root);
        m_tv_lbl_gpkg_bundle__repo_container_url__root___canon_tail = (TextView)findViewById(R.id.tv_lbl_gpkg_bundle__repo_container_url__root___canon_tail);
        m_edt_gpkg_bundle__name = (EditText)findViewById(R.id.edt_gpkg_bundle__name);
        m_edt_gpkg_bundle__name.setOnEditorActionListener(OnEditorActionListener__m_edt_gpkg_bundle__name);
        m_edt_gpkg_bundle__name.setOnFocusChangeListener(OnFocusChangeListener__m_edt_gpkg_bundle__name);
        m_btn_gpkg_bundle__install = (Button)findViewById(R.id.btn_gpkg_bundle__install);
        m_vw_progress = findViewById(R.id.sect_content__item__install_gpkg_bundle__progress);
        m_pb_toml = (ProgressBar)findViewById(R.id.pb_toml);
        m_pb_gpkg = (ProgressBar)findViewById(R.id.pb_gpkg);

        m_btn_gpkg_bundle__close.setOnClickListener(OnClickListener__m_btn_install__geopackage_bundle__close);
        m_btn_gpkg_bundle__install.setOnClickListener(OnClickListener__m_btn_gpkg_bundle__install);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        m_edt_gpkg_bundle__repo_container_url__root.setText(getString(R.string.gpkg_bundle__root_url__default));

        m_btn_gpkg_bundle__install.setEnabled(false);

        m_pb_toml.setMax(PROG_MAX);
        m_pb_gpkg.setMax(PROG_MAX);
        m_vw_progress.setVisibility(View.GONE);
    }
}
