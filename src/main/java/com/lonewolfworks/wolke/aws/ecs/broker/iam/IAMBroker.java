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
package com.lonewolfworks.wolke.aws.ecs.broker.iam;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.model.CreateRoleRequest;
import com.amazonaws.services.identitymanagement.model.DeleteRolePolicyRequest;
import com.amazonaws.services.identitymanagement.model.GetRolePolicyRequest;
import com.amazonaws.services.identitymanagement.model.GetRoleRequest;
import com.amazonaws.services.identitymanagement.model.NoSuchEntityException;
import com.amazonaws.services.identitymanagement.model.PutRolePermissionsBoundaryRequest;
import com.amazonaws.services.identitymanagement.model.PutRolePolicyRequest;
import com.amazonaws.services.identitymanagement.model.Role;
import com.amazonaws.services.identitymanagement.model.Tag;
import com.amazonaws.services.identitymanagement.model.TagRoleRequest;
import com.amazonaws.services.identitymanagement.model.UpdateAssumeRolePolicyRequest;
import com.amazonaws.services.securitytoken.AWSSecurityTokenService;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClientBuilder;
import com.amazonaws.services.securitytoken.model.GetCallerIdentityRequest;
import com.lonewolfworks.wolke.aws.AwsExecException;
import com.lonewolfworks.wolke.aws.credentials.CredentialsHandler;
import com.lonewolfworks.wolke.aws.ecs.PropertyHandler;
import com.lonewolfworks.wolke.aws.ecs.PushType;
import com.lonewolfworks.wolke.aws.tags.HermanTag;
import com.lonewolfworks.wolke.aws.tags.TagUtil;
import com.lonewolfworks.wolke.logging.HermanLogger;

public class IAMBroker {

    private static final Logger LOGGER = LoggerFactory.getLogger(IAMBroker.class);

    private static final String POLICY_SUFFIX = "-policy";
    private static final String HERMAN_PERM_BOUNDARY_NAME = "lfg-iam-boundary-herman-otto-001";
    private static final String STANDARD_PERM_BOUNDARY_NAME = "lfg-iam-boundary-herman-standard-001";
    private HermanLogger buildLogger;

    public IAMBroker(HermanLogger buildLogger) {
        this.buildLogger = buildLogger;
    }

        
    
    public Role brokerAppRole(AmazonIdentityManagement client,
                              IamAppDefinition definition,
                              String rolePolicy,
                              String rolePath,
                              String suffix,
                              List<Tag> tags,
                              PropertyHandler propertyHandler,
                              AWSCredentials sessionCredentials) {
        return brokerAppRole(client, definition, rolePolicy, rolePath, suffix, tags, propertyHandler, sessionCredentials, PushType.ECS);
    }

    public Role brokerAppRole(AmazonIdentityManagement client, 
    						  IamAppDefinition definition,
                              String rolePolicy, 
                              String rolePath, 
                              String suffix,
                              List<Tag> tags,
                              PropertyHandler propertyHandler,
                              AWSCredentials sessionCredentials, PushType pushType) {
    
        String roleName = definition.getAppName()+suffix;
        Role role = getRole(client, roleName);

        if (rolePath == null) {
            //Temporary, used for prior support
            rolePath = "/aws-ecs/";
        }

        String assumePolicy;
        try {
            InputStream policyStream = getClass().getResourceAsStream("/iam/assume-role-policy-" + pushType.name().toLowerCase() + ".json");
            assumePolicy = IOUtils.toString(policyStream, Charset.defaultCharset());
        } catch (IOException e) {
            throw new AwsExecException("Error getting assume policy", e);
        }

        if (role == null) {
            buildLogger.addLogEntry("... Creating new role: " + roleName);

            CreateRoleRequest createRoleRequest = new CreateRoleRequest()
                    .withPath(rolePath)
                    .withRoleName(roleName)
                    .withTags(tags)
                    .withAssumeRolePolicyDocument(assumePolicy);

            determinePermissionBoundary(rolePath, roleName, sessionCredentials).ifPresent(permissionBoundary -> {
                buildLogger.addLogEntry("... Adding Permission Boundary: " + permissionBoundary);
                createRoleRequest.withPermissionsBoundary(permissionBoundary);
            });

            client.createRole(createRoleRequest);

        } else {
            buildLogger.addLogEntry("... Using existing role: " + roleName);

            client.updateAssumeRolePolicy(new UpdateAssumeRolePolicyRequest().withRoleName(roleName).withPolicyDocument(assumePolicy));
            client.tagRole(new TagRoleRequest().withRoleName(roleName).withTags(tags));
            
            determinePermissionBoundary(rolePath, roleName, sessionCredentials).ifPresent(permissionBoundary -> {
                buildLogger.addLogEntry("... Adding Permission Boundary: " + permissionBoundary);
                PutRolePermissionsBoundaryRequest boundaryRequest = new PutRolePermissionsBoundaryRequest()
                        .withPermissionsBoundary(permissionBoundary)
                        .withRoleName(roleName);
                client.putRolePermissionsBoundary(boundaryRequest);
            });
        }

        if (rolePolicy != null) {
            buildLogger.addLogEntry("... Updating the role policy");
            String fullPolicy = propertyHandler.mapInProperties(rolePolicy);
            PutRolePolicyRequest putRolePolicyRequest = new PutRolePolicyRequest()
                    .withPolicyName(roleName + POLICY_SUFFIX)
                    .withRoleName(roleName).withPolicyDocument(fullPolicy);
            client.putRolePolicy(putRolePolicyRequest);
        } else {
            try {
                client.getRolePolicy(new GetRolePolicyRequest().withPolicyName(roleName + POLICY_SUFFIX).withRoleName(roleName+suffix));
                client.deleteRolePolicy(new DeleteRolePolicyRequest().withPolicyName(roleName + POLICY_SUFFIX).withRoleName(roleName+suffix));
                buildLogger.addLogEntry("... No policy specified. The role policy was deleted.");
            } catch (NoSuchEntityException e) {
                LOGGER.debug("Role policy does not exist: " + roleName + POLICY_SUFFIX, e);
            }
        }

        role = getRole(client, roleName);
        try {
            //Roles take a short bit to percolate in IAM, no real status
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AwsExecException(e);
        }
        buildLogger.addLogEntry("... App role ARN: " + role.getArn());
        return role;
    }

    private Optional<String> determinePermissionBoundary(String rolePath, String appName, AWSCredentials sessionCredentials) {
        if (rolePath.contains("lfg-epay-portfolio")) {
            if (appName.contains("otto") || (appName.contains("herman") && !appName.contains("herman-test-task"))) {
                return Optional.of(buildPermissionBoundaryIam(sessionCredentials, HERMAN_PERM_BOUNDARY_NAME));
            } else if (!appName.contains("db-util") && !appName.contains("herman-test-task") && !appName.contains("aws-admin")) {
                return Optional.of(buildPermissionBoundaryIam(sessionCredentials, STANDARD_PERM_BOUNDARY_NAME));
            }
        }

        return Optional.empty();
    }

    private String buildPermissionBoundaryIam(AWSCredentials sessionCredentials, String boundaryName) {
        AWSSecurityTokenService stsClient = AWSSecurityTokenServiceClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(sessionCredentials))
                .withClientConfiguration(CredentialsHandler.getConfiguration()).build();
        String account = stsClient.getCallerIdentity(new GetCallerIdentityRequest()).getAccount();

        return "arn:aws:iam::" + account + ":policy/" + boundaryName;
    }

    public Role getRole(AmazonIdentityManagement client, String roleName) {
        try {
            return client.getRole(new GetRoleRequest().withRoleName(roleName)).getRole();
        } catch (AmazonServiceException ase) {
            if ("NoSuchEntity".equals(ase.getErrorCode())) {
                return null;
            }
            throw ase;
        }
    }
}
