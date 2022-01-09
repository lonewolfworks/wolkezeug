# Auth0 Broker

## Background

The Auth0 Broker is a Lambda function that handles Auth0 registration for an application.

## Contract
| Input/Output | Type                                                                                                                        | Notes                                                                                                              |
|--------------|-----------------------------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------|
| Input        |                                                                                                                             | Object containing values needed to create an Auth0 Application                                                     |
| Output       | List<[HermanBrokerUpdate](../../src/main/java/com/libertymutualgroup/herman/aws/ecs/broker/domain/HermanBrokerUpdate.java)> | List of HermanBrokerUpdate objects containing a message to added to the build logs and a status for the message    |

## Setup

The code for this broker is not available on GitHub because it is organization-specific. It must be implemented by each organization. 
Liberty Mutual used an interface provided by the AWS Lambda Java core library ([aws-lambda-java-core](https://docs.aws.amazon.com/lambda/latest/dg/java-handler-using-predefined-interfaces.html)) 
to create a Lambda function handler for the Auth0 Broker.