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

import com.amazonaws.services.lambda.AWSLambda;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.lonewolfworks.wolke.aws.AwsExecException;
import com.lonewolfworks.wolke.aws.ecs.EcsPushDefinition;
import com.lonewolfworks.wolke.aws.ecs.PropertyHandler;
import com.lonewolfworks.wolke.logging.HermanLogger;
import com.lonewolfworks.wolke.task.ecs.ECSPushTaskProperties;
import com.lonewolfworks.wolke.util.FileUtil;
import org.springframework.util.Assert;

public class DdosWafHandler {

    private ECSPushTaskProperties taskProperties;
    private EcsPushDefinition definition;
    private HermanLogger logger;
    private PropertyHandler propertyHandler;
    private FileUtil fileUtil;
    private DdosWafBrokerClient ddosWafBrokerClient;

    public DdosWafHandler(ECSPushTaskProperties taskProperties,
        EcsPushDefinition definition, HermanLogger logger, AWSLambda lambdaClient,
        PropertyHandler propertyHandler, FileUtil fileUtil) {
        Assert.notNull(taskProperties, "taskProperties must not be null");
        this.taskProperties = taskProperties;

        Assert.notNull(definition, "definition must not be null");
        this.definition = definition;

        Assert.notNull(logger, "logger must not be null");
        this.logger = logger;

        Assert.notNull(propertyHandler, "propertyHandler must not be null");
        this.propertyHandler = propertyHandler;

        Assert.notNull(fileUtil, "fileUtil must not be null");
        this.fileUtil = fileUtil;

        Assert.notNull(lambdaClient, "lambdaClient must not be null");
        ddosWafBrokerClient = new DdosWafBrokerClient(logger, lambdaClient);
    }

    public boolean isBrokerActive() {
        return taskProperties.getDdosWaf() != null && taskProperties.getDdosWaf().getDdosWafLambda() != null;
    }

    public void brokerDDoSWAFConfiguration(String appName, String loadBalancerArn) {
        Assert.notNull(appName, "appName must not be null");
        Assert.notNull(loadBalancerArn, "loadBalancerArn must not be null");
        Assert.notNull(taskProperties.getDdosWaf(), "ddosWaf in ECSPushTaskProperties must not be null");
        Assert.notNull(taskProperties.getDdosWaf().getDdosWafLambda(),
            "ddosWafLambda in ECSPushTaskProperties must not be null");

        DdosWafBrokerProperties ddosWafBrokerProperties;
        if (definition.getWaf() != null) {
            WafConfiguration wafConfiguration;
            try {
                String wafConfigurationString = propertyHandler
                    .mapInProperties(fileUtil.findFile(definition.getWaf(), false));
                ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
                wafConfiguration = mapper.readValue(wafConfigurationString, WafConfiguration.class);
            } catch (Exception e) {
                throw new AwsExecException(e);
            }

            ddosWafBrokerProperties = new DdosWafBrokerProperties()
                .withDdosWafLambda(taskProperties.getDdosWaf().getDdosWafLambda())
                .withWafConfiguration(wafConfiguration);
            logger.addLogEntry("Using custom WAF rule actions");
        } else {
            ddosWafBrokerProperties = taskProperties.getDdosWaf();
            logger.addLogEntry("Using default WAF rule actions");
        }

        ddosWafBrokerClient.brokerDDoSWAFConfiguration(appName, loadBalancerArn, ddosWafBrokerProperties);
    }

    public void setDdosWafBrokerClient(
        DdosWafBrokerClient ddosWafBrokerClient) {
        this.ddosWafBrokerClient = ddosWafBrokerClient;
    }
}
