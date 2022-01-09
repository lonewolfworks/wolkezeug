# SNS Topics

This broker provides [AWS SNS Topics](https://aws.amazon.com/sns/). The
configuration key "topics" takes a list of topic definition objects
which currently supports the following fields:

## Topic Definition

| Name                    | Type                                 | Required | Description                                                                                                                                                     |
|-------------------------|--------------------------------------|----------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------|
| name                    | String                               | YES      | Name that will be assigned to the provisioned topic                                                                                                             |
| policyName              | String                               | YES      | Name to a JSON file which represents the [topic policy](https://docs.aws.amazon.com/sns/latest/dg/AccessPolicyLanguage.html) that will be applied to the bucket |
| autoRemoveSubscriptions | Bool                                 | NO       | Remove subscriptions that aren't in the subscription list                                                                                                       |
| serverSideEncryption    | Bool                                 | NO       | Indicates if server side encryption should be turned on. Default = false                                                                                        |
| kmsMasterKeyId          | String                               | NO       | KMS Key used to encrypt messages in the queue. Default = alias/aws/sns or null if serverSideEncryption is disabled                                              |
| subscriptions           | List&lt;Subscription\_Definition&gt; | NO       | List of subscriptions                                                                                                                                           |

  

## Subscription Definition

| Name     | Type   | Required | Description                                                                                     |
|----------|--------|----------|-------------------------------------------------------------------------------------------------|
| protocol | String | YES      | See [SNS protocol documentation](https://docs.aws.amazon.com/sns/latest/api/API_Subscribe.html) |
| endpoint | String | YES      | See [SNS protocol documentation](https://docs.aws.amazon.com/sns/latest/api/API_Subscribe.html) |
