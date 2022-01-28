package com.lonewolfworks.wolke.aws.ecs.broker.cloudwatch;


import com.lonewolfworks.wolke.aws.lambda.LambdaInjectConfiguration;
import com.lonewolfworks.wolke.logging.HermanLogger;

import software.amazon.awssdk.services.cloudwatchevents.CloudWatchEventsClient;
import software.amazon.awssdk.services.cloudwatchevents.model.DeleteRuleRequest;
import software.amazon.awssdk.services.cloudwatchevents.model.DescribeRuleRequest;
import software.amazon.awssdk.services.cloudwatchevents.model.DescribeRuleResponse;
import software.amazon.awssdk.services.cloudwatchevents.model.PutRuleRequest;
import software.amazon.awssdk.services.cloudwatchevents.model.PutRuleResponse;
import software.amazon.awssdk.services.cloudwatchevents.model.PutTargetsRequest;
import software.amazon.awssdk.services.cloudwatchevents.model.PutTargetsResponse;
import software.amazon.awssdk.services.cloudwatchevents.model.RemoveTargetsRequest;
import software.amazon.awssdk.services.cloudwatchevents.model.ResourceNotFoundException;
import software.amazon.awssdk.services.cloudwatchevents.model.RuleState;
import software.amazon.awssdk.services.cloudwatchevents.model.Target;
import software.amazon.awssdk.services.lambda.model.GetFunctionResponse;

public class CloudWatchEventsBroker {

    private HermanLogger buildLogger;
    private CloudWatchEventsClient amazonCloudWatchEvents;

    public CloudWatchEventsBroker(HermanLogger buildLogger, CloudWatchEventsClient amazonCloudWatchEvents) {
        this.buildLogger = buildLogger;
        this.amazonCloudWatchEvents = amazonCloudWatchEvents;
    }

    public void brokerScheduledRule(LambdaInjectConfiguration configuration, GetFunctionResponse output) {
        if (configuration.getScheduleExpression() != null) {
            this.buildLogger.addLogEntry("Brokering Scheduled Rule with schedule expression: " + configuration.getScheduleExpression());
            PutRuleRequest putRuleRequest = PutRuleRequest.builder()
                    .name(configuration.getFunctionName() + "-scheduled-trigger")
                    .scheduleExpression(configuration.getScheduleExpression())
                    .state(RuleState.ENABLED).build();

            PutRuleResponse putRuleResult = this.amazonCloudWatchEvents.putRule(putRuleRequest);
            this.buildLogger.addLogEntry("Created Rule: " + putRuleResult.ruleArn());

            Target target = Target.builder().arn(output.configuration().functionArn()).id(configuration.getFunctionName()).build();
            PutTargetsRequest putTargetsRequest = PutTargetsRequest.builder().targets(target).rule(configuration.getFunctionName() + "-scheduled-trigger").build();
            PutTargetsResponse putTargetsResult = this.amazonCloudWatchEvents.putTargets(putTargetsRequest);
            this.buildLogger.addLogEntry("Added target " + putTargetsResult.toString() + "to rule " + putRuleResult.ruleArn());
        } else {
            this.buildLogger.addLogEntry("No schedule expression provided. Removing any existing scheduled rules.");
            DescribeRuleRequest describeRuleRequest = DescribeRuleRequest.builder()
                    .name(configuration.getFunctionName() + "-scheduled-trigger").build();
            try {
                DescribeRuleResponse describeRuleResult = this.amazonCloudWatchEvents.describeRule(describeRuleRequest);


                RemoveTargetsRequest removeTargetsRequest = RemoveTargetsRequest.builder()
                        .rule(configuration.getFunctionName() + "-scheduled-trigger")
                        .ids(configuration.getFunctionName()).build();
                buildLogger.addErrorLogEntry("Removing target " + configuration.getFunctionName());
                this.amazonCloudWatchEvents.removeTargets(removeTargetsRequest);

                DeleteRuleRequest deleteRuleRequest = DeleteRuleRequest.builder()
                        .name(describeRuleResult.name()).build();
                this.amazonCloudWatchEvents.deleteRule(deleteRuleRequest);
                buildLogger.addErrorLogEntry("Deleted existing scheduled rule: " + describeRuleResult.name());
            } catch (ResourceNotFoundException e) {
                buildLogger.addLogEntry("No scheduled rule found. Skipping...");
            } catch (Exception e) {
                buildLogger.addErrorLogEntry("Exception while deleting scheduled rule.", e);
            }
        }
    }
}
