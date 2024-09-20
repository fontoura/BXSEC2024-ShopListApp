package com.github.fontoura.sample.shoplist.ui.dashboard;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.fontoura.sample.shoplist.LoginActivity;
import com.github.fontoura.sample.shoplist.R;
import com.github.fontoura.sample.shoplist.ShopListActivity;
import com.github.fontoura.sample.shoplist.ShopListApplication;
import com.github.fontoura.sample.shoplist.data.model.ModelUtils;
import com.github.fontoura.sample.shoplist.data.model.ShopListEntry;
import com.github.fontoura.sample.shoplist.databinding.FragmentDashboardBinding;
import com.github.fontoura.sample.shoplist.exception.InvalidAuthenticationException;
import com.github.fontoura.sample.shoplist.plugin.AsyncTaskPlugin;
import com.github.fontoura.sample.shoplist.plugin.BackgroundTask;
import com.github.fontoura.sample.shoplist.plugin.HttpConnectionPlugin;

import java.util.List;

public class DashboardFragment extends Fragment {

    public static final String TAG = DashboardFragment.class.getName();

    private ShopListApplication application;
    private HttpConnectionPlugin httpConnectionPlugin;
    private AsyncTaskPlugin asyncTaskPlugin;

    private FragmentDashboardBinding binding;
    private ShopListItemListAdapter shopListItemsAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        application = (ShopListApplication) requireActivity().getApplication();

        ShopListActivity shopListActivity = (ShopListActivity) requireActivity();
        asyncTaskPlugin = shopListActivity.getAsyncTaskPlugin();
        httpConnectionPlugin = shopListActivity.getHttpConnectionPlugin();

        binding = FragmentDashboardBinding.inflate(inflater, container, false);

        shopListItemsAdapter = new ShopListItemListAdapter(getContext());
        shopListItemsAdapter.setListener(new ShopListItemListener() {

            @Override
            public void onDecrementItem(ShopListEntry item) {
                DashboardFragment.this.onDecrementItem(item);
            }

            @Override
            public void onIncrementItem(ShopListEntry item) {
                DashboardFragment.this.onIncrementItem(item);
            }
        });

        binding.shopListItemsRecyclerView.setAdapter(shopListItemsAdapter);
        binding.shopListItemsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    onRemoveItem(position);
                }
            }
        });

        binding.shopListItemsAddButton.setOnClickListener(view -> onAddItem());

        binding.shopListItemsErrorActionButton.setOnClickListener(view -> loadData());

        itemTouchHelper.attachToRecyclerView(binding.shopListItemsRecyclerView);

        loadData();

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        application = null;
        httpConnectionPlugin = null;
        asyncTaskPlugin = null;

        binding = null;
        shopListItemsAdapter = null;
    }

    private void onIncrementItem(ShopListEntry entry) {
        entry.setLoading(true);
        shopListItemsAdapter.notifyItemChanged(entry);

        entry.setQuantity(entry.getQuantity() + 1);
        asyncTaskPlugin.doAsync(new BackgroundTask<ShopListEntry>() {

            @Override
            public ShopListEntry doInBackground() throws Throwable {
                return application.getShopListService().storeEntry(
                        httpConnectionPlugin.getHTTPRequestPreparer(),
                        application.getAuthenticationData(),
                        entry);
            }

            @Override
            public void continueInForeground(ShopListEntry value) {
                ModelUtils.copyShopListEntry(entry, value);
                entry.setLoading(false);
                shopListItemsAdapter.notifyItemChanged(entry);
            }

            @Override
            public void handleInForeground(Throwable t) {
                entry.setQuantity(entry.getQuantity() - 1);
                Log.e(TAG, "Error while trying to load data", t);

                if (t instanceof InvalidAuthenticationException) {
                    handleAuthenticationError();
                } else {
                    showError();
                }
            }
        });
    }

    private void onDecrementItem(ShopListEntry entry) {
        entry.setLoading(true);
        shopListItemsAdapter.notifyItemChanged(entry);

        entry.setQuantity(entry.getQuantity() - 1);
        asyncTaskPlugin.doAsync(new BackgroundTask<ShopListEntry>() {

            @Override
            public ShopListEntry doInBackground() throws Throwable {
                return application.getShopListService().storeEntry(
                        httpConnectionPlugin.getHTTPRequestPreparer(),
                        application.getAuthenticationData(),
                        entry);
            }

            @Override
            public void continueInForeground(ShopListEntry value) {
                ModelUtils.copyShopListEntry(entry, value);
                entry.setLoading(false);
                shopListItemsAdapter.notifyItemChanged(entry);
            }

            @Override
            public void handleInForeground(Throwable t) {
                entry.setQuantity(entry.getQuantity() + 1);
                Log.e(TAG, "Error while trying to load data", t);

                if (t instanceof InvalidAuthenticationException) {
                    handleAuthenticationError();
                } else {
                    showError();
                }
            }
        });
    }

    private void loadData() {
        showLoader();

        asyncTaskPlugin.doAsync(new BackgroundTask<List<ShopListEntry>>() {

            @Override
            public List<ShopListEntry> doInBackground() throws Throwable {
                return application.getShopListService().loadEntries(
                        httpConnectionPlugin.getHTTPRequestPreparer(),
                        application.getAuthenticationData());
            }

            @Override
            public void continueInForeground(List<ShopListEntry> value) {
                shopListItemsAdapter.setEntries(value);
                showActualComponent();
            }

            @Override
            public void handleInForeground(Throwable t) {
                Log.e(TAG, "Error while trying to load data", t);
                showError();

                if (t instanceof InvalidAuthenticationException) {
                    handleAuthenticationError();
                } else {
                    showError();
                }
            }
        });
    }

    private void onAddItem() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_item, null);
        builder.setView(dialogView);

        EditText editItemName = dialogView.findViewById(R.id.edit_item_name);
        Button buttonSave = dialogView.findViewById(R.id.button_save);
        Button buttonCancel = dialogView.findViewById(R.id.button_cancel);

        AlertDialog dialog = builder.create();

        buttonSave.setOnClickListener(v -> {
            String itemName = editItemName.getText().toString().trim();
            if (!itemName.isEmpty()) {
                dialog.dismiss();
                addItemToList(itemName);
            } else {
                editItemName.setError("Item name cannot be empty");
            }
        });

        buttonCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void addItemToList(String itemName) {
        ShopListEntry entryToInsert = ShopListEntry.builder()
                .name(itemName)
                .quantity(1)
                .loading(true)
                .build();
        int position = shopListItemsAdapter.addItem(entryToInsert);
        binding.shopListItemsRecyclerView.scrollToPosition(position);

        asyncTaskPlugin.doAsync(new BackgroundTask<ShopListEntry>() {
            @Override
            public ShopListEntry doInBackground() throws Throwable {
                return application.getShopListService().storeEntry(
                        httpConnectionPlugin.getHTTPRequestPreparer(),
                        application.getAuthenticationData(),
                        entryToInsert);
            }

            @Override
            public void continueInForeground(ShopListEntry value) {
                if (value != null) {
                    ModelUtils.copyShopListEntry(entryToInsert, value);
                    entryToInsert.setLoading(false);
                    shopListItemsAdapter.notifyItemChanged(entryToInsert);
                } else {
                    shopListItemsAdapter.removeItem(entryToInsert);
                    Log.w(TAG, "Failed to add item, will reload data just in case.");
                    loadData();
                }
            }

            @Override
            public void handleInForeground(Throwable t) {
                shopListItemsAdapter.removeItem(entryToInsert);
                Log.e(TAG, "Error while trying to add item", t);

                if (t instanceof InvalidAuthenticationException) {
                    handleAuthenticationError();
                } else {
                    showError();
                }
            }
        });
    }

    private void onRemoveItem(int position) {
        ShopListEntry entry = shopListItemsAdapter.getItem(position);
        entry.setLoading(true);
        shopListItemsAdapter.notifyItemChanged(entry);

        asyncTaskPlugin.doAsync(new BackgroundTask<Boolean>() {

            @Override
            public Boolean doInBackground() throws Throwable {
                return application.getShopListService().removeEntry(
                        httpConnectionPlugin.getHTTPRequestPreparer(),
                        application.getAuthenticationData(),
                        entry);
            }

            @Override
            public void continueInForeground(Boolean value) {
                if (value) {
                    shopListItemsAdapter.removeItem(entry);
                } else {
                    Log.w(TAG, "Failed to remove item, will reload data just in case.");
                    loadData();
                }
            }

            @Override
            public void handleInForeground(Throwable t) {
                entry.setLoading(false);
                shopListItemsAdapter.notifyItemChanged(entry);
                Log.e(TAG, "Error while trying to remove item", t);

                if (t instanceof InvalidAuthenticationException) {
                    handleAuthenticationError();
                } else {
                    showError();
                }
            }
        });
    }

    private void handleAuthenticationError() {
        application.setAuthenticatedUserData(null);

        Intent intent = new Intent(this.getActivity(), LoginActivity.class);
        startActivity(intent);
        getActivity().finish();
    }

    private void showLoader() {
        binding.dashboardFlipper.setDisplayedChild(0);
    }

    private void showActualComponent() {
        binding.dashboardFlipper.setDisplayedChild(1);
    }

    private void showError() {
        binding.dashboardFlipper.setDisplayedChild(2);
    }
}