/**
 * Copyright 2019 Pramati Prism, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.hyscale.controller.commands;

import java.util.List;
import java.util.concurrent.Callable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.hyscale.commons.constants.K8SRuntimeConstants;
import io.hyscale.commons.logger.TableFields;
import io.hyscale.commons.logger.TableFormatter;
import io.hyscale.commons.logger.TableFormatter.Builder;
import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.controller.builder.K8sAuthConfigBuilder;
import io.hyscale.deployer.core.model.AppMetadata;
import io.hyscale.deployer.services.deployer.Deployer;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * 
 * This class executes 'hyscale get deployments' command.
 *  It is a sub-command of the 'hyscale get' command
 *  @see HyscaleGetCommand .
 *  Every command/sub-command has to implement the {@link Callable} so that
 *  whenever the command is executed the {@link #call()}
 *  method will be invoked
 *
 * @option wide - to display extra information
 *
 * Eg: hyscale get deployments --wide
 *
 * Fetches all the apps and services deployed on the cluster.
 * If user selects wide option services are also shown else only namespace and apps
 * Only displays apps and services deployed through hyscale.
 * 
 * @author tushar
 *
 */
@Command(name = "deployments", description = "Get all deployments from cluster.")
@Component
public class HyscaleGetDeploymentsCommand implements Callable<Integer> {

    @Option(names = { "-h", "--help" }, usageHelp = true, description = "Displays the  help information of the specified command")
    private boolean helpRequested = false;

    @Option(names = { "--wide" }, required = false, description = "Display additional information like services.")
    private boolean wide = false;

    @Autowired
    private K8sAuthConfigBuilder authConfigBuilder;

    @Autowired
    private Deployer deployer;

    @Override
    public Integer call() throws Exception {

        List<AppMetadata> appInfoList = deployer.getAppsMetadata(authConfigBuilder.getAuthConfig());

        Builder tableBuilder = new TableFormatter.Builder()
                .addField(TableFields.NAMESPACE.getFieldName(), TableFields.NAMESPACE.getLength())
                .addField(TableFields.APPS.getFieldName(), TableFields.APPS.getLength());

        if (wide) {
            tableBuilder.addField(TableFields.SERVICES.getFieldName(), TableFields.SERVICES.getLength());
        }
        TableFormatter table = tableBuilder.build();

        appInfoList.stream().forEach(appInfo -> {
            if (K8SRuntimeConstants.SYSTEM_NAMESPACE.contains(appInfo.getNamespace())) {
                return;
            }
            String services = appInfo.getServices() == null ? null
                    : appInfo.getServices().isEmpty() ? null
                            : appInfo.getServices().toString().replace("[", "").replace("]", "");
            String[] row = new String[] { appInfo.getNamespace(), appInfo.getAppName(), services };
            table.addRow(row);
        });
        WorkflowLogger.logTable(table);

        return 0;
    }

}
