/*
 * Copyright (C) 2018, Liberty Mutual Group
 *
 * Created on 8/1/18
 */
package com.lonewolfworks.wolke.aws.cft;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.lonewolfworks.wolke.aws.AwsExecException;
import com.lonewolfworks.wolke.logging.HermanLogger;

import software.amazon.awssdk.services.cloudformation.CloudFormationClient;
import software.amazon.awssdk.services.cloudformation.model.DescribeStacksRequest;
import software.amazon.awssdk.services.cloudformation.model.ListStacksRequest;
import software.amazon.awssdk.services.cloudformation.model.ListStacksResponse;
import software.amazon.awssdk.services.cloudformation.model.Stack;
import software.amazon.awssdk.services.cloudformation.model.StackSummary;

public class StackUtils {
    private CloudFormationClient cftClient;
    private HermanLogger logger;

    private static final int POLLING_INTERVAL_MS = 10000;

    public StackUtils(CloudFormationClient cftClient, HermanLogger logger) {
        this.cftClient = cftClient;
        this.logger = logger;
    }

    public List<StackSummary> findStacksWithName(String name) {
        ListStacksResponse stacksResult = this.cftClient.listStacks();
        ArrayList<StackSummary> allStacks = new ArrayList<>(stacksResult.stackSummaries());
        String nextToken = stacksResult.nextToken();
        while (nextToken != null) {
            ListStacksRequest listRequest = ListStacksRequest.builder()
                .nextToken(nextToken).build();
            ListStacksResponse listStacksResult = this.cftClient.listStacks(listRequest);
            allStacks.addAll(listStacksResult.stackSummaries());
            nextToken = listStacksResult.nextToken();
        }

        List<StackSummary> filteredStacks = allStacks.stream().filter(stack -> stack.stackName().contains(name)).distinct().collect(Collectors.toList());

        return filteredStacks;
    }

    public void waitForCompletion(String stackName) {
        DescribeStacksRequest wait = DescribeStacksRequest.builder().stackName(stackName).build();
        Boolean completed = false;

        logger.addLogEntry("Waiting...");

        // Try waiting at the start to avoid a race before the stack starts updating
        sleep();
        while (!completed) {
            List<Stack> stacks = cftClient.describeStacks(wait).stacks();

            completed = reportStatusAndCheckCompletionOf(stacks);

            // Not done yet so sleep for 10 seconds.
            if (!completed) {
                sleep();
            }
        }

        logger.addLogEntry("done");
    }

    private void sleep() {
        try {
            Thread.sleep(POLLING_INTERVAL_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.addLogEntry("Interrupted while polling");
            throw new AwsExecException("Interrupted while polling");
        }
    }

    private Boolean reportStatusAndCheckCompletionOf(List<Stack> stacks) {
        for (Stack stack: stacks) {
            reportStatusOf(stack);
            if (stack.stackStatus().toString().contains("IN_PROGRESS")) {
                return false;
            }

            if (stack.stackStatus().toString().contains("FAILED") || stack.stackStatus().toString().contains("ROLLBACK")) {
                throw new AwsExecException("CFT pushed failed - " + stack.stackStatus());
            }
        }
        return true;
    }

    private void reportStatusOf(Stack stack) {

        String status = stack.stackStatus().toString();
        String reason = stack.stackStatusReason();
        if (reason != null) {
            status += " : " + reason;
        }
        logger.addLogEntry(status);
    }
}
