package com.github.fontoura.sample.shoplist;

import android.os.Bundle;
import android.os.PersistableBundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.github.fontoura.sample.shoplist.databinding.ActivityShoplistBinding;
import com.github.fontoura.sample.shoplist.plugin.ActivityPlugins;
import com.github.fontoura.sample.shoplist.plugin.AsyncTaskPlugin;
import com.github.fontoura.sample.shoplist.plugin.HttpConnectionPlugin;
import com.github.fontoura.sample.shoplist.plugin.TimerPlugin;
import com.github.fontoura.sample.shoplist.tasks.IntegrityCheckTask;
import com.github.fontoura.sample.shoplist.utils.DetectionUtils;
import com.github.fontoura.sample.shoplist.utils.FileUtils;

import lombok.Getter;

public class ShopListActivity extends AppCompatActivity  {

    private static final String TAG = ShopListActivity.class.getName();

    private ActivityShoplistBinding binding;

    @Getter
    public HttpConnectionPlugin httpConnectionPlugin = new HttpConnectionPlugin(this)
            .withPinningEnabled(true);

    @Getter
    public AsyncTaskPlugin asyncTaskPlugin = new AsyncTaskPlugin();

    public TimerPlugin timerPlugin = new TimerPlugin();

    private IntegrityCheckTask integrityCheckTask = new IntegrityCheckTask(this);

    @Getter
    private final ActivityPlugins activityPlugins = ActivityPlugins.builder()
            .withLogTag(TAG)
            .withPlugin(httpConnectionPlugin)
            .withPlugin(asyncTaskPlugin)
            .withPlugin(timerPlugin)
            .build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityPlugins.onCreate(savedInstanceState);

        binding = ActivityShoplistBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        integrityCheckTask.withCheck(() -> !FileUtils.anyFilesExist(DetectionUtils.ROOT_FILE_PATHS));
        timerPlugin.setTimeout(integrityCheckTask, 5000);

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_dashboard, R.id.navigation_settings)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        integrityCheckTask.withCheck(() -> httpConnectionPlugin.isPinningEnabled());;
        asyncTaskPlugin.doAsync(integrityCheckTask);
    }

    @Override
    protected void onStart() {
        super.onStart();
        activityPlugins.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        activityPlugins.onResume();
    }

    @Override
    protected void onPause() {
        activityPlugins.onPause();
        super.onPause();
    }

    @Override
    protected void onStop() {
        activityPlugins.onStop();
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState, @NonNull PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
        activityPlugins.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    protected void onDestroy() {
        binding = null;

        activityPlugins.onDestroy();
        super.onDestroy();
    }
}