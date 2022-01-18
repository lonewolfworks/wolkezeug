package com.lonewolfworks.wolke.aws.ecs;

import org.junit.Before;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ecs.AmazonECS;
import com.amazonaws.services.rds.AmazonRDS;
import com.lonewolfworks.wolke.aws.ecs.cluster.EcsClusterIntrospector;
import com.lonewolfworks.wolke.logging.HermanLogger;
import com.lonewolfworks.wolke.logging.SysoutLogger;

public class EcsClusterIntrospectorTest {

    HermanLogger logger = new SysoutLogger();

    @Mock
    AmazonECS ecsClient;
    @Mock
    AmazonEC2 ec2Client;
    @Mock
    AmazonRDS rdsClient;
    EcsClusterIntrospector introspector;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        introspector = new EcsClusterIntrospector(ecsClient, ec2Client, rdsClient, logger);
    }

//    @Test
//    public void testInspect() {
//        //GIVEN
//        String stackName = "some-cluster-stack";
//
//        StackSummary stackSummary = new StackSummary()
//            .withStackId(stackName)
//            .withStackName(stackName);
//        List<StackSummary> listStacksSummaries = Arrays.asList(stackSummary);
//        ListStacksResult listStacksResult = new ListStacksResult()
//            .withStackSummaries(listStacksSummaries)
//            .withNextToken(null);
//
//  
//
//        DescribeStackResourcesResult describeStackResourcesResult = new DescribeStackResourcesResult();
//        List<StackResource> resources = describeStackResourcesResult.getStackResources();
//        resources.add(
//            new StackResource().withLogicalResourceId("someELBSecurityGroup").withPhysicalResourceId("sg12345"));
//        resources.add(
//            new StackResource().withLogicalResourceId("someAppSecurityGroup").withPhysicalResourceId("sg23456"));
//        resources.add(
//            new StackResource().withLogicalResourceId("RDSSecurityGroup").withPhysicalResourceId("sg34567"));
//        resources.add(new StackResource().withLogicalResourceId("ECSCluster")
//            .withPhysicalResourceId("dev-plat-cluster-WOURKSLUY"));
//        resources.add(
//            new StackResource().withLogicalResourceId("EncryptionKey").withPhysicalResourceId("123-123-123-123"));
//        resources.add(new StackResource().withLogicalResourceId("DBSubnetGroup").withPhysicalResourceId("subnet12345"));
//        resources.add(new StackResource().withLogicalResourceId("InstanceRole").withPhysicalResourceId("plat-role"));
//
//        when(cftClient.describeStackResources(new DescribeStackResourcesRequest().withStackName(stackName)))
//            .thenReturn(describeStackResourcesResult);
//
//        Tag tag = new Tag().withKey("Name").withValue("some-cluster");
//        Parameter orgParam = new Parameter().withParameterKey("NrOrgTag").withParameterValue("Platform");
//        Parameter sbuParam = new Parameter().withParameterKey("NrSbuTag").withParameterValue("LMB");
//
//        Stack stack = new Stack().withTags(tag).withParameters(orgParam, sbuParam);
//        DescribeStacksResult describeStacksResult = new DescribeStacksResult().withStacks(stack);
//        when(cftClient.describeStacks(new DescribeStacksRequest().withStackName(stackName)))
//            .thenReturn(describeStacksResult);
//
//        com.amazonaws.services.ec2.model.Tag vpcTag = new com.amazonaws.services.ec2.model.Tag().withKey("Name")
//            .withValue("nonprod-vpc");
//        Vpc vpc = new Vpc().withTags(vpcTag).withVpcId("vpc12345");
//        when(ec2Client.describeVpcs()).thenReturn(new DescribeVpcsResult().withVpcs(vpc));
//
//        com.amazonaws.services.ec2.model.Tag subnetTag = new com.amazonaws.services.ec2.model.Tag().withKey("Name")
//            .withValue("private-elb-subnet");
//        Subnet subnet = new Subnet().withTags(subnetTag).withVpcId("vpc12345");
//        when(ec2Client.describeSubnets()).thenReturn(new DescribeSubnetsResult().withSubnets(subnet));
//
//        //WHEN
//        EcsClusterMetadata meta = introspector.introspect(stackName, Regions.US_EAST_1);
//
//        //THEN
//        assertEquals("sg23456", meta.getAppSecurityGroup());
//        assertEquals(1, meta.getClusterCftStackTags().size());
//        assertEquals("plat-role", meta.getClusterEcsRole());
//        assertEquals("dev-plat-cluster-WOURKSLUY", meta.getClusterId());
//        assertEquals("subnet12345", meta.getDbSubnetGroup());
//        assertEquals("123-123-123-123", meta.getEncryptionKey());
//        assertEquals("Platform", meta.getNewrelicOrgTag());
//        assertEquals("LMB", meta.getNewrelicSbuTag());
//        assertEquals("sg34567", meta.getRdsSecurityGroup());
////        assertEquals("vpc12345", meta.getVpcId());
////        assertEquals(1, meta.getElbSecurityGroups().size());
////        assertEquals(1, meta.getElbSubnets().size());
//    }
//
//    
//
//    @Test(expected = AwsExecException.class)
//    public void testIntrospectAwsExceptionOnNonExistentStack() {
//        introspector.introspect("not-a-real-stack", Regions.US_EAST_1);
//    }

}
