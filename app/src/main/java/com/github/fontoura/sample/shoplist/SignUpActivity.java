package com.github.fontoura.sample.shoplist;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.github.fontoura.sample.shoplist.data.model.AuthenticatedUserData;
import com.github.fontoura.sample.shoplist.databinding.ActivitySignupBinding;
import com.github.fontoura.sample.shoplist.plugin.ActivityPlugins;
import com.github.fontoura.sample.shoplist.plugin.AsyncTaskPlugin;
import com.github.fontoura.sample.shoplist.plugin.BackgroundTask;
import com.github.fontoura.sample.shoplist.plugin.HttpConnectionPlugin;
import com.github.fontoura.sample.shoplist.plugin.TimerPlugin;
import com.github.fontoura.sample.shoplist.tasks.IntegrityCheckTask;
import com.github.fontoura.sample.shoplist.utils.DetectionUtils;
import com.github.fontoura.sample.shoplist.utils.FileUtils;

import lombok.Getter;

public class SignUpActivity extends AppCompatActivity {

    private static final String TAG = LoginActivity.class.getName();

    private ShopListApplication application;

    private ActivitySignupBinding binding;

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
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        application = (ShopListApplication) getApplication();

        activityPlugins.onCreate(savedInstanceState);

        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.enrollButton.setOnClickListener(v -> onSignUpCommand());
        binding.backButton.setOnClickListener(v -> onBackCommand());

        integrityCheckTask.withCheck(() -> !FileUtils.anyFilesExist(DetectionUtils.ROOT_FILE_PATHS));
        timerPlugin.setTimeout(integrityCheckTask, 5000);

        TextWatcher loginTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // No action needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // No action needed
            }

            @Override
            public void afterTextChanged(Editable s) {
                String username = binding.usernameInput.getText().toString().trim();
                String password = binding.passwordInput.getText().toString().trim();
                binding.enrollButton.setEnabled(!username.isEmpty() && !password.isEmpty());
            }
        };
        binding.usernameInput.addTextChangedListener(loginTextWatcher);
        binding.passwordInput.addTextChangedListener(loginTextWatcher);

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
        application = null;

        activityPlugins.onDestroy();
        super.onDestroy();
    }

    private void onBackCommand() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void onSignUpCommand() {
        binding.usernameInput.setError(null);
        binding.passwordInput.setError(null);

        String username = binding.usernameInput.getText().toString();
        String password = binding.passwordInput.getText().toString();

        if (TextUtils.isEmpty(password)) {
            binding.passwordInput.setError(getString(R.string.error_invalid_password));
            binding.passwordInput.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(username)) {
            binding.usernameInput.setError(getString(R.string.error_field_required));
            binding.usernameInput.requestFocus();
            return;
        }

        onSignUpCommand(username, password);
    }

    private void onSignUpCommand(String username, String password) {
        if (integrityCheckTask.getCount() == 0) {
            asyncTaskPlugin.doAsync(integrityCheckTask);
            return;
        }
        if (!httpConnectionPlugin.isPinningEnabled()) {
            httpConnectionPlugin.setPinningEnabled(true);
        }

        binding.loading.setVisibility(View.VISIBLE);
        binding.backButton.setEnabled(false);

        asyncTaskPlugin.doAsync(new BackgroundTask<AuthenticatedUserData>() {

            @Override
            public AuthenticatedUserData doInBackground() throws Throwable {
                return application.getLoginService().enrollNewUser(
                        httpConnectionPlugin.getHTTPRequestPreparer(),
                        application.getAuthenticationData(),
                        username, password);
            }

            @Override
            public void continueInForeground(AuthenticatedUserData value) {
                binding.backButton.setEnabled(true);

                if (value != null) {
                    application.setAuthenticatedUserData(value);
                    Intent intent = new Intent(SignUpActivity.this, ShopListActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    binding.loading.setVisibility(View.GONE);
                }
            }

            @Override
            public void handleInForeground(Throwable t) {
                binding.backButton.setEnabled(true);

                binding.loading.setVisibility(View.GONE);

                // TODO handle exception better
                Log.e(TAG, "Error while trying to enroll user", t);
            }
        });
    }
}
