package com.lonewolfworks.wolke.aws.ecs.broker.auth0;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Auth0ConfigurationRequest {

    private transient Auth0InjectConfiguration injectNames;
    private String tenant;
    private Auth0Client client;
    private List<Auth0ClientGrants> clientGrants;
    private String clientId;
    private String encryptedClientSecret;

    public Auth0InjectConfiguration getInjectNames() {
        return injectNames;
    }

    public void setInjectNames(Auth0InjectConfiguration injectNames) {
        this.injectNames = injectNames;
    }

    public Auth0Client getClient() {
        return client;
    }

    public void setClient(Auth0Client client) {
        this.client = client;
    }

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    public List<Auth0ClientGrants> getClientGrants() {
        return clientGrants;
    }

    public void setClientGrants(List<Auth0ClientGrants> clientGrants) {
        this.clientGrants = clientGrants;
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

    public Auth0ConfigurationRequest withClient(Auth0Client client) {
        this.client = client;
        return this;
    }

    public Auth0ConfigurationRequest withClientGrants(List<Auth0ClientGrants> clientGrants) {
        this.clientGrants = clientGrants;
        return this;
    }

    public Auth0ConfigurationRequest withInjectNames(Auth0InjectConfiguration injectNames) {
        this.injectNames = injectNames;
        return this;
    }

    public Auth0ConfigurationRequest withEncryptedClientSecret(String encryptedClientSecret) {
        this.encryptedClientSecret = encryptedClientSecret;
        return this;
    }

    public Auth0ConfigurationRequest withTenant(String tenant) {
        this.tenant = tenant;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Auth0Configuration.class.getSimpleName() + "[", "]")
                .add("injectNames=" + injectNames)
                .add("client='" + client + "'")
                .add("clientGrants='" + clientGrants + "'")
                .add("clientId='" + clientId + "'")
                .add("encryptedClientSecret='" + encryptedClientSecret + "'")
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Auth0ConfigurationRequest that = (Auth0ConfigurationRequest) o;
        return Objects.equals(injectNames, that.injectNames) &&
                Objects.equals(client, that.client) &&
                Objects.equals(clientGrants, that.clientGrants) &&
                Objects.equals(clientId, that.clientId) &&
                Objects.equals(encryptedClientSecret, that.encryptedClientSecret);
    }

    @Override
    public int hashCode() {
        return Objects.hash(injectNames, client, clientGrants, clientId, encryptedClientSecret);
    }
}
