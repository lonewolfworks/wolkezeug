package com.lonewolfworks.wolke.util;

import java.util.Map;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.GetCallerIdentityRequest;
import com.lonewolfworks.wolke.aws.ecs.CliPropertyHandler;
import com.lonewolfworks.wolke.aws.ecs.PropertyHandler;
import com.lonewolfworks.wolke.logging.HermanLogger;

public class PropertyHandlerUtil {

  
    public PropertyHandler getCliPropertyHandler(AWSCredentials sessionCredentials, HermanLogger logger,
            String environmentName, String rootDirectory, Map<String, String> customVariables) {
        final PropertyHandler handler = new CliPropertyHandler(logger, environmentName, rootDirectory, customVariables);
        PropertyHandlerUtil.addStandardProperties(sessionCredentials, handler);
        return handler;
    }

    private static void addStandardProperties(AWSCredentials sessionCredentials, PropertyHandler propertyHandler){
        String accountId = getAccountId(sessionCredentials);
        propertyHandler.addProperty("account.id", accountId);
    }

    private static String getAccountId(AWSCredentials sessionCredentials){
        AWSSecurityTokenService stsClient = AWSSecurityTokenServiceClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(sessionCredentials))
                .withClientConfiguration(new ClientConfiguration().withMaxErrorRetry(10)).build();
        return stsClient.getCallerIdentity(new GetCallerIdentityRequest()).getAccount();
    }
}
