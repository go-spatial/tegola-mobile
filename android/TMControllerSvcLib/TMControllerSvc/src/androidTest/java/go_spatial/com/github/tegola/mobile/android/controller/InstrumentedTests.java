package go_spatial.com.github.tegola.mobile.android.controller;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class InstrumentedTests {
    private final String TAG = InstrumentedTests.class.getCanonicalName();
    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        Log.d(TAG, "useAppContext: appContext.getPackageName()==\"" + appContext.getPackageName() + "\"");
        assertEquals("go_spatial.com.github.tegola.mobile.android.controller.test", appContext.getPackageName());
    }
}
