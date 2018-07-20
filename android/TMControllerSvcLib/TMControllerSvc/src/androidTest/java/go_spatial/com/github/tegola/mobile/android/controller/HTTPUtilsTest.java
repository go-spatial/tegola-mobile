package go_spatial.com.github.tegola.mobile.android.controller;

import android.support.test.InstrumentationRegistry;
import android.util.Log;

import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

import go_spatial.com.github.tegola.mobile.android.controller.utils.HTTP;
import okhttp3.HttpUrl;

import static org.junit.Assert.assertEquals;

public class HTTPUtilsTest {
    private final String TAG = HTTPUtilsTest.class.getSimpleName();

    @Test
    public void asyncget_via_executor() throws Exception {
        HTTPAsyncGetExecutorService exec_svc = new HTTPAsyncGetExecutorService(new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable runnable, ThreadPoolExecutor threadPoolExecutor) {
                HTTP.AsyncGet.RunnableTask asynchttpgetrunnabletask = (HTTP.AsyncGet.RunnableTask)runnable;
                Log.d(TAG, String.format("rejectedExecution - httprequest: %s", asynchttpgetrunnabletask.get_http_get_call().request().toString()));
            }
        });

        LinkedHashMap<String, ArrayList<String>> gpkg_props_map = new LinkedHashMap<>();
        gpkg_props_map.put("athens", new ArrayList<String>(){{add("default.properties"); add("large-layers-partitioned.properties"); add("test-minimal.properties");}});
        gpkg_props_map.put("harare", new ArrayList<String>(){{add("default.properties");}});
        assertEquals(2, gpkg_props_map.size());

        ArrayList<Future<?>> al_future_httpasyncget = new ArrayList<>();
        if (gpkg_props_map.size() > 0) {
            Future<?> future_httpasyncget = null;
            String s_url_root = "https://sacontreras.github.io/tegola-mobile-gpkg-bundle";

            File files_dir = InstrumentationRegistry.getContext().getFilesDir();

            Log.d(TAG, String.format("asyncget_via_executor: starting queued downloads..."));
            for (String s_gpkg : gpkg_props_map.keySet()) {
                for (String s_gpkg_file : gpkg_props_map.get(s_gpkg)) {
                    future_httpasyncget = exec_svc.submit(
                        new HTTP.AsyncGet.CallableTask(
                            new HTTP.AsyncGet.HttpUrl_To_Local_File(
                                HttpUrl.parse(String.format("%s/%s/%s", s_url_root, s_gpkg, s_gpkg_file)),
                                new File(files_dir, String.format("%s-%s", s_gpkg, s_gpkg_file))
                            ),
                            new TestHTTPAsyncGetStageHandler()
                        )
                    );
                    al_future_httpasyncget.add(future_httpasyncget);
                }
            }
            assertEquals(4, al_future_httpasyncget.size());

            //now wait for queued downloads to complete
            for (Future<?> queued_download : al_future_httpasyncget) {
                HTTP.AsyncGet.HttpUrl_To_Local_File url_to_local_file = (HTTP.AsyncGet.HttpUrl_To_Local_File)queued_download.get();
                String url = url_to_local_file.get_url().toString();
                File f = url_to_local_file.get_file();
                String file_path = f.getCanonicalPath();
                boolean file_exists = f.exists();
                Log.d(
                    TAG,
                    String.format(
                        "asyncget_via_executor: queued (task) download of %s to local file %s is COMPLETE - local file exists: %b",
                        url,
                        file_path,
                        file_exists
                    )
                );
                assertEquals(true, file_exists);

                if (file_exists) {
                    file_exists = !f.delete();
                    Log.d(
                        TAG,
                        String.format(
                            "asyncget_via_executor: AFTER queued (task) download of %s to local file %s, attempt to delete local file - local file exists: %b",
                            url,
                            file_path,
                            file_exists
                        )
                    );
                    assertEquals(false, file_exists);
                }
            }

            exec_svc.shutdown();
        }
    }
}
