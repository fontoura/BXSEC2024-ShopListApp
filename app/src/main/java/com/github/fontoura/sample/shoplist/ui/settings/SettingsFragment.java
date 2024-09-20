package com.github.fontoura.sample.shoplist.ui.settings;

import android.content.Intent;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.github.fontoura.sample.shoplist.LoginActivity;
import com.github.fontoura.sample.shoplist.R;
import com.github.fontoura.sample.shoplist.ShopListActivity;
import com.github.fontoura.sample.shoplist.ShopListApplication;
import com.github.fontoura.sample.shoplist.data.model.AuthenticatedUserData;
import com.github.fontoura.sample.shoplist.databinding.FragmentSettingsBinding;
import com.github.fontoura.sample.shoplist.plugin.AsyncTaskPlugin;
import com.github.fontoura.sample.shoplist.plugin.BackgroundTask;
import com.github.fontoura.sample.shoplist.plugin.HttpConnectionPlugin;

public class SettingsFragment extends Fragment {

    public static final String TAG = SettingsFragment.class.getName();

    private ShopListApplication application;
    private HttpConnectionPlugin httpConnectionPlugin;
    private AsyncTaskPlugin asyncTaskPlugin;

    private FragmentSettingsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        application = (ShopListApplication) requireActivity().getApplication();

        ShopListActivity shopListActivity = (ShopListActivity) requireActivity();
        asyncTaskPlugin = shopListActivity.getAsyncTaskPlugin();
        httpConnectionPlugin = shopListActivity.getHttpConnectionPlugin();

        binding = FragmentSettingsBinding.inflate(inflater, container, false);

        String username = application.getAuthenticatedUserData().getUserName();
        String helloUserText = "Hello " + username + "!";
        SpannableString spannableString = new SpannableString(helloUserText);
        spannableString.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 6, helloUserText.length() - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        binding.textHelloUser.setText(spannableString);

        binding.buttonLogoff.setOnClickListener(v -> onLogoff());

        boolean isAdmin = application.getAuthenticatedUserData().isSuperUser();
        if (isAdmin) {
            binding.adminSection.setVisibility(View.VISIBLE);

            binding.buttonLoginAsUser.setOnClickListener(v -> onLoginAsUser());
        }


        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        application = null;
        httpConnectionPlugin = null;
        asyncTaskPlugin = null;

        binding = null;
    }

    private void onLogoff() {
        application.setAuthenticatedUserData(null);

        Intent intent = new Intent(this.getActivity(), LoginActivity.class);
        startActivity(intent);
        getActivity().finish();
    }

    private void onLoginAsUser() {
        String otherUsername = binding.editOtherUserName.getText().toString();
        binding.editOtherUserName.setEnabled(false);
        binding.buttonLoginAsUser.setEnabled(false);

        asyncTaskPlugin.doAsync(new BackgroundTask<AuthenticatedUserData>() {

            @Override
            public AuthenticatedUserData doInBackground() throws Throwable {
                return application.getLoginService().loginWithUsernameAndPassword(
                        httpConnectionPlugin.getHTTPRequestPreparer(),
                        application.getAuthenticatedUserData().getAuthenticationData(),
                        otherUsername, "");
            }

            @Override
            public void continueInForeground(AuthenticatedUserData value) {
                if (value != null) {
                    application.setAuthenticatedUserData(value);
                    Intent intent = new Intent(getActivity(), ShopListActivity.class);
                    startActivity(intent);
                    getActivity().finish();
                } else {
                    binding.editOtherUserName.setEnabled(true);
                    binding.buttonLoginAsUser.setEnabled(true);
                    binding.editOtherUserName.setError(getString(R.string.invalid_username));
                }
            }

            @Override
            public void handleInForeground(Throwable t) {
                binding.editOtherUserName.setEnabled(true);
                binding.buttonLoginAsUser.setEnabled(true);

                // TODO handle exception better
                Log.e(TAG, "Error while trying to load data", t);
            }
        });
    }
}