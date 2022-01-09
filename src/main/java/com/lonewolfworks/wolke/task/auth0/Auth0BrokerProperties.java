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
package com.lonewolfworks.wolke.task.auth0;

import java.util.StringJoiner;

public class Auth0BrokerProperties {

    private String tenantId;
    private String auth0Lambda;

    public String getAuth0Lambda() {
        return auth0Lambda;
    }

    public void setAuth0Lambda(String auth0Lambda) {
        this.auth0Lambda = auth0Lambda;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public Auth0BrokerProperties withAuth0Lambda(String auth0Lambda) {
        this.auth0Lambda = auth0Lambda;
        return this;
    }

    public Auth0BrokerProperties withTenantId(String tenantId) {
        this.tenantId = tenantId;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Auth0BrokerProperties.class.getSimpleName() + "[", "]")
                .add("tenantId='" + tenantId + "'")
                .add("auth0Lambda='" + auth0Lambda + "'")
                .toString();
    }
}
