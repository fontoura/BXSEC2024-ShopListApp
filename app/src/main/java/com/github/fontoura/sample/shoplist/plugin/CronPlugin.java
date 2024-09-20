package com.github.fontoura.sample.shoplist.plugin;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CronPlugin<T> implements ActivityPlugin {

    private final BackgroundTask<T> asyncTask;
    private final long interval;

    private Handler mainHandler;
    private IntervalRunner intervalRunner;

    @Override
    public void onCreate(Bundle bundle) {
        mainHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void onResume() {
        intervalRunner = new IntervalRunner();
        new Thread(intervalRunner).start();
    }

    @Override
    public void onPause() {
        intervalRunner.stop();
        intervalRunner = null;
    }

    private class IntervalRunner implements Runnable {
        private volatile boolean stopping;

        void stop() {
            stopping = true;
            synchronized (this) {
                this.notifyAll();
            }
        }

        @Override
        public void run() {
            Long lastTime = null;
            while (!stopping) {
                long now = System.currentTimeMillis();
                if (lastTime != null) {
                    long nextTimeout = lastTime + interval;
                    long sleepTime = nextTimeout - now;
                    if (sleepTime > 0) {
                        try {
                            synchronized (this) {
                                this.wait(sleepTime);
                            }
                        } catch (InterruptedException e) {
                            continue;
                        }
                    }

                    lastTime = nextTimeout;
                } else {
                    lastTime = now;
                }

                T value;
                try {
                    value = asyncTask.doInBackground();
                } catch (Throwable t) {
                    mainHandler.post(() -> asyncTask.handleInForeground(t));
                    continue;
                }
                mainHandler.post(() -> asyncTask.continueInForeground(value));
            }
        }
    }
}
