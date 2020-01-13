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

import javax.validation.constraints.Pattern;

import io.hyscale.controller.builder.K8sAuthConfigBuilder;
import io.hyscale.controller.util.CommandUtil;
import io.hyscale.controller.util.StatusUtil;
import org.springframework.beans.factory.annotation.Autowired;

import io.hyscale.commons.constants.ValidationConstants;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.logger.TableFields;
import io.hyscale.commons.logger.TableFormatter;
import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.commons.models.DeploymentContext;
import io.hyscale.controller.activity.ControllerActivity;
import io.hyscale.deployer.services.deployer.Deployer;
import io.hyscale.deployer.core.model.DeploymentStatus;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 *  This class executes the 'hyscale get app status' command
 *  It is a sub-command of the 'hyscale get app' command
 *  @see HyscaleGetAppCommand
 *  Every command/sub-command has to implement the Runnable so that
 *  whenever the command is executed the {@link #run()}
 *  method will be invoked
 *
 * @option namespace  namespace in which the app is deployed
 * @option appName   name of the app
 *
 * Fetches the deployment status {@link DeploymentStatus} of each service in the app and displays
 * in a table format to the user.
 */
@Command(name = "status", description = "Get App Deployment status")
public class HyscaleAppStatusCommand implements Runnable {

    @Option(names = {"-h", "--help"}, usageHelp = true, description = "Display help message")
    private boolean helpRequested = false;

    @Pattern(regexp = ValidationConstants.NAMESPACE_REGEX, message = ValidationConstants.INVALID_NAMESPACE_MSG)
    @Option(names = {"-n", "--namespace", "-ns"}, required = true, description = "Namespace")
    private String namespace;

    @Pattern(regexp = ValidationConstants.APP_NAME_REGEX, message = ValidationConstants.INVALID_APP_NAME_MSG)
    @Option(names = {"-a", "--app"}, required = true, description = "Application name.")
    private String appName;

    @Autowired
    private Deployer deployer;

    @Autowired
    private K8sAuthConfigBuilder authConfigBuilder;

    @Override
    public void run() {
        if (!CommandUtil.isInputValid(this)) {
            System.exit(1);
        }
        
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

        try {
        	List<DeploymentStatus> deploymentStatusList = deployer.getDeploymentStatus(deploymentContext);
        	
            if (deploymentStatusList != null && !deploymentStatusList.isEmpty()) {
                deploymentStatusList.stream().forEach(each -> {
                	String[] tableRow = StatusUtil.getRowData(each);
                    table.addRow(tableRow);
                });
                WorkflowLogger.logTable(table);
            } else {
                WorkflowLogger.info(ControllerActivity.NO_SERVICE_DEPLOYED);
            }
        } catch (HyscaleException e) {
            WorkflowLogger.error(ControllerActivity.ERROR_WHILE_FETCHING_STATUS, e.toString());
        }finally {
            WorkflowLogger.footer();
        }

    }

}