package com.github.fontoura.sample.shoplist.data.service.mapper;

import com.github.fontoura.sample.shoplist.data.model.ShopListEntry;
import com.github.fontoura.sample.shoplist.data.service.dto.ShopListEntryDto;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface ShopListMapper {

    @Mapping(target = "quantity", source = "amount")
    ShopListEntry toDomain(ShopListEntryDto value);

    @Mapping(target = "amount", source = "quantity")
    ShopListEntryDto toApi(ShopListEntry value);
}
