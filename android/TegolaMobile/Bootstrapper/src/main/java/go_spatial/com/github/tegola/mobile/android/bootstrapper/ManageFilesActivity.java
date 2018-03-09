package go_spatial.com.github.tegola.mobile.android.bootstrapper;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

import go_spatial.com.github.tegola.mobile.android.controller.Constants;

public class ManageFilesActivity extends AppCompatActivity {
    private ImageButton m_btn_manage__geopackage_bundles__close = null;
    private Button m_btn_gpkg_bundle__install = null;
    private ListView m_lv_gpk_bundles__installed = null;
    private final ArrayList<String> m_lv_gpkg_bundles__installed__items = new ArrayList<String>();
    private ArrayAdapter<String> m_lv_gpk_bundles__installed__dataadapter = null;
    private TextView m_tv_gpkg_bundles__none_installed = null;


    private final View.OnClickListener OnClickListener__m_btn_manage__geopackage_bundles__close = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ManageFilesActivity.this.finish();
        }
    };

    private final View.OnClickListener OnClickListener__m_btn_gpkg_bundle__install = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ManageFilesActivity.this.startActivityForResult(new Intent(ManageFilesActivity.this, InstallGpkgActivity.class), 0);
        }
    };

    //user selects a local config file selection from spinner - synchronizes selection with shared prefs
    private final AdapterView.OnItemClickListener OnItemClickListener__m_lv_gpk_bundles__installed = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_files);

        //map UI objects to UI resources
        m_btn_manage__geopackage_bundles__close = (ImageButton)findViewById(R.id.btn_manage__geopackage_bundles__close);
        m_btn_gpkg_bundle__install = (Button)findViewById(R.id.btn_gpkg_bundle__install);
        m_lv_gpk_bundles__installed = (ListView)findViewById(R.id.lv_gpkg_bundles__installed);
        m_tv_gpkg_bundles__none_installed = (TextView)findViewById(R.id.tv_gpkg_bundles__none_installed);

        //set up associated UI objects auxiliary objects if any - e.g. TAGs and data adapters
        m_lv_gpk_bundles__installed__dataadapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, m_lv_gpkg_bundles__installed__items);
        m_lv_gpk_bundles__installed.setAdapter(m_lv_gpk_bundles__installed__dataadapter);

        //associate listeners for user-UI-interaction and exec misc control init
        m_btn_manage__geopackage_bundles__close.setOnClickListener(OnClickListener__m_btn_manage__geopackage_bundles__close);
        m_btn_gpkg_bundle__install.setOnClickListener(OnClickListener__m_btn_gpkg_bundle__install);
        m_lv_gpk_bundles__installed.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        m_lv_gpk_bundles__installed.setItemsCanFocus(false);
        m_lv_gpk_bundles__installed.setOnItemClickListener(OnItemClickListener__m_lv_gpk_bundles__installed);
        m_lv_gpk_bundles__installed.setVisibility(View.GONE);
        m_tv_gpkg_bundles__none_installed.setVisibility(View.GONE);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        new Handler().postDelayed(new RefeshList_Runnable(), 50);
    }

    private class RefeshList_Runnable implements Runnable {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        //remove current entries from m_lv_gpk_bundles__installed__dataadapter
                        m_lv_gpkg_bundles__installed__items.clear();

                        //add installed geopackage bundles (directories) to m_lv_gpk_bundles__installed__dataadapter
                        File f_gpkg_root_dir = new File(getPackageManager().getPackageInfo(getPackageName(), 0).applicationInfo.dataDir + File.separator + Constants.Strings.GPKG_BUNDLE_SUBDIR);
                        File[] f_gpkg_root_dir_files = f_gpkg_root_dir.listFiles();
                        for (File f_gpkg_root_dir_file : f_gpkg_root_dir_files) {
                            if (f_gpkg_root_dir_file.isDirectory()) {
                                m_lv_gpkg_bundles__installed__items.add(f_gpkg_root_dir_file.getName());
                            }
                        }

                        m_lv_gpk_bundles__installed.setVisibility(m_lv_gpkg_bundles__installed__items.isEmpty() ? View.GONE : View.VISIBLE);
                        m_tv_gpkg_bundles__none_installed.setVisibility(m_lv_gpkg_bundles__installed__items.isEmpty() ? View.VISIBLE : View.GONE);
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}
