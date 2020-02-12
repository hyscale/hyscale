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

import javax.validation.constraints.Pattern;

import io.hyscale.controller.constants.WorkflowConstants;
import io.hyscale.controller.invoker.StatusComponentInvoker;
import io.hyscale.controller.model.WorkflowContext;
import io.hyscale.controller.util.CommandUtil;
import io.hyscale.controller.util.StatusUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import io.hyscale.commons.constants.ToolConstants;
import io.hyscale.commons.constants.ValidationConstants;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.logger.TableFields;
import io.hyscale.commons.logger.TableFormatter;
import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.controller.activity.ControllerActivity;
import io.hyscale.deployer.core.model.DeploymentStatus;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 *  This class executes the 'hyscale get app status' command
 *  It is a sub-command of the 'hyscale get app' command
 *  @see HyscaleGetAppCommand
 *  Every command/sub-command has to implement the {@link Callable} so that
 *  whenever the command is executed the {@link #call()}
 *  method will be invoked
 *
 * @option namespace  namespace in which the app is deployed
 * @option appName   name of the app
 *
 * Fetches the deployment status {@link DeploymentStatus} of each service in the app and displays
 * in a table format to the user.
 */
@Command(name = "status", description = "Get App Deployment status")
public class HyscaleAppStatusCommand implements Callable<Integer> {
    
    private final Logger logger = LoggerFactory.getLogger(HyscaleAppStatusCommand.class);

    @Option(names = {"-h", "--help"}, usageHelp = true, description = "Display help message")
    private boolean helpRequested = false;

    @Pattern(regexp = ValidationConstants.NAMESPACE_REGEX, message = ValidationConstants.INVALID_NAMESPACE_MSG)
    @Option(names = {"-n", "--namespace", "-ns"}, required = true, description = "Namespace")
    private String namespace;

    @Pattern(regexp = ValidationConstants.APP_NAME_REGEX, message = ValidationConstants.INVALID_APP_NAME_MSG)
    @Option(names = {"-a", "--app"}, required = true, description = "Application name.")
    private String appName;

    @Autowired
    private StatusComponentInvoker statusComponentInvoker;

    @Override
    public Integer call() throws Exception{
        if (!CommandUtil.isInputValid(this)) {
            return ToolConstants.INVALID_INPUT_ERROR_CODE;
        }
        
        WorkflowLogger.header(ControllerActivity.APP_NAME, appName);

        TableFormatter table = new TableFormatter.Builder()
                .addField(TableFields.SERVICE.getFieldName(), TableFields.SERVICE.getLength())
                .addField(TableFields.STATUS.getFieldName())
                .addField(TableFields.AGE.getFieldName(), TableFields.AGE.getLength())
                .addField(TableFields.SERVICE_ADDRESS.getFieldName(), TableFields.SERVICE_ADDRESS.getLength())
                .addField(TableFields.MESSAGE.getFieldName(), TableFields.MESSAGE.getLength()).build();
        
        WorkflowContext context = new WorkflowContext();
        context.setAppName(appName);
        context.setNamespace(namespace);

        try {
            statusComponentInvoker.execute(context);
            List<DeploymentStatus> deploymentStatusList = (List<DeploymentStatus>) context.getAttribute(
                    WorkflowConstants.DEPLOYMENT_STATUS_LIST);
        	
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
            logger.error("Error while getting app {} status in namespace {}", appName, namespace, e);
            throw e;
        } finally {
            WorkflowLogger.footer();
        }

        return 0;
    }

}