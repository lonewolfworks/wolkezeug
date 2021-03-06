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
package com.lonewolfworks.wolke.task.cli.ecr;

import com.amazonaws.regions.Regions;
import com.lonewolfworks.wolke.aws.credentials.CredentialsHandler;
import com.lonewolfworks.wolke.aws.ecr.EcrLogin;
import com.lonewolfworks.wolke.logging.HermanLogger;

public class ECRRepoLoginTask {
    private HermanLogger logger;

    public ECRRepoLoginTask(HermanLogger logger) {
        this.logger = logger;
    }

    public void runTask(Regions region) {
        EcrLogin ecrLogin = new EcrLogin(logger, CredentialsHandler.getCredentials(), CredentialsHandler.getConfiguration(), region);
        ecrLogin.login();    }
}
