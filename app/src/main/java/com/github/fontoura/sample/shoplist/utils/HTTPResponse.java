package com.github.fontoura.sample.shoplist.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class HTTPResponse {
    final int statusCode;
    final byte[] body;
}
