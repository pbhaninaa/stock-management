package com.example.finance;

import android.content.Context;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.assertEquals;

/**
 * Instrumented test, which will execute on an Android device or emulator.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)  // Use AndroidJUnit4 to run the test on an Android device/emulator
public class ExampleInstrumentedTest {

    @Test
    public void useAppContext() {
        // Context of the app under test
        Context appContext = ApplicationProvider.getApplicationContext();

        // Verify the package name of the app under test
        assertEquals("com.example.finance", appContext.getPackageName());
    }
}
