package go_spatial.com.github.tegola.mobile.android.bootstrapper;

import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResource;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.query.Filter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

/**
 * Created by stevencontreras on 3/22/18.
 */

public class GoogleDriveFileDownloadManager {
    private static final String TAG = GoogleDriveFileDownloadManager.class.getName();

    private GoogleApiClient m_google_api_client = null;

    public static abstract class FileContentsHandler implements DriveFile.DownloadProgressListener {
        public abstract void OnFileContentsDownloaded(final DriveContents google_drive_file_contents, final Metadata google_drive_file_metadata, final DriveFile google_drive_file);
    }

    private static GoogleDriveFileDownloadManager m_this = null;
    private GoogleDriveFileDownloadManager() {}
    public static GoogleDriveFileDownloadManager getInstance() {
        if (m_this == null)
            m_this = new GoogleDriveFileDownloadManager();
        return m_this;
    }

    public boolean validate_init_api_client(@NonNull final Activity activity, @NonNull final GoogleApiClient.ConnectionCallbacks gapic_conn_callbacks, @NonNull final GoogleApiClient.OnConnectionFailedListener gapic_conn_failed_listener) {
        if (m_google_api_client == null) {
            Log.i(TAG, "validate_init_api_client: GoogleApiClient flow handler: building new GoogleApiClient instance...");
            Toast.makeText(activity, "Initializing GoogleApiClient", Toast.LENGTH_LONG).show();
            m_google_api_client = new GoogleApiClient.Builder(activity)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addConnectionCallbacks(gapic_conn_callbacks)
                    .addOnConnectionFailedListener(gapic_conn_failed_listener)
                    .build();
            return false;
        }
        Log.i(TAG, "validate_init_api_client: GoogleApiClient flow handler: valid GoogleApiClient is instantiated");
        return true;
    }

    public boolean validate_connect_api_client(@NonNull final Activity activity) {
        if (!m_google_api_client.isConnected()) {
            Log.i(TAG, "validate_connect_api_client: GoogleApiClient flow handler: GoogleApiClient is not connected -- starting connection...");
            Toast.makeText(activity, "Connecting GoogleApiClient...", Toast.LENGTH_LONG).show();
            m_google_api_client.connect();
            return false;
        }
        Log.i(TAG, "validate_connect_api_client: GoogleApiClient flow handler: GoogleApiClient connection is valid");
        return true;
    }

    public void disconnect_api_client() {
        if (m_google_api_client != null) {
            // disconnect Google Android Drive API connection.
            m_google_api_client.disconnect();
        }
    }

    public void select_and_download_files(@NonNull final Activity activity, final int request_id) {
        Log.i(TAG, "select_and_download_files: attempting to select/download files from connected google drive...");
        Drive.DriveApi.newDriveContents(m_google_api_client).setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
            @Override
            public void onResult(@NonNull DriveApi.DriveContentsResult driveContentsResult) {
                if (driveContentsResult.getStatus().isSuccess()) {
                    IntentSender google_drive_file_open_intentSender = Drive.DriveApi
                            .newOpenFileActivityBuilder()
                            .build(m_google_api_client);
                    try {
                        activity.startIntentSenderForResult(google_drive_file_open_intentSender, request_id, null, 0, 0, 0);
                    } catch (IntentSender.SendIntentException e) {
                        Log.e(TAG, "google_drive__select_files: Failed to select/download Google Drive file contents -- IntentSender failure!", e);
                        Toast.makeText(activity, "Failed to select/download Google Drive file contents -- IntentSender failure!", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(activity, "Failed to select/download Google Drive file contents", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
    public void select_and_download_files(@NonNull final Activity activity, @NonNull final Filter file_selection_filter, final int request_id) {
        Log.i(TAG, "select_and_download_files: attempting to select/download files from connected google drive...");
        Drive.DriveApi.newDriveContents(m_google_api_client).setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
            @Override
            public void onResult(@NonNull DriveApi.DriveContentsResult driveContentsResult) {
                if (driveContentsResult.getStatus().isSuccess()) {
                    IntentSender google_drive_file_open_intentSender = Drive.DriveApi
                            .newOpenFileActivityBuilder()
                            .setSelectionFilter(file_selection_filter)
                            .build(m_google_api_client);
                    try {
                        activity.startIntentSenderForResult(google_drive_file_open_intentSender, request_id, null, 0, 0, 0);
                    } catch (IntentSender.SendIntentException e) {
                        Log.e(TAG, "google_drive__select_files: Failed to select/download Google Drive file contents -- IntentSender failure!", e);
                        Toast.makeText(activity, "Failed to select/download Google Drive file contents -- IntentSender failure!", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(activity, "Failed to select/download Google Drive file contents", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
    public void select_and_download_files(@NonNull final Activity activity, final String[] mime_types, final int request_id) {
        Log.i(TAG, "google_drive__select_files: attempting to select/download files from connected google drive...");
        Drive.DriveApi.newDriveContents(m_google_api_client).setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
            @Override
            public void onResult(@NonNull DriveApi.DriveContentsResult driveContentsResult) {
                if (driveContentsResult.getStatus().isSuccess()) {
                    IntentSender google_drive_file_open_intentSender = Drive.DriveApi
                            .newOpenFileActivityBuilder()
                            .setMimeType(mime_types)
                            .build(m_google_api_client);
                    try {
                        activity.startIntentSenderForResult(google_drive_file_open_intentSender, request_id, null, 0, 0, 0);
                    } catch (IntentSender.SendIntentException e) {
                        Log.e(TAG, "google_drive__select_files: Failed to select/download Google Drive file contents -- IntentSender failure!", e);
                        Toast.makeText(activity, "Failed to select/download Google Drive file contents -- IntentSender failure!", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(activity, "Failed to select/download Google Drive file contents", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    public void download_file_contents(@NonNull final Activity activity, final DriveId google_drive_id, final int mode, final FileContentsHandler fileContentsHandler) {
        //download file
        final DriveFile google_drive_file = Drive.DriveApi.getFile(m_google_api_client, google_drive_id);
        PendingResult<DriveApi.DriveContentsResult> pendingResult = google_drive_file.open(m_google_api_client, mode, fileContentsHandler);
        pendingResult.setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
            @Override
            public void onResult(final DriveApi.DriveContentsResult driveContentsResult) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        DriveResource.MetadataResult metadataresult = google_drive_file.getMetadata(m_google_api_client).await(1000, TimeUnit.MILLISECONDS);
                        Metadata google_drive_file_metadata = metadataresult.getMetadata();
                        String gd_filename = null;
                        try { gd_filename = google_drive_file_metadata.getOriginalFilename(); } catch (Exception e) {}
                        if (!driveContentsResult.getStatus().isSuccess()) {
                            final String s_err = "Failed to download contents of Google Drive file" + (gd_filename != null ? " \"" + gd_filename + "\"" : "");
                            Log.e(TAG, "download_file_contents--PendingResult.ResultCallback.onResult: " + s_err);
                            activity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(activity, s_err, Toast.LENGTH_LONG).show();
                                }
                            });
                            return;
                        }
                        final DriveContents google_drive_contents = driveContentsResult.getDriveContents();
                        fileContentsHandler.OnFileContentsDownloaded(google_drive_contents, google_drive_file_metadata, google_drive_file);
                        google_drive_contents.discard(m_google_api_client); //closes google drive file
                    }
                }).start();
            }
        });
    }

    public boolean import_file(@NonNull final Activity activity, final DriveContents google_drive_file_contents, final Metadata google_drive_file_metadata, final DriveFile google_drive_file) throws IOException {
        String gd_file_name = google_drive_file_metadata.getOriginalFilename();
        InputStream inputstream_gd_config_toml = google_drive_file_contents.getInputStream();
        byte[] buf_raw_config_toml = new byte[inputstream_gd_config_toml.available()];
        inputstream_gd_config_toml.read(buf_raw_config_toml);
        inputstream_gd_config_toml.close();
        FileOutputStream f_outputstream_new_tegola_config_toml = activity.openFileOutput(gd_file_name, Context.MODE_PRIVATE);
        f_outputstream_new_tegola_config_toml.write(buf_raw_config_toml);
        f_outputstream_new_tegola_config_toml.close();
        File f_new_tegola_config_toml = new File(activity.getFilesDir().getPath() + "/" + gd_file_name);
        return f_new_tegola_config_toml.exists();
    }
}
