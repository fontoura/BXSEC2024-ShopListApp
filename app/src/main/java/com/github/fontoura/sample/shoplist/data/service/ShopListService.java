package com.github.fontoura.sample.shoplist.data.service;

import com.github.fontoura.sample.shoplist.data.model.AuthenticationData;
import com.github.fontoura.sample.shoplist.data.model.ShopListEntry;
import com.github.fontoura.sample.shoplist.exception.InvalidAuthenticationException;
import com.github.fontoura.sample.shoplist.exception.ServiceUnreachableException;
import com.github.fontoura.sample.shoplist.utils.HTTPRequestPreparer;

import java.util.List;

public interface ShopListService {

    /**
     * Loads the shop list entries of the given user.
     *
     * @param requestPreparer The HTTP request preparer.
     * @param userAuthentication The user authentication.
     * @return The list of shop list entries.
     * @throws ServiceUnreachableException If the service is unreachable or responds with an illegal status code or payload.
     * @throws InvalidAuthenticationException If the authentication is invalid.
     */
    List<ShopListEntry> loadEntries(HTTPRequestPreparer requestPreparer, AuthenticationData userAuthentication)
            throws ServiceUnreachableException, InvalidAuthenticationException;

    /**
     * Stores the given entry in the shop list of the given user.
     *
     * @param requestPreparer The HTTP request preparer.
     * @param userAuthentication The user authentication.
     * @param entry The entry to store.
     * @throws ServiceUnreachableException If the service is unreachable or responds with an illegal status code or payload.
     * @throws InvalidAuthenticationException  If the authentication is invalid.
     */
    ShopListEntry storeEntry(HTTPRequestPreparer requestPreparer, AuthenticationData userAuthentication, ShopListEntry entry)
            throws ServiceUnreachableException, InvalidAuthenticationException;

    /**
     * Removes the given entry from the shop list of the given user.
     *
     * @param requestPreparer The HTTP request preparer.
     * @param userAuthentication The user authentication.
     * @param entry The entry to remove.
     * @return True if the entry was removed, false otherwise.
     * @throws ServiceUnreachableException If the service is unreachable or responds with an illegal status code or payload.
     * @throws InvalidAuthenticationException If the authentication is invalid.
     */
    boolean removeEntry(HTTPRequestPreparer requestPreparer, AuthenticationData userAuthentication, ShopListEntry entry)
            throws ServiceUnreachableException, InvalidAuthenticationException;
}
