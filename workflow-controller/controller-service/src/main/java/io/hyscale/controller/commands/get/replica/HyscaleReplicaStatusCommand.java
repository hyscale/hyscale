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
package io.hyscale.controller.commands.get.replica;

import io.hyscale.commons.constants.ToolConstants;
import io.hyscale.commons.constants.ValidationConstants;
import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.controller.activity.ControllerActivity;
import io.hyscale.controller.builder.WorkflowContextBuilder;
import io.hyscale.controller.model.WorkflowContext;
import io.hyscale.controller.service.ReplicaProcessingService;
import io.hyscale.controller.util.CommandUtil;
import io.hyscale.controller.validator.impl.ClusterValidator;
import io.hyscale.deployer.core.model.DeploymentStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

import javax.validation.constraints.Pattern;
import java.util.concurrent.Callable;

/**
 * This class executes 'hyscale get replica status' command.
 * It is a sub-command of the 'hyscale get replica' command
 *
 * @option service   service name
 * @option namespace namespace in which the app is deployed
 * @option appName   name of the app
 * <p>
 * Eg: hyscale get replica status -s s1 -n dev -a sample
 * <p>
 * Fetches the replica status of a service from the given cluster.
 * The replica status has been abstracted over the pod status,see {@link DeploymentStatus},
 * gives the information about service and the failure message when it is in
 * NotRunning status.
 * @see HyscaleGetReplicaCommand .
 * Every command/sub-command has to implement the {@link Callable} so that
 * whenever the command is executed the {@link #call()}
 * method will be invoked
 */


@CommandLine.Command(name = "status", description = "Get the replica status of the service")
@Component
public class HyscaleReplicaStatusCommand implements Callable<Integer> {

    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "Displays help information for the specified command")
    private boolean helpRequested = false;

    @Pattern(regexp = ValidationConstants.NAMESPACE_REGEX, message = ValidationConstants.INVALID_NAMESPACE_MSG)
    @CommandLine.Option(names = {"-n", "--namespace", "-ns"}, required = true, description = "Namespace of the service")
    private String namespace;

    @Pattern(regexp = ValidationConstants.APP_NAME_REGEX, message = ValidationConstants.INVALID_APP_NAME_MSG)
    @CommandLine.Option(names = {"-a", "--app"}, required = true, description = "Application name")
    private String appName;

    @Pattern(regexp = ValidationConstants.SERVICE_NAME_REGEX, message = ValidationConstants.INVALID_SERVICE_NAME_MSG)
    @CommandLine.Option(names = {"-s", "--service"}, required = true, description = "Service name")
    private String serviceName;
    
    @Autowired
    private ClusterValidator clusterValidator;

    @Autowired
    private ReplicaProcessingService replicaProcessingService;
    
    @Autowired
    private WorkflowContextBuilder workflowContextBuilder;

    @Override
    public Integer call() throws Exception {
        WorkflowLogger.header(ControllerActivity.PROCESSING_INPUT);
        if (!CommandUtil.isInputValid(this)) {
            return ToolConstants.INVALID_INPUT_ERROR_CODE;
        }
        WorkflowContext context = workflowContextBuilder.updateAuthConfig(new WorkflowContext());
        if (!clusterValidator.validate(context )) {
            WorkflowLogger.logPersistedActivities();
            return ToolConstants.INVALID_INPUT_ERROR_CODE;
        }
        
        WorkflowLogger.header(ControllerActivity.SERVICE_NAME, serviceName);
        
        replicaProcessingService.logReplicas(replicaProcessingService.getReplicas(appName, serviceName, namespace, true), false);

        return ToolConstants.HYSCALE_SUCCESS_CODE;
    }
}
