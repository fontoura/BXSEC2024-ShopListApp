package com.github.fontoura.sample.shoplist.utils;

import com.github.fontoura.sample.shoplist.data.model.AuthenticationData;
import com.github.fontoura.sample.shoplist.exception.InvalidAuthenticationException;
import com.github.fontoura.sample.shoplist.exception.ServiceUnreachableException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ServiceUtils {

    public static void requireCreatedStatus(HTTPResponse response) throws ServiceUnreachableException {
        if (response.getStatusCode() != 201) {
            throw new ServiceUnreachableException("The service responded with an illegal status code: " + response.getStatusCode());
        }
    }


    public static void requireOkStatus(HTTPResponse response) throws ServiceUnreachableException {
        if (response.getStatusCode() != 200) {
            throw new ServiceUnreachableException("The service responded with an illegal status code: " + response.getStatusCode());
        }
    }

    public static void requireNoContentStatus(HTTPResponse response) throws ServiceUnreachableException {
        if (response.getStatusCode() != 204) {
            throw new ServiceUnreachableException("The service responded with an illegal status code: " + response.getStatusCode());
        }
    }

    public static HTTPResponse getResponse(HTTPRequestPreparer requestPreparer, AuthenticationData userAuthentication, HttpURLConnection request, byte[] data)
            throws ServiceUnreachableException, InvalidAuthenticationException {
        userAuthentication.authenticate(request);
        requestPreparer.prepare(request);

        if (data != null) {
            request.setDoOutput(true);
            try {
                try (OutputStream stream = request.getOutputStream()) {
                    stream.write(data);
                }
            } catch (Exception e) {
                throw new ServiceUnreachableException("An error occurred while trying to send output.", e);
            }
        }

        int code;
        ByteArrayOutputStream byteArrayOutputStream;
        try {
            byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            // This forces HttpURLConnection to run the request, allowing us to access the error stream, if required.
            code = request.getResponseCode();
            try (InputStream stream = getResponseStream(request)) {
                int bytesRead;
                while ((bytesRead = stream.read(buffer)) != -1) {
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                }
            }
        } catch (Exception e) {
            throw new ServiceUnreachableException("An error occurred while trying to read input.", e);
        }
        if (code == 401) {
            throw new InvalidAuthenticationException("The service responded with an invalid authentication status code: " + code);
        }
        return new HTTPResponse(code, byteArrayOutputStream.toByteArray());
    }

    private static InputStream getResponseStream(HttpURLConnection request) {
        try {
            return request.getInputStream();
        } catch (IOException e) {
            return request.getErrorStream();
        }
    }
}
