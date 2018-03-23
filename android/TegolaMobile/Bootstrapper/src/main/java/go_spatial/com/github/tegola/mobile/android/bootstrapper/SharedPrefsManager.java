package go_spatial.com.github.tegola.mobile.android.bootstrapper;


import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefsManager {
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
        String TM_PROVIDER_TYPE_SEL__GEOPACKAGE = "TM_PROVIDER_TYPE_SEL__GEOPACKAGE";
        String TM_PROVIDER_TYPE_SEL__GEOPACKAGE__VAL = "TM_PROVIDER_TYPE_SEL__GEOPACKAGE__VAL";
        String TM_CONFIG_TYPE_SEL__REMOTE = "TM_CONFIG_TYPE_SEL__REMOTE";
        String TM_CONFIG_TYPE_SEL__LOCAL__VAL = "TM_CONFIG_TYPE_SEL__LOCAL__VAL";
        String TM_CONFIG_TYPE_SEL__REMOTE__VAL = "TM_CONFIG_TYPE_SEL__REMOTE__VAL";
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


    public enum BOOLEAN_SHARED_PREF {
        TM_PROVIDER_TYPE_SEL__GEOPACKAGE
        , TM_CONFIG_TYPE_SEL__REMOTE
        ;

        @Override
        public String toString() {
            switch (this) {
                case TM_CONFIG_TYPE_SEL__REMOTE: return Strings.TM_CONFIG_TYPE_SEL__REMOTE;
                case TM_PROVIDER_TYPE_SEL__GEOPACKAGE: return Strings.TM_PROVIDER_TYPE_SEL__GEOPACKAGE;
                default: return null;
            }
        }

        public void setValue(final boolean value) {m_this.SetBooleanSharedPref(this.toString(), value);}
        public boolean getValue() {return m_this.GetBooleanSharedPref(this.toString());}
    }

    public enum STRING_SHARED_PREF {
        TM_PROVIDER_TYPE_SEL__GEOPACKAGE__VAL
        , TM_CONFIG_TYPE_SEL__LOCAL__VAL
        , TM_CONFIG_TYPE_SEL__REMOTE__VAL
        ;

        @Override
        public String toString() {
            switch (this) {
                case TM_PROVIDER_TYPE_SEL__GEOPACKAGE__VAL: return Strings.TM_PROVIDER_TYPE_SEL__GEOPACKAGE__VAL;
                case TM_CONFIG_TYPE_SEL__LOCAL__VAL: return Strings.TM_CONFIG_TYPE_SEL__LOCAL__VAL;
                case TM_CONFIG_TYPE_SEL__REMOTE__VAL: return Strings.TM_CONFIG_TYPE_SEL__REMOTE__VAL;
                default: return null;
            }
        }

        public void setValue(final String value) {m_this.SetStringSharedPref(this.toString(), value);}
        public String getValue() {return m_this.GetStringSharedPref(this.toString());}
    }
}
