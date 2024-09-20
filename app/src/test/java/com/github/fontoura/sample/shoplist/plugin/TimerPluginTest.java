package com.github.fontoura.sample.shoplist.plugin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class TimerPluginTest {

    @Test
    public void testSchedule() throws InterruptedException {
        AtomicInteger counter = new AtomicInteger(0);
        TimerPlugin timerPlugin = new TimerPlugin();
        timerPlugin.onResume();
        timerPlugin.setTimeout(new BackgroundTask<Void>() {
            @Override
            public Void doInBackground() {
                counter.set(1);
                return null;
            }

            @Override
            public void continueInForeground(Void value) {
            }

            @Override
            public void handleInForeground(Throwable t) {
            }
        }, 100);
        Thread.sleep(50);
        assertEquals(0, counter.get());
        assertTrue(timerPlugin.isBackgroundThreadRunning());
        Thread.sleep(100);
        assertEquals(1, counter.get());
        assertFalse(timerPlugin.isBackgroundThreadRunning());
    }
}