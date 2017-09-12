package go_spatial.com.github.tegola.mobile.android.controller;


public class Constants {
    public interface Strings {
        String PKG = "go_spatial.com.github.tegola.android.controller";
        String TEGOLA_BIN__NORMALIZED_FNAME = "tegola.bin";
        String TEGOLA_CONFIG_TOML__NORMALIZED_FNAME = "config.toml";
        interface CTRLR_INTENT_ACTION {
            String CONTROLLER__START_FOREGROUND = PKG + "START_FOREGROUND";
            String CONTROLLER__STOP_FOREGROUND = PKG + "STOP_FOREGROUND";
            String MVT_SERVER__START = PKG + ".MVT_SERVER__START";
            String MVT_SERVER__STOP = PKG + ".MVT_SERVER__STOP";
        }
        interface CTRLR_INTENT_BR_NOTIFICATIONS {
            String CONTROLLER__FOREGROUND_STARTING = PKG + "FOREGROUND_STARTING";
            String CONTROLLER__FOREGROUND_STARTED = PKG + "FOREGROUND_STARTED";
            String CONTROLLER__FOREGROUND_STOPPING = PKG + "FOREGROUND_STOPPING";
            String CONTROLLER__FOREGROUND_STOPPED = PKG + "FOREGROUND_STOPPED";
            String MVT_SERVER__STARTING = PKG + ".MVT_SERVER__STARTING";
            String MVT_SERVER__STARTED = PKG + ".MVT_SERVER__STARTED";
            String MVT_SERVER__STOPPING = PKG + ".MVT_SERVER__STOPPING";
            String MVT_SERVER__STOPPED = PKG + ".MVT_SERVER__STOPPED";
        }
        interface TEGOLA_ARG {
            String CONFIG = "config";
        }
        interface CPU_ABI {//see https://developer.android.com/ndk/guides/abis.html
            String CPU_ABI__armeabi = "armeabi";
            String CPU_ABI__armeabi_v7a = "armeabi-v7a";
            String CPU_ABI__arm64_v8a = "arm64-v8a";
            String CPU_ABI__x86 = "x86";
            String CPU_ABI__x86_64 = "x86_64";
            String CPU_ABI__mips = "mips";
            String CPU_ABI__mips64 = "mips64";
        }
    }

    public interface Enums {
        enum E_CTRLR_INTENT_ACTION {
            CONTROLLER__START_FOREGROUND
            , CONTROLLER__STOP_FOREGROUND
            , MVT_SERVER__START
            , MVT_SERVER__STOP
            ;
            public static final E_CTRLR_INTENT_ACTION fromString(final String s) {
                switch (s) {
                    case Strings.CTRLR_INTENT_ACTION.CONTROLLER__START_FOREGROUND: return CONTROLLER__START_FOREGROUND;
                    case Strings.CTRLR_INTENT_ACTION.CONTROLLER__STOP_FOREGROUND: return CONTROLLER__STOP_FOREGROUND;
                    case Strings.CTRLR_INTENT_ACTION.MVT_SERVER__START: return MVT_SERVER__START;
                    case Strings.CTRLR_INTENT_ACTION.MVT_SERVER__STOP: return MVT_SERVER__STOP;
                    default: return null;
                }
            }
            @Override
            public String toString() {
                switch (this) {
                    case CONTROLLER__START_FOREGROUND: return Strings.CTRLR_INTENT_ACTION.CONTROLLER__START_FOREGROUND;
                    case CONTROLLER__STOP_FOREGROUND: return Strings.CTRLR_INTENT_ACTION.CONTROLLER__STOP_FOREGROUND;
                    case MVT_SERVER__START: return Strings.CTRLR_INTENT_ACTION.MVT_SERVER__START;
                    case MVT_SERVER__STOP: return Strings.CTRLR_INTENT_ACTION.MVT_SERVER__STOP;
                    default: return null;
                }
            }
        }
        enum E_CTRLR_BR_NOTIFICATIONS {
            CONTROLLER_FOREGROUND_STARTING
            , CONTROLLER_FOREGROUND_STARTED
            , CONTROLLER_FOREGROUND_STOPPING
            , CONTROLLER_FOREGROUND_STOPPED
            , MVT_SERVER__STARTING
            , MVT_SERVER__STARTED
            , MVT_SERVER__STOPPING
            , MVT_SERVER__STOPPED
            ;

            public static final E_CTRLR_BR_NOTIFICATIONS fromString(final String s) {
                switch (s) {
                    case Strings.CTRLR_INTENT_BR_NOTIFICATIONS.CONTROLLER__FOREGROUND_STARTING: return E_CTRLR_BR_NOTIFICATIONS.CONTROLLER_FOREGROUND_STARTING;
                    case Strings.CTRLR_INTENT_BR_NOTIFICATIONS.CONTROLLER__FOREGROUND_STARTED: return E_CTRLR_BR_NOTIFICATIONS.CONTROLLER_FOREGROUND_STARTED;
                    case Strings.CTRLR_INTENT_BR_NOTIFICATIONS.CONTROLLER__FOREGROUND_STOPPING: return E_CTRLR_BR_NOTIFICATIONS.CONTROLLER_FOREGROUND_STOPPING;
                    case Strings.CTRLR_INTENT_BR_NOTIFICATIONS.CONTROLLER__FOREGROUND_STOPPED: return E_CTRLR_BR_NOTIFICATIONS.CONTROLLER_FOREGROUND_STOPPED;
                    case Strings.CTRLR_INTENT_BR_NOTIFICATIONS.MVT_SERVER__STARTING: return E_CTRLR_BR_NOTIFICATIONS.MVT_SERVER__STARTING;
                    case Strings.CTRLR_INTENT_BR_NOTIFICATIONS.MVT_SERVER__STARTED: return E_CTRLR_BR_NOTIFICATIONS.MVT_SERVER__STARTED;
                    case Strings.CTRLR_INTENT_BR_NOTIFICATIONS.MVT_SERVER__STOPPING: return E_CTRLR_BR_NOTIFICATIONS.MVT_SERVER__STOPPING;
                    case Strings.CTRLR_INTENT_BR_NOTIFICATIONS.MVT_SERVER__STOPPED: return E_CTRLR_BR_NOTIFICATIONS.MVT_SERVER__STOPPED;
                    default: return null;
                }
            }
            @Override
            public String toString() {
                switch (this) {
                    case CONTROLLER_FOREGROUND_STARTING: return Strings.CTRLR_INTENT_BR_NOTIFICATIONS.CONTROLLER__FOREGROUND_STARTING;
                    case CONTROLLER_FOREGROUND_STARTED: return Strings.CTRLR_INTENT_BR_NOTIFICATIONS.CONTROLLER__FOREGROUND_STARTED;
                    case CONTROLLER_FOREGROUND_STOPPING: return Strings.CTRLR_INTENT_BR_NOTIFICATIONS.CONTROLLER__FOREGROUND_STOPPING;
                    case CONTROLLER_FOREGROUND_STOPPED: return Strings.CTRLR_INTENT_BR_NOTIFICATIONS.CONTROLLER__FOREGROUND_STOPPED;
                    case MVT_SERVER__STARTING: return Strings.CTRLR_INTENT_BR_NOTIFICATIONS.MVT_SERVER__STARTING;
                    case MVT_SERVER__STARTED: return Strings.CTRLR_INTENT_BR_NOTIFICATIONS.MVT_SERVER__STARTED;
                    case MVT_SERVER__STOPPING: return Strings.CTRLR_INTENT_BR_NOTIFICATIONS.MVT_SERVER__STOPPING;
                    case MVT_SERVER__STOPPED: return Strings.CTRLR_INTENT_BR_NOTIFICATIONS.MVT_SERVER__STOPPED;
                    default: return null;
                }
            }
        }
        enum TEGOLA_ARG {
            CONFIG
            ;
            public static final TEGOLA_ARG fromString(final String s) {
                switch (s) {
                    case Strings.TEGOLA_ARG.CONFIG: return CONFIG;
                    default: return null;
                }
            }
            @Override
            public String toString() {
                switch (this) {
                    case CONFIG: return Strings.TEGOLA_ARG.CONFIG;
                    default: return null;
                }
            }
        }
        enum CPU_ABI {  //provides string mapping to Build.CPU_ABI; see https://developer.android.com/ndk/guides/abis.html
            armeabi
            , armeabi_v7a
            , arm64_v8a
            , x86
            , x86_64
            , mips
            , mips64
            ;

            public static final CPU_ABI fromString(final String s) {
                switch (s) {
                    case Strings.CPU_ABI.CPU_ABI__armeabi: return armeabi;
                    case Strings.CPU_ABI.CPU_ABI__armeabi_v7a: return armeabi_v7a;
                    case Strings.CPU_ABI.CPU_ABI__arm64_v8a: return arm64_v8a;
                    case Strings.CPU_ABI.CPU_ABI__x86: return x86;
                    case Strings.CPU_ABI.CPU_ABI__x86_64: return x86_64;
                    case Strings.CPU_ABI.CPU_ABI__mips: return mips;
                    case Strings.CPU_ABI.CPU_ABI__mips64: return mips64;
                    default: return null;
                }
            }


            @Override
            public String toString() {
                switch (this) {
                    case armeabi: return Strings.CPU_ABI.CPU_ABI__armeabi;
                    case armeabi_v7a: return Strings.CPU_ABI.CPU_ABI__armeabi_v7a;
                    case arm64_v8a: return Strings.CPU_ABI.CPU_ABI__arm64_v8a;
                    case x86: return Strings.CPU_ABI.CPU_ABI__x86;
                    case x86_64: return Strings.CPU_ABI.CPU_ABI__x86_64;
                    case mips: return Strings.CPU_ABI.CPU_ABI__mips;
                    case mips64: return Strings.CPU_ABI.CPU_ABI__mips64;
                    default: return null;
                }
            }
        }
    }

    public interface ASNB_NOTIFICATIONS {
        int FGS_NB_ID = 99991;   //largest 5-digit prime number (for fun and to provide high degree of statistical uniqueness)
    }
}
