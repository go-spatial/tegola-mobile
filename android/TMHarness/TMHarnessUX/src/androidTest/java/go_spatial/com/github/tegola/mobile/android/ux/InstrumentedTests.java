package go_spatial.com.github.tegola.mobile.android.ux;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Test;
import org.junit.runner.RunWith;

import go_spatial.com.github.tegola.mobile.android.controller.ClientAPI;
import go_spatial.com.github.tegola.mobile.android.controller.NotificationBroadcastReceiver;

import static org.junit.Assert.*;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class InstrumentedTests {
    final private String TAG = InstrumentedTests.class.getCanonicalName();

//    @Rule
//    public ActivityTestRule<MainActivity> activityRule = new ActivityTestRule<>(MainActivity.class);

    private class TestNotificationBroadcastReceiverListener implements NotificationBroadcastReceiver.Listener {
        final private String TAG = TestNotificationBroadcastReceiverListener.class.getSimpleName();

        final public Object mon_ctrlr_running = new Object();
        public boolean ctrlr_running = false;

        final public Object mon_mvt_srvr_running = new Object();
        public boolean mvt_srvr_running = false;

        final public Object mon_mvt_srvr_listening = new Object();
        public boolean mvt_srvr_listening = false;

        @Override
        public void OnControllerStarting() {
            Log.d(TAG, "OnControllerStarting");
        }

        @Override
        public void OnControllerRunning() {
            Log.d(TAG, "OnControllerRunning");
            synchronized (mon_ctrlr_running) {
                ctrlr_running = true;
                mon_ctrlr_running.notifyAll();
            }
        }

        @Override
        public void OnControllerStopping() {
            Log.d(TAG, "OnControllerStopping");
        }

        @Override
        public void OnControllerStopped() {
            Log.d(TAG, "OnControllerStopped");
            synchronized (mon_ctrlr_running) {
                ctrlr_running = false;
                mon_ctrlr_running.notifyAll();
            }
        }

        @Override
        public void OnMVTServerStarting() {
            Log.d(TAG, "OnMVTServerStarting");
        }

        @Override
        public void OnMVTServerStartFailed(@NonNull String reason) {
            Log.d(TAG, "OnMVTServerStartFailed");
            synchronized (mon_mvt_srvr_running) {
                mvt_srvr_running = false;
                mon_mvt_srvr_running.notifyAll();
            }
        }

        @Override
        public void OnMVTServerRunning(int pid) {
            Log.d(TAG, "OnMVTServerRunning");
            synchronized (mon_mvt_srvr_running) {
                mvt_srvr_running = true;
                mon_mvt_srvr_running.notifyAll();
            }
        }

        @Override
        public void OnMVTServerListening(int port) {
            Log.d(TAG, "OnMVTServerListening");
            synchronized (mon_mvt_srvr_listening) {
                mvt_srvr_listening = true;
                mon_mvt_srvr_listening.notifyAll();
            }
        }

        @Override
        public void OnMVTServerOutputLogcat(@NonNull String logcat_line) {
            Log.d(TAG, "OnMVTServerOutputLogcat - \"" + logcat_line + "\"");
        }

        @Override
        public void OnMVTServerOutputStdErr(@NonNull String stderr_line) {
            Log.d(TAG, "OnMVTServerOutputStdErr - \"" + stderr_line + "\"");
        }

        @Override
        public void OnMVTServerOutputStdOut(@NonNull String stdout_line) {
            Log.d(TAG, "OnMVTServerOutputStdOut - \"" + stdout_line + "\"");
        }

        @Override
        public void OnMVTServerJSONRead(@NonNull String tegola_url_root, @NonNull String json_url_endpoint, @NonNull String json, @NonNull String purpose) {
            Log.d(TAG, "OnMVTServerJSONRead");
        }

        @Override
        public void OnMVTServerJSONReadFailed(@NonNull String tegola_url_root, @NonNull String json_url_endpoint, @NonNull String purpose, @NonNull String reason) {
            Log.d(TAG, "OnMVTServerJSONReadFailed");
        }

        @Override
        public void OnMVTServerStopping() {
            Log.d(TAG, "OnMVTServerStopping");
        }

        @Override
        public void OnMVTServerStopped() {
            Log.d(TAG, "OnMVTServerStopped");
            synchronized (mon_mvt_srvr_running) {
                mvt_srvr_running = false;
                mon_mvt_srvr_running.notifyAll();
            }
        }
    };

    @Test
    public void useAppContext() throws Exception {
        Context appContext = InstrumentationRegistry.getTargetContext();
        assertEquals("go_spatial.com.github.tegola.mobile.android.ux", appContext.getPackageName());
    }

    @Test
    public void testClientAPI__br_main_looper() {
        TestNotificationBroadcastReceiverListener listener = new TestNotificationBroadcastReceiverListener();

        ClientAPI.Client controllerClient = ClientAPI.initClient(
            InstrumentationRegistry.getTargetContext(),
            listener,
            new Handler(InstrumentationRegistry.getTargetContext().getMainLooper())
        );
        assertTrue(
            controllerClient != null
            && controllerClient.getContext() != null
            && controllerClient.getNotificationListener() != null
            && controllerClient.getRcvr_hndlr_wrkr_thrd() == null
            && controllerClient.getRcvr_hndlr() != null
        );

        controllerClient.controller__start(controllerClient.getContext().getClass().getName());
        try {
            synchronized (listener.mon_ctrlr_running) {
                listener.mon_ctrlr_running.wait(1000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertTrue(listener.ctrlr_running);

        controllerClient.controller__stop();
        try {
            synchronized (listener.mon_ctrlr_running) {
                listener.mon_ctrlr_running.wait(1000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertTrue(!listener.ctrlr_running);

        ClientAPI.uninitClient(controllerClient);
    }
    @Test
    public void testClientAPI__br_wrk_thrd_looper() {
        TestNotificationBroadcastReceiverListener listener = new TestNotificationBroadcastReceiverListener();

        ClientAPI.Client controllerClient = ClientAPI.initClient(
            InstrumentationRegistry.getTargetContext(),
            listener
        );
        assertTrue(
            controllerClient != null
            && controllerClient.getContext() != null
            && controllerClient.getNotificationListener() != null
            && controllerClient.getRcvr_hndlr_wrkr_thrd() != null
            && controllerClient.getRcvr_hndlr() != null
        );
        assertTrue(controllerClient.getRcvr_hndlr_wrkr_thrd().isAlive());

        controllerClient.controller__start(controllerClient.getContext().getClass().getName());
        try {
            synchronized (listener.mon_ctrlr_running) {
                listener.mon_ctrlr_running.wait(1000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertTrue(listener.ctrlr_running);

        controllerClient.controller__stop();
        try {
            synchronized (listener.mon_ctrlr_running) {
                listener.mon_ctrlr_running.wait(1000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertTrue(!listener.ctrlr_running);

        ClientAPI.uninitClient(controllerClient);
        assertTrue(!controllerClient.getRcvr_hndlr_wrkr_thrd().isAlive());
    }
}
