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
package com.lonewolfworks.wolke.task.s3;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.lonewolfworks.wolke.aws.ecs.broker.s3.S3BrokerProperties;
import com.lonewolfworks.wolke.task.common.CommonTaskProperties;
import com.lonewolfworks.wolke.task.ecs.ECSPushTaskProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class S3CreateTaskProperties extends CommonTaskProperties {

    private S3BrokerProperties s3;
    private String logsBucket;
    private String s3LogsBucket;

    public S3CreateTaskProperties fromECSPushTaskProperties(ECSPushTaskProperties taskProperties) {
        return this
            .withCompany(taskProperties.getCompany())
            .withEngine(taskProperties.getEngine())
            .withOrg(taskProperties.getOrg())
            .withSbu(taskProperties.getSbu())
            .withS3(taskProperties.getS3())
            .withLogsBucket(taskProperties.getLogsBucket())
            .withS3LogsBucket(taskProperties.getLogsBucket())
            ;
    }

    public String getS3LogsBucket() {
    	return s3LogsBucket;
    }
    
    public void setS3LogsBucket(String s3LogsBucket) {
    	this.s3LogsBucket = s3LogsBucket;
    }
    
    public S3BrokerProperties getS3() {
        return s3;
    }

    public void setS3(S3BrokerProperties s3) {
        this.s3 = s3;
    }

    public String getLogsBucket() {
        return logsBucket;
    }

    public void setLogsBucket(String logsBucket) {
        this.logsBucket = logsBucket;
    }

    public S3CreateTaskProperties withS3(final S3BrokerProperties s3) {
        this.s3 = s3;
        return this;
    }

    public S3CreateTaskProperties withLogsBucket(final String logsBucket) {
        this.logsBucket = logsBucket;
        return this;
    }
    
    public S3CreateTaskProperties withS3LogsBucket(final String s3LogsBucket) {
        this.s3LogsBucket = s3LogsBucket;
        return this;
    }
    
    @Override
    public String toString() {
        return "S3CreateTaskProperties{" +
            "s3=" + s3 +
            ", logsBucket='" + logsBucket + '\'' +
            "} " + super.toString();
    }

    @Override
    public S3CreateTaskProperties withCompany(final String company) {
        this.setCompany(company);
        return this;
    }

    @Override
    public S3CreateTaskProperties withSbu(final String sbu) {
        this.setSbu(sbu);
        return this;
    }

    @Override
    public S3CreateTaskProperties withOrg(final String org) {
        this.setOrg(org);
        return this;
    }

    @Override
    public S3CreateTaskProperties withEngine(final String engine) {
        this.setEngine(engine);
        return this;
    }
}