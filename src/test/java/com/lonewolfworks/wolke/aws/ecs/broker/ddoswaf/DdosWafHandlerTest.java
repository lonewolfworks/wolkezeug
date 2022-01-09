package com.lonewolfworks.wolke.aws.ecs.broker.ddoswaf;

import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.waf.model.WafActionType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.lonewolfworks.wolke.aws.ecs.EcsPushDefinition;
import com.lonewolfworks.wolke.aws.ecs.PropertyHandler;
import com.lonewolfworks.wolke.logging.HermanLogger;
import com.lonewolfworks.wolke.task.ecs.ECSPushTaskProperties;
import com.lonewolfworks.wolke.util.FileUtil;
import java.util.Arrays;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class DdosWafHandlerTest {

    @Test
    public void constructorDoesNotThrowError() {
        // GIVEN
        ECSPushTaskProperties taskProperties = mock(ECSPushTaskProperties.class);
        EcsPushDefinition definition = mock(EcsPushDefinition.class);
        HermanLogger logger = mock(HermanLogger.class);
        AWSLambda lambdaClient = mock(AWSLambda.class);
        PropertyHandler propertyHandler = mock(PropertyHandler.class);
        FileUtil fileUtil = mock(FileUtil.class);

        // WHEN
        DdosWafHandler handler = new DdosWafHandler(taskProperties, definition, logger, lambdaClient, propertyHandler, fileUtil);

        // THEN
        // Error not thrown
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructorThrowsError() {
        // GIVEN
        ECSPushTaskProperties taskProperties = mock(ECSPushTaskProperties.class);
        EcsPushDefinition definition = mock(EcsPushDefinition.class);
        HermanLogger logger = mock(HermanLogger.class);
        AWSLambda lambdaClient = mock(AWSLambda.class);
        PropertyHandler propertyHandler = mock(PropertyHandler.class);
        FileUtil fileUtil = null;

        // WHEN
        DdosWafHandler handler = new DdosWafHandler(taskProperties, definition, logger, lambdaClient, propertyHandler, fileUtil);

        // THEN
        // Error thrown
    }

    @Test
    public void isBrokerActive_true() {
        // GIVEN
        ECSPushTaskProperties taskProperties = new ECSPushTaskProperties().withDdosWaf(new DdosWafBrokerProperties()
            .withDdosWafLambda("ddos-lambda-broker")
            .withWafConfiguration(new WafConfiguration()
                .withDefaultAction(WafActionType.ALLOW)
                .withRuleActions(Arrays.asList(new WafRuleAction()
                    .withAction(WafActionType.ALLOW)
                    .withId("0b5450d4-825e-48a4-9fa7-ab224fb32b58")))));
        EcsPushDefinition definition = mock(EcsPushDefinition.class);
        HermanLogger logger = mock(HermanLogger.class);
        AWSLambda lambdaClient = mock(AWSLambda.class);
        PropertyHandler propertyHandler = mock(PropertyHandler.class);
        FileUtil fileUtil = mock(FileUtil.class);

        // WHEN
        DdosWafHandler handler = new DdosWafHandler(taskProperties, definition, logger, lambdaClient, propertyHandler, fileUtil);

        // THEN
        assertTrue(handler.isBrokerActive());
    }

    @Test
    public void isBrokerActive_false() {
        // GIVEN
        ECSPushTaskProperties taskProperties = new ECSPushTaskProperties();
        EcsPushDefinition definition = mock(EcsPushDefinition.class);
        HermanLogger logger = mock(HermanLogger.class);
        AWSLambda lambdaClient = mock(AWSLambda.class);
        PropertyHandler propertyHandler = mock(PropertyHandler.class);
        FileUtil fileUtil = mock(FileUtil.class);

        // WHEN
        DdosWafHandler handler = new DdosWafHandler(taskProperties, definition, logger, lambdaClient, propertyHandler, fileUtil);

        // THEN
        assertFalse(handler.isBrokerActive());
    }

    @Test
    public void brokerDDoSWAFConfiguration_defaultConfig() {
        // GIVEN
        ECSPushTaskProperties taskProperties = new ECSPushTaskProperties().withDdosWaf(new DdosWafBrokerProperties()
            .withDdosWafLambda("ddos-lambda-broker")
            .withWafConfiguration(new WafConfiguration()
                .withDefaultAction(WafActionType.ALLOW)
                .withRuleActions(Arrays.asList(new WafRuleAction()
                    .withAction(WafActionType.ALLOW)
                    .withId("0b5450d4-825e-48a4-9fa7-ab224fb32b58")))));
        EcsPushDefinition definition = mock(EcsPushDefinition.class);
        HermanLogger logger = mock(HermanLogger.class);
        AWSLambda lambdaClient = mock(AWSLambda.class);
        PropertyHandler propertyHandler = mock(PropertyHandler.class);
        FileUtil fileUtil = mock(FileUtil.class);

        final String appName = "herman-test-app";
        final String loadBalancerArn = "testArn";
        final DdosWafBrokerClient client = mock(DdosWafBrokerClient.class);

        // WHEN
        DdosWafHandler handler = new DdosWafHandler(taskProperties, definition, logger, lambdaClient, propertyHandler, fileUtil);
        handler.setDdosWafBrokerClient(client);
        handler.brokerDDoSWAFConfiguration(appName, loadBalancerArn);

        // THEN
        verify(client, times(1)).brokerDDoSWAFConfiguration(appName, loadBalancerArn, taskProperties.getDdosWaf());
    }

    @Test
    public void brokerDDoSWAFConfiguration_customConfig() throws JsonProcessingException {
        // GIVEN
        ECSPushTaskProperties taskProperties = new ECSPushTaskProperties().withDdosWaf(new DdosWafBrokerProperties()
            .withDdosWafLambda("ddos-lambda-broker")
            .withWafConfiguration(new WafConfiguration()
                .withDefaultAction(WafActionType.ALLOW)
                .withRuleActions(Arrays.asList(new WafRuleAction()
                    .withAction(WafActionType.ALLOW)
                    .withId("0b5450d4-825e-48a4-9fa7-ab224fb32b58")))));
        EcsPushDefinition definition = new EcsPushDefinition();
        HermanLogger logger = mock(HermanLogger.class);
        AWSLambda lambdaClient = mock(AWSLambda.class);
        PropertyHandler propertyHandler = mock(PropertyHandler.class);
        FileUtil fileUtil = mock(FileUtil.class);

        final String appName = "herman-test-app";
        final String loadBalancerArn = "testArn";
        final String wafFile = "wafConfig.ynl";
        final DdosWafBrokerClient client = mock(DdosWafBrokerClient.class);

        WafConfiguration config = new WafConfiguration()
            .withDefaultAction(WafActionType.BLOCK)
            .withRuleActions(Arrays.asList(new WafRuleAction()
                .withAction(WafActionType.ALLOW)
                .withId("0b5450d9-825e-48a4-9fa7-ab224fb32b58")));
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        String configAsYml = mapper.writeValueAsString(config);

        definition.setWaf(wafFile);
        when(fileUtil.findFile(wafFile, false)).thenReturn(configAsYml);
        when(propertyHandler.mapInProperties(configAsYml)).thenReturn(configAsYml);

        // WHEN
        DdosWafHandler handler = new DdosWafHandler(taskProperties, definition, logger, lambdaClient, propertyHandler, fileUtil);
        handler.setDdosWafBrokerClient(client);
        handler.brokerDDoSWAFConfiguration(appName, loadBalancerArn);

        // THEN
        ArgumentCaptor<DdosWafBrokerProperties> argument = ArgumentCaptor.forClass(DdosWafBrokerProperties.class);
        verify(client).brokerDDoSWAFConfiguration(eq(appName), eq(loadBalancerArn), argument.capture());
        assertEquals("ddos-lambda-broker", argument.getValue().getDdosWafLambda());
        assertEquals(mapper.writeValueAsString(config), mapper.writeValueAsString(argument.getValue().getWafConfiguration()));
    }
}