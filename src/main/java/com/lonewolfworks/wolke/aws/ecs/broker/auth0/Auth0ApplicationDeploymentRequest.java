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

import java.util.StringJoiner;

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Auth0ApplicationDeploymentRequest {

    private String revision;
    private String version;
    private String user;

    public String getRevision() {
        return revision;
    }

    public void setRevision(String revision) {
        this.revision = revision;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Auth0ApplicationDeploymentRequest withRevision(final String revision) {
        this.revision = revision;
        return this;
    }

    public Auth0ApplicationDeploymentRequest withVersion(final String version) {
        this.version = version;
        return this;
    }

    public Auth0ApplicationDeploymentRequest withUser(final String user) {
        this.user = user;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Auth0ApplicationDeploymentRequest.class.getSimpleName() + "[", "]")
                .add("revision='" + revision + "'")
                .add("version='" + version + "'")
                .add("user='" + user + "'")
                .toString();
    }
}