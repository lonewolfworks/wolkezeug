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
package com.lonewolfworks.wolke.aws.ecs.broker.secretsmgr;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.model.CreateSecretRequest;
import com.amazonaws.services.secretsmanager.model.ListSecretsRequest;
import com.amazonaws.services.secretsmanager.model.SecretListEntry;
import com.amazonaws.services.secretsmanager.model.Tag;
import com.amazonaws.services.secretsmanager.model.UpdateSecretRequest;
import com.lonewolfworks.wolke.logging.HermanLogger;

public class SecretsManagerBroker {

	private static final Logger LOGGER = LoggerFactory.getLogger(SecretsManagerBroker.class);

	private HermanLogger hermanLogger;

	public SecretsManagerBroker(HermanLogger hermanLogger) {
		this.hermanLogger = hermanLogger;
	}

	public void brokerSecretsManagerShell(AWSSecretsManager client, String path, String kmsKeyId, String appName, List<Tag> tags) {
		hermanLogger.addLogEntry("Brokering SecretsManager shell");
		List<SecretListEntry> entries = client.listSecrets(new ListSecretsRequest().withMaxResults(100)).getSecretList();
		String arn = null;
		for(SecretListEntry entry : entries) {
			if(path.equals(entry.getName())) {
				arn = entry.getARN();
			}
		}
		if(arn==null) {
			//create new
			hermanLogger.addLogEntry(">> None existing, creating new under "+path);
			client.createSecret(new CreateSecretRequest()
					.withName(path)
					.withKmsKeyId(kmsKeyId)
					.withDescription("Secrets container for "+appName)
					.withTags(tags)
					);
		} else {
			hermanLogger.addLogEntry(">> Existing, updating "+ arn + " under "+path);
			client.updateSecret(new UpdateSecretRequest()
					.withSecretId(arn)
					.withKmsKeyId(kmsKeyId)
					.withDescription("Secrets container for "+appName)
					);
		}

	}

}
