package go_spatial.com.github.tegola.mobile.android.ux;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.DataSetObserver;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.ArraySet;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import go_spatial.com.github.tegola.mobile.android.controller.GPKG;
import go_spatial.com.github.tegola.mobile.android.controller.utils.Files;

public class ManageGpkgBundlesActivity extends AppCompatActivity {
    private final static String TAG = ManageGpkgBundlesActivity.class.getCanonicalName();

    private ImageButton m_btn_manage__geopackage_bundles__close = null;
    private Button m_btn_gpkg_bundle__install = null;
    private ListView m_lv_gpk_bundles__installed = null;
    private final ArrayList<String> m_lv_gpkg_bundles__installed__items = new ArrayList<String>();
    private Long m_hashcode__first__installed__items = null;
    private TextView m_tv_gpkg_bundles__none_installed = null;
    private Button m_btn_gpkg_bundle__uninstall = null;

    public static final int MNG_GPKG_BUNDLES_RESULT__CHANGED = 0;
    public static final int MNG_GPKG_BUNDLES_RESULT__UNCHANGED = -1;

    private class CheckableItemArrayAdapter extends ArrayAdapter<String> {
        private final String TAG = CheckableItemArrayAdapter.class.getCanonicalName();

        private final ArraySet<String> m_set_items = new ArraySet<String>();
        private final HashSet<String> m_set_checkeditems = new HashSet<String>();

        public CheckableItemArrayAdapter(@NonNull Context context, int resource, ArrayList<String> al_items) {
            super(context, resource, al_items);
        }

        public void setCheckedItem(final int item_pos) {
            String item = getItem(item_pos);
            if (item == null) {
//                Log.e(TAG, "setCheckedItem: there is no item at pos: " + item_pos);
                return;
            }
            if (m_set_checkeditems.contains(item)) {
//                Log.d(TAG, "setCheckedItem: item \"" + item + "\" at pos " + item_pos + " is currently checked; removing this item from m_set_checkeditems");
                m_set_checkeditems.remove(item);
            }
            else {
//                Log.d(TAG, "setCheckedItem: item \"" + item + "\" at pos " + item_pos + " is not currently checked; adding this item to m_set_checkeditems");
                m_set_checkeditems.add(item);
            }
        }

        public HashSet<String> getCheckedItems(){
            return m_set_checkeditems;
        }

        @Override
        public void add(@Nullable String item) {
            super.add(item);
//            Log.d(TAG, "add: item \"" + item + "\" added to CheckableItemArrayAdapter at pos " + getPosition(item));
            m_set_items.add(item);
//            Log.d(TAG, "add: item \"" + item + "\" added to m_set_items");
        }

        @Override
        public void remove(@Nullable String item) {
            if (m_set_checkeditems.contains(item)) {
//                Log.d(TAG, "remove: m_set_checkeditems contains item \"" + item + "\"; removing it...");
                m_set_checkeditems.remove(item);
            }
            m_set_items.remove(item);
//            Log.d(TAG, "remove: item \"" + item + "\" removed from m_set_items");
            super.remove(item);
//            Log.d(TAG, "remove: item \"" + item + "\" removed from CheckableItemArrayAdapter");
        }

        @Override
        public void clear() {
            m_set_checkeditems.clear();
            m_set_items.clear();
            super.clear();
        }

        public ArraySet<String> getItems() {
            return m_set_items;
        }
    }
    private CheckableItemArrayAdapter m_lv_gpk_bundles__installed__dataadapter = null;


    private int m_result = MNG_GPKG_BUNDLES_RESULT__CHANGED;
    private final View.OnClickListener OnClickListener__m_btn_manage__geopackage_bundles__close = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ManageGpkgBundlesActivity.this.setResult(m_result);
            ManageGpkgBundlesActivity.this.finish();
        }
    };

    private final View.OnClickListener OnClickListener__m_btn_gpkg_bundle__install = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ManageGpkgBundlesActivity.this.startActivityForResult(new Intent(ManageGpkgBundlesActivity.this, InstallGpkgBundleActivity.class), Constants.REQUEST_CODES.REQUEST_CODE__INSTALL_GPKG_BUNDLE);
        }
    };

    //user selects an installed geopackage-bundle
    private final AdapterView.OnItemClickListener OnItemClickListener__m_lv_gpk_bundles__installed = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            CheckableItemArrayAdapter checkableitemadapterview_dataadapter = (CheckableItemArrayAdapter)parent.getAdapter();
            checkableitemadapterview_dataadapter.setCheckedItem(position);
            m_btn_gpkg_bundle__uninstall.setEnabled(checkableitemadapterview_dataadapter.getCheckedItems().size() > 0);
        }
    };

    private final DataSetObserver DataSetObserver__m_lv_gpk_bundles__installed__dataadapter = new DataSetObserver() {
        @Override
        public void onChanged() {
            m_btn_gpkg_bundle__uninstall.setEnabled(m_lv_gpk_bundles__installed__dataadapter.getCheckedItems().size() > 0);
            m_lv_gpk_bundles__installed.setVisibility(m_lv_gpkg_bundles__installed__items.isEmpty() ? View.GONE : View.VISIBLE);
            m_tv_gpkg_bundles__none_installed.setVisibility(m_lv_gpkg_bundles__installed__items.isEmpty() ? View.VISIBLE : View.GONE);
        }
    };

    private final View.OnClickListener OnClickListener__m_btn_gpkg_bundle__uninstall = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d(TAG, "OnClickListener__m_btn_gpkg_bundle__uninstall.onClick: m_lv_gpk_bundles__installed__dataadapter listview contains " + m_lv_gpk_bundles__installed__dataadapter.getCheckedItems().size() + " checked items");
            if (m_lv_gpk_bundles__installed__dataadapter.getCheckedItems().size() > 0) {
                AlertDialog.Builder ad_builder = new AlertDialog.Builder(ManageGpkgBundlesActivity.this);
                ad_builder
                        .setTitle("Confirm Geopackage-Bundle Removal")
                        .setMessage(getString(R.string.srvr_config_type__remote__no_url_specified__alert_msg))
                        .setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                File f_gpkg_root_dir = null;
                                try {
                                    f_gpkg_root_dir = new File(GPKG.Local.F_GPKG_DIR.getInstance(ManageGpkgBundlesActivity.this.getApplicationContext()).getPath());
                                    File[] f_gpkg_bundles = f_gpkg_root_dir.listFiles();
                                    Log.d(TAG, "OnClickListener__m_btn_gpkg_bundle__uninstall.AlertDialog.PositiveButton.onClick: " + f_gpkg_root_dir.getPath() + " contains " + f_gpkg_bundles.length + " geopackage-bundles");
                                    for (File f_gpkg_bundle : f_gpkg_bundles) {
                                        if (f_gpkg_bundle.isDirectory()) {
                                            String s_gpkg_bundle_name = f_gpkg_bundle.getName();
                                            if (m_lv_gpk_bundles__installed__dataadapter.getCheckedItems().contains(s_gpkg_bundle_name)) {
                                                Log.d(TAG, "OnClickListener__m_btn_gpkg_bundle__uninstall.AlertDialog.PositiveButton.onClick: removing geopackage-bundle \"" + s_gpkg_bundle_name + "\"...");
                                                Files.delete(f_gpkg_bundle);
                                            }
                                        }
                                    }
                                    new Handler().postDelayed(new RefeshList_Runnable(), 50);
                                } catch (PackageManager.NameNotFoundException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                } finally {
                                    dialog.dismiss();
                                }
                            }
                        })
                        .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                StringBuilder sb_alert_msg = new StringBuilder()
                        .append("Are you sure you want to remove the following geopackage-bundles?\n\n")
                        .append("Geopackage-Bundles to be removed:\n");
                int n_items = m_lv_gpk_bundles__installed__dataadapter.getCount();
                for (int i = 0; i < n_items; i++) {
                    String item = m_lv_gpk_bundles__installed__dataadapter.getItem(i);
                    if (m_lv_gpk_bundles__installed__dataadapter.getCheckedItems().contains(item)) {
                        Log.d(TAG, "OnClickListener__m_btn_gpkg_bundle__uninstall.onClick: item \"" + item + "\" at pos " + i + " is checked");
                        sb_alert_msg.append("\t" + item + "\n");
                    } else {
                        Log.d(TAG, "OnClickListener__m_btn_gpkg_bundle__uninstall.onClick: item \"" + item + "\" at pos " + i + " is not checked");
                    }
                }
                sb_alert_msg.append("\n\n");
                ad_builder.setMessage(sb_alert_msg.toString());
                ad_builder.create().show();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_gpkg_bundles);

        //map UI objects to UI resources
        m_btn_manage__geopackage_bundles__close = (ImageButton)findViewById(R.id.btn_manage__geopackage_bundles__close);
        m_btn_gpkg_bundle__install = (Button)findViewById(R.id.btn_install_remote_gpkg_bundle);
        m_lv_gpk_bundles__installed = (ListView)findViewById(R.id.lv_gpkg_bundles__installed);
        m_tv_gpkg_bundles__none_installed = (TextView)findViewById(R.id.tv_gpkg_bundles__none_installed);
        m_btn_gpkg_bundle__uninstall = (Button)findViewById(R.id.btn_gpkg_bundle__uninstall);

        //set up associated UI objects auxiliary objects if any - e.g. TAGs and data adapters
        //m_lv_gpk_bundles__installed__dataadapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_multiple_choice, m_lv_gpkg_bundles__installed__items);
        m_lv_gpk_bundles__installed__dataadapter = new CheckableItemArrayAdapter(this, android.R.layout.simple_list_item_multiple_choice, m_lv_gpkg_bundles__installed__items);
        m_lv_gpk_bundles__installed__dataadapter.registerDataSetObserver(DataSetObserver__m_lv_gpk_bundles__installed__dataadapter);
        m_lv_gpk_bundles__installed.setAdapter(m_lv_gpk_bundles__installed__dataadapter);

        //associate listeners for user-UI-interaction and exec misc control init
        m_btn_manage__geopackage_bundles__close.setOnClickListener(OnClickListener__m_btn_manage__geopackage_bundles__close);
        m_btn_gpkg_bundle__install.setOnClickListener(OnClickListener__m_btn_gpkg_bundle__install);
        m_lv_gpk_bundles__installed.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        m_lv_gpk_bundles__installed.setItemsCanFocus(false);
        m_lv_gpk_bundles__installed.setOnItemClickListener(OnItemClickListener__m_lv_gpk_bundles__installed);
        m_lv_gpk_bundles__installed.setVisibility(View.GONE);
        m_tv_gpkg_bundles__none_installed.setVisibility(View.GONE);
        m_btn_gpkg_bundle__uninstall.setOnClickListener(OnClickListener__m_btn_gpkg_bundle__uninstall);
        m_btn_gpkg_bundle__uninstall.setEnabled(false);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        Log.d(TAG, "onPostCreate: posting RefreshList_Runnable()...");
        new Handler().postDelayed(new RefeshList_Runnable(), 50);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private class RefeshList_Runnable implements Runnable {
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "RefeshList_Runnable.run: running on ui thread");
                    try {
                        //remove current entries from m_lv_gpk_bundles__installed__dataadapter
                        m_lv_gpk_bundles__installed__dataadapter.getCheckedItems().clear();
                        m_lv_gpk_bundles__installed__dataadapter.getItems().clear();
                        m_lv_gpk_bundles__installed.clearChoices();
                        m_lv_gpkg_bundles__installed__items.clear();

                        //add installed geopackage bundles (directories) to m_lv_gpk_bundles__installed__dataadapter
                        File f_gpkg_root_dir = new File(GPKG.Local.F_GPKG_DIR.getInstance(ManageGpkgBundlesActivity.this.getApplicationContext()).getPath());
                        File[] f_gpkg_root_dir_files = f_gpkg_root_dir.listFiles();
                        Log.d(TAG, "RefeshList_Runnable.run: " + f_gpkg_root_dir.getPath() + " contains " + f_gpkg_root_dir_files.length + " geopackage-bundles");
                        for (File f_gpkg_root_dir_file : f_gpkg_root_dir_files) {
                            if (f_gpkg_root_dir_file.isDirectory()) {
                                String s_gpkg_bundle_name = f_gpkg_root_dir_file.getName();
                                Log.d(TAG, "RefeshList_Runnable.run: adding geopackage-bundle name \"" + s_gpkg_bundle_name + "\" to listviewbg");
                                m_lv_gpkg_bundles__installed__items.add(s_gpkg_bundle_name);

                                //now compute size of corresponding files
                            }
                        }
                        long hashcode__installed__items = hash_gpkgs();
                        Log.d(TAG, "RefeshList_Runnable.run: current m_lv_gpkg_bundles__installed__items.hashCode() == " + hashcode__installed__items);
                        if (m_hashcode__first__installed__items == null) {
                            m_hashcode__first__installed__items = hashcode__installed__items;
                            Log.d(TAG, "RefeshList_Runnable.run: set m_hashcode__first__installed__items := " + m_hashcode__first__installed__items);
                        }
                        if (hashcode__installed__items != m_hashcode__first__installed__items) {
                            Log.d(TAG, "RefeshList_Runnable.run: current m_lv_gpkg_bundles__installed__items.hashCode() value (" + hashcode__installed__items + ") differs from original value (" + m_hashcode__first__installed__items + "); updating m_result=MNG_GPKG_BUNDLES_RESULT__CHANGED;");
                            m_result = MNG_GPKG_BUNDLES_RESULT__CHANGED;
                        } else {
                            Log.d(TAG, "RefeshList_Runnable.run: current m_lv_gpkg_bundles__installed__items.hashCode() value (" + hashcode__installed__items + ") does not differ from original value (" + m_hashcode__first__installed__items + "); updating m_result=MNG_GPKG_BUNDLES_RESULT__UNCHANGED;");
                            m_result = MNG_GPKG_BUNDLES_RESULT__UNCHANGED;
                        }

                        m_lv_gpk_bundles__installed__dataadapter.notifyDataSetChanged();
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private long hash_gpkgs() {
        long hash = 0;
        File f_gpkg_root_dir = null;
        try {
            f_gpkg_root_dir = new File(GPKG.Local.F_GPKG_DIR.getInstance(ManageGpkgBundlesActivity.this.getApplicationContext()).getPath());
            File[] f_gpkg_bundles = f_gpkg_root_dir.listFiles();
            Log.d(TAG, "hash_gpkgs: " + f_gpkg_root_dir.getPath() + " contains " + f_gpkg_bundles.length + " geopackage-bundles");
            for (File f_gpkg_bundle : f_gpkg_bundles) {
                if (f_gpkg_bundle.isDirectory()) {
                    File[] f_fpkg_files = f_gpkg_bundle.listFiles();
                    for (int i = 0; i < f_fpkg_files.length; i++) {
                        File f_gpkg_bundle_file = f_fpkg_files[i];
                        if (f_gpkg_bundle_file.isFile()) {
                            int fhc = f_gpkg_bundle_file.hashCode();
                            hash += fhc;
                            Log.d(TAG, "hash_gpkgs: gpkg-bundle " + f_gpkg_bundle.getName() + " contains file " + f_gpkg_bundle_file.getName() + ", w/ hash " + fhc + "; updated hash to: " + hash);
                        }
                    }
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return hash;
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case Constants.REQUEST_CODES.REQUEST_CODE__INSTALL_GPKG_BUNDLE: {
                switch (resultCode) {
                    case InstallGpkgBundleActivity.INSTALL_GPKG_BUNDLE_RESULT__SUCCESSFUL: {
                        Log.i(TAG, "onActivityResult: requestCode: REQUEST_CODE__INSTALL_GPKG_BUNDLE | resultCode: INSTALL_GPKG_BUNDLE_RESULT__SUCCESSFUL");
                        new Handler().postDelayed(new RefeshList_Runnable(), 50);
                        break;
                    }
                    case InstallGpkgBundleActivity.INSTALL_GPKG_BUNDLE_RESULT__CANCELLED: {
                        Log.i(TAG, "onActivityResult: requestCode: REQUEST_CODE__INSTALL_GPKG_BUNDLE | resultCode: INSTALL_GPKG_BUNDLE_RESULT__CANCELLED");
                        break;
                    }
                    case InstallGpkgBundleActivity.INSTALL_GPKG_BUNDLE_RESULT__FAILED: {
                        Log.i(TAG, "onActivityResult: requestCode: REQUEST_CODE__INSTALL_GPKG_BUNDLE | resultCode: INSTALL_GPKG_BUNDLE_RESULT__FAILED");
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
}
