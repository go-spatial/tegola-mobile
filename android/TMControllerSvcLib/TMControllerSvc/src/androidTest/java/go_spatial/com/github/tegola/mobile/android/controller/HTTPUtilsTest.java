package go_spatial.com.github.tegola.mobile.android.controller;

import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;
import android.util.Log;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

import go_spatial.com.github.tegola.mobile.android.controller.utils.Files;
import go_spatial.com.github.tegola.mobile.android.controller.utils.HTTP;
import okhttp3.HttpUrl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HTTPUtilsTest {
    private final String TAG = HTTPUtilsTest.class.getSimpleName();

    private class RunnableTask extends HTTP.AsyncGet.Task implements Runnable {
        final HTTP.AsyncGet.HttpUrl_To_Local_File url_to_local_file;
        final public HTTP.AsyncGet.HttpUrl_To_Local_File get_httpUrl_to_local_file() {
            return url_to_local_file;
        }

        public RunnableTask(@NonNull final HTTP.AsyncGet.HttpUrl_To_Local_File url_to_local_file, @NonNull final HTTP.AsyncGet.TaskStageHandler asyncgetfiletask_stage_handler) {
            super(asyncgetfiletask_stage_handler);
            this.url_to_local_file = url_to_local_file;
            asyncgetfiletask_stage_handler.set_httpUrl_to_local_file(url_to_local_file);
        }

        @Override
        public void run() {
            this.onPreExecute();
            this.onPostExecute(this.doInBackground(new HTTP.AsyncGet.HttpUrl_To_Local_File[]{url_to_local_file}));
        }
    }

    private String build_local_gpkg_bundle_path_string(final String s_gpkg_bundle) throws PackageManager.NameNotFoundException, IOException {
        return new StringBuilder()
                .append(GPKG.Local.F_GPKG_BUNDLE_ROOT_DIR.getInstance(InstrumentationRegistry.getContext()).getPath())
                .append(File.separator)
                .append(s_gpkg_bundle)
                .toString();
    }


    @Test
    public void asyncget_via_executor__incompatible_tasks() throws Exception {
        TestHTTPAsyncGetQueuedDownloadObserver queuedDownloadObserver = new TestHTTPAsyncGetQueuedDownloadObserver();
        HTTP.AsyncGet.ExecutorService exec_svc = new HTTP.AsyncGet.ExecutorService(
            (runnable, threadPoolExecutor) -> {
                Log.d(TAG, String.format("rejectedExecution - %s", runnable.getClass().getCanonicalName()));
            },
            queuedDownloadObserver
        );

        String s_url_root = "https://sacontreras.github.io/tegola-mobile-gpkg-bundle";

        LinkedHashMap<String, ArrayList<String>> gpkg_props_map = new LinkedHashMap<>();
        gpkg_props_map.put("athens", new ArrayList<String>(){{add("default.properties"); add("large-layers-partitioned.properties"); add("test-minimal.properties");}});
        gpkg_props_map.put("harare", new ArrayList<String>(){{add("default.properties");}});
        assertEquals(2, gpkg_props_map.size());

        if (gpkg_props_map.size() > 0) {
            Exception caught_exception = null;
            for (String s_gpkg : gpkg_props_map.keySet()) {
                for (String s_gpkg_file : gpkg_props_map.get(s_gpkg)) {
                    caught_exception = null;
                    try {
                        exec_svc.submit(
                            new RunnableTask(
                                new HTTP.AsyncGet.HttpUrl_To_Local_File(
                                    HttpUrl.parse(
                                        String.format(
                                            "%s/%s/%s",
                                            s_url_root,
                                            s_gpkg,
                                            s_gpkg_file
                                        )
                                    ),
                                    new File(
                                        build_local_gpkg_bundle_path_string(s_gpkg),
                                        s_gpkg_file
                                    )
                                ),
                                new TestHTTPAsyncGetStageHandler()
                            )
                        );
                    } catch (ClassCastException e) {
                        caught_exception = e;
                        Log.d(TAG, String.format("asyncget_via_executor__incompatible_tasks - caught expected exception: %s", e.getMessage()));
                    }
                    assertTrue(caught_exception != null && (caught_exception instanceof ClassCastException));
                }
            }
        }

        Log.d(
            TAG,
            String.format(
                "asyncget_via_executor__incompatible_tasks: shutting down HTTP.AsyncGet.ExecutorService"
            )
        );
        exec_svc.shutdown();
    }

    @Test
    public void asyncget_via_executor__total_success() throws Exception {
        TestHTTPAsyncGetQueuedDownloadObserver queuedDownloadObserver = new TestHTTPAsyncGetQueuedDownloadObserver();
        HTTP.AsyncGet.ExecutorService exec_svc = new HTTP.AsyncGet.ExecutorService(
            (runnable, threadPoolExecutor) -> {
                Log.d(TAG, String.format("rejectedExecution - %s", runnable.getClass().getCanonicalName()));
            },
            queuedDownloadObserver
        );

        String s_url_root = "https://sacontreras.github.io/tegola-mobile-gpkg-bundle";

        LinkedHashMap<String, ArrayList<String>> gpkg_props_map = new LinkedHashMap<>();
        gpkg_props_map.put("athens", new ArrayList<String>(){{add("default.properties"); add("large-layers-partitioned.properties"); add("test-minimal.properties");}});
        gpkg_props_map.put("harare", new ArrayList<String>(){{add("default.properties");}});
        assertEquals(2, gpkg_props_map.size());

        if (gpkg_props_map.size() > 0) {
            File files_dir = InstrumentationRegistry.getContext().getFilesDir();

            Log.d(TAG, String.format("asyncget_via_executor__total_success: starting queued downloads..."));
            for (String s_gpkg : gpkg_props_map.keySet()) {
                for (String s_gpkg_file : gpkg_props_map.get(s_gpkg)) {
                    exec_svc.submit(
                        new HTTP.AsyncGet.CallableTask(
                            new HTTP.AsyncGet.HttpUrl_To_Local_File(
                                HttpUrl.parse(
                                    String.format(
                                        "%s/%s/%s",
                                        s_url_root,
                                        s_gpkg, s_gpkg_file
                                    )
                                ),
                                new File(
                                    build_local_gpkg_bundle_path_string(s_gpkg),
                                    s_gpkg_file
                                )
                            ),
                            new TestHTTPAsyncGetStageHandler()
                        )
                    );
                }
            }

            Log.d(TAG, String.format("asyncget_via_executor__total_success: awaiting queued downloads to complete..."));
            queuedDownloadObserver.waitUntilAllDownloadsFinish();
            Log.d(TAG, String.format("asyncget_via_executor__total_success: queuedDownloadObserver.queue_empty_monitor notified"));

            //delete gpkg-bundle dir (and all files/subdirectories within it)
            Log.d(TAG, String.format("asyncget_via_executor__total_success: removing root geopackage-bundle..."));
            File dir_gpkg_bundle_root = null;
            try {
                dir_gpkg_bundle_root = GPKG.Local.F_GPKG_BUNDLE_ROOT_DIR.getInstance(InstrumentationRegistry.getContext());
                Log.d(TAG, String.format("asyncget_via_executor__total_success: removing root geopackage-bundle: %s", dir_gpkg_bundle_root.getCanonicalPath()));
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            assertTrue(dir_gpkg_bundle_root.exists());
            try {
                Files.delete(dir_gpkg_bundle_root);
            } catch (IOException e) {
                e.printStackTrace();
            }
            assertTrue(!dir_gpkg_bundle_root.exists());
        }

        Log.d(TAG, String.format("asyncget_via_executor__total_success: shutting down HTTP.AsyncGet.ExecutorService"));
        exec_svc.shutdown();
    }

    @Test
    public void asyncget_via_executor__total_failure() throws Exception {
        TestHTTPAsyncGetQueuedDownloadObserver queuedDownloadObserver = new TestHTTPAsyncGetQueuedDownloadObserver();
        HTTP.AsyncGet.ExecutorService exec_svc = new HTTP.AsyncGet.ExecutorService(
            (runnable, threadPoolExecutor) -> {
                Log.d(TAG, String.format("rejectedExecution - %s", runnable.getClass().getCanonicalName()));
            },
            queuedDownloadObserver
        );

        String s_url_root = "https://sacontreras.github.io/tegola-mobile-gpkg"; //bad url

        LinkedHashMap<String, ArrayList<String>> gpkg_props_map = new LinkedHashMap<>();
        gpkg_props_map.put("athens", new ArrayList<String>(){{add("default.properties"); add("large-layers-partitioned.properties"); add("test-minimal.properties");}});
        gpkg_props_map.put("harare", new ArrayList<String>(){{add("default.properties");}});
        assertEquals(2, gpkg_props_map.size());

        if (gpkg_props_map.size() > 0) {
            File files_dir = InstrumentationRegistry.getContext().getFilesDir();

            Log.d(TAG, String.format("asyncget_via_executor__total_failure: starting queued downloads..."));
            for (String s_gpkg : gpkg_props_map.keySet()) {
                for (String s_gpkg_file : gpkg_props_map.get(s_gpkg)) {
                    exec_svc.submit(
                        new HTTP.AsyncGet.CallableTask(
                            new HTTP.AsyncGet.HttpUrl_To_Local_File(
                                HttpUrl.parse(
                                    String.format(
                                        "%s/%s/%s",
                                        s_url_root,
                                        s_gpkg, s_gpkg_file
                                    )
                                ),
                                new File(
                                    build_local_gpkg_bundle_path_string(s_gpkg),
                                    s_gpkg_file
                                )
                            ),
                            new TestHTTPAsyncGetStageHandler()
                        )
                    );
                }
            }

            Log.d(TAG, String.format("asyncget_via_executor__total_failure: awaiting queued downloads to complete..."));
            queuedDownloadObserver.waitUntilAllDownloadsFinish();
            Log.d(TAG, String.format("asyncget_via_executor__total_failure: queuedDownloadObserver.queue_empty_monitor notified"));
        }

        Log.d(TAG, String.format("asyncget_via_executor__total_failure: shutting down HTTP.AsyncGet.ExecutorService"));
        exec_svc.shutdown();
    }

    @Test
    public void asyncget_via_executor__partial_success_failure() throws Exception {
        TestHTTPAsyncGetQueuedDownloadObserver queuedDownloadObserver = new TestHTTPAsyncGetQueuedDownloadObserver();
        HTTP.AsyncGet.ExecutorService exec_svc = new HTTP.AsyncGet.ExecutorService(
            (runnable, threadPoolExecutor) -> {
                Log.d(TAG, String.format("rejectedExecution - %s", runnable.getClass().getCanonicalName()));
            },
            queuedDownloadObserver
        );

        String s_url_root = "https://sacontreras.github.io/tegola-mobile-gpkg-bundle";

        LinkedHashMap<String, ArrayList<String>> gpkg_props_map = new LinkedHashMap<>();
        gpkg_props_map.put("athens", new ArrayList<String>(){{add("default.properties"); add("large-layers-partitioned.properties"); add("test-minimal.properties");}});
        gpkg_props_map.put("harare", new ArrayList<String>(){{add("default.properties");add("this-file-does-not-exist.properties");}});
        assertEquals(2, gpkg_props_map.size());

        if (gpkg_props_map.size() > 0) {
            File files_dir = InstrumentationRegistry.getContext().getFilesDir();

            Log.d(TAG, String.format("asyncget_via_executor__partial_success_failure: starting queued downloads..."));
            for (String s_gpkg : gpkg_props_map.keySet()) {
                for (String s_gpkg_file : gpkg_props_map.get(s_gpkg)) {
                    exec_svc.submit(
                        new HTTP.AsyncGet.CallableTask(
                            new HTTP.AsyncGet.HttpUrl_To_Local_File(
                                HttpUrl.parse(
                                    String.format(
                                        "%s/%s/%s",
                                        s_url_root,
                                        s_gpkg, s_gpkg_file
                                    )
                                ),
                                new File(
                                    build_local_gpkg_bundle_path_string(s_gpkg),
                                    s_gpkg_file
                                )
                            ),
                            new TestHTTPAsyncGetStageHandler()
                        )
                    );
                }
            }

            Log.d(TAG, String.format("asyncget_via_executor__partial_success_failure: awaiting queued downloads to complete..."));
            queuedDownloadObserver.waitUntilAllDownloadsFinish();
            Log.d(TAG, String.format("asyncget_via_executor__partial_success_failure: queuedDownloadObserver.queue_empty_monitor notified"));

            //delete gpkg-bundle dir (and all files/subdirectories within it)
            Log.d(TAG, String.format("asyncget_via_executor__partial_success_failure: removing root geopackage-bundle..."));
            File dir_gpkg_bundle_root = null;
            try {
                dir_gpkg_bundle_root = GPKG.Local.F_GPKG_BUNDLE_ROOT_DIR.getInstance(InstrumentationRegistry.getContext());
                Log.d(TAG, String.format("asyncget_via_executor__partial_success_failure: removing root geopackage-bundle: %s", dir_gpkg_bundle_root.getCanonicalPath()));
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            assertTrue(dir_gpkg_bundle_root.exists());
            try {
                Files.delete(dir_gpkg_bundle_root);
            } catch (IOException e) {
                e.printStackTrace();
            }
            assertTrue(!dir_gpkg_bundle_root.exists());
        }

        Log.d(TAG, String.format("asyncget_via_executor__partial_success_failure: shutting down HTTP.AsyncGet.ExecutorService"));
        exec_svc.shutdown();
    }
}

