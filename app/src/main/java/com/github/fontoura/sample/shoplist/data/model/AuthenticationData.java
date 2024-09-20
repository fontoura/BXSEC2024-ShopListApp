package com.github.fontoura.sample.shoplist.data.model;

import java.net.HttpURLConnection;

public interface AuthenticationData {

    void authenticate(HttpURLConnection request);
}
