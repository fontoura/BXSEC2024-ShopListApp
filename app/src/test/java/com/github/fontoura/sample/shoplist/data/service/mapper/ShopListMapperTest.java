package com.github.fontoura.sample.shoplist.data.service.mapper;

import static org.junit.Assert.*;

import com.github.fontoura.sample.shoplist.data.model.ShopListEntry;
import com.github.fontoura.sample.shoplist.data.service.dto.ShopListEntryDto;

import org.junit.Test;
import org.mapstruct.factory.Mappers;

public class ShopListMapperTest {

    private ShopListMapper shopListMapper = Mappers.getMapper(ShopListMapper.class);

    @Test
    public void toDomain_returnsNull_whenInputIsNull() {
        assertNull(shopListMapper.toDomain(null));
    }
    @Test
    public void toDomain_returnsNonNull_whenInputIsNonNull() {
        ShopListEntryDto source = new ShopListEntryDto();
        source.setId(1L);
        source.setName("name");
        source.setAmount(1);

        ShopListEntry target = shopListMapper.toDomain(source);

        assertNotNull(target);
        assertEquals(source.getId(), target.getId());
        assertEquals(source.getName(), target.getName());
        assertEquals(source.getAmount(), target.getQuantity());
    }

    @Test
    public void toApi_returnsNull_whenInputIsNull() {
        assertNull(shopListMapper.toApi(null));
    }

    @Test
    public void toApi_returnsNonNull_whenInputIsNonNull() {
        ShopListEntry source = new ShopListEntry();
        source.setId(1L);
        source.setName("name");
        source.setQuantity(1);

        ShopListEntryDto target = shopListMapper.toApi(source);

        assertNotNull(target);
        assertEquals(source.getId(), target.getId());
        assertEquals(source.getName(), target.getName());
        assertEquals(source.getQuantity(), target.getAmount());
    }

}