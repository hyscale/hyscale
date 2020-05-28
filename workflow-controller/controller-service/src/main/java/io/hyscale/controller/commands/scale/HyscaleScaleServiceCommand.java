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
package io.hyscale.controller.commands.scale;

import io.hyscale.commons.constants.ToolConstants;
import io.hyscale.commons.constants.ValidationConstants;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.commons.models.AuthConfig;
import io.hyscale.controller.activity.ControllerActivity;
import io.hyscale.controller.builder.K8sAuthConfigBuilder;
import io.hyscale.controller.commands.input.ScaleArg;
import io.hyscale.controller.exception.ControllerErrorCodes;
import io.hyscale.controller.util.CommandUtil;
import io.hyscale.deployer.services.deployer.Deployer;
import io.hyscale.deployer.services.model.ScaleOperation;
import io.hyscale.deployer.services.model.ScaleSpec;
import io.hyscale.deployer.services.model.ScaleStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

import javax.validation.constraints.Pattern;
import java.util.concurrent.Callable;

/**
 * This class executes  'hyscale scale service' command
 * It is a sub-command of the 'hyscale scale' command
 *
 * @see io.hyscale.controller.commands.scale.HyscaleScaleCommand
 * Every command/sub-command has to implement the {@link Callable} so that
 * whenever the command is executed the {@link #call()}
 * method will be invoked
 * <p>
 * The sub-commands of are handled by @Command annotation
 */

@CommandLine.Command(name = "service", description = "scales the service of an application")
@Component
public class HyscaleScaleServiceCommand implements Callable<Integer> {

    private static final Logger logger = LoggerFactory.getLogger(HyscaleScaleServiceCommand.class);

    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "Displays the  help information of the specified command")
    private boolean helpRequested = false;

    @Pattern(regexp = ValidationConstants.NAMESPACE_REGEX, message = ValidationConstants.INVALID_NAMESPACE_MSG)
    @CommandLine.Option(names = {"-n", "--namespace", "-ns"}, required = true, description = "Namespace of the service")
    private String namespace;

    @Pattern(regexp = ValidationConstants.APP_NAME_REGEX, message = ValidationConstants.INVALID_APP_NAME_MSG)
    @CommandLine.Option(names = {"-a", "--app", "--application"}, required = true, description = "Application name")
    private String appName;

    @Pattern(regexp = ValidationConstants.SERVICE_NAME_REGEX, message = ValidationConstants.INVALID_SERVICE_NAME_MSG)
    @CommandLine.Option(names = {"-s", "--service"}, required = true, description = "Service name")
    private String serviceName;

    @CommandLine.ArgGroup(exclusive = true, heading = "Scaling Options", order = 10)
    private ScaleArg scaleArg;

    @Autowired
    private Deployer deployer;

    @Autowired
    private K8sAuthConfigBuilder k8sAuthConfigBuilder;


    @Override
    public Integer call() throws Exception {


        if (!CommandUtil.isInputValid(this)) {
            return ToolConstants.INVALID_INPUT_ERROR_CODE;
        }
        AuthConfig authConfig = k8sAuthConfigBuilder.getAuthConfig();

        ScaleSpec scaleSpec = null;
        if (scaleArg.getUp() != null) {
            scaleSpec = new ScaleSpec(ScaleOperation.SCALE_UP, scaleArg.getUp());
        } else if (scaleArg.getDown() != null) {
            scaleSpec = new ScaleSpec(ScaleOperation.SCALE_DOWN, scaleArg.getDown());
        } else if (scaleArg.getTo() != null) {
            scaleSpec = new ScaleSpec(ScaleOperation.SCALE_TO, scaleArg.getTo());
        } else {
            WorkflowLogger.error(ControllerActivity.UNEXPECTED_ERROR);
            return ToolConstants.INVALID_INPUT_ERROR_CODE;
        }

        ScaleStatus status = deployer.scale(authConfig, appName, serviceName, namespace, scaleSpec);

        if (!status.isSuccess()) {
            HyscaleException ex = new HyscaleException(ControllerErrorCodes.FAILED_TO_SCALE_SERVICE, serviceName, appName, namespace);
            WorkflowLogger.error(ControllerActivity.ERROR, ex.getMessage());
            throw ex;

        }
        return ToolConstants.HYSCALE_SUCCESS_CODE;
    }
}
