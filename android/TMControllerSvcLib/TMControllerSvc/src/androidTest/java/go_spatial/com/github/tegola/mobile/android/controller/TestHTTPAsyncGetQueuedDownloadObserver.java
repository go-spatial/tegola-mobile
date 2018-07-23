package go_spatial.com.github.tegola.mobile.android.controller;

import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import go_spatial.com.github.tegola.mobile.android.controller.utils.Files;
import go_spatial.com.github.tegola.mobile.android.controller.utils.HTTP;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestHTTPAsyncGetQueuedDownloadObserver extends HTTP.AsyncGet.ExecutorService.DownloadQueueObserver {
    private final String TAG = TestHTTPAsyncGetQueuedDownloadObserver.class.getSimpleName();

    @Override
    public void onDownloaded(HTTP.AsyncGet.HttpUrl_To_Local_File httpUrl_to_local_file, int n_pending) {
        super.onDownloaded(httpUrl_to_local_file, n_pending);
        assertEquals(true, httpUrl_to_local_file.get_file().exists());
    }

    @Override
    public void onAllDownloadsFinished() {
        int
            n_succeeded = getSuccessfulDownloads().size(),
            n_failed = getFailedDownloadExceptions().size();
        Log.d(
            TAG,
            String.format(
                "onAllDownloadsFinished: all queued downloads have finished -%s%s",
                    n_succeeded > 0 ? " " + n_succeeded + " succeeded" : "",
                    n_failed > 0 ? " " + n_failed + " failed (with exceptions) " : " "
            )
        );

        for (HTTP.AsyncGet.CallableTaskException callableTaskException : getFailedDownloadExceptions()) {
            Exception exception = (Exception)callableTaskException.getCause();
            if (exception != null) {
                String s_url = callableTaskException.get_httpUrl_to_local_file().get_url().toString();
                String s_file = "<failed to retrieve local file path>";
                try {s_file = callableTaskException.get_httpUrl_to_local_file().get_file().getCanonicalPath();} catch (IOException e) {}
                Log.d(
                    TAG,
                    String.format(
                        "onAllDownloadsFinished: failed download of %s to %s: %s",
                        s_url,
                        s_file,
                        exception.getMessage()
                    )
                );
            }
        }

        for (HTTP.AsyncGet.HttpUrl_To_Local_File httpUrl_to_local_file : getSuccessfulDownloads()) {
            String s_url = httpUrl_to_local_file.get_url().toString();
            File file = httpUrl_to_local_file.get_file();
            String file_path = "<failed to retrieve local file path>";
            try {file_path = file.getCanonicalPath();} catch (IOException e) {}
            assertEquals(true, file != null && file.exists());
            Log.d(
                TAG,
                String.format(
                    "onAllDownloadsFinished: successfully downloaded %s to %s - local file exists: %b",
                    s_url,
                    file_path,
                    file.exists()
                )
            );
        }

        if (n_failed > 0) {
            if (n_succeeded > 0) {
                //detect partial installations
                HashMap<String, File>
                    partially_installed_gpkg_bundles = new HashMap<>();
                for (HTTP.AsyncGet.CallableTaskException callableTaskException : getFailedDownloadExceptions()) {
                    File f_local_file__failed_download = callableTaskException.get_httpUrl_to_local_file().get_file();
                    File dir_gpkg_bundle__failed_download = f_local_file__failed_download.getParentFile();
                    boolean partially_installed = false;
                    if (!partially_installed_gpkg_bundles.containsKey(dir_gpkg_bundle__failed_download.getName())) {
                        Log.d(TAG, String.format("onAllDownloadsFinished: failed download of %s - running partial-installation detection routine for associated geopackage-bundle \"%s\"...", callableTaskException.get_httpUrl_to_local_file().get_url().toString(), dir_gpkg_bundle__failed_download.getName()));
                        for (HTTP.AsyncGet.HttpUrl_To_Local_File httpUrl_to_local_file : getSuccessfulDownloads()) {
                            File f_local_file__successful_download = httpUrl_to_local_file.get_file();
                            assertTrue(f_local_file__successful_download.exists());
                            File dir_gpkg_bundle__successful_download = f_local_file__successful_download.getParentFile();
                            try {
                                if (dir_gpkg_bundle__failed_download.getCanonicalPath().compareTo(dir_gpkg_bundle__successful_download.getCanonicalPath()) == 0) {
                                    Log.d(TAG, String.format("onAllDownloadsFinished: partial-installation of geopackage-bundle \"%s\" detected!", dir_gpkg_bundle__failed_download.getName()));
                                    partially_installed_gpkg_bundles.put(dir_gpkg_bundle__failed_download.getName(), dir_gpkg_bundle__failed_download);
                                    partially_installed = true;
                                    break;
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    } else
                        partially_installed = true;
                    if (!partially_installed) {
                        Log.d(TAG, String.format("onAllDownloadsFinished: geopackage-bundle \"%s\" was not only partially installed (already existed)", dir_gpkg_bundle__failed_download.getName()));
                    }
                }
                for (String s_partially_installed_gpkg_bundle : partially_installed_gpkg_bundles.keySet()) {
                    Log.d(TAG, String.format("onAllDownloadsFinished: removing partially installed geopackage: %s...", s_partially_installed_gpkg_bundle));
                    File dir_partially_installed_gpkg_bundle = partially_installed_gpkg_bundles.get(s_partially_installed_gpkg_bundle);
                    assertTrue(dir_partially_installed_gpkg_bundle.exists());
                    try {
                        Files.delete(dir_partially_installed_gpkg_bundle);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    assertTrue(!dir_partially_installed_gpkg_bundle.exists());
                }
            }
        }

        super.onAllDownloadsFinished();
    }
}
