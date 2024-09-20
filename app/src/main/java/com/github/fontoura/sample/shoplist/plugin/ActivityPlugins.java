package com.github.fontoura.sample.shoplist.plugin;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class ActivityPlugins {

    private boolean created = false;
    private final List<ActivityPlugin> pluginList;
    private final String logTag;

    private ActivityPlugins(ActivityPluginsBuilder builder) {
        this.logTag = builder.logTag != null ? builder.logTag : ActivityPlugins.class.getName();
        this.pluginList = builder.pluginList;
    }

    public static ActivityPluginsBuilder builder() {
        return new ActivityPluginsBuilder();
    }

    public void onCreate(Bundle bundle) {
        created = true;
        for (ActivityPlugin plugin : pluginList) {
            try {
                plugin.onCreate(bundle);
            } catch (Exception e) {
                Log.e(logTag, String.format("Failed to invoke onCreate on plugin %s", plugin), e);
            }
        }
    }

    public void onStart() {
        if (!created) {
            return;
        }

        for (ActivityPlugin plugin : pluginList) {
            try {
                plugin.onStart();
            } catch (Exception e) {
                Log.e(logTag, String.format("Failed to invoke onStart on plugin %s", plugin), e);
            }
        }
    }

    public void onResume() {
        if (!created) {
            return;
        }

        for (ActivityPlugin plugin : pluginList) {
            try {
                plugin.onResume();
            } catch (Exception e) {
                Log.e(logTag, String.format("Failed to invoke onResume on plugin %s", plugin), e);
            }
        }
    }

    public void onPause() {
        if (!created) {
            return;
        }

        for (ActivityPlugin plugin : pluginList) {
            try {
                plugin.onPause();
            } catch (Exception e) {
                Log.e(logTag, String.format("Failed to invoke onPause on plugin %s", plugin), e);
            }
        }
    }

    public void onStop() {
        if (!created) {
            return;
        }

        for (ActivityPlugin plugin : pluginList) {
            try {
                plugin.onStop();
            } catch (Exception e) {
                Log.e(logTag, String.format("Failed to invoke onStop on plugin %s", plugin), e);
            }
        }
    }

    public void onSaveInstanceState(Bundle bundle, PersistableBundle persistentState) {
        if (!created) {
            return;
        }

        for (ActivityPlugin plugin : pluginList) {
            try {
                plugin.onSaveInstanceState(bundle, persistentState);
            } catch (Exception e) {
                Log.e(logTag, String.format("Failed to invoke onSaveInstanceState on plugin %s", plugin), e);
            }
        }
    }

    public void onDestroy() {
        if (!created) {
            return;
        }
        created = false;

        for (ActivityPlugin plugin : pluginList) {
            try {
                plugin.onDestroy();
            } catch (Exception e) {
                Log.e(logTag, String.format("Failed to invoke onDestroy on plugin %s", plugin), e);
            }
        }
    }

    public static class ActivityPluginsBuilder {
        private String logTag;
        private final List<ActivityPlugin> pluginList = new ArrayList<>();

        public ActivityPluginsBuilder withLogTag(String logTag) {
            if (logTag == null) {
                throw new NullPointerException("The log tag cannot be null");
            }
            this.logTag = logTag;
            return this;
        }

        public ActivityPluginsBuilder withPlugin(ActivityPlugin plugin) {
            if (plugin == null) {
                throw new NullPointerException("The plugin cannot be null");
            }
            if (pluginList.contains(plugin)) {
                throw new IllegalArgumentException("The plugin has already been added");
            }
            pluginList.add(plugin);
            return this;
        }

        public ActivityPlugins build() {
            return new ActivityPlugins(this);
        }
    }
}
