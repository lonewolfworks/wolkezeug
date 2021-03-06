package com.lonewolfworks.wolke.cli.command;

import com.amazonaws.regions.Regions;
import com.lonewolfworks.wolke.aws.credentials.CredentialsHandler;
import com.lonewolfworks.wolke.aws.ecs.broker.s3.BucketMeta;
import com.lonewolfworks.wolke.aws.ecs.broker.s3.S3Broker;
import com.lonewolfworks.wolke.logging.SysoutLogger;
import com.lonewolfworks.wolke.util.ConfigurationUtil;
import com.lonewolfworks.wolke.util.PropertyHandlerUtil;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class S3CreateCommandTest {

    @Mock
    ConfigurationUtil configUtil;

    @Mock
    PropertyHandlerUtil propUtil;

    @Mock
    CredentialsHandler credHandler;

    @Mock
    S3Broker s3Broker;

    @Before
    public void setup(){
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldCallS3Broker(){

        S3CreateCommand command = spy(new S3CreateCommand());
        doReturn(s3Broker).when(command).getBroker(any());
        doReturn(new BucketMeta()).when(s3Broker).brokerFromConfigurationFile();

        command.executeS3Task(mock(SysoutLogger.class), Regions.US_EAST_1, credHandler, propUtil, configUtil);

        verify(s3Broker).brokerFromConfigurationFile();

    }
}
