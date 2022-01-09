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

import com.amazonaws.services.kms.AWSKMS;
import com.amazonaws.services.kms.model.EncryptRequest;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.model.InvocationType;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lonewolfworks.wolke.aws.ecs.EcsPushDefinition;
import com.lonewolfworks.wolke.aws.ecs.PropertyHandler;
import com.lonewolfworks.wolke.aws.ecs.broker.domain.HermanBrokerStatus;
import com.lonewolfworks.wolke.aws.ecs.logging.LoggingService;
import com.lonewolfworks.wolke.logging.HermanLogger;
import com.lonewolfworks.wolke.util.FileUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.lonewolfworks.wolke.util.HttpStatusUtil.isSuccessful;

public class Auth0Broker {

    private static final String CIPHER_PREFIX = "{cipher}";
    private HermanLogger buildLogger;
    private PropertyHandler bambooPropertyHandler;
    private FileUtil fileUtil;
    private Auth0BrokerConfiguration brokerConfiguration;
    private AWSLambda lambdaClient;

    public Auth0Broker(Auth0CreateContext context) {
        this.bambooPropertyHandler = context.getPropertyHandler();
        this.buildLogger = context.getLogger();
        this.fileUtil = context.getFileUtil();
        this.brokerConfiguration = context.getBrokerConfiguration();
        this.lambdaClient = context.getLambdaClient();
    }

    public Auth0BrokerResponse brokerAuth0Definition(Auth0Configuration auth0Configuration) {
        buildLogger.addLogEntry("\n");
        buildLogger.addLogEntry("Brokering Auth0 configuration");

        String payload;
        try {
            Auth0BrokerRequest auth0BrokerRequest = getAuth0BrokerRequest(auth0Configuration);
            payload = new ObjectMapper().writeValueAsString(auth0BrokerRequest);
        } catch (Exception ex) {
            throw new RuntimeException("Error getting Auth0 Broker payload", ex);
        }

        InvokeRequest auth0BrokerInvokeRequest = new InvokeRequest()
                .withFunctionName(brokerConfiguration.getBrokerProperties().getAuth0Lambda())
                .withInvocationType(InvocationType.RequestResponse)
                .withPayload(payload);
        buildLogger.addLogEntry("... Invoke request sent to the broker: " + brokerConfiguration.getBrokerProperties().getAuth0Lambda());

        InvokeResult invokeResult = this.lambdaClient.invoke(auth0BrokerInvokeRequest);

        if (isSuccessful(invokeResult.getStatusCode()) && StringUtils.isEmpty(invokeResult.getFunctionError())) {
            String auth0BrokerResponseJson = new String(invokeResult.getPayload().array(), StandardCharsets.UTF_8);

            Auth0BrokerResponse response;
            try {
                response = new ObjectMapper().readValue(auth0BrokerResponseJson, Auth0BrokerResponse.class);
            } catch (Exception e) {
                throw new RuntimeException("Unable to parse Auth0 broker response from: " + auth0BrokerResponseJson, e);
            }

            response.getUpdates().forEach(update -> {
                buildLogger.addLogEntry(String.format("... Auth0 Broker: [%s] %s", update.getStatus(), update.getMessage()));

                if (HermanBrokerStatus.ERROR.equals(update.getStatus())) {
                    buildLogger.addLogEntry("... Error returned by the Auth0 Broker given payload: " + payload);
                    throw new RuntimeException("Auth0 broker error. See logs.");
                }
            });

            addAuth0LinkToLogs(response.getClientId(), response.getTenantId());

            return response;
        } else {
            buildLogger.addLogEntry("... Error thrown by the Auth0 Broker given payload: " + payload);
            String auth0BrokerResponseJson = new String(invokeResult.getPayload().array(), StandardCharsets.UTF_8);
            throw new RuntimeException("Error invoking the Auth0 Broker: " + auth0BrokerResponseJson);
        }
    }

    public Auth0Configuration brokerAuth0ApplicationDeploymentFromEcsPush(EcsPushDefinition definition, AWSKMS kmsClient, String targetKey) {
        Auth0Configuration auth0Configuration = definition.getAuth0();

        auth0Configuration.setInjectNames(auth0Configuration.getInjectNames() == null ? new Auth0InjectConfiguration() : auth0Configuration.getInjectNames());
        auth0Configuration.getInjectNames().setDefaults();

        Auth0BrokerResponse auth0BrokerResponse = brokerAuth0Definition(auth0Configuration);

        injectAuth0Variables(auth0Configuration, auth0BrokerResponse, kmsClient, targetKey);

        return auth0Configuration;
    }

    private Auth0BrokerRequest getAuth0BrokerRequest(Auth0Configuration auth0ConfigurationDefinition) throws JsonProcessingException {
        Auth0ConfigurationRequest auth0ConfigurationRequest = getAuth0Configuration(auth0ConfigurationDefinition);
        Auth0ApplicationDeploymentRequest auth0ApplicationDeploymentRequest = new Auth0ApplicationDeploymentRequest()
                .withRevision(bambooPropertyHandler.lookupVariable("bamboo.planRepository.revision"))
                .withVersion(bambooPropertyHandler.lookupVariable("bamboo.deploy.version"));

        return new Auth0BrokerRequest()
                .withConfiguration(auth0ConfigurationRequest)
                .withDeployment(auth0ApplicationDeploymentRequest);
    }

    private Auth0ConfigurationRequest getAuth0Configuration(Auth0Configuration auth0ConfigurationDefinition) throws JsonProcessingException {
        Auth0ConfigurationRequest auth0ConfigurationRequest;
        if (auth0ConfigurationDefinition != null) {
            String client = bambooPropertyHandler.mapInProperties(fileUtil.findFile(auth0ConfigurationDefinition.getClient(), false));
            Auth0Client auth0Client = new ObjectMapper().readValue(client, Auth0Client.class);

            List<Auth0ClientGrants> auth0ClientGrants = null;
            if (StringUtils.isNotBlank(auth0ConfigurationDefinition.getClientGrants())) {
                String clientGrants = bambooPropertyHandler.mapInProperties(fileUtil.findFile(auth0ConfigurationDefinition.getClientGrants(), true));
                auth0ClientGrants = new ObjectMapper().readValue(clientGrants, new TypeReference<List<Auth0ClientGrants>>() {
                });
            }

            auth0ConfigurationRequest = new Auth0ConfigurationRequest()
                    .withTenant(auth0ConfigurationDefinition.getTenant())
                    .withClient(auth0Client)
                    .withClientGrants(auth0ClientGrants)
                    .withInjectNames(auth0ConfigurationDefinition.getInjectNames() == null ? new Auth0InjectConfiguration() : auth0ConfigurationDefinition.getInjectNames());
        } else {
            auth0ConfigurationRequest = null;
        }
        return auth0ConfigurationRequest;
    }

    private void addAuth0LinkToLogs(String clientId, String tenantName) {
        if (Optional.ofNullable(clientId).isPresent()) {
            LoggingService loggingService = new LoggingService(buildLogger);
            String link = String.format("https://manage.auth0.com/us/dashboard/us/%s/applications/%s/settings", tenantName, clientId);
            loggingService.logSection("Auth0 Console", link);
        }
    }

    private void injectAuth0Variables(Auth0Configuration auth0ConfigurationDefinition, Auth0BrokerResponse response, AWSKMS kmsClient, String targetKey) {
        String encryptedSecret = encryptClientSecret(kmsClient, targetKey, response.getClientSecret());

        buildLogger.addLogEntry("... clientId: " + response.getClientId());
        buildLogger.addLogEntry("... encrypted clientSecret: " + encryptedSecret);
        buildLogger.addLogEntry("... clientId encrypted using KMS key: " + targetKey);

        auth0ConfigurationDefinition.setClientId(response.getClientId());
        auth0ConfigurationDefinition.setEncryptedClientSecret(encryptedSecret);

        injectCipherNotation(auth0ConfigurationDefinition);
    }

    private String encryptClientSecret(AWSKMS kmsClient, String targetKey, String clientSecret) {
        Assert.hasText(targetKey, "kmsKeyId must not be blank");
        EncryptRequest encryptRequest = new EncryptRequest().withKeyId(targetKey).withPlaintext(ByteBuffer.wrap(clientSecret.getBytes()));
        ByteBuffer encryptedBytes = kmsClient.encrypt(encryptRequest).getCiphertextBlob();

        return StandardCharsets.UTF_8.decode(ByteBuffer.wrap(Base64.getEncoder().encode(encryptedBytes.array()))).toString();
    }

    private void injectCipherNotation(Auth0Configuration auth0Configuration) {
        String clientSecret = auth0Configuration.getEncryptedClientSecret();
        String clientSecretVariableName = auth0Configuration.getInjectNames().getEncryptedClientSecret();
        Set<String> encryptableSecretNames = new HashSet<>(Arrays.asList("auth0.encrypted.client.secret", "auth0_encrypted_client_secret"));

        if (encryptableSecretNames.contains(clientSecretVariableName.toLowerCase())) {
            clientSecret = CIPHER_PREFIX + clientSecret;
        }

        auth0Configuration.setEncryptedClientSecret(clientSecret);
    }
}
