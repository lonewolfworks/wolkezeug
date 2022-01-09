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

import com.amazonaws.services.lambda.AWSLambda;
import com.lonewolfworks.wolke.aws.ecs.EcsPushContext;
import com.lonewolfworks.wolke.aws.ecs.PropertyHandler;
import com.lonewolfworks.wolke.logging.HermanLogger;
import com.lonewolfworks.wolke.util.FileUtil;

import java.util.StringJoiner;

public class Auth0CreateContext {

    private HermanLogger logger;
    private PropertyHandler propertyHandler;
    private String rootPath;
    private FileUtil fileUtil;
    private AWSLambda lambdaClient;
    private Auth0BrokerConfiguration brokerConfiguration;

    public Auth0CreateContext fromECSPushContext(EcsPushContext pushContext, AWSLambda lambdaClient, Auth0BrokerConfiguration brokerConfiguration) {
        this.logger = pushContext.getLogger();
        this.propertyHandler = pushContext.getPropertyHandler();
        this.rootPath = pushContext.getRootPath();
        this.fileUtil = new FileUtil(pushContext.getRootPath(), pushContext.getLogger());
        this.lambdaClient = lambdaClient;
        this.brokerConfiguration = brokerConfiguration;
        return this;
    }

    public HermanLogger getLogger() {
        return logger;
    }

    public void setLogger(HermanLogger logger) {
        this.logger = logger;
    }

    public PropertyHandler getPropertyHandler() {
        return propertyHandler;
    }

    public void setPropertyHandler(PropertyHandler propertyHandler) {
        this.propertyHandler = propertyHandler;
    }

    public String getRootPath() {
        return rootPath;
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }

    public FileUtil getFileUtil() {
        return fileUtil;
    }

    public void setFileUtil(FileUtil fileUtil) {
        this.fileUtil = fileUtil;
    }

    public AWSLambda getLambdaClient() {
        return lambdaClient;
    }

    public void setLambdaClient(AWSLambda lambdaClient) {
        this.lambdaClient = lambdaClient;
    }

    public Auth0BrokerConfiguration getBrokerConfiguration() {
        return brokerConfiguration;
    }

    public void setBrokerConfiguration(Auth0BrokerConfiguration brokerConfiguration) {
        this.brokerConfiguration = brokerConfiguration;
    }

    public Auth0CreateContext withLogger(final HermanLogger logger) {
        this.logger = logger;
        return this;
    }

    public Auth0CreateContext withPropertyHandler(
            final PropertyHandler propertyHandler) {
        this.propertyHandler = propertyHandler;
        return this;
    }

    public Auth0CreateContext withRootPath(final String rootPath) {
        this.rootPath = rootPath;
        return this;
    }

    public Auth0CreateContext withLambdaClient(final AWSLambda lambdaClient) {
        this.lambdaClient = lambdaClient;
        return this;
    }

    public Auth0CreateContext withFileUtil(final FileUtil fileUtil) {
        this.fileUtil = fileUtil;
        return this;
    }

    public Auth0CreateContext withBrokerConfiguration(final Auth0BrokerConfiguration brokerConfiguration) {
        this.brokerConfiguration = brokerConfiguration;
        return this;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Auth0CreateContext.class.getSimpleName() + "[", "]")
                .add("logger=" + logger)
                .add("propertyHandler=" + propertyHandler)
                .add("rootPath='" + rootPath + "'")
                .add("fileUtil=" + fileUtil)
                .add("lambdaClient=" + lambdaClient)
                .add("brokerConfiguration=" + brokerConfiguration)
                .toString();
    }
}
