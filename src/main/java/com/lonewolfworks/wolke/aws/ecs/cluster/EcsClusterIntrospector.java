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
package com.lonewolfworks.wolke.aws.ecs.cluster;

import java.util.Arrays;
import java.util.Iterator;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudformation.model.Parameter;
import com.amazonaws.services.cloudformation.model.StackResource;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2ClientBuilder;
import com.amazonaws.services.ec2.model.DescribeInstancesRequest;
import com.amazonaws.services.ec2.model.DescribeSecurityGroupsRequest;
import com.amazonaws.services.ec2.model.Filter;
import com.amazonaws.services.ec2.model.GroupIdentifier;
import com.amazonaws.services.ec2.model.IpPermission;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.SecurityGroup;
import com.amazonaws.services.ecs.AmazonECS;
import com.amazonaws.services.ecs.AmazonECSClientBuilder;
import com.amazonaws.services.ecs.model.Cluster;
import com.amazonaws.services.ecs.model.ContainerInstance;
import com.amazonaws.services.ecs.model.DescribeClustersRequest;
import com.amazonaws.services.ecs.model.DescribeContainerInstancesRequest;
import com.amazonaws.services.ecs.model.ListContainerInstancesRequest;
import com.amazonaws.services.rds.AmazonRDS;
import com.amazonaws.services.rds.AmazonRDSClientBuilder;
import com.amazonaws.services.rds.model.DBSubnetGroup;
import com.lonewolfworks.wolke.logging.HermanLogger;

public class EcsClusterIntrospector {

    private AmazonECS ecsClient;
    private AmazonEC2 ec2Client;
    private AmazonRDS rdsClient;
    private HermanLogger logger;

    public EcsClusterIntrospector(AmazonECS ecsClient, AmazonEC2 ec2Client, AmazonRDS rdsClient, HermanLogger logger) {
        this.ecsClient = ecsClient;
        this.ec2Client = ec2Client;
        this.rdsClient = rdsClient;
        this.logger = logger;
    }

    public EcsClusterMetadata introspect(String name, Regions region) {
        EcsClusterMetadata ecsClusterMetadata = new EcsClusterMetadata();

        Cluster cluster = ecsClient.describeClusters(new DescribeClustersRequest().withClusters(name)).getClusters().get(0);
        
        ecsClusterMetadata.setClusterCftStackTags(cluster.getTags());
        ecsClusterMetadata.setClusterId(name);
        
        
        
        String instArn = ecsClient.listContainerInstances(new ListContainerInstancesRequest().withCluster(name)).getContainerInstanceArns().get(0);
        ContainerInstance inst = ecsClient.describeContainerInstances(new DescribeContainerInstancesRequest().withCluster(name).withContainerInstances(instArn)).getContainerInstances().get(0);

        Reservation r = ec2Client.describeInstances(new DescribeInstancesRequest().withInstanceIds(inst.getEc2InstanceId())).getReservations().get(0);
        
        ecsClusterMetadata.setClusterEcsRole(r.getInstances().get(0).getIamInstanceProfile().getArn());
        
        for(GroupIdentifier g : r.getInstances().get(0).getSecurityGroups()) {
            SecurityGroup grp = ec2Client.describeSecurityGroups(new DescribeSecurityGroupsRequest().withGroupIds(g.getGroupId())).getSecurityGroups().get(0);
            

            for(IpPermission perm : grp.getIpPermissions()) {
                if(perm.getFromPort().equals(49153) && perm.getToPort().equals(65535)) {
                    ecsClusterMetadata.setAppSecurityGroup(grp.getGroupId());
       
                    ecsClusterMetadata.getElbSecurityGroups().add(perm.getUserIdGroupPairs().get(0).getGroupId());
 
                }
            }
        }
        Filter f = new Filter("ip-permission.group-id").withValues( ecsClusterMetadata.getAppSecurityGroup());
        SecurityGroup dbgrp = ec2Client.describeSecurityGroups(new DescribeSecurityGroupsRequest().withFilters(f)).getSecurityGroups().get(0);
        ecsClusterMetadata.setRdsSecurityGroup(dbgrp.getGroupId());
     
        for(DBSubnetGroup sub : rdsClient.describeDBSubnetGroups().getDBSubnetGroups()) {
            if(sub.getVpcId().equals(ecsClusterMetadata.getVpcId()) && sub.getDBSubnetGroupName().contains(ecsClusterMetadata.getClusterId())) {
                ecsClusterMetadata.setDbSubnetGroup(sub.getDBSubnetGroupName());
            }
        }
        
        System.out.println(ecsClusterMetadata);

        logger.addLogEntry("Introspection complete:");
        logger.addLogEntry(ecsClusterMetadata.toString());
        return ecsClusterMetadata;
    }
}
