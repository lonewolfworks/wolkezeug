/*
 * Copyright (C) 2018, Liberty Mutual Group
 *
 * Created on 8/2/18
 */
package com.lonewolfworks.wolke.aws.asg;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.lonewolfworks.wolke.logging.HermanLogger;

import software.amazon.awssdk.services.autoscaling.AutoScalingClient;
import software.amazon.awssdk.services.autoscaling.model.AutoScalingException;
import software.amazon.awssdk.services.autoscaling.model.AutoScalingGroup;
import software.amazon.awssdk.services.autoscaling.model.DescribeAutoScalingGroupsRequest;
import software.amazon.awssdk.services.autoscaling.model.Instance;
import software.amazon.awssdk.services.autoscaling.model.LifecycleState;
import software.amazon.awssdk.services.autoscaling.model.ResumeProcessesRequest;
import software.amazon.awssdk.services.autoscaling.model.SetInstanceHealthRequest;
import software.amazon.awssdk.services.autoscaling.model.SuspendProcessesRequest;

public class AutoscalingGroupHandler {
    private AutoScalingClient asgClient;
    private HermanLogger logger;

    private final List<String> SUSPEND_SCALING_PROCESSES = Arrays.asList("Launch", "HealthCheck", "ReplaceUnhealthy", "AZRebalance", "AlarmNotification", "ScheduledActions", "AddToLoadBalancer");

    public AutoscalingGroupHandler(AutoScalingClient asgClient, HermanLogger logger) {
        this.asgClient = asgClient;
        this.logger = logger;
    }

    public void pauseScalingOperations(String asgName) {
        this.logger.addLogEntry("...Suspending Auto Scaling operations on: " + asgName);
        SuspendProcessesRequest suspendRequest = SuspendProcessesRequest.builder()
            .autoScalingGroupName(asgName)
            .scalingProcesses(SUSPEND_SCALING_PROCESSES).build();
        try {
            this.asgClient.suspendProcesses(suspendRequest);
        }
        catch (AutoScalingException ex) {
            this.logger.addErrorLogEntry("Unable to suspend autoscaling operations, proceeding with update...");
        }
    }

    public void resumeScalingOperations(String asgName) {
        this.logger.addLogEntry("...Resuming Auto Scaling operations on: " + asgName);
        ResumeProcessesRequest resumeRequest = ResumeProcessesRequest.builder()
            .autoScalingGroupName(asgName).build();
        this.asgClient.resumeProcesses(resumeRequest);
    }

    public AutoScalingGroup getAsg(String asgName) {
        DescribeAutoScalingGroupsRequest request = DescribeAutoScalingGroupsRequest.builder()
            .autoScalingGroupNames(asgName).build();
        return this.asgClient.describeAutoScalingGroups(request).autoScalingGroups().get(0);
    }

    public List<Instance> getAsgInstancesInService(String asgName) {
        AutoScalingGroup group = getAsg(asgName);
        return group.instances().stream()
            .filter(instance -> LifecycleState.IN_SERVICE.toString().equals(instance.lifecycleState()) && "Healthy".equals(instance.healthStatus()))
            .collect(Collectors.toList());
    }

    public void setEc2Unhealthy(String instanceId) {
        SetInstanceHealthRequest healthRequest = SetInstanceHealthRequest.builder()
            .instanceId(instanceId)
            .healthStatus("Unhealthy").build();
        this.asgClient.setInstanceHealth(healthRequest);
    }
}
