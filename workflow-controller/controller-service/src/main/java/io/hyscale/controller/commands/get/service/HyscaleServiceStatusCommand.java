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
package io.hyscale.controller.commands.get.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.validation.constraints.Pattern;

import io.hyscale.commons.logger.TableFields;
import io.hyscale.controller.builder.K8sAuthConfigBuilder;
import io.hyscale.controller.constants.WorkflowConstants;
import io.hyscale.controller.invoker.StatusComponentInvoker;
import io.hyscale.controller.model.WorkflowContext;
import io.hyscale.controller.util.CommandUtil;
import io.hyscale.controller.util.StatusUtil;
import io.hyscale.controller.validator.impl.ClusterValidator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import io.hyscale.commons.constants.ToolConstants;
import io.hyscale.commons.constants.ValidationConstants;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.logger.TableFormatter;
import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.controller.activity.ControllerActivity;
import io.hyscale.controller.model.WorkflowContextBuilder;
import io.hyscale.deployer.core.model.DeploymentStatus;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

/**
 * This class executes 'hyscale get service status' command.
 * It is a sub-command of the 'hyscale get service' command
 *
 * Options:
 *   serviceList - list of service names
 *   namespace - namespace in which the app is deployed
 *   appName - name of the app
 * <p>
 * Eg: hyscale get service status -s s1 -n dev -a sample
 * <p>
 * Fetches the service status from the given cluster.
 * The service status has been abstracted over the pod status,see {@link DeploymentStatus},
 * gives the information about service and the failure message when it is in
 * NotRunning status.
 * @see HyscaleGetServiceCommand .
 * Every command/sub-command has to implement the {@link Callable} so that
 * whenever the command is executed the {@link #call()}
 * method will be invoked
 */
@Command(name = "status", description = "Get the status of the deployment")
public class HyscaleServiceStatusCommand implements Callable<Integer> {

    private static final Logger logger = LoggerFactory.getLogger(HyscaleServiceStatusCommand.class);

    @Option(names = {"-h", "--help"}, usageHelp = true, description = "Displays the  help information of the specified command")
    private boolean helpRequested = false;

    @Pattern(regexp = ValidationConstants.NAMESPACE_REGEX, message = ValidationConstants.INVALID_NAMESPACE_MSG)
    @Option(names = {"-n", "--namespace", "-ns"}, required = true, description = "Namespace of the service")
    private String namespace;

    @Pattern(regexp = ValidationConstants.APP_NAME_REGEX, message = ValidationConstants.INVALID_APP_NAME_MSG)
    @Option(names = {"-a", "--app","--application"}, required = true, description = "Application name")
    private String appName;

    @Option(names = {"-s", "--service"}, required = true, description = "Service names", split = ",")
    private List<
            @Pattern(regexp = ValidationConstants.SERVICE_NAME_REGEX, message = ValidationConstants.INVALID_SERVICE_NAME_MSG)
                    String> serviceList;

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

        List<WorkflowContext> contextList = new ArrayList<>();
        for (String each : serviceList) {
            WorkflowContext context = new WorkflowContextBuilder(appName).withNamespace(namespace)
                    .withServiceName(each).withAuthConfig(authConfigBuilder.getAuthConfig()).get();
            if (!clusterValidator.validate(context)) {
                WorkflowLogger.logPersistedActivities();
                return ToolConstants.INVALID_INPUT_ERROR_CODE;
            }
            contextList.add(context);
        }

        WorkflowLogger.info(ControllerActivity.WAITING_FOR_SERVICE_STATUS);

        WorkflowLogger.header(ControllerActivity.APP_NAME, appName);
        try {
            boolean isLarge = false;
            List<String[]> rowList = new ArrayList<String[]>();
            for (WorkflowContext context : contextList) {
                statusComponentInvoker.execute(context);
                Object statusAttr = context.getAttribute(
                        WorkflowConstants.DEPLOYMENT_STATUS);
                if (statusAttr != null) {
                    DeploymentStatus serviceStatus = (DeploymentStatus) statusAttr;
                    if (serviceStatus.getServiceAddress() != null) {
                        isLarge = isLarge ? isLarge : serviceStatus.getServiceAddress().length() > TableFields.SERVICE_ADDRESS.getLength();
                    }
                    String[] tableRow = StatusUtil.getRowData(serviceStatus);
                    rowList.add(tableRow);
                }
            }
            TableFormatter table = StatusUtil.getStatusTable(isLarge);
            rowList.forEach(each -> table.addRow(each));
            WorkflowLogger.logTable(table);
        } catch (HyscaleException e) {
            logger.error("Error while getting status for app: {}, in namespace: {}", appName, namespace);
            WorkflowLogger.error(ControllerActivity.ERROR_WHILE_FETCHING_STATUS, e.toString());
            throw e;
        } finally {
            WorkflowLogger.footer();
        }
        return ToolConstants.HYSCALE_SUCCESS_CODE;
    }

}
