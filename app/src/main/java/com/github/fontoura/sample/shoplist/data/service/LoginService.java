package com.github.fontoura.sample.shoplist.data.service;

import com.github.fontoura.sample.shoplist.data.model.AuthenticatedUserData;
import com.github.fontoura.sample.shoplist.data.model.AuthenticationData;
import com.github.fontoura.sample.shoplist.exception.ServiceUnreachableException;
import com.github.fontoura.sample.shoplist.utils.HTTPRequestPreparer;

public interface LoginService {

    /**
     * Logs in with the given username and password.
     *
     * @param requestPreparer The HTTP request preparer.
     * @param authenticationData The authentication data.
     * @param username The username.
     * @param password The password.
     * @return The authenticated user data.
     * @throws ServiceUnreachableException If the service is unreachable or responds with an illegal status code or payload.
     */
    AuthenticatedUserData loginWithUsernameAndPassword(HTTPRequestPreparer requestPreparer, AuthenticationData authenticationData, String username, String password)
            throws ServiceUnreachableException;

    /**
     * Enrolls a new user with the given username and password.
     *
     * @param requestPreparer The HTTP request preparer.
     * @param authenticationData The authentication data.
     * @param username The username.
     * @param password The password.
     * @return The authenticated user data for the newly created user.
     * @throws ServiceUnreachableException If the service is unreachable or responds with an illegal status code or payload.
     */
    AuthenticatedUserData enrollNewUser(HTTPRequestPreparer requestPreparer, AuthenticationData authenticationData, String username, String password)
            throws ServiceUnreachableException;
}
