package com.github.fontoura.sample.shoplist.data.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * An entry in a shop list.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShopListEntry {

    private Long id;
    private String name;
    private int quantity;

    private boolean loading;
}
