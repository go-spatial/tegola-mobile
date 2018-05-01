package go_spatial.com.github.tegola.mobile.android.ux;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ScrollView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ConfigFileEditorActivity extends AppCompatActivity {
    private static final String TAG = ConfigFileEditorActivity.class.getName();

    private MenuItem m_mnu_item__cfg_file_editor__edit = null;
    private MenuItem m_mnu_item__cfg_file_editor__commit_changes = null;
    private MenuItem m_mnu_item__cfg_file_editor__revert_changes = null;
    private MenuItem m_mnu_item__cfg_file_editor__close = null;
    private ScrollView m_scvw_config_file_contents = null;
    private CustomEditText m_editor = null;

    private int m_result = RESULT_CANCELED;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_file_editor);

        m_scvw_config_file_contents = (ScrollView)findViewById(R.id.scvw_config_file_contents);
        m_editor = (CustomEditText)findViewById(R.id.edt_config_file_contents);
        m_editor.setBackgroundDrawable(null);
        set_editable(false);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    load_config_file_contents();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 50);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_configfileeditor, menu);
        m_mnu_item__cfg_file_editor__edit = menu.findItem(R.id.menu_item__cfg_file_editor__edit);
        m_mnu_item__cfg_file_editor__commit_changes = menu.findItem(R.id.menu_item__cfg_file_editor__commit_changes);
        m_mnu_item__cfg_file_editor__revert_changes = menu.findItem(R.id.menu_item__cfg_file_editor__revert_changes);
        m_mnu_item__cfg_file_editor__close = menu.findItem(R.id.menu_item__cfg_file_editor__close);

        m_mnu_item__cfg_file_editor__commit_changes.setVisible(false);
        m_mnu_item__cfg_file_editor__revert_changes.setVisible(false);
        return true;
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    private void set_editable(final boolean isEditable) {
        m_editor.setTextIsSelectable(true);
        m_editor.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (isEditable) {
                    m_mnu_item__cfg_file_editor__commit_changes.setVisible(true);
                    m_mnu_item__cfg_file_editor__revert_changes.setVisible(true);
                    return false;
                } else
                    return true;
            }
        });
        m_editor.setEnabled(isEditable);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item__cfg_file_editor__edit: {
                set_editable(true);
                m_mnu_item__cfg_file_editor__edit.setVisible(false);
                return true;
            }
            case R.id.menu_item__cfg_file_editor__commit_changes: {
                try {
                    save_config_file_contents();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            }
            case R.id.menu_item__cfg_file_editor__revert_changes: {
                try {
                    load_config_file_contents();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            }
            case R.id.menu_item__cfg_file_editor__close: {
                //clean-up, set appropriate result value
                setResult(compute_result());
                finish();
                return true;
            }
            default: return(super.onOptionsItemSelected(item));
        }
    }

    private void load_config_file_contents() throws FileNotFoundException, IOException {
        final String filename = getIntent().getStringExtra(Constants.Strings.EDITOR_INTENT_EXTRAS.FILENAME);
        m_editor.setText("");
        final int initial_max_lines = 4096;
        m_editor.setMaxLines(initial_max_lines);
        set_editable(false);
        m_mnu_item__cfg_file_editor__commit_changes.setVisible(false);
        m_mnu_item__cfg_file_editor__revert_changes.setVisible(false);
        invalidateOptionsMenu();
        m_result = RESULT_CANCELED;
        Log.d(TAG, "load_config_file_contents: starting working thread...");
        final Thread worker_thread = new Thread(new Runnable() {
            private final String TAG = "load_cfg_file_wrkr_thrd";
            @Override
            public void run() {
                Log.d(TAG, "thread started");
                File f_config_toml = new File(getFilesDir().getPath() + "/" + filename);
                if (!f_config_toml.exists()) {
                    Log.d(TAG, f_config_toml.getPath() + " file not found!");
                    return;
                }
                InputStream inputstream_raw_config_toml = null;
                try {
                    Log.d(TAG, "opening " + f_config_toml.getPath() + " contents...");
                    inputstream_raw_config_toml = openFileInput(filename);
                    Log.d(TAG, f_config_toml.getPath() + "file contents opened");
                    final int n_bytes_available_total = inputstream_raw_config_toml.available();
                    int
                        n_bytes_available = n_bytes_available_total
                        , n_bytes_read = 0
                        , n_byte_read_total = 0
                        , n_line_count = 0
                        , n_editor_new_line_count_limit = initial_max_lines;
                    Log.d(TAG, "loading " + f_config_toml.getPath() + " contents into editor -- " + n_bytes_available + " bytes available");
                    BufferedReader cfg_file_reader = new BufferedReader(new InputStreamReader(inputstream_raw_config_toml));
                    String s_line = null;
                    while ((s_line = cfg_file_reader.readLine()) != null) {
                        n_byte_read_total += n_bytes_read = (n_bytes_available_total - inputstream_raw_config_toml.available());
                        n_line_count += 1;
//                        Log.d(TAG, "read line " + n_line_count + ": " + n_bytes_read + " bytes out of " + n_bytes_available + " bytes available from " + f_config_toml.getPath());
                        n_bytes_available -= n_bytes_read;
                        n_editor_new_line_count_limit += 1;
                        final int final_n_editor_new_line_count_limit = n_editor_new_line_count_limit;
                        final String final_s_line = s_line;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
//                                Log.d(TAG, "resizing editor's max line count to: " + final_n_editor_new_line_count_limit);
                                m_editor.setMaxLines(final_n_editor_new_line_count_limit);
//                                Log.d(TAG, "appending curent line to editor");
                                m_editor.append(final_s_line + "\n");
                            }
                        });
                    }
                    if (n_byte_read_total > 0) {
                        Log.d(TAG, "loaded " + n_byte_read_total + " bytes out of " + n_bytes_available_total + " total bytes available from " + f_config_toml.getPath() + " contents into " + n_line_count + " lines in editor");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                StringBuilder sb_title = new StringBuilder();
                                sb_title.append(getString(R.string.activity_title__config_file_viewer) + " - " + filename);
                                setTitle(sb_title.toString());
                                m_scvw_config_file_contents.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        m_scvw_config_file_contents.fullScroll(View.FOCUS_UP);
                                    }
                                }, 100);
                            }
                        });
                    }
                    if (cfg_file_reader != null)
                        cfg_file_reader.close();
                    inputstream_raw_config_toml.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    return;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "done - exiting thread");
            }
        });
        worker_thread.start();
    }

    private void save_config_file_contents() throws FileNotFoundException, IOException {
        //TODO: write implementation!
        m_result = RESULT_OK;
    }

    private int compute_result() {
        return m_result;
    }
}
