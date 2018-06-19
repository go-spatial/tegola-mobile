package go_spatial.com.github.tegola.mobile.android.ux;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class InstrumentedTests_UX_Basic {
    final private String TAG = InstrumentedTests_UX_Basic.class.getCanonicalName();

//    @Rule
//    public ActivityTestRule<MainActivity> activityRule = new ActivityTestRule<>(MainActivity.class);
//    @Rule
//    public ActivityTestRule<InstallGpkgBundleActivity> activityRule = new ActivityTestRule<>(InstallGpkgBundleActivity.class);

    @Test
    public void useAppContext() throws Exception {
        Context appContext = InstrumentationRegistry.getTargetContext();
        assertEquals("go_spatial.com.github.tegola.mobile.android.ux", appContext.getPackageName());
    }
}
