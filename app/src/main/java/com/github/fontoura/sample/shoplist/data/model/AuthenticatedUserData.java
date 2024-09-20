package com.github.fontoura.sample.shoplist.data.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticatedUserData {

    private String userName;
    private boolean isSuperUser;
    private AuthenticationData authenticationData;
}