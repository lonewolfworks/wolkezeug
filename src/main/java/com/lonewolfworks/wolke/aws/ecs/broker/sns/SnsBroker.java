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
package com.lonewolfworks.wolke.aws.ecs.broker.sns;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.CreateTopicRequest;
import com.amazonaws.services.sns.model.CreateTopicResult;
import com.amazonaws.services.sns.model.SetTopicAttributesRequest;
import com.amazonaws.services.sns.model.SubscribeRequest;
import com.amazonaws.services.sns.model.Subscription;
import com.amazonaws.services.sns.model.UnsubscribeRequest;
import com.amazonaws.util.StringUtils;
import com.lonewolfworks.wolke.aws.ecs.PropertyHandler;
import com.lonewolfworks.wolke.logging.HermanLogger;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SnsBroker {


    public static final String AND_ENDPOINT = " and endpoint: ";
    private HermanLogger logger;
    private PropertyHandler handler;

    public SnsBroker(HermanLogger logger, PropertyHandler handler) {
        this.logger = logger;
        this.handler = handler;
    }

    /**
     * http://docs.aws.amazon.com/sns/latest/api/API_SetTopicAttributes.html
     * <p>
     * Valid Map Keys: Policy
     */

    public void brokerTopic(AmazonSNS client, SnsTopic topic, String topicPolicy) {

        CreateTopicResult createTopicResult;
        logger.addLogEntry("Creating topic :  " + topic.getName());
        createTopicResult = client.createTopic(new CreateTopicRequest().withName(topic.getName()));

        String topicArn = createTopicResult.getTopicArn();

        if (topicPolicy != null) {
            String fullPolicy = handler.mapInProperties(topicPolicy);
            SetTopicAttributesRequest setTopicAttributesRequest = new SetTopicAttributesRequest()
                    .withAttributeName("Policy").withAttributeValue(fullPolicy);
            client.setTopicAttributes(setTopicAttributesRequest.withTopicArn(topicArn));
        }

        updateDeliveryStatusAttributes(client, topic, topicArn);

        if (topic.getSubscriptions() != null) {
            //automatically remove subscriptions
            autoDeleteSubscriptions(client, topic, topicArn);

            addSubscriptions(client, topic, topicArn);
        }

        addServerSideEncryption(client, topic, topicArn);
    }

    private void addServerSideEncryption(AmazonSNS client, SnsTopic topic, String topicArn) {
        if (topic.getServerSideEncryption()) {
            if (!StringUtils.isNullOrEmpty(topic.getKmsMasterKeyId())) {
                SetTopicAttributesRequest setAttributesRequest = new SetTopicAttributesRequest()
                        .withTopicArn(topicArn)
                        .withAttributeName("KmsMasterKeyId")
                        .withAttributeValue(topic.getKmsMasterKeyId());

                client.setTopicAttributes(setAttributesRequest);
            } else {
                SetTopicAttributesRequest setAttributesRequest = new SetTopicAttributesRequest()
                        .withTopicArn(topicArn)
                        .withAttributeName("KmsMasterKeyId")
                        .withAttributeValue("alias/aws/sns");

                client.setTopicAttributes(setAttributesRequest);
            }
        } else {
            SetTopicAttributesRequest setAttributesRequest = new SetTopicAttributesRequest()
                    .withTopicArn(topicArn)
                    .withAttributeName("KmsMasterKeyId")
                    .withAttributeValue("");

            client.setTopicAttributes(setAttributesRequest);
        }
    }

    private void addSubscriptions(AmazonSNS client, SnsTopic topic, String topicArn) {
        for (SnsSubscription subscription : topic.getSubscriptions()) {
            String protocol = subscription.getProtocol();
            String endpoint = subscription.getEndpoint();
            if (!StringUtils.isNullOrEmpty(protocol) && !StringUtils.isNullOrEmpty(endpoint)) {
                logger.addLogEntry("Adding subscription with protocol: " + protocol + AND_ENDPOINT + endpoint);
                SubscribeRequest subscribeRequest = new SubscribeRequest(topicArn, protocol, endpoint);
                client.subscribe(subscribeRequest);
            } else {
                logger.addLogEntry("Skipping subscription with protocol: " + protocol + AND_ENDPOINT + endpoint);
            }
        }
    }

    private void autoDeleteSubscriptions(AmazonSNS client, SnsTopic topic, String topicArn) {
        if (topic.getAutoRemoveSubscriptions() != null && topic.getAutoRemoveSubscriptions()) {
            for (Subscription subscription : client.listSubscriptionsByTopic(topicArn).getSubscriptions()) {
                boolean subscriptionExists = topic.getSubscriptions().stream().anyMatch(
                        sub -> equalsWithoutNonEndpointCharacters(sub.getEndpoint(), subscription.getEndpoint()) &&
                                equalsWithoutNonEndpointCharacters(sub.getProtocol(), subscription.getProtocol()));
                if (!subscriptionExists) {
                    logger.addLogEntry(
                            "Delete subscription with protocol: " + subscription.getProtocol() + AND_ENDPOINT + subscription
                                    .getEndpoint() +
                                    " as it is no longer in the subscription list");
                    client.unsubscribe(new UnsubscribeRequest(subscription.getSubscriptionArn()));
                }
            }
        }
    }

    private void updateDeliveryStatusAttributes(AmazonSNS client, SnsTopic topic, String topicArn) {

        Map<String, String> requestedTopicAttributeNameValueMap = new HashMap<>(
                CollectionUtils.isEmpty(topic.getDeliveryStatusAttributes()) ? 0
                        : topic.getDeliveryStatusAttributes().size());
        if (CollectionUtils.isNotEmpty(topic.getDeliveryStatusAttributes())) {
            topic.getDeliveryStatusAttributes().forEach(i -> {
                logger.addLogEntry(
                        "Updating topic delivery status attribute. Name: " + i.getName() + " and value: " + i.getValue());
                requestedTopicAttributeNameValueMap.put(i.getName(), i.getValue());
            });
        }

        Map<String, String> possibleDeliveryAttributeNameValueMap = new HashMap<>(
                SnsDeliveryStatusAttribute.values().length);
        Arrays.asList(SnsDeliveryStatusAttribute.values())
                .forEach(i -> possibleDeliveryAttributeNameValueMap.put(i.getName(), i.getValue()));

        Map<String, String> updatedDeliveryStatusTopicAttributeNameValueMap = new HashMap<>(
                requestedTopicAttributeNameValueMap);

        client.getTopicAttributes(topicArn).getAttributes().forEach((k, v) -> {
            if (possibleDeliveryAttributeNameValueMap.get(k) != null && !requestedTopicAttributeNameValueMap
                    .containsKey(k)) {
                logger.addLogEntry("Defaulting topic delivery status attribute. Key: " + k + " and value: "
                        + possibleDeliveryAttributeNameValueMap.get(k));
                updatedDeliveryStatusTopicAttributeNameValueMap
                        .put(k, SnsDeliveryStatusAttribute.valueOf(k).getValue());
            }
        });

        updatedDeliveryStatusTopicAttributeNameValueMap
                .forEach((k, v) -> logger.addLogEntry("Attributes to be updated - key : " + k + " value : " + v));

        updatedDeliveryStatusTopicAttributeNameValueMap.forEach((k, v) -> {
            SetTopicAttributesRequest setTopicAttributesRequest =
                    new SetTopicAttributesRequest().withAttributeName(k).withAttributeValue(v);
            client.setTopicAttributes(setTopicAttributesRequest.withTopicArn(topicArn));
        });
    }

    public boolean equalsWithoutNonEndpointCharacters(String s1, String s2) {
        String string1 = s1.replaceAll("[^a-zA-Z0-9@\\.]", "");
        String string2 = s2.replaceAll("[^a-zA-Z0-9@\\.]", "");
        return equalsIgnoreCaseWithNullChecking(string1, string2);
    }

    public boolean equalsIgnoreCaseWithNullChecking(String s1, String s2) {
        return s1 == s2 || (s1 != null && s1.equalsIgnoreCase(s2));
    }

}
