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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.amazonaws.services.ecs.model.ContainerDefinition;
import com.amazonaws.services.ecs.model.KeyValuePair;
import com.amazonaws.services.ecs.model.Secret;
import com.lonewolfworks.wolke.aws.ecs.broker.auth0.Auth0Configuration;
import com.lonewolfworks.wolke.aws.ecs.broker.rds.RdsInstance;
import com.lonewolfworks.wolke.aws.ecs.broker.secretsmgr.SecretsManagerBroker;
import com.lonewolfworks.wolke.aws.ecs.cluster.EcsClusterMetadata;

public class EcsDefaultEnvInjection {

    public void injectEnvironment(EcsPushDefinition definition, String region, String deployEnv,
                                  EcsClusterMetadata meta) {

        for (ContainerDefinition container : definition.getContainerDefinitions()) {
            inject(container, definition.getAppName(), region, deployEnv,
                    meta.getNewrelicOrgTag());
        }

    }

    private void inject(ContainerDefinition def, String appName, String region, String deployEnv,
                        String org) {
        List<KeyValuePair> env = def.getEnvironment();
        if (!propExists(env, "NEW_RELIC_APP_NAME")) {
            def.getEnvironment()
                    .add(new KeyValuePair().withName("NEW_RELIC_APP_NAME").withValue(appName + " (" + region + ")"));
        }

        if (!propExists(env, "newrelic.config.labels")) {
            //def.getEnvironment().add(new KeyValuePair().withName("newrelic.config.labels")
            //        .withValue("Environment:" + deployEnv + ";Region:" + region + ";Organization:" + org + ";"));
            def.getEnvironment().add(new KeyValuePair().withName("NEWRELIC_CONFIG_LABELS")
                    .withValue("Environment:" + deployEnv + ";Region:" + region + ";Organization:" + org + ";"));
        }
//        if (!propExists(env, "NEW_RELIC_LICENSE_KEY") && key != null) {
//            def.getEnvironment().add(new KeyValuePair().withName("NEW_RELIC_LICENSE_KEY")
//                    .withValue(key));
//        }

        def.getEnvironment().add(new KeyValuePair().withName("aws.region").withValue(region));
        def.getEnvironment().add(new KeyValuePair().withName("AWS_REGION").withValue(region));
    }

    public void injectRds(EcsPushDefinition definition, RdsInstance rds, SecretsManagerBroker broker) {
    	Map<String, String> paramMap = new HashMap();
    	paramMap.put("host", rds.getEndpoint().getAddress());
    	paramMap.put("port", rds.getEndpoint().getPort().toString());
    	paramMap.put("dbName", rds.getDBName());
    	paramMap.put("connectionString", rds.getConnectionString());
    	paramMap.put("dbiResourceId", rds.getDbiResourceId());
    	paramMap.put("masterUsername", rds.getMasterUsername());
    	paramMap.put("appUsername", rds.getAppUsername());
    	paramMap.put("adminUsername", rds.getAdminUsername());
    	
//    	Map<String, String> secretMap = new HashMap();
//    	secretMap.put("adminPassword", rds.getAdminPassword());
//    	secretMap.put("appPassword", rds.getAppPassword());

    	
    	for (ContainerDefinition def : definition.getContainerDefinitions()) {
    		for(KeyValuePair val : def.getEnvironment()) {
    			if(val.getValue().contains("${rdsbroker:")) {
    				String rdsKey = StringUtils.substringBetween(val.getValue(), "${rdsbroker:", "}");
    				String currentVal = val.getValue();
    				
    				val.setValue(currentVal.replace("${rdsbroker:"+rdsKey+"}", paramMap.get(rdsKey)));
    			}
    		}
        }
    	
    	for (ContainerDefinition def : definition.getContainerDefinitions()) {
    		for(Secret sec : def.getSecrets()) {
    			System.out.println("RDS SEC"+sec);
    			if(sec.getValueFrom().startsWith("rdsbroker:")) {
    				//rdsbroker:appUsername
    				String rdsKey = sec.getValueFrom().split(":")[1];
    				String path = rds.getSecretPathPrefix();
    				//TODO should already be set...just broker arn?
    				String arn = broker.brokerSecretsManagerShell(path+"/"+rdsKey, definition.getAppName());
            		sec.setValueFrom(arn);
    			}
    		}
        }
    	
    	
    }

    public void injectAuth0(EcsPushDefinition definition, Auth0Configuration auth0Configuration) {
        for (ContainerDefinition def : definition.getContainerDefinitions()) {
            def.getEnvironment().add(new KeyValuePair()
                    .withName(auth0Configuration.getInjectNames().getClientId())
                    .withValue(auth0Configuration.getClientId()));

            def.getEnvironment().add(new KeyValuePair()
                    .withName(auth0Configuration.getInjectNames().getEncryptedClientSecret())
                    .withValue(auth0Configuration.getEncryptedClientSecret()));
        }
    }

    private boolean propExists(List<KeyValuePair> env, String prop) {
        for (KeyValuePair pair : env) {
            if (pair.getName().equals(prop)) {
                return true;
            }
        }
        return false;
    }

    public void setDefaultContainerName(EcsPushDefinition definition) {
        if (definition.getContainerDefinitions().size() != 1) {
            return;
        } else {
            if (definition.getContainerDefinitions().get(0).getName() == null) {
                definition.getContainerDefinitions().get(0).setName("default");
            }
        }
    }
}
