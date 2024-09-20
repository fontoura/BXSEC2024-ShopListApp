package com.github.fontoura.sample.shoplist.data.service;

import static com.github.fontoura.sample.shoplist.utils.ServiceUtils.getResponse;
import static com.github.fontoura.sample.shoplist.utils.ServiceUtils.requireCreatedStatus;
import static com.github.fontoura.sample.shoplist.utils.ServiceUtils.requireNoContentStatus;
import static com.github.fontoura.sample.shoplist.utils.ServiceUtils.requireOkStatus;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fontoura.sample.shoplist.data.model.AuthenticationData;
import com.github.fontoura.sample.shoplist.data.model.ShopListEntry;
import com.github.fontoura.sample.shoplist.data.service.dto.ShopListEntryDto;
import com.github.fontoura.sample.shoplist.data.service.dto.ShopListEntryListDto;
import com.github.fontoura.sample.shoplist.data.service.mapper.ShopListMapper;
import com.github.fontoura.sample.shoplist.exception.InvalidAuthenticationException;
import com.github.fontoura.sample.shoplist.exception.ServiceUnreachableException;
import com.github.fontoura.sample.shoplist.utils.HTTPRequestPreparer;
import com.github.fontoura.sample.shoplist.utils.HTTPResponse;

import org.mapstruct.factory.Mappers;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ShopListServiceImpl implements ShopListService {

    private final String baseUrl;

    private final ShopListMapper shopListMapper = Mappers.getMapper(ShopListMapper.class);

    @Override
    public List<ShopListEntry> loadEntries(HTTPRequestPreparer requestPreparer, AuthenticationData userAuthentication)
            throws ServiceUnreachableException, InvalidAuthenticationException {
        HttpURLConnection httpGet = null;
        try {
            httpGet = (HttpURLConnection) new URL(baseUrl + "/entries").openConnection();
        } catch (IOException e) {
            throw new ServiceUnreachableException("Could not create HTTP client", e);
        }

        HTTPResponse response = getResponse(requestPreparer, userAuthentication, httpGet, null);

        requireOkStatus(response);

        String rawJson = new String(response.getBody());

        ShopListEntryListDto shopListEntryList;
        try {
            shopListEntryList = new ObjectMapper().readValue(rawJson, ShopListEntryListDto.class);
        } catch (JsonProcessingException e) {
            throw new ServiceUnreachableException("An error occurred while trying to parse the response.", e);
        }

        return shopListEntryList.getEntries().stream().map(shopListMapper::toDomain).collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public ShopListEntry storeEntry(HTTPRequestPreparer requestPreparer, AuthenticationData userAuthentication, ShopListEntry entry) throws ServiceUnreachableException, InvalidAuthenticationException {
        HttpURLConnection httpRequest;
        byte[] payload;

        boolean willCreate = entry.getId() == null;
        ShopListEntryDto entryDto = shopListMapper.toApi(entry);

        try {
            if (willCreate) {
                httpRequest = (HttpURLConnection) new URL(baseUrl + "/entries").openConnection();
                httpRequest.setRequestMethod("POST");
            } else {
                httpRequest = (HttpURLConnection) new URL(baseUrl + "/entries/" + entry.getId()).openConnection();
                httpRequest.setRequestMethod("PUT");
            }
            httpRequest.setRequestProperty("Content-Type", "application/json");
        } catch (IOException e) {
            throw new ServiceUnreachableException("Could not create HTTP client", e);
        }

        try {
            payload = new ObjectMapper().writeValueAsString(entryDto).getBytes();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting entity to JSON", e);
        }

        HTTPResponse response = getResponse(requestPreparer, userAuthentication, httpRequest, payload);
        if (willCreate) {
            requireCreatedStatus(response);
        } else {
            if (response.getStatusCode() == 404) {
                return null;
            }

            requireOkStatus(response);
        }
        String rawJson = new String(response.getBody());

        ShopListEntryDto shopListEntry;
        try {
            shopListEntry = new ObjectMapper().readValue(rawJson, ShopListEntryDto.class);
        } catch (JsonProcessingException e) {
            throw new ServiceUnreachableException("An error occurred while trying to parse the response.", e);
        }

        return shopListMapper.toDomain(shopListEntry);
    }

    @Override
    public boolean removeEntry(HTTPRequestPreparer requestPreparer, AuthenticationData userAuthentication, ShopListEntry entry) throws ServiceUnreachableException, InvalidAuthenticationException {
        HttpURLConnection httpRequest = null;
        try {
            httpRequest = (HttpURLConnection) new URL(baseUrl + "/entries/" + entry.getId()).openConnection();
            httpRequest.setRequestMethod("DELETE");
        } catch (IOException e) {
            throw new ServiceUnreachableException("Could not create HTTP client", e);
        }

        try {
            HTTPResponse response = getResponse(requestPreparer, userAuthentication, httpRequest, null);

            if (response.getStatusCode() == 404) {
                return false;
            }

            requireNoContentStatus(response);
            return true;
        } catch (InvalidAuthenticationException e) {
            throw new ServiceUnreachableException("An error occurred while trying to read the service response.", e);
        }
    }
}
