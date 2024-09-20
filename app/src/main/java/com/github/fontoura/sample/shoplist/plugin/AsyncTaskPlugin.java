package com.github.fontoura.sample.shoplist.plugin;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lombok.AllArgsConstructor;

public class AsyncTaskPlugin implements ActivityPlugin {

    private ExecutorService executorService;
    private Handler mainHandler;

    public <T> void doAsync(BackgroundTask<T> asyncTask) {
        executorService.execute(new BackgroundTaskRunnable<>(asyncTask, mainHandler));
    }

    @Override
    public void onCreate(Bundle bundle) {
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void onDestroy() {
        executorService.shutdown();
    }

    @AllArgsConstructor
    private static final class BackgroundTaskRunnable<T> implements Runnable {
        private final BackgroundTask<T> asyncTask;
        private final Handler mainHandler;

        @Override
        public void run() {
            T value;
            try {
                value = asyncTask.doInBackground();
            } catch (Throwable t) {
                mainHandler.post(() -> asyncTask.handleInForeground(t));
                return;
            }
            mainHandler.post(() -> asyncTask.continueInForeground(value));
        }

    }
}
