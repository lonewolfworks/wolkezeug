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
import com.fasterxml.jackson.annotation.JsonProperty;
import com.lonewolfworks.wolke.task.auth0.Auth0BrokerProperties;

import java.util.StringJoiner;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Auth0BrokerConfiguration {

    @JsonProperty("auth0")
    private Auth0BrokerProperties brokerProperties;

    public Auth0BrokerProperties getBrokerProperties() {
        return brokerProperties;
    }

    public void setBrokerProperties(Auth0BrokerProperties brokerProperties) {
        this.brokerProperties = brokerProperties;
    }

    public Auth0BrokerConfiguration withBrokerProperties(final Auth0BrokerProperties brokerProperties) {
        this.brokerProperties = brokerProperties;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Auth0BrokerConfiguration.class.getSimpleName() + "[", "]")
                .add("brokerProperties=" + brokerProperties)
                .toString();
    }
}