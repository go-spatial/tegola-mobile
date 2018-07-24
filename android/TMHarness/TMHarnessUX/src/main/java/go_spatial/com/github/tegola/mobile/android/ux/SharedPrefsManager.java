package go_spatial.com.github.tegola.mobile.android.ux;


import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;

public class SharedPrefsManager {
    private final String TAG = SharedPrefsManager.class.getCanonicalName();

    private static final String SHARED_PREFS_KEY = "TegolaMobile_SHARED_PREFS";

    private final Context m_context;
    private static SharedPrefsManager m_this;
    private static SharedPreferences m_tmsharedprefs = null;
    private static final Object m_tmsharedprefs_lock = new Object();

    private SharedPrefsManager(final Context context) {
        m_context = context;
        m_this = this;
    }
    private SharedPrefsManager() {
        m_context = null;
        m_this = this;
    }

    public static final SharedPrefsManager newInstance(final Context context) {
        synchronized (m_tmsharedprefs_lock) {
            return new SharedPrefsManager(context);
        }
    }
    public static final SharedPrefsManager getInstance() {
        return m_this;
    }

    public interface Strings {
        String TM_TILE_SOURCE__IS_LOCAL = "TM_TILE_SOURCE__IS_LOCAL";
        String TM_PROVIDER__IS_GEOPACKAGE = "TM_PROVIDER__IS_GEOPACKAGE";
        String TM_PROVIDER__GPKG_BUNDLE__SELECTION = "TM_PROVIDER__GPKG_BUNDLE__SELECTION";
        String TM_PROVIDER__GPKG_BUNDLE_PROPS__SELECTION = "TM_PROVIDER__GPKG_BUNDLE_PROPS__SELECTION";
        String TM_CONFIG_TOML__IS_REMOTE = "TM_CONFIG_TOML__IS_REMOTE";
        String TM_CONFIG_TOML__LOCAL__SELECTION = "TM_CONFIG_TOML__LOCAL__SELECTION";
        String TM_CONFIG_TOML__REMOTE__SELECTION = "TM_CONFIG_TOML__REMOTE__SELECTION";
        String TM_CANONICAL_REMOTE_TILE_SERVERS = "TM_CANONICAL_REMOTE_TILE_SERVERS";
        String TM_TILE_SOURCE__REMOTE = "TM_TILE_SOURCE__REMOTE";
        String MBGL_CONFIG__CONNECT_TIMEOUT = "MBGL_CONFIG__CONNECT_TIMEOUT";
        String MBGL_CONFIG__READ_TIMEOUT = "MBGL_CONFIG__READ_TIMEOUT";
        String MBGL_CONFIG__MAX_REQ_PER_HOST = "MBGL_CONFIG__MAX_REQ_PER_HOST";
        String MBGL_CONFIG__CACHE_SIZE = "MBGL_CONFIG__CACHE_SIZE";
    }


    private boolean GetBooleanSharedPref(final String desired_boolean_shared_pref) throws NullPointerException {
        if (desired_boolean_shared_pref == null || desired_boolean_shared_pref.isEmpty())
            throw new NullPointerException("desired_boolean_shared_pref cannot be null or empty");
        synchronized (m_tmsharedprefs_lock) {
            if (m_tmsharedprefs == null)
                m_tmsharedprefs = m_context.getSharedPreferences(SHARED_PREFS_KEY, Context.MODE_PRIVATE);
            if (!m_tmsharedprefs.contains(desired_boolean_shared_pref))    //if boolean shared prefs setting does not yet exist, we first create it then set it to false
                SetBooleanSharedPref(desired_boolean_shared_pref, false);
            return m_tmsharedprefs.getBoolean(desired_boolean_shared_pref, false);
        }
    }
    private void SetBooleanSharedPref(final String target_boolean_shared_pref, final boolean value) throws NullPointerException {
        if (target_boolean_shared_pref == null || target_boolean_shared_pref.isEmpty())
            throw new NullPointerException("target_boolean_shared_pref cannot be null or empty");
        synchronized (m_tmsharedprefs_lock) {
            if (m_tmsharedprefs == null)
                m_tmsharedprefs = m_context.getSharedPreferences(SHARED_PREFS_KEY, Context.MODE_PRIVATE);
            SharedPreferences.Editor sp_editor = m_tmsharedprefs.edit();
            sp_editor.putBoolean(target_boolean_shared_pref, value);
            sp_editor.commit();
        }
    }

    private Integer GetIntegerSharedPref(final String desired_int_shared_pref) throws NullPointerException {
        if (desired_int_shared_pref == null || desired_int_shared_pref.isEmpty())
            throw new NullPointerException("desired_int_shared_pref cannot be null or empty");
        synchronized (m_tmsharedprefs_lock) {
            if (m_tmsharedprefs == null)
                m_tmsharedprefs = m_context.getSharedPreferences(SHARED_PREFS_KEY, Context.MODE_PRIVATE);
            if (m_tmsharedprefs.contains(desired_int_shared_pref))
                return m_tmsharedprefs.getInt(desired_int_shared_pref, -1);
            return null;
        }
    }
    private void SetIntegerSharedPref(final String target_int_shared_pref, final int value) throws NullPointerException {
        if (target_int_shared_pref == null || target_int_shared_pref.isEmpty())
            throw new NullPointerException("target_int_shared_pref cannot be null or empty");
        synchronized (m_tmsharedprefs_lock) {
            if (m_tmsharedprefs == null)
                m_tmsharedprefs = m_context.getSharedPreferences(SHARED_PREFS_KEY, Context.MODE_PRIVATE);
            SharedPreferences.Editor sp_editor = m_tmsharedprefs.edit();
            sp_editor.putInt(target_int_shared_pref, value);
            sp_editor.commit();
        }
    }

    private String GetStringSharedPref(final String desired_string_shared_pref) throws NullPointerException {
        if (desired_string_shared_pref == null || desired_string_shared_pref.isEmpty())
            throw new NullPointerException("desired_string_shared_pref cannot be null or empty");
        synchronized (m_tmsharedprefs_lock) {
            if (m_tmsharedprefs == null)
                m_tmsharedprefs = m_context.getSharedPreferences(SHARED_PREFS_KEY, Context.MODE_PRIVATE);
            if (!m_tmsharedprefs.contains(desired_string_shared_pref))    //if string shared prefs setting does not yet exist, we first create it then set it to ""
                SetStringSharedPref(desired_string_shared_pref, "");
            return m_tmsharedprefs.getString(desired_string_shared_pref, "");
        }
    }
    private void SetStringSharedPref(final String target_string_shared_pref, final String value) throws NullPointerException {
        if (target_string_shared_pref == null || target_string_shared_pref.isEmpty())
            throw new NullPointerException("target_string_shared_pref cannot be null or empty");
        synchronized (m_tmsharedprefs_lock) {
            if (m_tmsharedprefs == null)
                m_tmsharedprefs = m_context.getSharedPreferences(SHARED_PREFS_KEY, Context.MODE_PRIVATE);
            SharedPreferences.Editor sp_editor = m_tmsharedprefs.edit();
            sp_editor.putString(target_string_shared_pref, value != null ? value : "");
            sp_editor.commit();
        }
    }

    private String[] GetStringArraySharedPref(final String desired_string_array_shared_pref) throws NullPointerException {
        String[] s_ary = null;
        if (desired_string_array_shared_pref == null || desired_string_array_shared_pref.isEmpty())
            throw new NullPointerException("desired_string_array_shared_pref cannot be null or empty");
        synchronized (m_tmsharedprefs_lock) {
            if (m_tmsharedprefs == null)
                m_tmsharedprefs = m_context.getSharedPreferences(SHARED_PREFS_KEY, Context.MODE_PRIVATE);
            if (!m_tmsharedprefs.contains(desired_string_array_shared_pref))    //if string-array shared prefs setting does not yet exist, we first create it then set it to "[]"
                SetStringArraySharedPrefs(desired_string_array_shared_pref, null);
            try {
                JSONArray jsonArray = new JSONArray(m_tmsharedprefs.getString(desired_string_array_shared_pref, "[]"));
                Log.d(TAG, String.format("GetStringArraySharedPref: (key) \"%s\": %s", desired_string_array_shared_pref, jsonArray.toString()));
                int n_len = jsonArray.length();
                s_ary = new String[n_len];
                for (int i = 0; i < n_len; i++)
                    s_ary[i] = jsonArray.getString(i);
            } catch (Exception e) {
                e.printStackTrace();
                s_ary = new String[0];
            }
            return s_ary;
        }
    }
    private void SetStringArraySharedPrefs(final String target_string_array_shared_pref, final String[] s_ary) throws NullPointerException {
        if (target_string_array_shared_pref == null || target_string_array_shared_pref.isEmpty())
            throw new NullPointerException("target_string_array_shared_pref cannot be null or empty");
        JSONArray jsonArray = new JSONArray();
        if (s_ary != null && s_ary.length > 0) {
            int n = 0;
            try {
                for (String s_val : s_ary)
                    jsonArray.put(n++, s_val);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        synchronized (m_tmsharedprefs_lock) {
            if (m_tmsharedprefs == null)
                m_tmsharedprefs = m_context.getSharedPreferences(SHARED_PREFS_KEY, Context.MODE_PRIVATE);
            SharedPreferences.Editor sp_editor = m_tmsharedprefs.edit();
            Log.d(TAG, String.format("SetStringArraySharedPrefs: (key) \"%s\": %s", target_string_array_shared_pref, jsonArray.toString()));
            sp_editor.putString(target_string_array_shared_pref, jsonArray.toString());
            sp_editor.commit();
        }
    }




    public enum BOOLEAN_SHARED_PREF {
        TM_TILE_SOURCE__IS_LOCAL            //true: use embedded/local tegola mvt server
        , TM_PROVIDER__IS_GEOPACKAGE        //true: provider type is geopackage, else provider type is postgis
        , TM_CONFIG_TOML__IS_REMOTE         //true: config toml file is hosted remotely (http), else config toml file is hosted locally on device
        ;

        @Override
        public String toString() {
            switch (this) {
                case TM_TILE_SOURCE__IS_LOCAL: return Strings.TM_TILE_SOURCE__IS_LOCAL;
                case TM_PROVIDER__IS_GEOPACKAGE: return Strings.TM_PROVIDER__IS_GEOPACKAGE;
                case TM_CONFIG_TOML__IS_REMOTE: return Strings.TM_CONFIG_TOML__IS_REMOTE;
                default: return null;
            }
        }

        public void setValue(final boolean value) {m_this.SetBooleanSharedPref(this.toString(), value);}
        public boolean getValue() {return m_this.GetBooleanSharedPref(this.toString());}
    }

    public enum INTEGER_SHARED_PREF {
        MBGL_CONFIG__CONNECT_TIMEOUT
        , MBGL_CONFIG__READ_TIMEOUT
        , MBGL_CONFIG__MAX_REQ_PER_HOST
        , MBGL_CONFIG__CACHE_SIZE
        ;

        @Override
        public String toString() {
            switch (this) {
                case MBGL_CONFIG__CONNECT_TIMEOUT: return Strings.MBGL_CONFIG__CONNECT_TIMEOUT;
                case MBGL_CONFIG__READ_TIMEOUT: return Strings.MBGL_CONFIG__READ_TIMEOUT;
                case MBGL_CONFIG__MAX_REQ_PER_HOST: return Strings.MBGL_CONFIG__MAX_REQ_PER_HOST;
                case MBGL_CONFIG__CACHE_SIZE: return Strings.MBGL_CONFIG__CACHE_SIZE;
                default: return null;
            }
        }

        public void setValue(final int value) {m_this.SetIntegerSharedPref(this.toString(), value);}
        public Integer getValue() {return m_this.GetIntegerSharedPref(this.toString());}
    }

    public enum STRING_SHARED_PREF {
        TM_PROVIDER__GPKG_BUNDLE__SELECTION
        , TM_PROVIDER__GPKG_BUNDLE_PROPS__SELECTION
        , TM_CONFIG_TOML__LOCAL__SELECTION
        , TM_CONFIG_TOML__REMOTE__SELECTION
        , TM_TILE_SOURCE__REMOTE
        ;

        @Override
        public String toString() {
            switch (this) {
                case TM_PROVIDER__GPKG_BUNDLE__SELECTION: return Strings.TM_PROVIDER__GPKG_BUNDLE__SELECTION;
                case TM_PROVIDER__GPKG_BUNDLE_PROPS__SELECTION: return Strings.TM_PROVIDER__GPKG_BUNDLE_PROPS__SELECTION;
                case TM_CONFIG_TOML__LOCAL__SELECTION: return Strings.TM_CONFIG_TOML__LOCAL__SELECTION;
                case TM_CONFIG_TOML__REMOTE__SELECTION: return Strings.TM_CONFIG_TOML__REMOTE__SELECTION;
                case TM_TILE_SOURCE__REMOTE: return Strings.TM_TILE_SOURCE__REMOTE;
                default: return null;
            }
        }

        public void setValue(final String value) {m_this.SetStringSharedPref(this.toString(), value);}
        public String getValue() {return m_this.GetStringSharedPref(this.toString());}
    }

    public enum STRING_ARRAY_SHARED_PREF {
        TM_CANONICAL_REMOTE_TILE_SERVERS
        ;

        @Override
        public String toString() {
            switch (this) {
                case TM_CANONICAL_REMOTE_TILE_SERVERS: return Strings.TM_CANONICAL_REMOTE_TILE_SERVERS;
                default: return null;
            }
        }

        public void setValue(final String[] s_ary) {m_this.SetStringArraySharedPrefs(this.toString(), s_ary);}
        public String[] getValue() {return m_this.GetStringArraySharedPref(this.toString());}
    }
}
