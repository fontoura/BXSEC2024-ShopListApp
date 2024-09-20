package com.github.fontoura.sample.shoplist.utils;

import java.net.HttpURLConnection;

public interface HTTPRequestPreparer {
    void prepare(HttpURLConnection connection);
}
