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
package com.lonewolfworks.wolke.aws.ecs.broker.newrelic;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class NewRelicDefinition {

    private String policyName;
    private NewRelicConfiguration newRelic;

    public String getPolicyName() {
        return policyName;
    }

    public void setPolicyName(String policyName) {
        this.policyName = policyName;
    }

    public NewRelicConfiguration getNewRelic() {
        return newRelic;
    }

    public void setNewRelic(NewRelicConfiguration newRelic) {
        this.newRelic = newRelic;
    }

    public NewRelicDefinition withPolicyName(final String policyName) {
        this.policyName = policyName;
        return this;
    }

    public NewRelicDefinition withNewRelic(final NewRelicConfiguration newRelic) {
        this.newRelic = newRelic;
        return this;
    }

    @Override
    public String toString() {
        return "NewRelicDefinition{" +
            "policyName='" + policyName + '\'' +
            ", newRelic=" + newRelic +
            '}';
    }

    public NewRelicDefinition withFormattedPolicyName() {
        this.policyName = this.policyName.replace(" ", "-").toLowerCase();
        return this;
    }
}
