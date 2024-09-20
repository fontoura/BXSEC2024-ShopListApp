package com.github.fontoura.sample.shoplist.data.model;

import com.github.fontoura.sample.shoplist.data.model.internal.InstanceMapper;

import org.mapstruct.factory.Mappers;

public class ModelUtils {

    private static InstanceMapper instanceMapper = Mappers.getMapper(InstanceMapper.class);

    public static void copyShopListEntry(ShopListEntry target, ShopListEntry source) {
        instanceMapper.copyShopListEntry(target, source);
    }
}
