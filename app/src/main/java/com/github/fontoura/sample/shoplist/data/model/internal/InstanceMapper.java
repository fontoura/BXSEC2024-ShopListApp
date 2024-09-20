package com.github.fontoura.sample.shoplist.data.model.internal;

import com.github.fontoura.sample.shoplist.data.model.ShopListEntry;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper
public interface InstanceMapper {

    void copyShopListEntry(@MappingTarget ShopListEntry target, ShopListEntry source);
}
