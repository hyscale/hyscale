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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.hyscale.controller.builder.K8sAuthConfigBuilder;
import io.hyscale.controller.util.StatusUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.logger.TableFields;
import io.hyscale.commons.logger.TableFormatter;
import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.commons.models.DeploymentContext;
import io.hyscale.controller.activity.ControllerActivity;
import io.hyscale.deployer.core.model.DeploymentStatus;
import io.hyscale.deployer.services.deployer.Deployer;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 *  This class executes 'hyscale get service status' command.
 *  It is a sub-command of the 'hyscale get service' command
 *  @see HyscaleGetServiceCommand .
 *  Every command/sub-command has to implement the Runnable so that
 *  whenever the command is executed the {@link #run()}
 *  method will be invoked
 *
 * @option serviceList list of service names
 * @option namespace  namespace in which the app is deployed
 * @option appName   name of the app
 *
 * Eg: hyscale get service status -s s1 -n dev -a sample
 *
 * Fetches the service status from the given cluster.
 * The service status has been abstracted over the pod status,see {@link DeploymentStatus},
 * gives the information about service and the failure message when it is in
 * NotRunning status.
 *
 */
@Command(name = "status", description = "Get the status of the deployment")
public class HyscaleServiceStatusCommand implements Runnable {

    @Option(names = {"-h", "--help"}, usageHelp = true, description = "Displays the  help information of the specified command")
    private boolean helpRequested = false;

    @Option(names = {"-n", "--namespace", "-ns"}, required = true, description = "Namespace of the service")
    private String namespace;

    @Option(names = {"-a", "--app"}, required = true, description = "Application name")
    private String appName;

    @Option(names = {"-s", "--service"}, required = true, description = "Service names.")
    private String[] serviceList;

    @Autowired
    private Deployer deployer;

    @Autowired
    private K8sAuthConfigBuilder authConfigBuilder;

    private static final Logger logger = LoggerFactory.getLogger(HyscaleServiceStatusCommand.class);

    @Override
    public void run() {

        WorkflowLogger.header(ControllerActivity.APP_NAME, appName);

        TableFormatter table = new TableFormatter.Builder()
                .addField(TableFields.SERVICE.getFieldName(), TableFields.SERVICE.getLength())
                .addField(TableFields.STATUS.getFieldName())
                .addField(TableFields.AGE.getFieldName(), TableFields.AGE.getLength())
                .addField(TableFields.SERVICE_ADDRESS.getFieldName(), TableFields.SERVICE_ADDRESS.getLength())
                .addField(TableFields.MESSAGE.getFieldName(), TableFields.MESSAGE.getLength()).build();

        DeploymentContext deploymentContext = new DeploymentContext();
        deploymentContext.setAuthConfig(authConfigBuilder.getAuthConfig());
        deploymentContext.setNamespace(namespace.trim());
        deploymentContext.setAppName(appName.trim());
        deploymentContext.setWaitForReadiness(false);

        List<DeploymentStatus> deploymentStatusList = new ArrayList<>();

        try {
        	Set<String> services = new HashSet<String>(Arrays.asList(serviceList));
        	WorkflowLogger.logTableFields(table);
        	for (String serviceName : services) {
        		deploymentContext.setServiceName(serviceName);
        		DeploymentStatus serviceStatus = deployer.getServiceDeploymentStatus(deploymentContext);
        		if (serviceStatus != null) {
        			String[] tableRow = StatusUtil.getRowData(serviceStatus);
        			table.addRow(tableRow);
					WorkflowLogger.logTableRow(table, tableRow);
        		}
				deploymentStatusList.add(serviceStatus);
        		
        	}
        } catch (HyscaleException e) {
            WorkflowLogger.error(ControllerActivity.ERROR_WHILE_FETCHING_STATUS, e.toString());
        }

    }

}
