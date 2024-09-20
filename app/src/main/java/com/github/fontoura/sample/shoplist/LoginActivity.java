package com.github.fontoura.sample.shoplist;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.github.fontoura.sample.shoplist.data.model.AuthenticatedUserData;
import com.github.fontoura.sample.shoplist.databinding.ActivityLoginBinding;
import com.github.fontoura.sample.shoplist.plugin.ActivityPlugins;
import com.github.fontoura.sample.shoplist.plugin.AsyncTaskPlugin;
import com.github.fontoura.sample.shoplist.plugin.BackgroundTask;
import com.github.fontoura.sample.shoplist.plugin.HttpConnectionPlugin;
import com.github.fontoura.sample.shoplist.plugin.TimerPlugin;
import com.github.fontoura.sample.shoplist.tasks.IntegrityCheckTask;
import com.github.fontoura.sample.shoplist.utils.DetectionUtils;
import com.github.fontoura.sample.shoplist.utils.FileUtils;

import lombok.Getter;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = LoginActivity.class.getName();

    private ShopListApplication application;

    private ActivityLoginBinding binding;

    @Getter
    public HttpConnectionPlugin httpConnectionPlugin = new HttpConnectionPlugin(this)
            .withPinningEnabled(true);

    @Getter
    public AsyncTaskPlugin asyncTaskPlugin = new AsyncTaskPlugin();

    public TimerPlugin timerPlugin = new TimerPlugin();

    private IntegrityCheckTask integrityCheckTask = new IntegrityCheckTask(this);

    private boolean scheduled;

    @Getter
    private final ActivityPlugins activityPlugins = ActivityPlugins.builder()
            .withLogTag(TAG)
            .withPlugin(httpConnectionPlugin)
            .withPlugin(timerPlugin)
            .withPlugin(asyncTaskPlugin)
            .build();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        application = (ShopListApplication) getApplication();
        if (application.getAuthenticatedUserData() != null) {
            Intent intent = new Intent(this, ShopListActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        integrityCheckTask.withCheck(() -> !FileUtils.anyFilesExist(DetectionUtils.ROOT_FILE_PATHS));

        activityPlugins.onCreate(savedInstanceState);

        timerPlugin.setInterval(integrityCheckTask, 5000);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.loginButton.setOnClickListener(v -> onLoginCommand());
        binding.signUpText.setOnClickListener(v -> onSignUpCommand());

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
                binding.loginButton.setEnabled(!username.isEmpty() && !password.isEmpty());
            }
        };
        binding.usernameInput.addTextChangedListener(loginTextWatcher);
        binding.passwordInput.addTextChangedListener(loginTextWatcher);

        integrityCheckTask.withCheck(() -> httpConnectionPlugin.isPinningEnabled());
        integrityCheckTask.withCheck(() -> FileUtils.anyFilesExist(new String[] { getFilesDir().getPath() }));
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

    private void onSignUpCommand() {
        Intent intent = new Intent(this, SignUpActivity.class);
        startActivity(intent);
        finish();
    }

    private void onLoginCommand() {
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

        onLoginCommand(username, password);
    }

    private void onLoginCommand(String username, String password) {
        if (integrityCheckTask.getCount() == 0) {
            asyncTaskPlugin.doAsync(integrityCheckTask);
            return;
        }

        if (!httpConnectionPlugin.isPinningEnabled()) {
            httpConnectionPlugin.setPinningEnabled(true);
        }

        checkTampering();

        binding.loading.setVisibility(View.VISIBLE);
        binding.usernameInput.setEnabled(false);
        binding.passwordInput.setEnabled(false);
        binding.loginButton.setEnabled(false);
        binding.signUpText.setEnabled(false);

        asyncTaskPlugin.doAsync(new BackgroundTask<AuthenticatedUserData>() {

            @Override
            public AuthenticatedUserData doInBackground() throws Throwable {
                return application.getLoginService().loginWithUsernameAndPassword(
                        httpConnectionPlugin.getHTTPRequestPreparer(),
                        application.getAuthenticationData(),
                        username, password);
            }

            @Override
            public void continueInForeground(AuthenticatedUserData value) {
                if (value != null) {
                    application.setAuthenticatedUserData(value);
                    Intent intent = new Intent(LoginActivity.this, ShopListActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    finallyInForeground();

                    CharSequence text = "Incorrect credentials!";
                    int duration = Toast.LENGTH_SHORT;
                    Toast toast = Toast.makeText(LoginActivity.this, text, duration);
                    toast.show();
                }
            }

            @Override
            public void handleInForeground(Throwable t) {
                finallyInForeground();

                CharSequence text = "Failed to reach the server";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(LoginActivity.this, text, duration);
                toast.show();
            }

            private void finallyInForeground() {
                binding.loading.setVisibility(View.GONE);
                binding.usernameInput.setEnabled(true);
                binding.passwordInput.setEnabled(true);
                binding.loginButton.setEnabled(true);
                binding.signUpText.setEnabled(true);
            }
        });
    }

    private void checkTampering() {
        IntegrityCheckTask integrityCheckTask2 = new IntegrityCheckTask(this);
        integrityCheckTask2.withCheck(() -> false);
        boolean failed;
        try {
            failed = !integrityCheckTask2.doInBackground();
        } catch (Throwable t) {
            failed = false;
        }

        if (!failed) {
            Log.w(TAG, "Tampering detected!");

            Intent intent = new Intent(this, ErrorActivity.class);
            startActivity(intent);
            finish();
        }
    }
}