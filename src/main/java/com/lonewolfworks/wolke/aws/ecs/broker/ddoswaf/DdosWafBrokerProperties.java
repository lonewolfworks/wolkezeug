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
package com.lonewolfworks.wolke.aws.ecs.broker.ddoswaf;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DdosWafBrokerProperties {

    private String ddosWafLambda;
    private WafConfiguration wafConfiguration;

    public String getDdosWafLambda() {
        return ddosWafLambda;
    }

    public void setDdosWafLambda(String ddosWafLambda) {
        this.ddosWafLambda = ddosWafLambda;
    }

    public WafConfiguration getWafConfiguration() {
        return wafConfiguration;
    }

    public void setWafConfiguration(WafConfiguration wafConfiguration) {
        this.wafConfiguration = wafConfiguration;
    }

    public DdosWafBrokerProperties withDdosWafLambda(final String ddosWafLambda) {
        this.ddosWafLambda = ddosWafLambda;
        return this;
    }

    public DdosWafBrokerProperties withWafConfiguration(
        final WafConfiguration wafConfiguration) {
        this.wafConfiguration = wafConfiguration;
        return this;
    }

    @Override
    public String toString() {
        return "DdosWafBrokerProperties{" +
            "ddosWafLambda='" + ddosWafLambda + '\'' +
            ", wafConfiguration=" + wafConfiguration +
            '}';
    }
}
