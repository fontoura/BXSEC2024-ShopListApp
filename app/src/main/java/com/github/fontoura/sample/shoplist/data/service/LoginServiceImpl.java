package com.github.fontoura.sample.shoplist.data.service;

import static com.github.fontoura.sample.shoplist.utils.ServiceUtils.getResponse;
import static com.github.fontoura.sample.shoplist.utils.ServiceUtils.requireCreatedStatus;
import static com.github.fontoura.sample.shoplist.utils.ServiceUtils.requireOkStatus;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fontoura.sample.shoplist.data.model.AuthenticatedUserData;
import com.github.fontoura.sample.shoplist.data.model.AuthenticationData;
import com.github.fontoura.sample.shoplist.data.model.TokenAuthentication;
import com.github.fontoura.sample.shoplist.data.service.dto.EnrollUserRequestDto;
import com.github.fontoura.sample.shoplist.data.service.dto.EnrollUserResponseDto;
import com.github.fontoura.sample.shoplist.data.service.dto.LoginRequestDto;
import com.github.fontoura.sample.shoplist.data.service.dto.LoginResponseDto;
import com.github.fontoura.sample.shoplist.exception.InvalidAuthenticationException;
import com.github.fontoura.sample.shoplist.exception.ServiceUnreachableException;
import com.github.fontoura.sample.shoplist.utils.HTTPRequestPreparer;
import com.github.fontoura.sample.shoplist.utils.HTTPResponse;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LoginServiceImpl implements LoginService {

    private final String baseUrl;

    @Override
    public AuthenticatedUserData loginWithUsernameAndPassword(HTTPRequestPreparer requestPreparer, AuthenticationData authenticationData, String username, String password) throws ServiceUnreachableException {
        LoginRequestDto requestPayload = new LoginRequestDto();
        requestPayload.setUsername(username);
        requestPayload.setPassword(password);

        HttpURLConnection httpPost = null;
        try {
            httpPost = (HttpURLConnection) new URL(baseUrl + "/users/login").openConnection();
            httpPost.setRequestProperty("Content-Type","application/json");
        } catch (IOException e) {
            throw new ServiceUnreachableException("Could not create HTTP client", e);
        }
        byte[] payload;
        try {
            payload = new ObjectMapper().writeValueAsString(requestPayload).getBytes();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting entity to JSON", e);
        }

        String rawJson;
        try {
            HTTPResponse response = getResponse(requestPreparer, authenticationData, httpPost, payload);

            requireOkStatus(response);

            rawJson = new String(response.getBody());
        } catch (InvalidAuthenticationException e) {
            return null;
        }

        LoginResponseDto responsePayload;
        try {
            responsePayload = new ObjectMapper().readValue(rawJson, LoginResponseDto.class);
        } catch (JsonProcessingException e) {
            throw new ServiceUnreachableException("An error occurred while trying to parse the response.", e);
        }

        return AuthenticatedUserData.builder()
            .userName(username)
            .isSuperUser(responsePayload.isSuperUser())
            .authenticationData(new TokenAuthentication(responsePayload.getToken()))
            .build();
    }

    @Override
    public AuthenticatedUserData enrollNewUser(HTTPRequestPreparer requestPreparer, AuthenticationData authenticationData, String username, String password) throws ServiceUnreachableException {
        EnrollUserRequestDto requestPayload = new EnrollUserRequestDto();
        requestPayload.setUsername(username);
        requestPayload.setPassword(password);

        HttpURLConnection httpPost = null;
        try {
            httpPost = (HttpURLConnection) new URL(baseUrl + "/users").openConnection();
            httpPost.setRequestProperty("Content-Type","application/json");
        } catch (IOException e) {
            throw new ServiceUnreachableException("Could not create HTTP client", e);
        }
        byte[] payload;
        try {
            payload = new ObjectMapper().writeValueAsString(requestPayload).getBytes();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting entity to JSON", e);
        }

        String rawJson;
        try {
            HTTPResponse response = getResponse(requestPreparer, authenticationData, httpPost, payload);
            requireCreatedStatus(response);

            rawJson = new String(response.getBody());
        } catch (InvalidAuthenticationException e) {
            throw new ServiceUnreachableException("An error occurred while trying to read the service response.", e);
        }

        EnrollUserResponseDto responsePayload;
        try {
            responsePayload = new ObjectMapper().readValue(rawJson, EnrollUserResponseDto.class);
        } catch (JsonProcessingException e) {
            throw new ServiceUnreachableException("An error occurred while trying to parse the response.", e);
        }

        if (responsePayload.getUsername() == null) {
            throw new ServiceUnreachableException("The service did not return the username of the newly created user.");
        }
        return loginWithUsernameAndPassword(requestPreparer, authenticationData, username, password);
    }
}
