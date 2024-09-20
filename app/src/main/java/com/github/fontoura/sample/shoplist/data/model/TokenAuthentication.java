package com.github.fontoura.sample.shoplist.data.model;

import java.net.HttpURLConnection;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class TokenAuthentication implements AuthenticationData {

    private final String token;

    @Override
    public void authenticate(HttpURLConnection request) {
        request.setRequestProperty("Authorization", "Bearer " + token);
    }
}
