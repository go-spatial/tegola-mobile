package go_spatial.com.github.tegola.mobile.android.bootstrapper;

import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

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
    private ProgressBar m_pb_toml = null;
    private ProgressBar m_pb_gpkg = null;

    final int PROG_MAX = 100;

    private class AsyncGetGpkgBundleFileHandler extends Utils.HTTP.AsyncGetFile.Handler {
        private final String TAG = AsyncGetGpkgBundleFileHandler.class.getSimpleName();
        private boolean firstUpdate = true;
        private long total_bytes_read = 0;
        private FileOutputStream f_outputstream = null;
        private final ProgressBar m_pb;

        public AsyncGetGpkgBundleFileHandler(@NonNull final ProgressBar pb) {
            m_pb = pb;
        }

        @Override
        public void onPreExecute() {
        }

        @Override
        public void onChunkRead(Buffer sink, long bytesRead, long contentLength, boolean done) {
            try {
                if (!done) {
                    if (firstUpdate) {
                        Log.d(TAG, "onChunkRead: firstUpdate==true!");
                        firstUpdate = false;
                        Utils.HTTP.HttpUrl_To_Local_File httpUrl_to_local_file = get_httpUrl_to_local_file();
                        File file = httpUrl_to_local_file.get_file();
                        if (file.exists()) {
                            throw new Utils.HTTP.AsyncGetFileAlreadyExistsException(file.getPath());
                        } else {
                            Log.d(TAG, "onChunkRead: file " + file.getPath() + " does not exist");
                            if (!file.getParentFile().exists()) {
                                Log.d(TAG, "onChunkRead: file " + file.getParentFile() + " does not exist; creating...");
                                file.getParentFile().mkdirs();
                            }
                            boolean created_file = file.createNewFile();
                            Log.d(TAG, "onChunkRead: " + (created_file ? "Succcessfully created" : "Failed to create") + " file " + file.getPath());
                            f_outputstream = new FileOutputStream(file);
                        }
                    }

                    //write bytes to outputstream
                    //Log.d(TAG, "onChunkRead: sink.size()==" + sink.size());
                    sink.copyTo(f_outputstream);
                    sink.flush();
                    f_outputstream.flush();

                    total_bytes_read += bytesRead;
                    //Log.d(TAG, "onChunkRead: total_bytes_read==" + total_bytes_read);

                    //now update progress
                    //Log.d(TAG, "onChunkRead: contentLength==" + contentLength);
                    if (contentLength > 0) {
                        final double dbl_progress = ((double)total_bytes_read*1.0)/((double)contentLength*1.0);
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
                        //Log.d(TAG, "onChunkRead: cannot update progress bar since contentLength==" + contentLength);
                    }
                } else {//done
                    if (f_outputstream != null)
                        f_outputstream.close();
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
        }

        @Override
        public void onPostExecute(Exception exception) {
            Utils.HTTP.HttpUrl_To_Local_File httpUrl_to_local_file = get_httpUrl_to_local_file();
            File file = httpUrl_to_local_file.get_file();

            AlertDialog alertDialog = new AlertDialog.Builder(InstallGpkgActivity.this).create();
            alertDialog.setTitle("Success!");
            StringBuilder sb_msg = new StringBuilder()
                    .append("Remote geopackage file ")
                    .append(httpUrl_to_local_file.get_url().toString())
                    .append(" has been successfully installed to: ")
                    .append(file.getParentFile().getPath())
                    .append("\n\n")
                    .append("Files:\n");
            File[] f_gpkg_files = file.getParentFile().listFiles();
            for (File f_gpkg_file : f_gpkg_files) {
                if (!f_gpkg_file.isDirectory()) {
                    sb_msg.append("\t" + f_gpkg_file.getName());
                    sb_msg.append("\n");
                }
            }
            alertDialog.setMessage(sb_msg.toString());
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
        }
    }

    private Utils.HTTP.AsyncGetFile m_asyncgetfile_toml = null;
    private Utils.HTTP.AsyncGetFile m_asyncgetfile_gpkg = null;

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
            InstallGpkgActivity.this.setResult(0);
            InstallGpkgActivity.this.finish();
        }
    };

    private final View.OnClickListener OnClickListener__m_btn_gpkg_bundle__install = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            try {
                String gpkg_bundle_local_dir = build_gpkg_bundle_local_path_string();
                Log.d(TAG, "OnClickListener__m_btn_gpkg_bundle__install.onClick: gpkg_bundle_local_dir==" + gpkg_bundle_local_dir);

                //toml
                HttpUrl httpurl_toml = HttpUrl.parse(build_gpkg_bundle_remote_file_url_string(getString(R.string.canon_fname__toml)));
                Log.d(TAG, "OnClickListener__m_btn_gpkg_bundle__install.onClick: httpurl_toml ==" + httpurl_toml.toString());
                File localfile_toml = new File(gpkg_bundle_local_dir, getString(R.string.canon_fname__toml));
                Log.d(TAG, "OnClickListener__m_btn_gpkg_bundle__install.onClick: localfile_toml path ==" + localfile_toml.getPath());
                m_asyncgetfile_toml.execute(new Utils.HTTP.HttpUrl_To_Local_File(
                                httpurl_toml
                                , localfile_toml
                        )
                );

                //gpkg
                HttpUrl httpurl_gpkg = HttpUrl.parse(build_gpkg_bundle_remote_file_url_string(getString(R.string.canon_fname__gpkg)));
                Log.d(TAG, "OnClickListener__m_btn_gpkg_bundle__install.onClick: httpurl_gpkg ==" + httpurl_gpkg.toString());
                File localfile_gpkg = new File(gpkg_bundle_local_dir, getString(R.string.canon_fname__gpkg));
                Log.d(TAG, "OnClickListener__m_btn_gpkg_bundle__install.onClick: localfile_gpkg path ==" + localfile_gpkg.getPath());
                m_asyncgetfile_gpkg.execute(new Utils.HTTP.HttpUrl_To_Local_File(
                                httpurl_gpkg
                                , localfile_gpkg
                        )
                );
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
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
        m_btn_gpkg_bundle__install = (Button)findViewById(R.id.btn_gpkg_bundle__install);
        m_pb_toml = (ProgressBar)findViewById(R.id.pb_toml);
        m_pb_gpkg = (ProgressBar)findViewById(R.id.pb_gpkg);

        m_btn_gpkg_bundle__close.setOnClickListener(OnClickListener__m_btn_install__geopackage_bundle__close);
        m_btn_gpkg_bundle__install.setOnClickListener(OnClickListener__m_btn_gpkg_bundle__install);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        m_edt_gpkg_bundle__repo_container_url__root.setText(getString(R.string.gpkg_bundle__root_url__default));
        m_pb_toml.setMax(PROG_MAX);
        m_pb_gpkg.setMax(PROG_MAX);

        m_asyncgetfile_toml = new Utils.HTTP.AsyncGetFile(new AsyncGetGpkgBundleFileHandler(m_pb_toml));
        m_asyncgetfile_gpkg = new Utils.HTTP.AsyncGetFile(new AsyncGetGpkgBundleFileHandler(m_pb_gpkg));
    }
}
