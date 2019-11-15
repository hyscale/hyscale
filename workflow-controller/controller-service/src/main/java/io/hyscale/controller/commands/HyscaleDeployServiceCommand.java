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
import java.util.List;

import io.hyscale.commons.config.SetupConfig;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.controller.constants.WorkflowConstants;
import io.hyscale.controller.invoker.DockerfileGeneratorComponentInvoker;
import io.hyscale.controller.model.WorkflowContext;
import io.hyscale.controller.util.CommandUtil;
import io.hyscale.controller.util.ServiceSpecMapper;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
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

/**
 *  This class executes 'hyscale deploy service' command
 *  It is a sub-command of the 'hyscale deploy' command
 *  @see HyscaleDeployCommand
 *  Every command/sub-command has to implement the Runnable so that
 *  whenever the command is executed the {@link #run()}
 *  method will be invoked
 *
 * @option namespace  name of the namespace in which the service to be deployed
 * @option appName   name of the app to logically group your services
 * @option serviceSpecs   list of service specs that are to be deployed
 * @option verbose  prints the verbose output of the deployment
 *
 *  Eg: hyscale deploy service -f s1.hspec.yaml -f s2.hspec.yaml -n dev -a sample
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
public class HyscaleDeployServiceCommand implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(HyscaleDeployCommand.class);

    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "Displays the help information of the specified command")
    private boolean helpRequested = false;

    @CommandLine.Option(names = {"-n", "--namespace", "-ns"}, required = true, description = "Namespace of the service ")
    private String namespace;

    @CommandLine.Option(names = {"-a", "--app"}, required = true, description = "Application name")
    private String appName;

    @CommandLine.Option(names = {"-v", "--verbose", "-verbose"}, required = false, description = "Verbose output")
    private boolean verbose = false;

    @CommandLine.Option(names = {"-f", "--file", "-file"}, required = true, description = "Service spec")
    private String[] serviceSpecs;

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
    public void run() {
        for (int i = 0; i < serviceSpecs.length; i++) {

            WorkflowContext workflowContext = new WorkflowContext();
            workflowContext.addAttribute(WorkflowConstants.DEPLOY_START_TIME, System.currentTimeMillis());
            String serviceName = null;
            try {
                File serviceSpecFile = new File(serviceSpecs[i]);
                ServiceSpec serviceSpec = serviceSpecMapper.from(serviceSpecFile);
                workflowContext.setServiceSpec(serviceSpec);
                serviceName = serviceSpec.get(HyscaleSpecFields.name, String.class);
                workflowContext.setServiceName(serviceName);
                SetupConfig.clearAbsolutePath();
                SetupConfig.setAbsolutePath(serviceSpecFile.getAbsoluteFile().getParent());
            } catch (HyscaleException e) {
                WorkflowLogger.error(ControllerActivity.CANNOT_PROCESS_SERVICE_SPEC, e.getMessage());
                return;
            }

            WorkflowLogger.header(ControllerActivity.SERVICE_NAME, serviceName);
            workflowContext.setAppName(appName.trim());
            workflowContext.setNamespace(namespace.trim());
            workflowContext.setEnvName(CommandUtil.getEnvName(null, appName.trim()));
            workflowContext.addAttribute(WorkflowConstants.VERBOSE, verbose);

            // clean up service dir before dockerfileGen
            workflowContext.addAttribute(WorkflowConstants.CLEAN_UP_SERVICE_DIR, true);

            // Generate Docker file
            dockerfileGeneratorComponentInvoker.execute(workflowContext);

            if (!workflowContext.isFailed()) {
                imageBuildComponentInvoker.execute(workflowContext);
            }

            if (!workflowContext.isFailed()) {
                manifestGeneratorComponentInvoker.execute(workflowContext);
            }

            if (!workflowContext.isFailed()) {
                List<Manifest> manifestList = (List<Manifest>) workflowContext.getAttribute(WorkflowConstants.OUTPUT);
                workflowContext.addAttribute(WorkflowConstants.GENERATED_MANIFESTS, manifestList);
                deployComponentInvoker.execute(workflowContext);
            }

            logWorkflowInfo(workflowContext);

        }
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
