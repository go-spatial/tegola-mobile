package go_spatial.com.github.tegola.mobile.android.bootstrapper;


public class Constants {
    public interface REQUEST_CODES {
        int REQUEST_CODE__GOOGLEAPICLIENT__RESOLVE_CONNECTION_FAILURE = 1;
        int REQUEST_CODE__GOOGLEDRIVE__IMPORT_TOML_FILES = 2;
        int REQUEST_CODE__SDCARD__IMPORT_TOML_FILES = 3;
        int REQUEST_CODE__EDIT_TOML_FILE = 4;
    }
    public interface Strings {
        interface EDITOR_INTENT_EXTRAS {
            String FILENAME = "FILENAME";
        }
    }
    public interface Enums {
        enum E_CONTROLLER_STATE { //borrowed from ControllerLib Constants.Enums.E_CTRLR_BR_NOTIFICATIONS subset related to controller state
            CONTROLLER_FOREGROUND_STARTING
            , CONTROLLER_FOREGROUND_STARTED
            , CONTROLLER_FOREGROUND_STOPPING
            , CONTROLLER_FOREGROUND_STOPPED
            ;
        }
        enum E_SERVER_STATE {   //borrowed from ControllerLib Constants.Enums.E_CTRLR_BR_NOTIFICATIONS subset related to controller state
            MVT_SERVER__STARTING
            , MVT_SERVER__STARTED
            , MVT_SERVER__STOPPING
            , MVT_SERVER__STOPPED
            ;
        }
    }
}
