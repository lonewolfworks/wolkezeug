package com.lonewolfworks.wolke.aws.ecs.broker.auth0;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.lonewolfworks.wolke.aws.ecs.broker.domain.HermanBrokerUpdate;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Auth0BrokerResponse {

    List<HermanBrokerUpdate> updates = new ArrayList<>();
    String tenantId;
    String clientId;
    String clientSecret;

    public List<HermanBrokerUpdate> getUpdates() {
        return updates;
    }

    public void setUpdates(List<HermanBrokerUpdate> updates) {
        this.updates = updates;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Auth0BrokerResponse.class.getSimpleName() + "[", "]")
                .add("updates=" + updates)
                .add("tenantId='" + tenantId + "'")
                .add("clientId='" + clientId + "'")
                .add("clientSecret='" + clientSecret + "'")
                .toString();
    }
}
