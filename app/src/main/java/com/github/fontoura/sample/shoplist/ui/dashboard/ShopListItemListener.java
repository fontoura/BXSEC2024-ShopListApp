package com.github.fontoura.sample.shoplist.ui.dashboard;

import com.github.fontoura.sample.shoplist.data.model.ShopListEntry;

public interface ShopListItemListener {

    void onDecrementItem(ShopListEntry item);

    void onIncrementItem(ShopListEntry item);
}
