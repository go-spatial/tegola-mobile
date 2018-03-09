package go_spatial.com.github.tegola.mobile.android.bootstrapper;

import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import okhttp3.Request;
import okio.Buffer;

public class InstallGpkgActivity extends AppCompatActivity {
    private final static String TAG = Utils.Files.class.getName();

    private ImageButton m_btn_gpkg_bundle__close = null;
    private TextView m_tv_lbl_repo_container_url__http_proto_prefix = null;
    private EditText m_edt_gpkg_bundle__repo_container_url__root = null;
    private TextView m_tv_lbl_gpkg_bundle__repo_container_url__root___canon_tail = null;
    private EditText m_edt_gpkg_bundle__name = null;
    private Button m_btn_gpkg_bundle__install = null;
    private ProgressBar m_pb_toml = null;
    private ProgressBar m_pb_gpkg = null;

    private class AsyncGetGpkgBundleFileHandler extends Utils.HTTP.AsyncGetFile.Handler {
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
                        firstUpdate = false;
                        Utils.HTTP.HttpUrl_To_Local_File httpUrl_to_local_file = get_httpUrl_to_local_file();
                        File file = httpUrl_to_local_file.get_file();
                        if (file.exists()) {
                            throw new Utils.HTTP.AsyncGetFileAlreadyExistsException(file.getPath());
                        } else {
                            if (!file.getParentFile().exists())
                                file.getParentFile().mkdirs();
                            file.createNewFile();
                            f_outputstream = new FileOutputStream(file);
                        }
                    }

                    //write bytes to outputstream
                    sink.copyTo(f_outputstream);
                    sink.flush();
                    f_outputstream.flush();

                    total_bytes_read += bytesRead;

                    //now update progress
                    if (contentLength > 0) {
                        //System.out.println("content-length: unknown");
                    } else {
                        //System.out.format("content-length: %d\n", contentLength);
                        m_pb.setMax(100);
                        m_pb.setProgress((int)(total_bytes_read/contentLength) * 100);
                    }
                } else {//done
                    f_outputstream.close();

                    m_pb.setMax(100);
                    m_pb.setProgress(100);
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
        }
    }

    private final Utils.HTTP.AsyncGetFile m_asyncgetfile_toml = new Utils.HTTP.AsyncGetFile(new AsyncGetGpkgBundleFileHandler(m_pb_toml));
    private final Utils.HTTP.AsyncGetFile m_asyncgetfile_gpkg = new Utils.HTTP.AsyncGetFile(new AsyncGetGpkgBundleFileHandler(m_pb_gpkg));

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
                .append(File.pathSeparator)
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
                String gpkg_bundle_name = m_edt_gpkg_bundle__name.getText().toString();

                //toml
                String gpkg_bundle_fname = getString(R.string.canon_fname__toml);
                String s_gpkg_bundle_remote_file_url = build_gpkg_bundle_remote_file_url_string(gpkg_bundle_fname);
                Log.d(TAG, "Starting download of remote '" + gpkg_bundle_name + "' gpkg bundle toml file: " + s_gpkg_bundle_remote_file_url);
                m_asyncgetfile_toml.execute(new Utils.HTTP.HttpUrl_To_Local_File(
                                new Request.Builder().url(s_gpkg_bundle_remote_file_url).build().url()
                                , new File(build_gpkg_bundle_local_path_string(), gpkg_bundle_fname)
                        )
                );

                //gpkg
                gpkg_bundle_fname = getString(R.string.canon_fname__gpkg);
                s_gpkg_bundle_remote_file_url = build_gpkg_bundle_remote_file_url_string(gpkg_bundle_fname);
                Log.d(TAG, "Starting download of remote '" + gpkg_bundle_name + "' gpkg bundle gpkg file: " + s_gpkg_bundle_remote_file_url);
                m_asyncgetfile_gpkg.execute(new Utils.HTTP.HttpUrl_To_Local_File(
                                new Request.Builder().url(s_gpkg_bundle_remote_file_url).build().url()
                                , new File(build_gpkg_bundle_local_path_string(), gpkg_bundle_fname)
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
    }
}
