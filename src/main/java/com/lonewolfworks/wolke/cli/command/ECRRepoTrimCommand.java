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
package com.lonewolfworks.wolke.cli.command;

import com.lonewolfworks.wolke.cli.Cli;
import com.lonewolfworks.wolke.task.cli.ecr.ECRRepoTaskConfiguration;
import com.lonewolfworks.wolke.task.cli.ecr.ECRRepoTrimTask;

import static picocli.CommandLine.Command;
import static picocli.CommandLine.Help;
import static picocli.CommandLine.Option;
import static picocli.CommandLine.ParentCommand;

@Command(name = "ecr-repo-trim", mixinStandardHelpOptions = true)
public class ECRRepoTrimCommand implements Runnable {
    @ParentCommand
    private Cli cli;

    @Option(names = {"-repo", "--repoName"}, description = "Name of ECR repository to create", showDefaultValue = Help.Visibility.ALWAYS, arity = "1")
    private String repoName;

    @Override public void run() {
        cli.getLogger().addLogEntry("Starting ECR Repo Trim...");
        ECRRepoTaskConfiguration config = new ECRRepoTaskConfiguration()
            .withRegion(cli.getRegion())
            .withRepoName(repoName);
        ECRRepoTrimTask trimTask = new ECRRepoTrimTask(cli.getLogger());
        trimTask.runTask(config);
    }
}
