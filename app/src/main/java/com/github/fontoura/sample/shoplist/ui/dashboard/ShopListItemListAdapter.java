package com.github.fontoura.sample.shoplist.ui.dashboard;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.fontoura.sample.shoplist.R;
import com.github.fontoura.sample.shoplist.data.model.ShopListEntry;
import com.github.fontoura.sample.shoplist.databinding.ListItemBinding;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

public class ShopListItemListAdapter extends RecyclerView.Adapter<ShopListItemListAdapter.ViewHolder>  {

    private List<ShopListEntry> items;
    private Context context;

    @Getter
    @Setter
    private ShopListItemListener listener;

    public ShopListItemListAdapter(Context context) {
        this.context = context;
        this.items = new ArrayList<>();
    }

    public void setEntries(List<ShopListEntry> items) {
        this.items.clear();
        this.items.addAll(items);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ShopListEntry item = items.get(position);

        holder.binding.shopListItemName.setText(item.getName());
        holder.binding.shopListItemQuantity.setText(String.valueOf(item.getQuantity()));
        holder.binding.shopListItemName.setEnabled(!item.isLoading());
        holder.binding.shopListItemDecrement.setEnabled(item.getQuantity() > 1 && !item.isLoading());
        holder.binding.shopListItemQuantity.setEnabled(!item.isLoading());
        holder.binding.shopListItemIncrement.setEnabled(!item.isLoading());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public int addItem(ShopListEntry newItem) {
        int index = items.size();
        items.add(newItem);
        notifyItemInserted(index);
        return index;
    }

    public ShopListEntry getItem(int position) {
        return items.get(position);
    }

    public void removeItem(int position) {
        items.remove(position);
        notifyItemRemoved(position);
    }

    public void removeItem(ShopListEntry entry) {
        int index = items.indexOf(entry);
        if (index >= 0) {
            removeItem(index);
        }
    }

    public void notifyItemChanged(ShopListEntry entry) {
        int index = items.indexOf(entry);
        if (index >= 0) {
            notifyItemChanged(index);
        }
    }

    private void onDecrementItem(int position) {
        if (listener != null) {
            listener.onDecrementItem(items.get(position));
        }
    }

    private void onIncrementItem(int position) {
        if (listener != null) {
            listener.onIncrementItem(items.get(position));
        }
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ListItemBinding binding;

        public ViewHolder(View itemView) {
            super(itemView);

            binding = ListItemBinding.bind(itemView);
            binding.shopListItemDecrement.setOnClickListener(v -> ShopListItemListAdapter.this.onDecrementItem(getBindingAdapterPosition()));
            binding.shopListItemIncrement.setOnClickListener(v -> ShopListItemListAdapter.this.onIncrementItem(getBindingAdapterPosition()));
        }
    }
}
