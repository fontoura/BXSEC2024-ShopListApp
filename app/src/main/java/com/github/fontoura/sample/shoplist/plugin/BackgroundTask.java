package com.github.fontoura.sample.shoplist.plugin;

public interface BackgroundTask<T> {
    T doInBackground() throws Throwable;

    void continueInForeground(T value);

    void handleInForeground(Throwable t);
}
