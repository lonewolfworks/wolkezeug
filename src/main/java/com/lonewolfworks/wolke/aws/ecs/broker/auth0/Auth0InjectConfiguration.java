package com.lonewolfworks.wolke.aws.ecs.broker.auth0;

import java.util.StringJoiner;

public class Auth0InjectConfiguration {

    private String clientId;
    private String encryptedClientSecret;

    public void setDefaults() {
        clientId = clientId == null ? "auth0_client_id" : clientId;
        encryptedClientSecret = encryptedClientSecret == null ? "auth0_encrypted_client_secret" : encryptedClientSecret;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getEncryptedClientSecret() {
        return encryptedClientSecret;
    }

    public void setEncryptedClientSecret(String encryptedClientSecret) {
        this.encryptedClientSecret = encryptedClientSecret;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Auth0InjectConfiguration.class.getSimpleName() + "[", "]")
                .add("clientId='" + clientId + "'")
                .add("encryptedClientSecret='" + encryptedClientSecret + "'")
                .toString();
    }
}
