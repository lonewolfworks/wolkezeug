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

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Auth0BrokerRequest {

    private String policyName;
    private Auth0ApplicationDeploymentRequest deployment;
    private Auth0ConfigurationRequest configuration;

    public String getPolicyName() {
        return policyName;
    }

    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    public Auth0ApplicationDeploymentRequest getDeployment() {
        return deployment;
    }

    public void setDeployment(Auth0ApplicationDeploymentRequest deployment) {
        this.deployment = deployment;
    }

    public Auth0ConfigurationRequest getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Auth0ConfigurationRequest configuration) {
        this.configuration = configuration;
    }

    public Auth0BrokerRequest withPolicyName(final String policyName) {
        this.policyName = policyName;
        return this;
    }

    public Auth0BrokerRequest withDeployment(final Auth0ApplicationDeploymentRequest deployment) {
        this.deployment = deployment;
        return this;
    }

    public Auth0BrokerRequest withConfiguration(final Auth0ConfigurationRequest configuration) {
        this.configuration = configuration;
        return this;
    }

    @Override
    public String toString() {
        return "NewRelicBrokerRequest{" +
                "policyName='" + policyName + '\'' +
                ", deployment=" + deployment +
                ", configuration=" + configuration +
                '}';
    }
}
