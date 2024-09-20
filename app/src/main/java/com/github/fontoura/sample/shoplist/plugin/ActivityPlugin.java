package com.github.fontoura.sample.shoplist.plugin;

import android.os.Bundle;
import android.os.PersistableBundle;

public interface ActivityPlugin {

    default void onCreate(Bundle bundle) {
    }

    default void onStart() {
    }

    default void onResume() {
    }

    default void onPause() {
    }

    default void onStop() {
    }

    default void onSaveInstanceState(Bundle bundle, PersistableBundle persistentState) {
    }

    default void onDestroy() {
    }
}
