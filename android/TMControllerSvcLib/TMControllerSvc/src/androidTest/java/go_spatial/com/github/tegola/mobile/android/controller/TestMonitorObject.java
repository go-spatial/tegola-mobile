package go_spatial.com.github.tegola.mobile.android.controller;

import android.util.Log;

public class TestMonitorObject extends Object {
    final private String TAG = TestMonitorObject.class.getCanonicalName();

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

    public TestMonitorObject() {}

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
