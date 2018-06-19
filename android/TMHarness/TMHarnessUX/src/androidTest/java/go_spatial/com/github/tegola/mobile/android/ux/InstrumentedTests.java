package go_spatial.com.github.tegola.mobile.android.ux;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Rule;
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

    private class MonitorObject extends Object {
        final private String TAG = MonitorObject.class.getCanonicalName();

        private int m_notified = 0;
        final public Integer getNotified() {
            synchronized (this) {
                return m_notified;
            }
        }
        final public void setNotified(final int notified) {
            synchronized (this) {
                Log.d(TAG, "setNotified: " + notified);
                m_notified = notified;
            }
        }

        public MonitorObject() {}

        public void track_notify() {
            synchronized (this) {
                int notified = getNotified();
                Log.d(TAG, "track_notify: getNotified(): " + notified + "; increment to: " + (notified + 1));
                setNotified(notified + 1);
                notify();
            }
        }

        public void track_notifyAll() {
            synchronized (this) {
                int notified = getNotified();
                Log.d(TAG, "track_notifyAll: getNotified(): " + notified + "; increment to: " + (notified + 1));
                setNotified(notified + 1);
                notifyAll();
            }
        }
    }

//    @Rule
//    public ActivityTestRule<MainActivity> activityRule = new ActivityTestRule<>(MainActivity.class);
//    @Rule
//    public ActivityTestRule<InstallGpkgBundleActivity> activityRule = new ActivityTestRule<>(InstallGpkgBundleActivity.class);

    private class TestNotificationBroadcastReceiverListener implements NotificationBroadcastReceiver.Listener {
        final private String TAG = TestNotificationBroadcastReceiverListener.class.getSimpleName();

        final private int WAIT_TIMEOUT_MS = 10000;   //hardcode to 10 sec

        final public MonitorObject mon_ctrlr_running = new MonitorObject();
        public boolean ctrlr_running = false;

        final public MonitorObject mon_mvt_srvr_running = new MonitorObject();
        public boolean mvt_srvr_running = false;

        final public MonitorObject mon_mvt_srvr_listening = new MonitorObject();
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
                mon_ctrlr_running.track_notifyAll();
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
                mon_ctrlr_running.track_notifyAll();
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
                mon_mvt_srvr_running.track_notifyAll();
            }
        }

        @Override
        public void OnMVTServerRunning(int pid) {
            Log.d(TAG, "OnMVTServerRunning");
            synchronized (mon_mvt_srvr_running) {
                mvt_srvr_running = true;
                mon_mvt_srvr_running.track_notifyAll();
            }
        }

        @Override
        public void OnMVTServerListening(int port) {
            Log.d(TAG, "OnMVTServerListening");
            synchronized (mon_mvt_srvr_listening) {
                mvt_srvr_listening = true;
                mon_mvt_srvr_listening.track_notifyAll();
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
                mon_mvt_srvr_running.track_notifyAll();
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
                listener.mon_ctrlr_running.wait(listener.WAIT_TIMEOUT_MS);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "testClientAPI__br_main_looper: listener.mon_ctrlr_running.getNotified(): " + listener.mon_ctrlr_running.getNotified());
        assertTrue(listener.mon_ctrlr_running.getNotified() == 1);
        listener.mon_ctrlr_running.setNotified(0);
        assertTrue(listener.ctrlr_running);

        controllerClient.controller__stop();
        try {
            synchronized (listener.mon_ctrlr_running) {
                listener.mon_ctrlr_running.wait(listener.WAIT_TIMEOUT_MS);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "testClientAPI__br_main_looper: listener.mon_ctrlr_running.getNotified(): " + listener.mon_ctrlr_running.getNotified());
        assertTrue(listener.mon_ctrlr_running.getNotified() == 1);
        listener.mon_ctrlr_running.setNotified(0);
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
                listener.mon_ctrlr_running.wait(listener.WAIT_TIMEOUT_MS);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "testClientAPI__br_wrk_thrd_looper: listener.mon_ctrlr_running.getNotified(): " + listener.mon_ctrlr_running.getNotified());
        assertTrue(listener.mon_ctrlr_running.getNotified() == 1);
        listener.mon_ctrlr_running.setNotified(0);
        assertTrue(listener.ctrlr_running);

        controllerClient.controller__stop();
        try {
            synchronized (listener.mon_ctrlr_running) {
                listener.mon_ctrlr_running.wait(listener.WAIT_TIMEOUT_MS);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "testClientAPI__br_wrk_thrd_looper: listener.mon_ctrlr_running.getNotified(): " + listener.mon_ctrlr_running.getNotified());
        assertTrue(listener.mon_ctrlr_running.getNotified() == 1);
        listener.mon_ctrlr_running.setNotified(0);
        assertTrue(!listener.ctrlr_running);

        ClientAPI.uninitClient(controllerClient);
        assertTrue(!controllerClient.getRcvr_hndlr_wrkr_thrd().isAlive());
    }
}
