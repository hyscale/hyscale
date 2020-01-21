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

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.Map;

import io.hyscale.commons.component.ComponentInvoker;
import io.hyscale.commons.config.SetupConfig;
import io.hyscale.commons.constants.ToolConstants;
import io.hyscale.commons.constants.ValidationConstants;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.controller.constants.WorkflowConstants;
import io.hyscale.controller.invoker.DockerfileGeneratorComponentInvoker;
import io.hyscale.controller.model.WorkflowContext;
import io.hyscale.controller.util.CommandUtil;
import io.hyscale.controller.util.ServiceProfileUtil;
import io.hyscale.controller.util.ServiceSpecMapper;
import io.hyscale.controller.util.ServiceSpecUtil;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.commons.models.Manifest;
import io.hyscale.controller.activity.ControllerActivity;
import io.hyscale.controller.invoker.DeployComponentInvoker;
import io.hyscale.controller.invoker.ImageBuildComponentInvoker;
import io.hyscale.controller.invoker.ManifestGeneratorComponentInvoker;
import picocli.CommandLine;

import javax.annotation.PreDestroy;
import javax.validation.constraints.Pattern;

/**
 *  This class executes 'hyscale deploy service' command
 *  It is a sub-command of the 'hyscale deploy' command
 *  @see HyscaleDeployCommand
 *  Every command/sub-command has to implement the {@link Callable} so that
 *  whenever the command is executed the {@link #call()}
 *  method will be invoked
 *
 * @option namespace  name of the namespace in which the service to be deployed
 * @option appName   name of the app to logically group your services
 * @option serviceSpecs   list of service specs that are to be deployed
 * @option profiles list of profiles for services
 * @option verbose  prints the verbose output of the deployment
 *
 *  Eg: hyscale deploy service -f s1.hspec.yaml -f s2.hspec.yaml -p p1-s1.hprof.yaml -n dev -a sample
 *
 *
 *  Responsible for deploying a service with the given 'hspec' to
 *  the configured kubernetes cluster ,starting from image building to manifest generation
 *  to deployment. Creates a WorkflowContext to communicate across
 *  all deployment stages.
 *
 */
@CommandLine.Command(name = "service", aliases = {"services"},
        description = "Deploys the service to kubernetes cluster")
@Component
public class HyscaleDeployServiceCommand implements Callable<Integer> {

    private static final Logger logger = LoggerFactory.getLogger(HyscaleDeployServiceCommand.class);

    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "Displays the help information of the specified command")
    private boolean helpRequested = false;

    @Pattern(regexp = ValidationConstants.NAMESPACE_REGEX, message = ValidationConstants.INVALID_NAMESPACE_MSG)
    @CommandLine.Option(names = {"-n", "--namespace", "-ns"}, required = true, description = "Namespace of the service ")
    private String namespace;

    @Pattern(regexp = ValidationConstants.APP_NAME_REGEX, message = ValidationConstants.INVALID_APP_NAME_MSG)
    @CommandLine.Option(names = {"-a", "--app"}, required = true, description = "Application name")
    private String appName;

    @CommandLine.Option(names = {"-v", "--verbose", "-verbose"}, required = false, description = "Verbose output")
    private boolean verbose = false;

    @CommandLine.Option(names = {"-f", "--files"}, required = true, description = "Service specs files.", split = ",")
    private List<
    @Pattern(regexp = ValidationConstants.SERVICE_SPEC_NAME_REGEX, message = ValidationConstants.INVALID_SERVICE_SPEC_NAME_MSG)
    String> serviceSpecs;
    
    @CommandLine.Option(names = {"-p", "--profile"}, required = false, description = "Profile for service.", split = ",")
    private List<
    @Pattern(regexp = ValidationConstants.PROFILE_FILENAME_REGEX, message = ValidationConstants.INVALID_PROFILE_FILE_NAME_MSG)
    String> profiles;

    @Autowired
    private ImageBuildComponentInvoker imageBuildComponentInvoker;

    @Autowired
    private ManifestGeneratorComponentInvoker manifestGeneratorComponentInvoker;

    @Autowired
    private DockerfileGeneratorComponentInvoker dockerfileGeneratorComponentInvoker;

    @Autowired
    private DeployComponentInvoker deployComponentInvoker;
    
    @Autowired
    private ServiceSpecMapper serviceSpecMapper;

    @Override
    public Integer call() throws Exception {
        if (!CommandUtil.isInputValid(this)) {
            return ToolConstants.INVALID_INPUT_ERROR_CODE;
        }
        
        Map<String, String> serviceProfileMap = new HashMap<String, String>();
        try {
            serviceProfileMap = ServiceProfileUtil.getServiceProfileMap(profiles);
        } catch (HyscaleException e) {
            WorkflowLogger.error(ControllerActivity.CANNOT_PROCESS_SERVICE_PROFILE, e.getMessage());
            throw e;
        }
        
        boolean isCommandFailed = false;
        for (String serviceSpecPath : serviceSpecs) {
            boolean isServiceFailed = false;

            WorkflowContext workflowContext = new WorkflowContext();
            workflowContext.addAttribute(WorkflowConstants.DEPLOY_START_TIME, System.currentTimeMillis());
            String serviceName = ServiceSpecUtil.getServiceNameFromPath(serviceSpecPath);
            File serviceSpecFile = new File(serviceSpecPath);
            String profilePath = serviceProfileMap.remove(serviceName);
            try {
                ServiceSpec serviceSpec = serviceSpecMapper.from(serviceSpecPath, profilePath);
                workflowContext.setServiceSpec(serviceSpec);
            } catch (HyscaleException e) {
                WorkflowLogger.error(ControllerActivity.CANNOT_PROCESS_SERVICE_SPEC, e.getMessage());
                throw e;
            }
            workflowContext.setServiceName(serviceName);
            SetupConfig.clearAbsolutePath();
            SetupConfig.setAbsolutePath(serviceSpecFile.getAbsoluteFile().getParent());
            WorkflowLogger.header(ControllerActivity.SERVICE_NAME, serviceName);
            workflowContext.setAppName(appName.trim());
            workflowContext.setNamespace(namespace.trim());
            workflowContext.setEnvName(CommandUtil.getEnvName(profilePath, appName.trim()));
            workflowContext.addAttribute(WorkflowConstants.VERBOSE, verbose);

            // clean up service dir before dockerfileGen
            workflowContext.addAttribute(WorkflowConstants.CLEAN_UP_SERVICE_DIR, true);

            // TODO Handle invoker delta processing through callback
            isServiceFailed = isServiceFailed ? isServiceFailed : !executeInvoker(dockerfileGeneratorComponentInvoker, workflowContext);
            
            isServiceFailed = isServiceFailed ? isServiceFailed : !executeInvoker(imageBuildComponentInvoker, workflowContext);
            
            isServiceFailed = isServiceFailed ? isServiceFailed : !executeInvoker(manifestGeneratorComponentInvoker, workflowContext);
            
            if (!isServiceFailed) {
                List<Manifest> manifestList = (List<Manifest>) workflowContext.getAttribute(WorkflowConstants.OUTPUT);
                workflowContext.addAttribute(WorkflowConstants.GENERATED_MANIFESTS, manifestList);
                isServiceFailed = isServiceFailed ? isServiceFailed : !executeInvoker(deployComponentInvoker, workflowContext);
            }
            logWorkflowInfo(workflowContext);
            isCommandFailed = isCommandFailed ? isCommandFailed : isServiceFailed;
        }
        if (!serviceProfileMap.isEmpty()) {
            ServiceProfileUtil.printWarnMsg(serviceProfileMap);
        }
        return isCommandFailed ? ToolConstants.HYSCALE_ERROR_CODE : 0;
    }
    
    private boolean executeInvoker(ComponentInvoker invoker, WorkflowContext context) {
        try {
            invoker.execute(context);
        } catch (HyscaleException e) {
            logger.error("Error while executing component invoker: {}, for app: {}, service: {}", 
                    invoker.getClass(), appName, context.getServiceName(), e);
            return false;
        }
        return context.isFailed() ? false : true;
    }

    private void logWorkflowInfo(WorkflowContext workflowContext) {
        WorkflowLogger.header(ControllerActivity.INFORMATION);

        WorkflowLogger.logPersistedActivities();

        long startTime = (long) workflowContext.getAttribute(WorkflowConstants.DEPLOY_START_TIME);
        CommandUtil.logMetaInfo(String.valueOf((System.currentTimeMillis() - startTime) / 1000) + "s", ControllerActivity.TOTAL_TIME);
        CommandUtil.logMetaInfo(SetupConfig.getMountPathOf((String) workflowContext.getAttribute(WorkflowConstants.DOCKERFILE_INPUT)),
                ControllerActivity.DOCKERFILE_PATH);
        CommandUtil.logMetaInfo(SetupConfig.getMountPathOf((String) workflowContext.getAttribute(WorkflowConstants.BUILD_LOGS)),
                ControllerActivity.BUILD_LOGS);
        CommandUtil.logMetaInfo(SetupConfig.getMountPathOf((String) workflowContext.getAttribute(WorkflowConstants.PUSH_LOGS)),
                ControllerActivity.PUSH_LOGS);
        CommandUtil.logMetaInfo(SetupConfig.getMountPathOf((String) workflowContext.getAttribute(WorkflowConstants.MANIFESTS_PATH)),
                ControllerActivity.MANIFESTS_GENERATION_PATH);
        CommandUtil.logMetaInfo(SetupConfig.getMountPathOf((String) workflowContext.getAttribute(WorkflowConstants.DEPLOY_LOGS)),
                ControllerActivity.DEPLOY_LOGS_AT);
        WorkflowLogger.footer();
        CommandUtil.logMetaInfo((String) workflowContext.getAttribute(WorkflowConstants.SERVICE_IP),
                ControllerActivity.SERVICE_URL);
    }


    @PreDestroy
    public void clear() {
        SetupConfig.clearAbsolutePath();
    }
}
