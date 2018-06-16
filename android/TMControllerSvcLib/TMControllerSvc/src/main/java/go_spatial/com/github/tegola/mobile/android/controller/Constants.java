package go_spatial.com.github.tegola.mobile.android.controller;


import android.os.Build;

import static go_spatial.com.github.tegola.mobile.android.controller.Constants.Strings.PKG;

public class Constants {
    public interface Strings {
        String PKG = "go_spatial.com.github.tegola.mobile.android.controller";

        interface TEGOLA_PROCESS {
            interface TILE_CACHE {
                interface FILE {
                    String SUB_PATH = "tile_cache";
                    String BASE_PATH_ENV_VAR = "FILE_CACHE_BASE_PATH";
                }
            }
        }

        interface GPKG_BUNDLE {
            String SUBDIR = "gpkg-bundle";
            interface VERSION_PROPS {
                String FNAME = "version.properties";
                interface PROP {
                    String TOML_FILE = "TOML_FILE";
                    String TOML_VERSION = "TOML_VERSION";
                    String GPKG_FILES = "GPKG_FILES";
                    String GPKG_VERSIONS = "GPKG_VERSIONS";
                    String GPKG_PATH_ENV_VARS = "GPKG_PATH_ENV_VARS";
                }
            }

        }

        interface INTENT {
            String STRING = PKG + "." + "INTENT";
            interface ACTION {
                interface REQUEST {
                    String STRING = INTENT.STRING + "." + "REQUEST";
                    interface FGS {
                        String STRING = REQUEST.STRING + "." + "FGS";
                        interface COMMAND {
                            String STRING = FGS.STRING + "." + "COMMAND";
                            interface START {
                                String STRING = COMMAND.STRING + "." + "START";
                                interface EXTRA_KEY {
                                    String STRING = START.STRING + "." + "EXTRA_KEY";
                                    interface HARNESS_CLASS_NAME {
                                        String STRING = EXTRA_KEY.STRING + "." + "HARNESS_CLASS_NAME";
                                    }
                                }
                            }
                            interface STOP {
                                String STRING = COMMAND.STRING + "." + "STOP";
                            }

                        }
                    }
                    interface MVT_SERVER {
                        String STRING = REQUEST.STRING + "." + "MVT_SERVER";
                        interface CONTROL {
                            String STRING = MVT_SERVER.STRING + "." + "CONTROL";
                            interface START {
                                String STRING = CONTROL.STRING + "." + "START";
                                interface EXTRA__KEY {
                                    String STRING = START.STRING + "." + "EXTRA_KEY";
                                    interface PROVIDER {
                                        String STRING = EXTRA__KEY.STRING + "." + "PROVIDER";
                                        interface GPKG {
                                            String STRING = PROVIDER.STRING + "." + "GPKG";     //boolean: true: use local gpkg provider, else use postgis provider
                                            interface BUNDLE {
                                                String STRING = GPKG.STRING + "." + "BUNDLE";
                                                interface PROPS {
                                                    String STRING = BUNDLE.STRING + "." + "PROPS";
                                                }
                                            }
                                        }
                                    }
                                    interface CONFIG {
                                        String STRING = EXTRA__KEY.STRING + "." + "CONFIG";
                                        interface REMOTE {
                                            String STRING = CONFIG.STRING + "." + "REMOTE";     //boolean: true: config toml file is retreieved from a remote host, else config toml file exists on local device
                                        }
                                        interface PATH {
                                            String STRING = CONFIG.STRING + "." + "PATH";         //string: the path to config toml file - note that if MVT_SERVER__START__CONFIG__IS_REMOTE is true, then MVT_SERVER__START__CONFIG__PATH should be a URL pointing to remote config toml file
                                        }
                                    }

                                }
                            }
                            interface STOP {
                                String STRING = CONTROL.STRING + "." + "STOP";
                            }

                        }
                        interface STATE {
                            String STRING = MVT_SERVER.STRING + "." + "STATE";
                            interface IS_RUNNING {
                                String STRING = STATE.STRING + "." + "IS_RUNNING";
                            }
                            interface LISTEN_PORT {
                                String STRING = STATE.STRING + "." + "LISTEN_PORT";
                            }
                        }
                        interface HTTP_URL_API {
                            String STRING = MVT_SERVER.STRING + "." + "HTTP_URL_API";
                            interface GET_JSON {
                                String STRING = HTTP_URL_API.STRING + "." + "GET_JSON";
                                interface EXTRA_KEY {
                                    String STRING = GET_JSON.STRING + "." + "EXTRA_KEY";
                                    interface PURPOSE {
                                        String STRING = EXTRA_KEY.STRING + "." + "PURPOSE";
                                        interface VALUE {
                                            String STRING = PURPOSE.STRING + "." + "VALUE";
                                            interface LOAD_MAP {
                                                String STRING = VALUE.STRING + "." + "LOAD_MAP";
                                            }
                                        }
                                    }
                                    interface ROOT_URL {
                                        String STRING = GET_JSON.STRING + "." + "ROOT_URL";
                                    }
                                    interface ENDPOINT {
                                        String STRING = GET_JSON.STRING + "." + "ENDPOINT";
                                    }
                                }
                            }
                        }
                    }
                }

                interface NOTIFICATION {
                    String STRING = INTENT.STRING + "." + "NOTIFICATION";
                    interface FGS {
                        String STRING = NOTIFICATION.STRING + "." + "FGS";
                        interface STATE {
                            String STRING = FGS.STRING + "." + "STATE";
                            interface STARTING {
                                String STRING = STATE.STRING + "." + "STARTING";
                            }
                            interface RUNNING {
                                String STRING = STATE.STRING + "." + "RUNNING";
                            }
                            interface STOPPING {
                                String STRING = STATE.STRING + "." + "STOPPING";
                            }
                            interface STOPPED {
                                String STRING = STATE.STRING + "." + "STOPPED";
                            }
                        }
                    }
                    interface MVT_SERVER {
                        String STRING = NOTIFICATION.STRING + "." + "MVT_SERVER";
                        interface STATE {
                            String STRING = MVT_SERVER.STRING + "." + "STATE";
                            interface STARTING {
                                String STRING = STATE.STRING + "." + "STARTING";
                            }
                            interface START_FAILED {
                                String STRING = STATE.STRING + "." + "START_FAILED";
                                interface EXTRA_KEY {
                                    String STRING = START_FAILED.STRING + "." + "EXTRA_KEY";
                                    interface REASON {
                                        String STRING = EXTRA_KEY.STRING + "." + "REASON";
                                    }
                                }
                            }
                            interface RUNNING {
                                String STRING = STATE.STRING + "." + "RUNNING";
                                interface EXTRA_KEY {
                                    String STRING = RUNNING.STRING + "." + "EXTRA_KEY";
                                    interface PID {
                                        String STRING = EXTRA_KEY.STRING + "." + "PID";
                                    }
                                }
                            }
                            interface LISTENING {
                                String STRING = STATE.STRING + "." + "LISTENING";
                                interface EXTRA_KEY {
                                    String STRING = LISTENING.STRING + "." + "EXTRA_KEY";
                                    interface PORT {
                                        String STRING = EXTRA_KEY.STRING + "." + "PORT";
                                    }
                                }
                            }
                            interface STOPPING {
                                String STRING = STATE.STRING + "." + "STOPPING";
                            }
                            interface STOPPED {
                                String STRING = STATE.STRING + "." + "STOPPED";
                            }
                        }
                        interface MONITOR {
                            String STRING = STATE.STRING + "." + "MONITOR";
                            interface LOGCAT {
                                String STRING = MONITOR.STRING + "." + "LOGCAT";
                                interface OUTPUT {
                                    String STRING = LOGCAT.STRING + "." + "OUTPUT";
                                    interface EXTRA_KEY {
                                        String STRING = OUTPUT.STRING + "." + "EXTRA_KEY";
                                        interface LINE {
                                            String STRING = EXTRA_KEY.STRING + "." + "LINE";
                                        }
                                    }
                                }
                            }
                            interface STDERR {
                                String STRING = MONITOR.STRING + "." + "STDERR";
                                interface OUTPUT {
                                    String STRING = STDERR.STRING + "." + "OUTPUT";
                                    interface EXTRA_KEY {
                                        String STRING = OUTPUT.STRING + "." + "EXTRA_KEY";
                                        interface LINE {
                                            String STRING = EXTRA_KEY.STRING + "." + "LINE";
                                        }
                                    }
                                }
                            }
                            interface STDOUT {
                                String STRING = MONITOR.STRING + "." + "STDOUT";
                                interface OUTPUT {
                                    String STRING = STDOUT.STRING + "." + "OUTPUT";
                                    interface EXTRA_KEY {
                                        String STRING = OUTPUT.STRING + "." + "EXTRA_KEY";
                                        interface LINE {
                                            String STRING = EXTRA_KEY.STRING + "." + "LINE";
                                        }
                                    }
                                }
                            }
                        }
                        interface HTTP_URL_API {
                            String STRING = MVT_SERVER.STRING + "." + "HTTP_URL_API";
                            interface GOT_JSON {
                                String STRING = HTTP_URL_API.STRING + "." + "GOT_JSON";
                                interface EXTRA_KEY {
                                    String STRING = GOT_JSON.STRING + "." + "EXTRA_KEY";
                                    interface PURPOSE {
                                        String STRING = EXTRA_KEY.STRING + "." + "PURPOSE";
                                        interface VALUE {
                                            String STRING = PURPOSE.STRING + "." + "VALUE";
                                            interface LOAD_MAP {
                                                String STRING = VALUE.STRING + "." + "LOAD_MAP";
                                            }
                                        }
                                    }
                                    interface ROOT_URL {
                                        String STRING = GOT_JSON.STRING + "." + "ROOT_URL";
                                    }
                                    interface ENDPOINT {
                                        String STRING = GOT_JSON.STRING + "." + "ENDPOINT";
                                    }
                                    interface CONTENT {
                                        String STRING = GOT_JSON.STRING + "." + "CONTENT";
                                    }
                                }
                            }
                            interface GET_JSON_FAILED {
                                String STRING = HTTP_URL_API.STRING + "." + "GET_JSON_FAILED";
                                interface EXTRA_KEY {
                                    String STRING = GET_JSON_FAILED.STRING + "." + "EXTRA_KEY";
                                    interface PURPOSE {
                                        String STRING = EXTRA_KEY.STRING + "." + "PURPOSE";
                                    }
                                    interface ROOT_URL {
                                        String STRING = GET_JSON_FAILED.STRING + "." + "ROOT_URL";
                                    }
                                    interface ENDPOINT {
                                        String STRING = GET_JSON_FAILED.STRING + "." + "ENDPOINT";
                                    }
                                    interface REASON {
                                        String STRING = GET_JSON_FAILED.STRING + "." + "REASON";
                                    }
                                }
                            }
                        }
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
        enum E_INTENT_ACTION__REQUEST {
            FGS_COMMAND_STOP
            , FGS_COMMAND_START
            , MVT_SERVER_CONTROL_START
            , MVT_SERVER_CONTROL_STOP
            , MVT_SERVER_STATE_IS_RUNNING
            , MVT_SERVER_STATE_LISTEN_PORT
            , MVT_SERVER_HTTP_URL_API_READ_JSON
            ;

            public static final E_INTENT_ACTION__REQUEST fromString(final String s) {
                switch (s) {
                    case Strings.INTENT.ACTION.REQUEST.FGS.COMMAND.START.STRING: return FGS_COMMAND_START;
                    case Strings.INTENT.ACTION.REQUEST.FGS.COMMAND.STOP.STRING: return FGS_COMMAND_STOP;
                    case Strings.INTENT.ACTION.REQUEST.MVT_SERVER.CONTROL.START.STRING: return MVT_SERVER_CONTROL_START;
                    case Strings.INTENT.ACTION.REQUEST.MVT_SERVER.CONTROL.STOP.STRING: return MVT_SERVER_CONTROL_STOP;
                    case Strings.INTENT.ACTION.REQUEST.MVT_SERVER.STATE.IS_RUNNING.STRING: return MVT_SERVER_STATE_IS_RUNNING;
                    case Strings.INTENT.ACTION.REQUEST.MVT_SERVER.STATE.LISTEN_PORT.STRING: return MVT_SERVER_STATE_LISTEN_PORT;
                    case Strings.INTENT.ACTION.REQUEST.MVT_SERVER.HTTP_URL_API.GET_JSON.STRING: return MVT_SERVER_HTTP_URL_API_READ_JSON;
                    default: return null;
                }
            }

            @Override
            public String toString() {
                switch (this) {
                    case FGS_COMMAND_START: return Strings.INTENT.ACTION.REQUEST.FGS.COMMAND.START.STRING;
                    case FGS_COMMAND_STOP: return Strings.INTENT.ACTION.REQUEST.FGS.COMMAND.STOP.STRING;
                    case MVT_SERVER_CONTROL_START: return Strings.INTENT.ACTION.REQUEST.MVT_SERVER.CONTROL.START.STRING;
                    case MVT_SERVER_CONTROL_STOP: return Strings.INTENT.ACTION.REQUEST.MVT_SERVER.CONTROL.STOP.STRING;
                    case MVT_SERVER_STATE_IS_RUNNING: return Strings.INTENT.ACTION.REQUEST.MVT_SERVER.STATE.IS_RUNNING.STRING;
                    case MVT_SERVER_STATE_LISTEN_PORT: return Strings.INTENT.ACTION.REQUEST.MVT_SERVER.STATE.LISTEN_PORT.STRING;
                    case MVT_SERVER_HTTP_URL_API_READ_JSON: return Strings.INTENT.ACTION.REQUEST.MVT_SERVER.HTTP_URL_API.GET_JSON.STRING;
                    default: return null;
                }
            }
        }

        enum E_INTENT_ACTION__NOTIFICATION {
            FGS_STATE_STARTING
            , FGS_STATE_RUNNING
            , FGS_STATE_STOPPING
            , FGS_STATE_STOPPED
            , MVT_SERVER_STATE_STARTING
            , MVT_SERVER_STATE_START_FAILED
            , MVT_SERVER_STATE_RUNNING
            , MVT_SERVER_STATE_LISTENING
            , MVT_SERVER_MONITOR_LOGCAT_OUTPUT
            , MVT_SERVER_MONITOR_STDERR_OUTPUT
            , MVT_SERVER_MONITOR_STDOUT_OUTPUT
            , MVT_SERVER_HTTP_URL_API_GOT_JSON
            , MVT_SERVER_HTTP_URL_API_GET_JSON_FAILED
            , MVT_SERVER_STATE_STOPPING
            , MVT_SERVER_STATE_STOPPED
            ;

            public static final E_INTENT_ACTION__NOTIFICATION fromString(final String s) {
                switch (s) {
                    case Strings.INTENT.ACTION.NOTIFICATION.FGS.STATE.STARTING.STRING: return FGS_STATE_STARTING;
                    case Strings.INTENT.ACTION.NOTIFICATION.FGS.STATE.RUNNING.STRING: return FGS_STATE_RUNNING;
                    case Strings.INTENT.ACTION.NOTIFICATION.FGS.STATE.STOPPING.STRING: return FGS_STATE_STOPPING;
                    case Strings.INTENT.ACTION.NOTIFICATION.FGS.STATE.STOPPED.STRING: return FGS_STATE_STOPPED;
                    case Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.STATE.STARTING.STRING: return MVT_SERVER_STATE_STARTING;
                    case Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.STATE.START_FAILED.STRING: return MVT_SERVER_STATE_START_FAILED;
                    case Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.STATE.RUNNING.STRING: return MVT_SERVER_STATE_RUNNING;
                    case Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.STATE.LISTENING.STRING: return MVT_SERVER_STATE_LISTENING;
                    case Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.MONITOR.LOGCAT.OUTPUT.STRING: return MVT_SERVER_MONITOR_LOGCAT_OUTPUT;
                    case Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.MONITOR.STDERR.OUTPUT.STRING: return MVT_SERVER_MONITOR_STDERR_OUTPUT;
                    case Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.MONITOR.STDOUT.OUTPUT.STRING: return MVT_SERVER_MONITOR_STDOUT_OUTPUT;
                    case Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.HTTP_URL_API.GOT_JSON.STRING: return MVT_SERVER_HTTP_URL_API_GOT_JSON;
                    case Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.HTTP_URL_API.GET_JSON_FAILED.STRING: return MVT_SERVER_HTTP_URL_API_GET_JSON_FAILED;
                    case Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.STATE.STOPPING.STRING: return MVT_SERVER_STATE_STOPPING;
                    case Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.STATE.STOPPED.STRING: return MVT_SERVER_STATE_STOPPED;
                    default: return null;
                }
            }

            @Override
            public String toString() {
                switch (this) {
                    case FGS_STATE_STARTING: return Strings.INTENT.ACTION.NOTIFICATION.FGS.STATE.STARTING.STRING;
                    case FGS_STATE_RUNNING: return Strings.INTENT.ACTION.NOTIFICATION.FGS.STATE.RUNNING.STRING;
                    case FGS_STATE_STOPPING: return Strings.INTENT.ACTION.NOTIFICATION.FGS.STATE.STOPPING.STRING;
                    case FGS_STATE_STOPPED: return Strings.INTENT.ACTION.NOTIFICATION.FGS.STATE.STOPPED.STRING;
                    case MVT_SERVER_STATE_STARTING: return Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.STATE.STARTING.STRING;
                    case MVT_SERVER_STATE_START_FAILED: return Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.STATE.START_FAILED.STRING;
                    case MVT_SERVER_STATE_RUNNING: return Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.STATE.RUNNING.STRING;
                    case MVT_SERVER_STATE_LISTENING: return Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.STATE.LISTENING.STRING;
                    case MVT_SERVER_MONITOR_LOGCAT_OUTPUT: return Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.MONITOR.LOGCAT.OUTPUT.STRING;
                    case MVT_SERVER_MONITOR_STDERR_OUTPUT: return Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.MONITOR.STDERR.OUTPUT.STRING;
                    case MVT_SERVER_MONITOR_STDOUT_OUTPUT: return Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.MONITOR.STDOUT.OUTPUT.STRING;
                    case MVT_SERVER_HTTP_URL_API_GOT_JSON: return Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.HTTP_URL_API.GOT_JSON.STRING;
                    case MVT_SERVER_HTTP_URL_API_GET_JSON_FAILED: return Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.HTTP_URL_API.GET_JSON_FAILED.STRING;
                    case MVT_SERVER_STATE_STOPPING: return Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.STATE.STOPPING.STRING;
                    case MVT_SERVER_STATE_STOPPED: return Strings.INTENT.ACTION.NOTIFICATION.MVT_SERVER.STATE.STOPPED.STRING;
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

    public interface ASNB_NOTIFICATIONS {//Android System Notification Bar - Notifications
        int NOTIFICATION_ID__CONTROLLER_SERVICE = 99991;   //largest 5-digit prime number (for fun and to provide high degree of statistical uniqueness)
        String NOTIFICATION_CHANNEL_ID__CONTROLLER_SERVICE = PKG + ".service";
    }
}
