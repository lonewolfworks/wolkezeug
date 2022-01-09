/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.lonewolfworks.wolke.aws.ecs.broker.auth0;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.StringJoiner;

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Auth0Configuration {

    private transient Auth0InjectConfiguration injectNames;
    private String tenant;
    private String client;
    private String clientGrants;
    private String clientId;
    private String encryptedClientSecret;

    public Auth0InjectConfiguration getInjectNames() {
        return injectNames;
    }

    public void setInjectNames(Auth0InjectConfiguration injectNames) {
        this.injectNames = injectNames;
    }

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public String getClientGrants() {
        return clientGrants;
    }

    public void setClientGrants(String clientGrants) {
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

    public Auth0Configuration withClient(String client) {
        this.client = client;
        return this;
    }

    public Auth0Configuration withClientGrants(String clientGrants) {
        this.clientGrants = clientGrants;
        return this;
    }

    public Auth0Configuration withInjectNames(Auth0InjectConfiguration injectNames) {
        this.injectNames = injectNames;
        return this;
    }

    public Auth0Configuration withEncryptedClientSecret(String encryptedClientSecret) {
        this.encryptedClientSecret = encryptedClientSecret;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Auth0Configuration.class.getSimpleName() + "[", "]")
                .add("injectNames=" + injectNames)
                .add("tenant='" + tenant + "'")
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

        Auth0Configuration that = (Auth0Configuration) o;

        return new EqualsBuilder()
                .append(injectNames, that.injectNames)
                .append(tenant, that.tenant)
                .append(client, that.client)
                .append(clientGrants, that.clientGrants)
                .append(clientId, that.clientId)
                .append(encryptedClientSecret, that.encryptedClientSecret)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(injectNames)
                .append(tenant)
                .append(client)
                .append(clientGrants)
                .append(clientId)
                .append(encryptedClientSecret)
                .toHashCode();
    }
}
