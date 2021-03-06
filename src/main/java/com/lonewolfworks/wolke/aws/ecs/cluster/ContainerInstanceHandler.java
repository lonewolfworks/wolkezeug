/*
 * Copyright (C) 2018, Liberty Mutual Group
 *
 * Created on 8/2/18
 */
package com.lonewolfworks.wolke.aws.ecs.cluster;

import com.amazonaws.services.autoscaling.model.Instance;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.DescribeTagsRequest;
import com.amazonaws.services.ec2.model.DescribeTagsResult;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.TagDescription;
import com.amazonaws.services.ecs.AmazonECS;
import com.amazonaws.services.ecs.model.Attribute;
import com.amazonaws.services.ecs.model.ContainerInstance;
import com.amazonaws.services.ecs.model.DescribeContainerInstancesRequest;
import com.amazonaws.services.ecs.model.DescribeContainerInstancesResult;
import com.amazonaws.services.ecs.model.ListContainerInstancesRequest;
import com.amazonaws.services.ecs.model.ListContainerInstancesResult;
import com.amazonaws.services.ecs.model.PutAttributesRequest;
import com.amazonaws.services.ecs.model.TargetType;
import com.lonewolfworks.wolke.aws.asg.AutoscalingGroupHandler;
import com.lonewolfworks.wolke.logging.HermanLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ContainerInstanceHandler {
    private AmazonECS ecsClient;
    private AmazonEC2 ec2Client;
    private HermanLogger logger;

    public ContainerInstanceHandler(AmazonECS ecsClient, AmazonEC2 ec2Client, HermanLogger logger) {
        this.ecsClient = ecsClient;
        this.ec2Client = ec2Client;
        this.logger = logger;
    }

    public List<ContainerInstance> getContainerInstances(String cluster) {
        ListContainerInstancesRequest listRequest = new ListContainerInstancesRequest()
            .withCluster(cluster);
        ListContainerInstancesResult listResult = this.ecsClient.listContainerInstances(listRequest);
        if (listResult.getContainerInstanceArns().isEmpty()) {
            return null;
        }
        DescribeContainerInstancesRequest descrRequest = new DescribeContainerInstancesRequest()
            .withCluster(cluster)
            .withContainerInstances(listResult.getContainerInstanceArns());
        DescribeContainerInstancesResult descrResult = this.ecsClient.describeContainerInstances(descrRequest);
        return descrResult.getContainerInstances();
    }

    public void setAttributeOnCluster(String cluster, String key, String value) {
        this.logger.addLogEntry("Setting " + key + ": " + value + " on cluster " + cluster);
        List<ContainerInstance> containerInstances = getContainerInstances(cluster);
        if (containerInstances == null) {
            this.logger.addLogEntry("... No container instances found in cluster, skipping attribute set");
        }
        else {
            for (ContainerInstance containerInstance : containerInstances) {
                this.setAttribute(cluster, containerInstance.getContainerInstanceArn(), key, value);
            }
        }
    }

    public Set<String> getUnregisteredInstanceIds(String cluster, String asgName, AutoscalingGroupHandler asgHandler) {
        List<String> newAsgInstanceIds = asgHandler.getAsgInstancesInService(asgName).stream()
            .map(Instance::getInstanceId)
            .collect(Collectors.toList());
        List<ContainerInstance> clusterInstances = getContainerInstances(cluster);
        List<String> newContainerInstanceIds = clusterInstances.stream()
            .filter(instance -> (!"pre-drain".equals(getInstanceAttributeValue(instance, "state"))))
            .map(ContainerInstance::getEc2InstanceId)
            .collect(Collectors.toList());

        return newAsgInstanceIds.stream()
            .filter(newInstance -> !newContainerInstanceIds.contains(newInstance))
            .collect(Collectors.toSet());
    }

    public List<TagDescription> getContainerInstanceTags(ContainerInstance containerInstance) {
        DescribeTagsResult tagsResult = this.ec2Client.describeTags(new DescribeTagsRequest()
            .withFilters(
                new Filter("resource-type").withValues("instance"),
                new Filter("resource-id").withValues(containerInstance.getEc2InstanceId())
            )
        );
        String nextToken = tagsResult.getNextToken();
        ArrayList<TagDescription> tags = new ArrayList<>(tagsResult.getTags());
        while (nextToken != null) {
            DescribeTagsResult pagedResult = this.ec2Client.describeTags(new DescribeTagsRequest()
                .withFilters(
                    new Filter("resource-type").withValues("instance"),
                    new Filter("resource-id").withValues(containerInstance.getEc2InstanceId())
                    )
                .withNextToken(nextToken)
            );
            tags.addAll(pagedResult.getTags());
            nextToken = pagedResult.getNextToken();
        }

        return tags;
    }


    private String getInstanceAttributeValue(ContainerInstance instance, String key) {
        for (Attribute attribute : instance.getAttributes()) {
            if (key.equals(attribute.getName())) {
                return attribute.getValue();
            }
        }
        return null;
    }

    private void setAttribute(String cluster, String containerInstanceId, String key, String value) {
        Attribute attribute = new Attribute()
            .withName(key)
            .withValue(value)
            .withTargetType(TargetType.ContainerInstance)
            .withTargetId(containerInstanceId);
        PutAttributesRequest putAttributesRequest = new PutAttributesRequest()
            .withAttributes(attribute)
            .withCluster(cluster);
        this.ecsClient.putAttributes(putAttributesRequest);
    }
}
