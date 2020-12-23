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
package io.hyscale.controller.commands.get.app;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.validation.constraints.Pattern;

import io.hyscale.controller.builder.K8sAuthConfigBuilder;
import org.apache.commons.lang3.StringUtils;
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
import io.hyscale.controller.model.WorkflowContextBuilder;
import io.hyscale.controller.constants.WorkflowConstants;
import io.hyscale.controller.invoker.StatusComponentInvoker;
import io.hyscale.controller.model.WorkflowContext;
import io.hyscale.controller.util.CommandUtil;
import io.hyscale.controller.util.StatusUtil;
import io.hyscale.controller.validator.impl.ClusterValidator;
import io.hyscale.deployer.core.model.DeploymentStatus;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * This class executes the 'hyscale get app status' command
 * It is a sub-command of the 'hyscale get app' command
 * Options:
 *  namespace - namespace in which the app is deployed
 *  appName - name of the app
 * <p>
 * Fetches the deployment status {@link DeploymentStatus} of each service in the app
 * Displays app data in a table format to the user.
 * @see HyscaleGetAppsCommand
 * Every command/sub-command has to implement the {@link Callable} so that
 * whenever the command is executed the {@link #call()}
 * method will be invoked
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
    private ClusterValidator clusterValidator;

    @Autowired
    private StatusComponentInvoker statusComponentInvoker;

    @Autowired
    private K8sAuthConfigBuilder authConfigBuilder;

    @Override
    public Integer call() throws Exception {
        if (!CommandUtil.isInputValid(this)) {
            return ToolConstants.INVALID_INPUT_ERROR_CODE;
        }

        WorkflowContextBuilder builder = new WorkflowContextBuilder(appName);
        builder.withNamespace(namespace);
        builder.withAuthConfig(authConfigBuilder.getAuthConfig());
        WorkflowContext context = builder.get();
        if (!clusterValidator.validate(context)) {
            WorkflowLogger.logPersistedActivities();
            return ToolConstants.INVALID_INPUT_ERROR_CODE;
        }
        WorkflowLogger.info(ControllerActivity.WAITING_FOR_SERVICE_STATUS);

        WorkflowLogger.header(ControllerActivity.APP_NAME, appName);
        try {
            statusComponentInvoker.execute(context);

            Object statusAttr = context.getAttribute(
                    WorkflowConstants.DEPLOYMENT_STATUS_LIST);

            if (statusAttr == null) {
                WorkflowLogger.info(ControllerActivity.NO_SERVICE_DEPLOYED);
                return ToolConstants.HYSCALE_SUCCESS_CODE;
            }
            List<DeploymentStatus> deploymentStatusList = (List<DeploymentStatus>) statusAttr;

            if (deploymentStatusList.isEmpty()) {
                WorkflowLogger.info(ControllerActivity.NO_SERVICE_DEPLOYED);
                return ToolConstants.HYSCALE_SUCCESS_CODE;
            }

            List<String[]> rowList = new ArrayList<String[]>();
            boolean isLarge = false;
            for (DeploymentStatus deploymentStatus : deploymentStatusList) {
                if (deploymentStatus == null) {
                    continue;
                }
                if (StringUtils.isNotBlank(deploymentStatus.getServiceAddress())) {
                    isLarge = isLarge ? isLarge : deploymentStatus.getServiceAddress().length() > TableFields.SERVICE_ADDRESS.getLength();
                }
                String[] tableRow = StatusUtil.getRowData(deploymentStatus);
                rowList.add(tableRow);
            }
            TableFormatter table = StatusUtil.getStatusTable(isLarge);
            rowList.forEach(each -> table.addRow(each));
            WorkflowLogger.logTable(table);
        } catch (HyscaleException e) {
            WorkflowLogger.error(ControllerActivity.ERROR_WHILE_FETCHING_STATUS, e.toString());
            logger.error("Error while getting app {} status in namespace {}", appName, namespace, e);
            throw e;
        } finally {
            WorkflowLogger.footer();
        }
        return ToolConstants.HYSCALE_SUCCESS_CODE;
    }

}