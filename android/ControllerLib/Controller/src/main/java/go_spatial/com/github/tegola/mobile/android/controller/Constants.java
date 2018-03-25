package go_spatial.com.github.tegola.mobile.android.controller;


import android.os.Build;
import android.support.annotation.NonNull;

public class Constants {
    public interface Strings {
        String PKG = "go_spatial.com.github.tegola.android.controller";
        String GPKG_BUNDLE_SUBDIR = "gpkg-bundle";

        interface INTENT {
            interface ACTION {
                String EXTRA__KEY__HARNESS = "HARNESS";
                String EXTRA__KEY__CONFIG = "CONFIG";
                String EXTRA__KEY__PATH = "PATH";
                String EXTRA__KEY__MSG = "MSG";
                String EXTRA__KEY__VERSION = "VERSION";
                String EXTRA__KEY__PID = "PID";
                String EXTRA__KEY__REASON = "REASON";
                String EXTRA__KEY__PROVIDER = "PROVIDER";
                String EXTRA__KEY__REMOTE = "REMOTE";
                String EXTRA__KEY__GPKG = "GPKG";
                String EXTRA__KEY__BUNDLE = "BUNDLE";

                interface FGS_CONTROL_REQUEST {
                    String FGS__START_FOREGROUND = PKG + "START_FOREGROUND";
                    String FGS__STOP_FOREGROUND = PKG + "STOP_FOREGROUND";

                    interface EXTRA__KEY {
                        String FGS__START_FOREGROUND__HARNESS = FGS__START_FOREGROUND + "." + EXTRA__KEY__HARNESS;
                    }
                }
                interface MVT_SERVER_CONTROL_REQUEST {
                    String MVT_SERVER__START = PKG + ".MVT_SERVER__START";
                    String MVT_SERVER__STOP = PKG + ".MVT_SERVER__STOP";
                    interface EXTRA__KEY {
                        String MVT_SERVER__START__PROVIDER__IS_GPKG = MVT_SERVER__START + "." + EXTRA__KEY__PROVIDER + "." + EXTRA__KEY__GPKG;     //boolean: true: use local gpkg provider, else use postgis provider
                        String MVT_SERVER__START__GPKG_PROVIDER__BUNDLE = MVT_SERVER__START + "." + EXTRA__KEY__PROVIDER + "." + EXTRA__KEY__GPKG + "." + EXTRA__KEY__BUNDLE;
                        String MVT_SERVER__START__CONFIG__IS_REMOTE = MVT_SERVER__START + "." + EXTRA__KEY__CONFIG + "." + EXTRA__KEY__REMOTE;     //boolean: true: config toml file is retreieved from a remote host, else config toml file exists on local device
                        String MVT_SERVER__START__CONFIG__PATH = MVT_SERVER__START + "." + EXTRA__KEY__CONFIG + "." + EXTRA__KEY__PATH;         //string: the path to config toml file - note that if MVT_SERVER__START__CONFIG__IS_REMOTE is true, then MVT_SERVER__START__CONFIG__PATH should be a URL pointing to remote config toml file
                    }
                }
                interface CTRLR_NOTIFICATION {
                    String CONTROLLER__FOREGROUND_STARTING = PKG + "FOREGROUND_STARTING";
                    String CONTROLLER__FOREGROUND_STARTED = PKG + "FOREGROUND_STARTED";
                    String CONTROLLER__FOREGROUND_STOPPING = PKG + "FOREGROUND_STOPPING";
                    String CONTROLLER__FOREGROUND_STOPPED = PKG + "FOREGROUND_STOPPED";
                    String MVT_SERVER__STARTING = PKG + ".MVT_SERVER__STARTING";
                    String MVT_SERVER__START_FAILED = ".MVT_SERVER__STARTING";
                    String MVT_SERVER__STARTED = PKG + ".MVT_SERVER__STARTED";
                    String MVT_SERVER__OUTPUT__LOGCAT = PKG + ".MVT_SERVER__OUTPUT__LOGCAT";
                    String MVT_SERVER__OUTPUT__STDERR = PKG + ".MVT_SERVER__OUTPUT__STDERR";
                    String MVT_SERVER__OUTPUT__STDOUT = PKG + ".MVT_SERVER__OUTPUT__STDOUT";
                    String MVT_SERVER__STOPPING = PKG + ".MVT_SERVER__STOPPING";
                    String MVT_SERVER__STOPPED = PKG + ".MVT_SERVER__STOPPED";

                    interface EXTRA__KEY {
                        String MVT_SERVER__START_FAILED__REASON = MVT_SERVER__START_FAILED + "." + EXTRA__KEY__REASON;
                        String MVT_SERVER__STARTED__VERSION = MVT_SERVER__STARTED + "." + EXTRA__KEY__VERSION;
                        String MVT_SERVER__STARTED__PID = MVT_SERVER__STARTED + "." + EXTRA__KEY__PID;
                        String MVT_SERVER__OUTPUT__LOGCAT__LINE = MVT_SERVER__OUTPUT__LOGCAT + "." + EXTRA__KEY__MSG;
                        String MVT_SERVER__OUTPUT__STDERR__LINE = MVT_SERVER__OUTPUT__STDERR + "." + EXTRA__KEY__MSG;
                        String MVT_SERVER__OUTPUT__STDOUT__LINE = MVT_SERVER__OUTPUT__STDOUT + "." + EXTRA__KEY__MSG;
                    }
                }
            }
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

        interface SIG_NAME {
            String SIGABRT = "SIGABRT";
            String SIGFPE = "SIGFPE";
            String SIGILL = "SIGILL";
            String SIGINT = "SIGINT";
            String SIGSEGV = "SIGSEGV";
            String SIGTERM = "SIGTERM";
        }

        interface SIG_DESC {
            String SIGABRT = "'abort', abnormal termination";
            String SIGFPE = "floating point exception";
            String SIGILL = "'illegal', invalid instruction";
            String SIGINT = "'interrupt', interactive attention request sent to the program";
            String SIGSEGV = "'segmentation violation/fault', invalid memory access";
            String SIGTERM = "'terminate', termination request sent to the program";
        }
    }

    public interface Enums {
        enum E_INTENT_ACTION__FGS_CONTROL_REQUEST {
            FGS__STOP_FOREGROUND
            , FGS__START_FOREGROUND
            ;

            public static final E_INTENT_ACTION__FGS_CONTROL_REQUEST fromString(final String s) {
                switch (s) {
                    case Strings.INTENT.ACTION.FGS_CONTROL_REQUEST.FGS__START_FOREGROUND: return FGS__START_FOREGROUND;
                    case Strings.INTENT.ACTION.FGS_CONTROL_REQUEST.FGS__STOP_FOREGROUND: return FGS__STOP_FOREGROUND;
                    default: return null;
                }
            }

            @Override
            public String toString() {
                switch (this) {
                    case FGS__START_FOREGROUND: return Strings.INTENT.ACTION.FGS_CONTROL_REQUEST.FGS__START_FOREGROUND;
                    case FGS__STOP_FOREGROUND: return Strings.INTENT.ACTION.FGS_CONTROL_REQUEST.FGS__STOP_FOREGROUND;
                    default: return null;
                }
            }
        }

        enum E_INTENT_ACTION__MVT_SERVER_CONTROL_REQUEST {
            MVT_SERVER__START
            , MVT_SERVER__STOP
            ;

            public static final E_INTENT_ACTION__MVT_SERVER_CONTROL_REQUEST fromString(final String s) {
                switch (s) {
                    case Strings.INTENT.ACTION.MVT_SERVER_CONTROL_REQUEST.MVT_SERVER__START: return MVT_SERVER__START;
                    case Strings.INTENT.ACTION.MVT_SERVER_CONTROL_REQUEST.MVT_SERVER__STOP: return MVT_SERVER__STOP;
                    default: return null;
                }
            }

            @Override
            public String toString() {
                switch (this) {
                    case MVT_SERVER__START: return Strings.INTENT.ACTION.MVT_SERVER_CONTROL_REQUEST.MVT_SERVER__START;
                    case MVT_SERVER__STOP: return Strings.INTENT.ACTION.MVT_SERVER_CONTROL_REQUEST.MVT_SERVER__STOP;
                    default: return null;
                }
            }
        }

        enum E_INTENT_ACTION__CTRLR_NOTIFICATION {
            CONTROLLER_FOREGROUND_STARTING
            , CONTROLLER_FOREGROUND_STARTED
            , CONTROLLER_FOREGROUND_STOPPING
            , CONTROLLER_FOREGROUND_STOPPED
            , MVT_SERVER__STARTING
            , MVT_SERVER__START_FAILED
            , MVT_SERVER__STARTED
            , MVT_SERVER__OUTPUT__LOGCAT
            , MVT_SERVER__OUTPUT__STDERR
            , MVT_SERVER__OUTPUT__STDOUT
            , MVT_SERVER__STOPPING
            , MVT_SERVER__STOPPED
            ;

            public static final E_INTENT_ACTION__CTRLR_NOTIFICATION fromString(final String s) {
                switch (s) {
                    case Strings.INTENT.ACTION.CTRLR_NOTIFICATION.CONTROLLER__FOREGROUND_STARTING: return CONTROLLER_FOREGROUND_STARTING;
                    case Strings.INTENT.ACTION.CTRLR_NOTIFICATION.CONTROLLER__FOREGROUND_STARTED: return CONTROLLER_FOREGROUND_STARTED;
                    case Strings.INTENT.ACTION.CTRLR_NOTIFICATION.CONTROLLER__FOREGROUND_STOPPING: return CONTROLLER_FOREGROUND_STOPPING;
                    case Strings.INTENT.ACTION.CTRLR_NOTIFICATION.CONTROLLER__FOREGROUND_STOPPED: return CONTROLLER_FOREGROUND_STOPPED;
                    case Strings.INTENT.ACTION.CTRLR_NOTIFICATION.MVT_SERVER__STARTING: return MVT_SERVER__STARTING;
                    case Strings.INTENT.ACTION.CTRLR_NOTIFICATION.MVT_SERVER__START_FAILED: return MVT_SERVER__START_FAILED;
                    case Strings.INTENT.ACTION.CTRLR_NOTIFICATION.MVT_SERVER__STARTED: return MVT_SERVER__STARTED;
                    case Strings.INTENT.ACTION.CTRLR_NOTIFICATION.MVT_SERVER__OUTPUT__LOGCAT: return MVT_SERVER__OUTPUT__LOGCAT;
                    case Strings.INTENT.ACTION.CTRLR_NOTIFICATION.MVT_SERVER__OUTPUT__STDERR: return MVT_SERVER__OUTPUT__STDERR;
                    case Strings.INTENT.ACTION.CTRLR_NOTIFICATION.MVT_SERVER__OUTPUT__STDOUT: return MVT_SERVER__OUTPUT__STDOUT;
                    case Strings.INTENT.ACTION.CTRLR_NOTIFICATION.MVT_SERVER__STOPPING: return MVT_SERVER__STOPPING;
                    case Strings.INTENT.ACTION.CTRLR_NOTIFICATION.MVT_SERVER__STOPPED: return MVT_SERVER__STOPPED;
                    default: return null;
                }
            }

            @Override
            public String toString() {
                switch (this) {
                    case CONTROLLER_FOREGROUND_STARTING: return Strings.INTENT.ACTION.CTRLR_NOTIFICATION.CONTROLLER__FOREGROUND_STARTING;
                    case CONTROLLER_FOREGROUND_STARTED: return Strings.INTENT.ACTION.CTRLR_NOTIFICATION.CONTROLLER__FOREGROUND_STARTED;
                    case CONTROLLER_FOREGROUND_STOPPING: return Strings.INTENT.ACTION.CTRLR_NOTIFICATION.CONTROLLER__FOREGROUND_STOPPING;
                    case CONTROLLER_FOREGROUND_STOPPED: return Strings.INTENT.ACTION.CTRLR_NOTIFICATION.CONTROLLER__FOREGROUND_STOPPED;
                    case MVT_SERVER__STARTING: return Strings.INTENT.ACTION.CTRLR_NOTIFICATION.MVT_SERVER__STARTING;
                    case MVT_SERVER__START_FAILED: return Strings.INTENT.ACTION.CTRLR_NOTIFICATION.MVT_SERVER__START_FAILED;
                    case MVT_SERVER__STARTED: return Strings.INTENT.ACTION.CTRLR_NOTIFICATION.MVT_SERVER__STARTED;
                    case MVT_SERVER__OUTPUT__LOGCAT: return Strings.INTENT.ACTION.CTRLR_NOTIFICATION.MVT_SERVER__OUTPUT__LOGCAT;
                    case MVT_SERVER__OUTPUT__STDERR: return Strings.INTENT.ACTION.CTRLR_NOTIFICATION.MVT_SERVER__OUTPUT__STDERR;
                    case MVT_SERVER__OUTPUT__STDOUT: return Strings.INTENT.ACTION.CTRLR_NOTIFICATION.MVT_SERVER__OUTPUT__STDOUT;
                    case MVT_SERVER__STOPPING: return Strings.INTENT.ACTION.CTRLR_NOTIFICATION.MVT_SERVER__STOPPING;
                    case MVT_SERVER__STOPPED: return Strings.INTENT.ACTION.CTRLR_NOTIFICATION.MVT_SERVER__STOPPED;
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

        enum TEGOLA_CONFIG_TYPE {
            LOCAL
            , REMOTE;
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

            public static final CPU_ABI fromDevice() {
                return fromString(Build.CPU_ABI);
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

        enum TEGOLA_BIN {
            tegola_bin__arm
            , tegola_bin__x86
            , tegola_bin__arm64
            , tegola_bin__x86_64
            ;

            public final CPU_ABI[] supported_ABIs() {
                switch (this) {
                    case tegola_bin__arm: return new CPU_ABI[]{CPU_ABI.armeabi, CPU_ABI.armeabi_v7a};
                    case tegola_bin__x86: return new CPU_ABI[]{CPU_ABI.x86};
                    case tegola_bin__arm64: return new CPU_ABI[]{CPU_ABI.arm64_v8a};
                    case tegola_bin__x86_64: return new CPU_ABI[]{CPU_ABI.x86_64};
                    default: return null;
                }
            }

            public final Integer min_api() {
                switch (this) {
                    case tegola_bin__arm:
                    case tegola_bin__x86: return 15;
                    case tegola_bin__arm64:
                    case tegola_bin__x86_64: return 21;
                    default: return null;
                }
            }

            public final Integer raw_res_id() {
                switch (this) {
                    case tegola_bin__arm: return R.raw.tegola_bin__android_arm;
                    case tegola_bin__x86: return R.raw.tegola_bin__android_x86;
                    case tegola_bin__arm64: return R.raw.tegola_bin__android_arm64;
                    case tegola_bin__x86_64: return R.raw.tegola_bin__android_x86_64;
                    default: return null;
                }
            }

            public static final TEGOLA_BIN get_for(final CPU_ABI for_cpu_abi) {
                switch (for_cpu_abi) {
                    case armeabi:
                    case armeabi_v7a: return tegola_bin__arm;
                    case arm64_v8a: return tegola_bin__arm64;
                    case x86: return tegola_bin__x86;
                    case x86_64: return tegola_bin__x86_64;
                    case mips:      //not yet supported since not currently in list of supported platforms for golang; see https://gist.github.com/paulkramme/db58787a786a7b186396fc784ccf424b
                    case mips64:    //not yet supported since not currently in list of supported platforms for golang; see https://gist.github.com/paulkramme/db58787a786a7b186396fc784ccf424b
                    default: return null;
                }
            }

            private static String m_s_ver = "UNKNOWN";
            public static void set_version_string(@NonNull final String s_ver) {
                m_s_ver = s_ver;
            }
            public static String get_version_string() {
                return m_s_ver;
            }
        }

        enum SIGNAL {
            SIGABRT     //note: we should not handle this signal but we include it here for the sake of completion - see https://en.wikipedia.org/wiki/C_signal_handling
            , SIGFPE
            , SIGILL
            , SIGINT    //note: we should not handle this signal but we include it here for the sake of completion - see https://en.wikipedia.org/wiki/C_signal_handling
            , SIGSEGV
            , SIGTERM   //note: we should not handle this signal but we include it here for the sake of completion - see https://en.wikipedia.org/wiki/C_signal_handling
            ;

            public static final SIGNAL fromString(final String s) {
                switch (s) {
                    case Strings.SIG_NAME.SIGABRT: return SIGABRT;
                    case Strings.SIG_NAME.SIGFPE: return SIGFPE;
                    case Strings.SIG_NAME.SIGILL: return SIGILL;
                    case Strings.SIG_NAME.SIGINT: return SIGINT;
                    case Strings.SIG_NAME.SIGSEGV: return SIGSEGV;
                    case Strings.SIG_NAME.SIGTERM: return SIGTERM;
                    default: return null;
                }
            }

            public final String description() {
                switch (this) {
                    case SIGABRT: return Strings.SIG_DESC.SIGABRT;
                    case SIGFPE: return Strings.SIG_DESC.SIGFPE;
                    case SIGILL: return Strings.SIG_DESC.SIGILL;
                    case SIGINT: return Strings.SIG_DESC.SIGINT;
                    case SIGSEGV: return Strings.SIG_DESC.SIGSEGV;
                    case SIGTERM: return Strings.SIG_DESC.SIGTERM;
                    default: return null;
                }
            }
        }
    }

    public interface ASNB_NOTIFICATIONS {
        int FGS_NB_ID = 99991;   //largest 5-digit prime number (for fun and to provide high degree of statistical uniqueness)
    }
}
