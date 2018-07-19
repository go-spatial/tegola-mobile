package go_spatial.com.github.tegola.mobile.android.controller;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import go_spatial.com.github.tegola.mobile.android.controller.utils.Files;

public final class TEGOLA_BIN {
    private final String TAG = TEGOLA_BIN.class.getCanonicalName();

    private static TEGOLA_BIN m_this = null;
    private final File f_tegola_bin;
    private Constants.Enums.CPU_ABI[] supp_abis;

    private TEGOLA_BIN(@NonNull final Context context) throws PackageManager.NameNotFoundException, IOException, Exceptions.TegolaBinaryNotExecutableException, Exceptions.UnsupportedCPUABIException {
        File f_libs = Files.F_LIBS_DIR.getInstance(context);
        if (BuildConfig.tegola_bin_abi_map.size() > 0) {
            ArrayList<Constants.Enums.CPU_ABI> al_supported_abis = null;
            String s_tegola_bin_for_abi = null;
            for (String s_tegola_bin_name : BuildConfig.tegola_bin_abi_map.keySet()) {
                Log.d(TAG, "TEGOLA_BIN ctor: BuildConfig.tegola_bin_abi_map - found key (tegola bin name): " + s_tegola_bin_name);
                String[] abi_list = BuildConfig.tegola_bin_abi_map.get(s_tegola_bin_name);
                if (abi_list != null && abi_list.length > 0) {
                    ArrayList<Constants.Enums.CPU_ABI> al_abis = new ArrayList<>();
                    Log.d(TAG, "TEGOLA_BIN ctor: \tabi list:");
                    for (int i = 0; i < abi_list.length; i++) {
                        String abi = abi_list[i];
                        al_abis.add(Constants.Enums.CPU_ABI.fromString(abi));
                        Log.d(TAG, "TEGOLA_BIN ctor: \t\t" + abi);
                        if (abi.equals(Build.CPU_ABI)) {
                            Log.d(TAG, "TEGOLA_BIN ctor: \t\t\tfound bin name (" + s_tegola_bin_name + ") for matching ABI (" + Build.CPU_ABI + ")!");
                            s_tegola_bin_for_abi = s_tegola_bin_name;
                            al_supported_abis = al_abis;
                        }
                    }
                }
            }
            if (s_tegola_bin_for_abi != null) {
                f_tegola_bin = new File(f_libs, s_tegola_bin_for_abi);
                f_tegola_bin.setReadOnly();
                f_tegola_bin.setExecutable(true);
                if (f_tegola_bin.setExecutable(true))
                    throw new Exceptions.TegolaBinaryNotExecutableException(f_tegola_bin.getName());
                Log.d(TAG, "TEGOLA_BIN ctor: teggola bin " + f_tegola_bin.getCanonicalPath() + " " + (f_tegola_bin.exists() ? "exists" : "does NOT exist"));
                supp_abis = al_supported_abis.toArray(new Constants.Enums.CPU_ABI[al_supported_abis.size()]);
            } else {
                throw new Exceptions.UnsupportedCPUABIException("no tegola binary exists for ABI " + Build.CPU_ABI);
            }
        } else
            f_tegola_bin = null;
    }
    private TEGOLA_BIN() {
        f_tegola_bin = null;
    }

    public static TEGOLA_BIN getInstance(@NonNull final Context context) throws PackageManager.NameNotFoundException, IOException, Exceptions.TegolaBinaryNotExecutableException, Exceptions.UnsupportedCPUABIException {
        if (m_this == null)
            m_this = new TEGOLA_BIN(context);
        return m_this;
    }

    public final File get() {
        return f_tegola_bin;
    }

    private String m_s_ver = "UNKNOWN";
    public void set_version_string(@NonNull final String s_ver) {m_s_ver = s_ver;}
    public String get_version_string() {return m_s_ver;}

    public final Constants.Enums.CPU_ABI[] supported_ABIs() {return supp_abis;}
}
