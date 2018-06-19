package go_spatial.com.github.tegola.mobile.android.controller;

import android.support.annotation.NonNull;
import android.util.Log;

public class TestControllerNotificationsListener implements ClientAPI.ControllerNotificationsListener {
    final private String TAG = TestControllerNotificationsListener.class.getSimpleName();

    final public int WAIT_TIMEOUT_MS = 10000;   //hardcode to 10 sec

    final public TestMonitorObject mon_ctrlr_running = new TestMonitorObject();
    public boolean ctrlr_running = false;

    final public TestMonitorObject mon_mvt_srvr_running = new TestMonitorObject();
    public boolean mvt_srvr_running = false;

    final public TestMonitorObject mon_mvt_srvr_listening = new TestMonitorObject();
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
}
