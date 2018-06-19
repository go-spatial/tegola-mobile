package go_spatial.com.github.tegola.mobile.android.controller;

import android.os.Handler;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class InstrumentedTests_Ctrlr_ClientAPI {
    final private String TAG = InstrumentedTests_Ctrlr_ClientAPI.class.getCanonicalName();

    @Test
    public void notification_handler_using_main_looper() {
        TestControllerNotificationsListener listener = new TestControllerNotificationsListener();

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
        Log.d(TAG, "notification_handler_using_main_looper: listener.mon_ctrlr_running.getNotified(): " + listener.mon_ctrlr_running.getNotified());
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
        Log.d(TAG, "notification_handler_using_main_looper: listener.mon_ctrlr_running.getNotified(): " + listener.mon_ctrlr_running.getNotified());
        assertTrue(listener.mon_ctrlr_running.getNotified() == 1);
        listener.mon_ctrlr_running.setNotified(0);
        assertTrue(!listener.ctrlr_running);

        ClientAPI.uninitClient(controllerClient);
    }

    @Test
    public void notification_handler_using_wrkr_thrd_looper() {
        TestControllerNotificationsListener listener = new TestControllerNotificationsListener();

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
        Log.d(TAG, "notification_handler_using_wrkr_thrd_looper: listener.mon_ctrlr_running.getNotified(): " + listener.mon_ctrlr_running.getNotified());
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
        Log.d(TAG, "notification_handler_using_wrkr_thrd_looper: listener.mon_ctrlr_running.getNotified(): " + listener.mon_ctrlr_running.getNotified());
        assertTrue(listener.mon_ctrlr_running.getNotified() == 1);
        listener.mon_ctrlr_running.setNotified(0);
        assertTrue(!listener.ctrlr_running);

        ClientAPI.uninitClient(controllerClient);
        assertTrue(!controllerClient.getRcvr_hndlr_wrkr_thrd().isAlive());
    }
}
