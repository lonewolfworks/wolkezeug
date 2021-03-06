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
package com.lonewolfworks.wolke.task.cli.ecs;

import com.amazonaws.auth.AWSCredentials;
import com.lonewolfworks.wolke.aws.credentials.CredentialsHandler;
import com.lonewolfworks.wolke.aws.ecs.EcsPush;
import com.lonewolfworks.wolke.aws.ecs.EcsPushContext;
import com.lonewolfworks.wolke.aws.ecs.PropertyHandler;
import com.lonewolfworks.wolke.logging.HermanLogger;
import com.lonewolfworks.wolke.task.ecs.ECSPushPropertyFactory;
import com.lonewolfworks.wolke.task.ecs.ECSPushTaskProperties;
import com.lonewolfworks.wolke.util.PropertyHandlerUtil;

public class ECSPushTask {
    private HermanLogger logger;

    public ECSPushTask(HermanLogger logger) {
        this.logger = logger;
    }

    public void runTask(ECSPushTaskConfiguration configuration) {
        final AWSCredentials sessionCredentials = CredentialsHandler.getCredentials();
        final PropertyHandler propertyHandler = new PropertyHandlerUtil().getCliPropertyHandler(
            sessionCredentials,
            logger,
            configuration.getEnvironmentName(),
            configuration.getRootPath(),
            configuration.getCustomVariables());
        final ECSPushTaskProperties taskProperties = ECSPushPropertyFactory.getTaskProperties(sessionCredentials, logger, configuration.getCustomConfigurationBucket(), configuration.getRegion(), propertyHandler);
        
        if(taskProperties.getRdsCredentialBrokerImage()!=null) {
        	propertyHandler.addProperty("herman.rdsCredentialBrokerImage", taskProperties.getRdsCredentialBrokerImage());
        }

        EcsPushContext context = new EcsPushContext()
            .withLogger(logger)
            .withPropertyHandler(propertyHandler)
            .withEnvName(configuration.getEnvironmentName())
            .withSessionCredentials(CredentialsHandler.getCredentials())
            .withAwsClientConfig(CredentialsHandler.getConfiguration())
            .withRegion(configuration.getRegion())
            .withTimeout(configuration.getTimeout())
            .withRootPath(configuration.getRootPath())
            .withTaskProperties(taskProperties)
            .withCustomConfigurationBucket(configuration.getCustomConfigurationBucket());
        EcsPush push = new EcsPush(context);
        push.push();

        logger.addLogEntry("Done!");
    }


}
