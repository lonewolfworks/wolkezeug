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
package com.lonewolfworks.wolke.aws.ecs;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ecs.AmazonECS;
import com.amazonaws.services.ecs.AmazonECSClientBuilder;
import com.amazonaws.services.ecs.model.AssignPublicIp;
import com.amazonaws.services.ecs.model.AwsVpcConfiguration;
import com.amazonaws.services.ecs.model.Container;
import com.amazonaws.services.ecs.model.ContainerDefinition;
import com.amazonaws.services.ecs.model.CreateServiceRequest;
import com.amazonaws.services.ecs.model.CreateServiceResult;
import com.amazonaws.services.ecs.model.DeregisterTaskDefinitionRequest;
import com.amazonaws.services.ecs.model.DescribeServicesRequest;
import com.amazonaws.services.ecs.model.DescribeServicesResult;
import com.amazonaws.services.ecs.model.DescribeTaskDefinitionRequest;
import com.amazonaws.services.ecs.model.DescribeTasksRequest;
import com.amazonaws.services.ecs.model.Failure;
import com.amazonaws.services.ecs.model.ListTaskDefinitionsRequest;
import com.amazonaws.services.ecs.model.ListTaskDefinitionsResult;
import com.amazonaws.services.ecs.model.LoadBalancer;
import com.amazonaws.services.ecs.model.NetworkConfiguration;
import com.amazonaws.services.ecs.model.RegisterTaskDefinitionRequest;
import com.amazonaws.services.ecs.model.RegisterTaskDefinitionResult;
import com.amazonaws.services.ecs.model.RunTaskRequest;
import com.amazonaws.services.ecs.model.RunTaskResult;
import com.amazonaws.services.ecs.model.Secret;
import com.amazonaws.services.ecs.model.Service;
import com.amazonaws.services.ecs.model.ServiceEvent;
import com.amazonaws.services.ecs.model.StopTaskRequest;
import com.amazonaws.services.ecs.model.Task;
import com.amazonaws.services.ecs.model.TaskDefinition;
import com.amazonaws.services.ecs.model.TaskDefinitionPlacementConstraint;
import com.amazonaws.services.ecs.model.TaskDefinitionPlacementConstraintType;
import com.amazonaws.services.ecs.model.UpdateServiceRequest;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClientBuilder;
import com.amazonaws.services.identitymanagement.model.Role;
import com.amazonaws.services.kinesis.AmazonKinesis;
import com.amazonaws.services.kinesis.AmazonKinesisClientBuilder;
import com.amazonaws.services.kms.AWSKMS;
import com.amazonaws.services.kms.AWSKMSClientBuilder;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.rds.AmazonRDS;
import com.amazonaws.services.rds.AmazonRDSClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.util.IOUtils;
import com.lonewolfworks.wolke.aws.AwsExecException;
import com.lonewolfworks.wolke.aws.ecs.broker.auth0.Auth0Broker;
import com.lonewolfworks.wolke.aws.ecs.broker.auth0.Auth0BrokerConfiguration;
import com.lonewolfworks.wolke.aws.ecs.broker.auth0.Auth0Configuration;
import com.lonewolfworks.wolke.aws.ecs.broker.auth0.Auth0CreateContext;
import com.lonewolfworks.wolke.aws.ecs.broker.autoscaling.AutoscalingBroker;
import com.lonewolfworks.wolke.aws.ecs.broker.ddoswaf.DdosWafHandler;
import com.lonewolfworks.wolke.aws.ecs.broker.dynamodb.DynamoDBBroker;
import com.lonewolfworks.wolke.aws.ecs.broker.iam.IAMBroker;
import com.lonewolfworks.wolke.aws.ecs.broker.kinesis.KinesisBroker;
import com.lonewolfworks.wolke.aws.ecs.broker.kinesis.KinesisStream;
import com.lonewolfworks.wolke.aws.ecs.broker.kms.KmsBroker;
import com.lonewolfworks.wolke.aws.ecs.broker.rds.EcsPushFactory;
import com.lonewolfworks.wolke.aws.ecs.broker.rds.RdsBroker;
import com.lonewolfworks.wolke.aws.ecs.broker.rds.RdsInstance;
import com.lonewolfworks.wolke.aws.ecs.broker.s3.S3Broker;
import com.lonewolfworks.wolke.aws.ecs.broker.s3.S3Bucket;
import com.lonewolfworks.wolke.aws.ecs.broker.s3.S3CreateContext;
import com.lonewolfworks.wolke.aws.ecs.broker.secretsmgr.SecretsManagerBroker;
import com.lonewolfworks.wolke.aws.ecs.broker.sns.SnsBroker;
import com.lonewolfworks.wolke.aws.ecs.broker.sns.SnsTopic;
import com.lonewolfworks.wolke.aws.ecs.broker.sqs.SqsBroker;
import com.lonewolfworks.wolke.aws.ecs.broker.sqs.SqsQueue;
import com.lonewolfworks.wolke.aws.ecs.cluster.EcsClusterIntrospector;
import com.lonewolfworks.wolke.aws.ecs.cluster.EcsClusterMetadata;
import com.lonewolfworks.wolke.aws.ecs.loadbalancing.CertHandler;
import com.lonewolfworks.wolke.aws.ecs.loadbalancing.DnsRegistrar;
import com.lonewolfworks.wolke.aws.ecs.loadbalancing.EcsLoadBalancerHandler;
import com.lonewolfworks.wolke.aws.ecs.loadbalancing.EcsLoadBalancerV2Handler;
import com.lonewolfworks.wolke.aws.ecs.loadbalancing.ElbOrAlbDecider;
import com.lonewolfworks.wolke.aws.ecs.loadbalancing.ServicePurger;
import com.lonewolfworks.wolke.aws.ecs.logging.LoggingService;
import com.lonewolfworks.wolke.aws.tags.HermanTag;
import com.lonewolfworks.wolke.aws.tags.TagUtil;
import com.lonewolfworks.wolke.logging.HermanLogger;
import com.lonewolfworks.wolke.task.ecs.ECSPushTaskProperties;
import com.lonewolfworks.wolke.util.FileUtil;

public class EcsPush {

    private static final String INTERRUPTED_WHILE_POLLING = "Interrupted while polling";
    private static final int POLLING_INTERVAL_MS = 10000;
    private static final Integer DEFAULT_GRACE_PERIOD = 600;

    private HermanLogger logger;
    private EcsPushContext pushContext;
    private PropertyHandler bambooPropertyHandler;
    private ECSPushTaskProperties taskProperties;
    private AmazonIdentityManagement iamClient;
    private AmazonECS ecsClient;
    private AmazonEC2 ec2Client;
    private com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancing elbClient;
    private com.amazonaws.services.elasticloadbalancingv2.AmazonElasticLoadBalancing elbV2Client;
    private AmazonS3 s3Client;
    private AmazonKinesis kinesisClient;
    private AmazonRDS rdsClient;
    private AWSKMS kmsClient;
    private AmazonSQS sqsClient;
    private AmazonSNS snsClient;
    private AmazonDynamoDB dynamoDbClient;
    private AWSLambda lambdaClient;
    private AmazonCloudWatch cloudWatchClient;
    private AWSSecretsManager secretsManagerClient;
    private FileUtil fileUtil;

    public EcsPush(EcsPushContext context) {
        this.logger = context.getLogger();
        this.bambooPropertyHandler = context.getPropertyHandler();
        this.taskProperties = context.getTaskProperties();
        this.pushContext = context;

        this.iamClient = AmazonIdentityManagementClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(context.getSessionCredentials()))
                .withClientConfiguration(context.getAwsClientConfig()).withRegion(context.getRegion()).build();

        this.ecsClient = AmazonECSClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(context.getSessionCredentials()))
                .withClientConfiguration(context.getAwsClientConfig()).withRegion(context.getRegion()).build();

        this.ec2Client = AmazonEC2ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(context.getSessionCredentials()))
                .withClientConfiguration(context.getAwsClientConfig()).withRegion(context.getRegion()).build();

        this.elbClient = com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(context.getSessionCredentials()))
                .withClientConfiguration(context.getAwsClientConfig()).withRegion(context.getRegion()).build();

        this.elbV2Client = com.amazonaws.services.elasticloadbalancingv2.AmazonElasticLoadBalancingClientBuilder
                .standard().withCredentials(new AWSStaticCredentialsProvider(context.getSessionCredentials()))
                .withClientConfiguration(context.getAwsClientConfig()).withRegion(context.getRegion()).build();

        this.s3Client = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(context.getSessionCredentials()))
                .withClientConfiguration(context.getAwsClientConfig()).withRegion(context.getRegion()).build();

        this.kinesisClient = AmazonKinesisClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(context.getSessionCredentials()))
                .withClientConfiguration(context.getAwsClientConfig()).withRegion(context.getRegion()).build();

        this.rdsClient = AmazonRDSClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(context.getSessionCredentials()))
                .withClientConfiguration(context.getAwsClientConfig()).withRegion(context.getRegion()).build();

        this.kmsClient = AWSKMSClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(context.getSessionCredentials()))
                .withClientConfiguration(context.getAwsClientConfig()).withRegion(context.getRegion()).build();

        this.sqsClient = AmazonSQSClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(context.getSessionCredentials()))
                .withClientConfiguration(context.getAwsClientConfig()).withRegion(context.getRegion()).build();

        this.snsClient = AmazonSNSClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(context.getSessionCredentials()))
                .withClientConfiguration(context.getAwsClientConfig()).withRegion(context.getRegion()).build();

        this.dynamoDbClient = AmazonDynamoDBClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(context.getSessionCredentials()))
                .withClientConfiguration(context.getAwsClientConfig()).withRegion(context.getRegion()).build();

        this.lambdaClient = AWSLambdaClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(context.getSessionCredentials()))
                .withClientConfiguration(
                        new ClientConfiguration().withClientExecutionTimeout(300000).withSocketTimeout(300000))
                .withRegion(context.getRegion()).build();

        this.cloudWatchClient = AmazonCloudWatchClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(pushContext.getSessionCredentials()))
                .withClientConfiguration(pushContext.getAwsClientConfig()).withRegion(pushContext.getRegion()).build();

        this.secretsManagerClient = AWSSecretsManagerClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(pushContext.getSessionCredentials()))
                .withClientConfiguration(pushContext.getAwsClientConfig()).withRegion(pushContext.getRegion()).build();

        this.fileUtil = new FileUtil(pushContext.getRootPath(), logger);
    }

    public void push() {
        EcsPushDefinition definition = getEcsPushDefinition();

        ArrayList<TaskDefinitionPlacementConstraint> placementConstraints;
        if (definition.getTaskPlacementConstraints() == null) {
            placementConstraints = new ArrayList<>();
        } else {
            placementConstraints = new ArrayList<>(definition.getTaskPlacementConstraints());
            for (TaskDefinitionPlacementConstraint c : placementConstraints) {
                if (c.getType() == null) {
                    // Marshalling not working...and only possible value
                    c.setType(TaskDefinitionPlacementConstraintType.MemberOf);
                }
            }
        }

        definition.setTaskPlacementConstraints(placementConstraints);

        logger.addLogEntry(definition.toString());
//        logInvocationInCloudWatch(definition);

        EcsClusterIntrospector clusterIntrospector = new EcsClusterIntrospector(ecsClient, ec2Client, rdsClient, logger);
        EcsClusterMetadata clusterMetadata = clusterIntrospector.introspect(definition.getCluster(), pushContext.getRegion());
        clusterMetadata.setPublicSubnets(taskProperties.getPublicExternalSubnets());
        clusterMetadata.setElbSubnets(taskProperties.getPublicInternalSubnets());
        clusterMetadata.setPrivateSubnets(taskProperties.getPrivateSubnets());
        clusterMetadata.setVpcId(taskProperties.getVpcId());
        
        TaskDefinition versionForRollback = getCurrentTaskDef(definition.getAppName(), ecsClient,
        		clusterMetadata.getClusterId());

        LoggingService loggingService = new LoggingService(logger).withSplunkInstanceValues(clusterMetadata.getSplunkUrl(), taskProperties);

        // Set app role
        String customIamPolicyFileName = Optional.ofNullable(definition.getIamPolicy()).orElse("iam-policy.json");
        String customIamPolicy = fileUtil.findFile(customIamPolicyFileName, true);

        String rolePath = taskProperties.getIamDefaultRolePath();

        IAMBroker iamBroker = new IAMBroker(logger);
        Role appRole;
        if (definition.getIamRole() == null || definition.getAppName().equals(definition.getIamRole())) {
            logger.addLogEntry("Brokering role with policy " + customIamPolicyFileName);
            appRole = iamBroker.brokerAppRole(iamClient, definition, customIamPolicy, rolePath, "", bambooPropertyHandler, pushContext.getSessionCredentials());
        } else {
            logger.addLogEntry("Using existing role: " + definition.getIamRole());
            appRole = iamBroker.getRole(iamClient, definition.getIamRole());
        }

        if (definition.getIamOptOut() == null) {
            definition.setTaskRoleArn(appRole.getArn());
        }
        bambooPropertyHandler.addProperty("app.iam", appRole.getArn());

        // Inject environment variables
        EcsDefaultEnvInjection injectMagic = new EcsDefaultEnvInjection();
        injectMagic.injectEnvironment(definition, pushContext.getRegion().getName(), pushContext.getEnvName(),
                clusterMetadata);
        injectMagic.setDefaultContainerName(definition);

        
        //run once to init for KMS' handling
        TaskDefExecRoleHandler exec = new TaskDefExecRoleHandler();
        String taskExecRoleBody = exec.generateTaskDefIam(definition);
        String execRoleArn = null;
        String origAppName=definition.getAppName();
//        if(taskExecRoleBody!=null) 
        {
        	if(definition.getAppName().endsWith("-rdsbroker")) {
        		definition.setAppName(origAppName.replace("-rdsbroker", ""));
        	}
        	Role execRole = iamBroker.brokerAppRole(iamClient, definition, taskExecRoleBody, rolePath, "-taskexec", bambooPropertyHandler, pushContext.getSessionCredentials());
        	execRoleArn = execRole.getArn();
        	logger.addLogEntry("Exec Role arn:"+execRoleArn);
        }
        //reset
        definition.setAppName(origAppName);
        
        brokerServicesPrePush(definition, versionForRollback, injectMagic, clusterMetadata, appRole);
        
        //rerun to fill in secrets
        taskExecRoleBody = exec.generateTaskDefIam(definition);
//        if(taskExecRoleBody!=null) {
        
        {
        	if(definition.getAppName().endsWith("-rdsbroker")) {
        		definition.setAppName(origAppName.replace("-rdsbroker", ""));
        	}
        	Role execRole = iamBroker.brokerAppRole(iamClient, definition, taskExecRoleBody, rolePath, "-taskexec", bambooPropertyHandler, pushContext.getSessionCredentials());
        	execRoleArn = execRole.getArn();
        	logger.addLogEntry("Exec Role arn:"+execRoleArn);
        }
        //reset
        definition.setAppName(origAppName);


        EcsPortHandler portHandler = new EcsPortHandler();
        LoadBalancer bal = null;
        TaskType type = portHandler.getTaskType(definition);
        if (Objects.equals(type, TaskType.WEB)) {
            ElbOrAlbDecider decider = new ElbOrAlbDecider(elbClient, logger);
            boolean useAlb = decider.shouldUseAlb(definition.getAppName(), definition);

            DnsRegistrar dnsRegistrar = new DnsRegistrar(lambdaClient, logger, taskProperties.getDnsBrokerLambda());
            CertHandler certHandler = new CertHandler(logger, taskProperties.getSslCertificates());
            DdosWafHandler ddosWafHandler = new DdosWafHandler(taskProperties, definition, logger, lambdaClient,
                    bambooPropertyHandler, fileUtil);
            if (useAlb) {
                EcsLoadBalancerV2Handler loadBalancerV2Handler = new EcsLoadBalancerV2Handler(elbV2Client, certHandler,
                        dnsRegistrar, logger, taskProperties, ddosWafHandler);
                bal = loadBalancerV2Handler.createLoadBalancer(clusterMetadata, definition);
            } else {
                EcsLoadBalancerHandler loadBalancerHandler = new EcsLoadBalancerHandler(elbClient, certHandler,
                        dnsRegistrar, logger, taskProperties);
                bal = loadBalancerHandler.createLoadBalancer(clusterMetadata, definition);
            }
        }


        RegisterTaskDefinitionResult taskResult = registerTask(definition, definition.getAppName(), ecsClient,
                clusterMetadata.getClusterId(), execRoleArn);

        logger.addLogEntry("Task role: " + definition.getTaskRoleArn());

        loggingService.provideSplunkLog(taskResult);

        if (type.equals(TaskType.WEB) || type.equals(TaskType.DAEMON)) {
            ServicePurger purger = new ServicePurger(ecsClient, logger);
            purger.purgeOtherClusters(definition.getCluster(), definition.getAppName());
            deployService(ecsClient, clusterMetadata, definition, bal, taskResult.getTaskDefinition(),
                    versionForRollback);

            // only post-push for services, not task
            brokerServicesPostPush(definition, clusterMetadata);

            loggingService.provideSplunkLog(taskResult);
            provideConsoleLink(loggingService, taskResult, clusterMetadata.getClusterId());

        } else if (Objects.equals(type, TaskType.TASK)) {
            runTask(clusterMetadata, ecsClient, taskResult.getTaskDefinition(), definition.getContainerDefinitions());
        }

//        logResultInCloudWatch(definition);
    }

    private void provideConsoleLink(LoggingService loggingService, RegisterTaskDefinitionResult task, String cluster) {
        String family = task.getTaskDefinition().getFamily();
        String region = pushContext.getRegion().getName();

        if (taskProperties.getEcsConsoleLinkPattern() != null) {
            String consoleLink = String.format(taskProperties.getEcsConsoleLinkPattern(),
                    bambooPropertyHandler.lookupVariable("account.id"), region, cluster, family);
            loggingService.logSection("ECS Console", consoleLink);
        }
    }

    private EcsPushDefinition getEcsPushDefinition() {
        EcsDefinitionParser parser = new EcsDefinitionParser(bambooPropertyHandler);
        String classpathTemplate = bambooPropertyHandler.lookupVariable("classpathTemplate");

        String template;
        EcsPushDefinition definition;
        boolean isJson;
        if (classpathTemplate != null) {
            logger.addLogEntry("Using classpathTemplate " + classpathTemplate);
            InputStream streamToParse = this.getClass().getResourceAsStream(classpathTemplate);
            if (streamToParse == null) {
                throw new AwsExecException("Resource " + classpathTemplate + " not found on the classpath");
            }
            try {
                template = IOUtils.toString(streamToParse);
            } catch (IOException e) {
                throw new AwsExecException(e);
            }
            isJson = classpathTemplate.endsWith(".json");
        } else if (fileUtil.fileExists("template.json")) {
            logger.addLogEntry("Using template.json");
            template = fileUtil.findFile("template.json", false);
            isJson = true;
        } else if (fileUtil.fileExists("template.yml")) {
            logger.addLogEntry("Using template.yml");
            template = fileUtil.findFile("template.yml", false);
            isJson = false;
        } else {
            throw new AwsExecException("No template provided!");
        }
        definition = parser.parse(template, isJson);

        return definition;
    }

    private void runTask(EcsClusterMetadata clusterMetadata, AmazonECS ecsClient, TaskDefinition taskDefinition,
                         List<ContainerDefinition> containerDefinitions) {
        RunTaskRequest runTaskRequest = new RunTaskRequest().withCluster(clusterMetadata.getClusterId())
                .withTaskDefinition(taskDefinition.getTaskDefinitionArn());
        logger.addLogEntry("Running task...");
        RunTaskResult runTaskResult = ecsClient.runTask(runTaskRequest);
        for (Failure f : runTaskResult.getFailures()) {
            logger.addLogEntry("Error running " + f.getArn());
            logger.addLogEntry(f.getReason());
        }

        for (Task task : runTaskResult.getTasks()) {
            waitForTaskCompletion(ecsClient, task.getTaskArn(), clusterMetadata.getClusterId(), containerDefinitions);
        }
    }

    private TaskDefinition getCurrentTaskDef(String appName, AmazonECS ecsClient, String clusterId) {
        TaskDefinition result = null;
        DescribeServicesResult serviceResult = ecsClient
                .describeServices(new DescribeServicesRequest().withCluster(clusterId).withServices(appName));
        if (!serviceResult.getServices().isEmpty()) {
            String taskDef = serviceResult.getServices().get(0).getTaskDefinition();
            result = ecsClient.describeTaskDefinition(new DescribeTaskDefinitionRequest().withTaskDefinition(taskDef))
                    .getTaskDefinition();
        }

        return result;
    }

    private RegisterTaskDefinitionResult registerTask(EcsPushDefinition definition, String appName, AmazonECS ecsClient,
                                                      String clusterId, String executionRoleArn) {
        RegisterTaskDefinitionResult taskResult = ecsClient.registerTaskDefinition(new RegisterTaskDefinitionRequest()
                .withFamily(appName).withContainerDefinitions(definition.getContainerDefinitions())
                .withVolumes(definition.getVolumes()).withPlacementConstraints(definition.getTaskPlacementConstraints())
                .withNetworkMode(definition.getNetworkMode()).withTaskRoleArn(definition.getTaskRoleArn())
                .withMemory(definition.getTaskMemory())
                .withExecutionRoleArn(executionRoleArn)
        		);
        logger.addLogEntry("Registered new task: " + taskResult.getTaskDefinition().getTaskDefinitionArn());

        DescribeServicesResult serviceResult = ecsClient
                .describeServices(new DescribeServicesRequest().withCluster(clusterId).withServices(appName));
        if (!serviceResult.getServices().isEmpty()) {
            String taskDef = serviceResult.getServices().get(0).getTaskDefinition();
            String currentTaskArn = ecsClient
                    .describeTaskDefinition(new DescribeTaskDefinitionRequest().withTaskDefinition(taskDef))
                    .getTaskDefinition().getTaskDefinitionArn();

            ListTaskDefinitionsResult taskListResult = ecsClient.listTaskDefinitions(
                    new ListTaskDefinitionsRequest().withFamilyPrefix(appName).withStatus("ACTIVE"));
            for (String arn : taskListResult.getTaskDefinitionArns()) {
                if (!Objects.equals(taskResult.getTaskDefinition().getTaskDefinitionArn(), arn)
                        && !Objects.equals(currentTaskArn, arn)) {
                    logger.addLogEntry("Deregistering prior task: " + arn);
                    ecsClient.deregisterTaskDefinition(new DeregisterTaskDefinitionRequest().withTaskDefinition(arn));
                }
            }
        }

        return taskResult;
    }

    private TaskDefinition registerPriorTaskDefinitionWithUpdatedContainerEnvConfig(EcsPushDefinition ecsPushDefinition,
                                                                                    TaskDefinition priorTaskDef, AmazonECS ecsClient) {
        RdsInstance currentRdsInstanceDefinition = ecsPushDefinition.getDatabase();
//        if (currentRdsInstanceDefinition != null) {
//            for (ContainerDefinition containerDef : priorTaskDef.getContainerDefinitions()) {
//                containerDef.getEnvironment().replaceAll(keyValuePair -> {
//                    if (keyValuePair.getName()
//                            .equals(currentRdsInstanceDefinition.getInjectNames().getEncryptedPassword()))
//                        keyValuePair.setValue(currentRdsInstanceDefinition.getEncryptedPassword());
//                    else if (keyValuePair.getName()
//                            .equals(currentRdsInstanceDefinition.getInjectNames().getAppEncryptedPassword()))
//                        keyValuePair.setValue(currentRdsInstanceDefinition.getAppEncryptedPassword());
//                    else if (keyValuePair.getName()
//                            .equals(currentRdsInstanceDefinition.getInjectNames().getAdminEncryptedPassword()))
//                        keyValuePair.setValue(currentRdsInstanceDefinition.getAdminEncryptedPassword());
//                    return keyValuePair;
//                });
//            }
//
//            RegisterTaskDefinitionResult taskResult = ecsClient
//                    .registerTaskDefinition(new RegisterTaskDefinitionRequest().withFamily(priorTaskDef.getFamily())
//                            .withContainerDefinitions(priorTaskDef.getContainerDefinitions())
//                            .withVolumes(priorTaskDef.getVolumes())
//                            .withPlacementConstraints(priorTaskDef.getPlacementConstraints())
//                            .withNetworkMode(priorTaskDef.getNetworkMode())
//                            .withTaskRoleArn(priorTaskDef.getTaskRoleArn()).withMemory(priorTaskDef.getMemory()));
//
//            logger.addLogEntry("Registered new task definition revision of prior working version: "
//                    + taskResult.getTaskDefinition().getTaskDefinitionArn());
//
//            return taskResult.getTaskDefinition();
//        }
        return priorTaskDef;

    }

    private String deployService(AmazonECS ecsClient, EcsClusterMetadata clusterMetadata, EcsPushDefinition definition,
                                 LoadBalancer balancer, TaskDefinition taskDefinition, TaskDefinition priorDef) {
        String appName = definition.getAppName();
        DescribeServicesResult serviceSearch = ecsClient.describeServices(
                new DescribeServicesRequest().withCluster(clusterMetadata.getClusterId()).withServices(appName));

        boolean serviceExists = false;
        String serviceArn = null;
        for (Service service : serviceSearch.getServices()) {
            if (!Objects.equals(service.getStatus(), "INACTIVE")) {
                logger.addLogEntry("Service found: " + service.getServiceName() + " : " + service.getStatus());
                serviceExists = true;
                serviceArn = service.getServiceArn();
            }
        }

        String serviceRole = null;
        if (balancer != null) {
            serviceRole = clusterMetadata.getClusterEcsRole();

            String rolePath = taskProperties.getIamDefaultRolePath();
            if (StringUtils.isNotEmpty(rolePath)) {
                serviceRole = rolePath + serviceRole;
            }

            logger.addLogEntry("Assuming service role: " + serviceRole);
        }

        NetworkConfiguration networkConfiguration = null;
        if ("awsvpc".equals(taskDefinition.getNetworkMode())) {
            List<String> groups = clusterMetadata.getElbSecurityGroups();
            groups.add(clusterMetadata.getAppSecurityGroup());

            networkConfiguration = new NetworkConfiguration()
                    .withAwsvpcConfiguration(new AwsVpcConfiguration().withAssignPublicIp(AssignPublicIp.DISABLED)
                            .withSecurityGroups(groups).withSubnets(clusterMetadata.getPrivateSubnets()));

        }

        Integer gracePeriod = definition.getService().getHealthCheckGracePeriodSeconds();
        if (gracePeriod == null || (gracePeriod != null && gracePeriod.equals(0))) {
            gracePeriod = DEFAULT_GRACE_PERIOD;
        }

        if (!serviceExists) {
            logger.addLogEntry("NEW SERVICE");
            CreateServiceRequest cr = new CreateServiceRequest().withCluster(clusterMetadata.getClusterId())
                    .withDesiredCount(definition.getService().getInstanceCount()).withRole(serviceRole)
                    .withTaskDefinition(taskDefinition.getTaskDefinitionArn()).withServiceName(appName)
                    .withDeploymentConfiguration(definition.getService().getDeploymentConfiguration())
                    .withClientToken(UUID.randomUUID().toString())
                    .withPlacementConstraints(definition.getService().getPlacementConstraints())
                    .withPlacementStrategy(definition.getService().getPlacementStrategies());

            if (balancer != null) {
                logger.addLogEntry("Adding load balancer to service");
                cr.withLoadBalancers(balancer)
                        .withHealthCheckGracePeriodSeconds(gracePeriod);
            }
            if (networkConfiguration != null) {
                cr.withNetworkConfiguration(networkConfiguration);
            }

            logger.addLogEntry("request is: " + cr.toString());
            CreateServiceResult csr = ecsClient.createService(cr);
            serviceArn = csr.getService().getServiceArn();

        } else {
            logger.addLogEntry("UPDATE SERVICE");
            UpdateServiceRequest updateRequest = new UpdateServiceRequest().withCluster(clusterMetadata.getClusterId())
                    .withDesiredCount(definition.getService().getInstanceCount())
                    .withDeploymentConfiguration(definition.getService().getDeploymentConfiguration())
                    .withTaskDefinition(taskDefinition.getTaskDefinitionArn()).withService(appName);

            if (balancer != null) {
                updateRequest
                        .withHealthCheckGracePeriodSeconds(gracePeriod);
            }
            if (networkConfiguration != null) {
                updateRequest.withNetworkConfiguration(networkConfiguration);
            }
            ecsClient.updateService(updateRequest);
        }

        waitForRequestInitialization(appName, ecsClient, clusterMetadata);
        boolean deploySuccessful = waitForDeployment(appName, ecsClient, clusterMetadata);

        if (!deploySuccessful) {
            if (priorDef != null) {
                logger.addLogEntry("Deployment never stabilized - rolling back!");
                TaskDefinition rollBackTaskDefinition = registerPriorTaskDefinitionWithUpdatedContainerEnvConfig(
                        definition, priorDef, ecsClient);
                logger.addLogEntry("Rolling back to " + rollBackTaskDefinition.getTaskRoleArn());

                UpdateServiceRequest updateRequest = new UpdateServiceRequest()
                        .withCluster(clusterMetadata.getClusterId())
                        .withDesiredCount(definition.getService().getInstanceCount())
                        .withDeploymentConfiguration(definition.getService().getDeploymentConfiguration())
                        .withTaskDefinition(rollBackTaskDefinition.getTaskDefinitionArn())// "aws-kms-encrypt-dev-us-east-1-task-LEP2I3IDX73G:1")
                        .withService(appName);

                if (balancer != null) {
                    updateRequest.withHealthCheckGracePeriodSeconds(gracePeriod);
                }

                ecsClient.updateService(updateRequest);

                waitForRequestInitialization(appName, ecsClient, clusterMetadata);
                boolean rollbackSuccessful = waitForDeployment(appName, ecsClient, clusterMetadata);

                if (!rollbackSuccessful) {
                    setUnsuccessfulServiceToZero(appName, ecsClient, clusterMetadata);
                    throw new AwsExecException(
                            "Rollback never stabilized. Shutting down to stop flapping, we tried...");
                } else {
                    throw new AwsExecException(
                            "Application rolled back successfully. Marking Bamboo as failed for notice.");
                }
            } else {
                setUnsuccessfulServiceToZero(appName, ecsClient, clusterMetadata);
                throw new AwsExecException(
                        "Deployment never stabilized, no prior version. Shutting down to stop flapping, all we can do.");
            }

        }

        return serviceArn;
    }

    private boolean waitForDeployment(String appName, AmazonECS ecsClient, EcsClusterMetadata clusterMetadata) {
        String lastEventId = "";
        int timeoutCount = this.pushContext.getTimeout() * 6;

        while (timeoutCount > 0) {
            DescribeServicesResult servicesResult = ecsClient.describeServices(
                    new DescribeServicesRequest().withCluster(clusterMetadata.getClusterId()).withServices(appName));
            Service service = servicesResult.getServices().get(0);
            ServiceEvent lastEvent = service.getEvents().get(0);

            logger.addLogEntry("Status:" + service.getStatus() + "  Desired:" + service.getDesiredCount() + "  Pending:"
                    + service.getPendingCount() + "  Running:" + service.getRunningCount());

            if (!Objects.equals(lastEvent.getId(), lastEventId)) {
                logger.addLogEntry(lastEvent.getMessage());
                lastEventId = lastEvent.getId();
            }

            ServiceEvent latest = service.getEvents().get(0);
            if (Objects.equals(service.getDesiredCount(), service.getRunningCount())
                    && latest.getMessage().contains("has reached a steady state")) {
                logger.addLogEntry("App has stabilized");
                return true;
            }
            try {
                Thread.sleep(POLLING_INTERVAL_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new AwsExecException(INTERRUPTED_WHILE_POLLING);
            }
            timeoutCount--;
        }
        return false;
    }

    private void setUnsuccessfulServiceToZero(String appName, AmazonECS ecsClient, EcsClusterMetadata clusterMetadata) {
        logger.addLogEntry("Deployment was not successful - setting instance count to 0");
        ecsClient.updateService(new UpdateServiceRequest().withCluster(clusterMetadata.getClusterId())
                .withDesiredCount(0).withService(appName));
    }

    private void waitForRequestInitialization(String appName, AmazonECS ecsClient, EcsClusterMetadata clusterMetadata) {
        int timeoutCount = this.pushContext.getTimeout() * 6;

        while (timeoutCount > 0) {
            try {
                Thread.sleep(POLLING_INTERVAL_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new AwsExecException(INTERRUPTED_WHILE_POLLING);
            }
            DescribeServicesResult servicesResult = ecsClient.describeServices(
                    new DescribeServicesRequest().withCluster(clusterMetadata.getClusterId()).withServices(appName));
            Service service = servicesResult.getServices().get(0);
            if (service != null && !service.getEvents().isEmpty()) {
                ServiceEvent lastEvent = service.getEvents().get(0);
                if (lastEvent != null) {
                    Date lastEventDate = lastEvent.getCreatedAt();
                    if (new Date().getTime() - lastEventDate.getTime() > 60000) {
                        logger.addLogEntry("Waiting for start...");
                    } else {
                        return;
                    }
                }
            }
            timeoutCount--;
        }
        setUnsuccessfulServiceToZero(appName, ecsClient, clusterMetadata);
        throw new AwsExecException("AWS never initiated the deployment");
    }

    private void waitForTaskCompletion(AmazonECS client, String taskName, String clusterName,
                                       List<ContainerDefinition> definitions) {

        logger.addLogEntry("Waiting for task completion: " + taskName);

        Set<String> essentialContainers = new HashSet<>();
        for (ContainerDefinition container : definitions) {
            // autoboxing...default is essential=true if null
            if (container.isEssential() == null || container.isEssential()) {
                essentialContainers.add(container.getName());
            }
        }

        int timeoutCount = this.pushContext.getTimeout() * 6;

        while (timeoutCount > 0) {
            DescribeTasksRequest req = new DescribeTasksRequest().withTasks(taskName).withCluster(clusterName);
            List<Task> tasks = client.describeTasks(req).getTasks();
            if (tasks.isEmpty()) {
                logger.addLogEntry("Tasks empty...waiting");
            }
            for (Task task : tasks) {
                logger.addLogEntry("Task Id: " + task.getTaskArn() + "  Status:" + task.getLastStatus() + "  Desired:"
                        + task.getDesiredStatus());
                if (Objects.equals(task.getLastStatus(), "STOPPED")) {
                    logTaskExitCodes(essentialContainers, task);
                    logger.addLogEntry("Task stopped: " + taskName);
                    return;
                }
            }

            // Not done yet so sleep for 10 seconds.
            try {
                Thread.sleep(POLLING_INTERVAL_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.addLogEntry(INTERRUPTED_WHILE_POLLING);
                logger.addLogEntry("Stopping task " + taskName);
                client.stopTask(new StopTaskRequest().withCluster(clusterName).withTask(taskName)
                        .withReason("Manual stop of deploy plan"));
                throw new AwsExecException(INTERRUPTED_WHILE_POLLING);
            }
            timeoutCount--;
        }
        logger.addLogEntry("Stopping task " + taskName);
        client.stopTask(new StopTaskRequest().withCluster(clusterName).withTask(taskName).withReason("Timed out!"));
        throw new AwsExecException("Task never stabilized");
    }

    private void logTaskExitCodes(Set<String> essentialContainers, Task task) {
        logger.addLogEntry("Stopped reason: " + task.getStoppedReason());
        for (Container c : task.getContainers()) {
            String code = c.getExitCode() != null ? c.getExitCode() + "" : "N/A";
            String essential = essentialContainers.contains(c.getName()) ? "true" : "false";
            logger.addLogEntry("Container: " + c.getName() + "  Code: " + code + "  Essential: " + essential);
            if (essentialContainers.contains(c.getName()) && (c.getExitCode() == null || c.getExitCode() != 0)) {
                logger.addLogEntry(c.getName() + " errored with " + c.getExitCode());
                throw new AwsExecException(c.getName() + " errored with " + c.getExitCode());
            }
        }
    }

    private void brokerServicesPrePush(EcsPushDefinition definition, TaskDefinition versionForRollback, EcsDefaultEnvInjection injectMagic,
                                       EcsClusterMetadata clusterMetadata, Role appRole) {
        String applicationKeyId = brokerKms(definition, clusterMetadata);
        brokerSecretsManager(definition, clusterMetadata, applicationKeyId);
        brokerSqs(definition);
        brokerSns(definition);
        brokerS3(definition, clusterMetadata, applicationKeyId);
        brokerKinesisStream(definition);
        brokerRds(definition, injectMagic, clusterMetadata, applicationKeyId, appRole);
        brokerDynamoDB(definition);
        brokerAuth0(definition, injectMagic, clusterMetadata, applicationKeyId);
    }

    private void brokerAuth0(EcsPushDefinition definition, EcsDefaultEnvInjection injectMagic,
                             EcsClusterMetadata clusterMetadata, String applicationKeyId) {
        if (definition.getAuth0() != null) {

            Auth0BrokerConfiguration auth0BrokerConfiguration = new Auth0BrokerConfiguration()
                    .withBrokerProperties(taskProperties.getAuth0());
            Auth0CreateContext auth0CreateContext = new Auth0CreateContext().fromECSPushContext(pushContext,
                    lambdaClient, auth0BrokerConfiguration);
            Auth0Broker auth0Broker = new Auth0Broker(auth0CreateContext);
            Auth0Configuration auth0Configuration = auth0Broker.brokerAuth0ApplicationDeploymentFromEcsPush(definition,
                    kmsClient, applicationKeyId);

            if (auth0Configuration != null) {
                logger.addLogEntry("Injecting Auth0 Configuration values as environment variables");
                injectMagic.injectAuth0(definition, auth0Configuration);
            }
        }
    }

    private void brokerSecretsManager(EcsPushDefinition definition, EcsClusterMetadata clusterMetadata,
                                      String kmsKeyId) {

    	List<HermanTag> tags = new ArrayList<>();
    	tags.add(new HermanTag(taskProperties.getSbuTagKey(), clusterMetadata.getNewrelicSbuTag()));
    	tags.add(new HermanTag(taskProperties.getOrgTagKey(), clusterMetadata.getNewrelicOrgTag()));
    	tags.add(new HermanTag(taskProperties.getAppTagKey(), definition.getAppName()));
    	tags.add(new HermanTag(taskProperties.getClusterTagKey(), clusterMetadata.getClusterId()));

    	Map<String, String> brokered = new HashMap();
    	for(ContainerDefinition def : definition.getContainerDefinitions()) {
    		for(Secret sec : def.getSecrets()) {
    			if(sec.getValueFrom().startsWith("secretsbroker:")) {
    				String path = sec.getValueFrom().replace("secretsbroker:", "");
    				if(!brokered.containsKey(path)) {
    					SecretsManagerBroker broker = new SecretsManagerBroker(logger, secretsManagerClient, kmsKeyId, TagUtil.hermanToSecretsManagerTags(tags));
                		String arn = broker.brokerSecretsManagerShell(path, definition.getAppName());
                		sec.setValueFrom(arn);
                		brokered.put(path, arn);
                	} else {
                		sec.setValueFrom(brokered.get(path));
                	}
    			}
    		}
    	}
//    	for(ContainerDefinition def : definition.getContainerDefinitions()) {
//    		for(Secret sec : def.getSecrets()) {
//    			if(sec.getValueFrom().startsWith("${rdsbroker:")) {
//    				String path = StringUtils.substringBetween(sec.getValueFrom(), "${secretsbroker:", "}");
//    				if(!brokered.containsKey(path)) {
//    					SecretsManagerBroker broker = new SecretsManagerBroker(logger);
//                		String arn = broker.brokerSecretsManagerShell(secretsManagerClient, path, kmsKeyId, definition.getAppName(), TagUtil.hermanToSecretsManagerTags(tags));
//                		sec.setValueFrom(arn);
//                		brokered.put(path, arn);
//                	} else {
//                		sec.setValueFrom(brokered.get(path));
//                	}
//    			}
//    		}
//    	}
    }

    private String brokerKms(EcsPushDefinition definition, EcsClusterMetadata clusterMetadata) {
        KmsBroker broker = new KmsBroker(logger, bambooPropertyHandler, fileUtil, taskProperties,
                this.pushContext.getSessionCredentials(), this.pushContext.getCustomConfigurationBucket(),
                this.pushContext.getRegion());

        List<HermanTag> tags = new ArrayList<>();
        tags.add(new HermanTag(taskProperties.getAppTagKey(), definition.getAppName()));
        tags.add(new HermanTag(taskProperties.getClusterTagKey(), clusterMetadata.getClusterId()));

        tags = TagUtil.mergeTags(tags, definition.getTags());
        String applicationKeyId = "";
        if (broker.isActive(definition)) {
            applicationKeyId = broker.brokerKey(kmsClient, definition, TagUtil.hermanToKmsTags(tags));
        } else {
            broker.deleteKey(kmsClient, definition);
        }
        return applicationKeyId;
    }

    private void brokerRds(EcsPushDefinition definition, EcsDefaultEnvInjection injectMagic,
                           EcsClusterMetadata clusterMetadata, String applicationKeyId, Role appRole) {
 
    	List<HermanTag> tags = new ArrayList<>();
    	tags.add(new HermanTag(taskProperties.getSbuTagKey(), clusterMetadata.getNewrelicSbuTag()));
    	tags.add(new HermanTag(taskProperties.getOrgTagKey(), clusterMetadata.getNewrelicOrgTag()));
    	tags.add(new HermanTag(taskProperties.getAppTagKey(), definition.getAppName()));
    	tags.add(new HermanTag(taskProperties.getClusterTagKey(), clusterMetadata.getClusterId()));
    	
    	SecretsManagerBroker broker = new SecretsManagerBroker(logger, secretsManagerClient, applicationKeyId, TagUtil.hermanToSecretsManagerTags(tags));
    	
        RdsBroker rdsBroker = new RdsBroker(pushContext, rdsClient, broker, definition, clusterMetadata,
                new EcsPushFactory(), fileUtil, appRole.getRoleName());
        
        if (definition.getDatabase() != null) {
            RdsInstance instance = rdsBroker.brokerDb();

            if (instance != null) {
                logger.addLogEntry("Injecting RDS instance values as environment variables");
                injectMagic.injectRds(definition, instance, broker);
            }
        }
    }

    private void brokerSqs(EcsPushDefinition definition) {
        SqsBroker sqsBroker = new SqsBroker(logger, bambooPropertyHandler);
        if (definition.getQueues() != null) {
            for (SqsQueue queue : definition.getQueues()) {
                if (queue.getPolicyName() != null) {
                    String policy = fileUtil.findFile(queue.getPolicyName(), false);
                    sqsBroker.brokerQueue(sqsClient, queue, policy, definition.getTags());
                } else {
                    sqsBroker.brokerQueue(sqsClient, queue, null, definition.getTags());
                }
            }
        }
    }

    private void brokerSns(EcsPushDefinition definition) {
        SnsBroker snsBroker = new SnsBroker(logger, bambooPropertyHandler);
        if (definition.getTopics() != null) {
            for (SnsTopic topic : definition.getTopics()) {
                if (topic.getPolicyName() != null) {
                    String policy = fileUtil.findFile(topic.getPolicyName(), false);
                    snsBroker.brokerTopic(snsClient, topic, policy);
                } else {
                    snsBroker.brokerTopic(snsClient, topic, null);
                }
            }
        }

    }

    private void brokerS3(EcsPushDefinition definition, EcsClusterMetadata clusterMetadata, String kmsKeyId) {
        S3Broker s3Broker = new S3Broker(new S3CreateContext().fromECSPushContext(pushContext));
        if (definition.getBuckets() != null) {
            for (S3Bucket bucket : definition.getBuckets()) {
                if (bucket.getPolicyName() != null) {
                    String policy = fileUtil.findFile(bucket.getPolicyName(), false);
                    s3Broker.brokerBucketFromEcsPush(s3Client, kmsClient, bucket, policy, kmsKeyId, clusterMetadata,
                            definition);
                } else {
                    s3Broker.brokerBucketFromEcsPush(s3Client, kmsClient, bucket, null, kmsKeyId, clusterMetadata,
                            definition);
                }
            }
        }
    }

    private void brokerKinesisStream(EcsPushDefinition definition) {
        KinesisBroker kinesisBroker = new KinesisBroker(logger, kinesisClient, definition, taskProperties);

        // delete any streams tied to this app that are no longer specified in the
        // PushDefinition
        kinesisBroker.checkStreamsToBeDeleted();

        if (definition.getStreams() != null) {
            for (KinesisStream stream : definition.getStreams()) {
                kinesisBroker.brokerStream(stream);
            }
        }
    }

    private void brokerDynamoDB(EcsPushDefinition definition) {
        DynamoDBBroker dynamoDBBroker = new DynamoDBBroker(logger, definition);

        if (definition.getDynamoDBTables() != null) {
            dynamoDBBroker.createDynamoDBTables(dynamoDbClient);
        }
    }

    private void brokerServicesPostPush(EcsPushDefinition definition, EcsClusterMetadata meta) {
//        if (taskProperties.getNewRelic() != null) {
//            NewRelicBrokerConfiguration newRelicBrokerConfiguration = new NewRelicBrokerConfiguration()
//                    .withBrokerProperties(taskProperties.getNewRelic());
//            NewRelicBroker newRelicBroker = new NewRelicBroker(bambooPropertyHandler, logger, fileUtil,
//                    newRelicBrokerConfiguration, lambdaClient);
//            newRelicBroker.brokerNewRelicApplicationDeployment(definition.getNewRelic(), definition.getAppName(),
//                    definition.getNewRelicApplicationName(), meta.getNewrelicLicenseKey());
//        }

        if (definition.getBetaAutoscale() != null) {
            AutoscalingBroker asb = new AutoscalingBroker(pushContext);
            asb.broker(meta, definition);
        }
    }

//    private void logInvocationInCloudWatch(EcsPushDefinition definition) {
//        try {
//            MetricDatum d = new MetricDatum().withMetricName("Invocation")
//                    .withDimensions(new Dimension().withName("application").withValue(definition.getAppName()),
//                            new Dimension().withName("cluster").withValue(definition.getCluster()),
//                            new Dimension().withName("env")
//                                    .withValue(bambooPropertyHandler.lookupVariable("bamboo.deploy.environment")),
//                            new Dimension().withName("deployProject")
//                                    .withValue(bambooPropertyHandler.lookupVariable("bamboo.deploy.project")),
//                            new Dimension().withName("type")
//                                    .withValue(System.getenv("bamboo_deploy_environment") != null ? "current-jar"
//                                            : "current-plugin"),
//                            new Dimension().withName("engine").withValue(taskProperties.getEngine()))
//                    .withUnit(StandardUnit.Count).withValue(1.0).withTimestamp(new Date());
//
//            cloudWatchClient.putMetricData(new PutMetricDataRequest().withNamespace("Herman/Deploy").withMetricData(d));
//        } catch (Exception e) { // NOSONAR
//            pushContext.getLogger().addLogEntry("Error logging invocation to CW: " + e.getMessage());// nothing to do
//        }
//    }

//    private void logResultInCloudWatch(EcsPushDefinition definition) {
//        try {
//            MetricDatum d = new MetricDatum().withMetricName("Result")
//                    .withDimensions(new Dimension().withName("application").withValue(definition.getAppName()),
//                            new Dimension().withName("cluster").withValue(definition.getCluster()),
//                            new Dimension().withName("env")
//                                    .withValue(bambooPropertyHandler.lookupVariable("bamboo.deploy.environment")),
//                            new Dimension().withName("deployProject")
//                                    .withValue(bambooPropertyHandler.lookupVariable("bamboo.deploy.project")),
//                            new Dimension().withName("type")
//                                    .withValue(System.getenv("bamboo_deploy_environment") != null ? "current-jar"
//                                            : "current-plugin"),
//                            new Dimension().withName("engine").withValue(taskProperties.getEngine()),
//                            new Dimension().withName("propKeysRequired")
//                                    .withValue(String.join(",",
//                                            ((TaskContextPropertyHandler) bambooPropertyHandler)
//                                                    .getPropertyKeysUsed())))
//                    .withUnit(StandardUnit.Count).withValue(1.0).withTimestamp(new Date());
//
//            cloudWatchClient.putMetricData(new PutMetricDataRequest().withNamespace("Herman/Deploy").withMetricData(d));
//        } catch (Exception e) { // NOSONAR
//            pushContext.getLogger().addLogEntry("Error logging result to CW: " + e.getMessage());// nothing to do
//        }
//    }
}
