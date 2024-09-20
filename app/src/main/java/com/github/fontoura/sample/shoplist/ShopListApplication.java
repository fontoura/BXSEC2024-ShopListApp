package com.github.fontoura.sample.shoplist;

import android.app.Application;

import com.github.fontoura.sample.shoplist.data.model.AuthenticatedUserData;
import com.github.fontoura.sample.shoplist.data.model.AuthenticationData;
import com.github.fontoura.sample.shoplist.data.model.TokenAuthentication;
import com.github.fontoura.sample.shoplist.data.service.LoginService;
import com.github.fontoura.sample.shoplist.data.service.LoginServiceImpl;
import com.github.fontoura.sample.shoplist.data.service.ShopListService;
import com.github.fontoura.sample.shoplist.data.service.ShopListServiceImpl;

import lombok.Getter;
import lombok.Setter;

public class ShopListApplication extends Application {

    private static final String HTTP_ENDPOINT = "https://{INSERT_URL_HERE}:8443";

    @Getter
    private final ShopListService shopListService = new ShopListServiceImpl(HTTP_ENDPOINT);

    @Getter
    private final LoginService loginService = new LoginServiceImpl(HTTP_ENDPOINT);

    @Getter
    AuthenticationData systemAuthenticationData;

    @Getter
    @Setter
    private AuthenticatedUserData authenticatedUserData;

    @Override
    public void onCreate() {
        super.onCreate();
        systemAuthenticationData = new TokenAuthentication(getString(R.string.system_key));
    }

    public AuthenticationData getAuthenticationData() {
        return authenticatedUserData != null ? authenticatedUserData.getAuthenticationData() : systemAuthenticationData;
    }
}
