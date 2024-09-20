package com.github.fontoura.sample.shoplist.plugin;

import android.os.Handler;
import android.os.Looper;

import java.util.SortedSet;
import java.util.TreeSet;

public class TimerPlugin implements ActivityPlugin {

    private SortedSet<TimerAction<?>> actions = new TreeSet<>((t1, t2) -> Long.compare(t1.nextTime, t2.nextTime));
    private volatile boolean running = false;
    private volatile boolean shouldBeActive = false;

    @Override
    public void onResume() {
        ActivityPlugin.super.onResume();
        synchronized (this) {
            shouldBeActive = true;
            if (actions.size() > 0 && !running) {
                running = true;
                new Thread(this::run).start();
            }
        }
    }

    @Override
    public void onPause() {
        synchronized (this) {
            shouldBeActive = false;
            if (running) {
                this.notifyAll();
            }
        }
    }

    public boolean isBackgroundThreadRunning() {
        return running;
    }

    public <T> void setTimeout(BackgroundTask<T> task, long interval) {
        addTask(task, interval, false);
    }

    public <T> void setInterval(BackgroundTask<T> task, long interval) {
        addTask(task, interval, true);
    }

    private <T> void addTask(BackgroundTask<T> task, long interval, boolean intervalTask) {
        TimerAction<T> action = new TimerAction<>(task, interval, intervalTask);

        boolean start = false;
        synchronized (this) {
            actions.add(action);
            if (!running) {
                if (shouldBeActive) {
                    start = true;
                    running = true;
                }
            } else if (actions.first() == action) {
                this.notifyAll();
            }
        }

        if (start) {
            new Thread(this::run).start();
        }
    }

    private void run() {
        while (true) {
            TimerAction<?> action;
            long now = System.currentTimeMillis();
            synchronized (this) {
                if (!shouldBeActive || actions.isEmpty()) {
                    running = false;
                    return;
                }

                action = actions.first();
                if (now < action.nextTime) {
                    long waitTime = action.nextTime - now;
                    try {
                        this.wait(waitTime);
                    } catch (InterruptedException e) {
                        // do nothing
                    }
                    continue;
                }

                actions.remove(action);
            }

            action.run();

            synchronized (this) {
                if (action.interval > 0 && !action.canceled) {
                    action.nextTime = now + action.interval;
                    actions.add(action);
                }
            }
        }
    }

    private static class TimerAction<T> {
        final BackgroundTask<T> task;;
        final long interval;

        volatile long nextTime;
        volatile boolean canceled = false;

        public TimerAction(BackgroundTask<T> task, long timeout, boolean interval) {
            this.task = task;
            if (interval) {
                this.interval = timeout;
            } else {
                this.interval = 0;
            }
            this.nextTime = System.currentTimeMillis() + timeout;
        }


        void run() {
            T value;
            try {
                value = task.doInBackground();
            } catch (Throwable t) {
                new Handler(Looper.getMainLooper()).post(() -> task.handleInForeground(t));
                return;
            }
            new Handler(Looper.getMainLooper()).post(() -> task.continueInForeground(value));
        }
    }
}
