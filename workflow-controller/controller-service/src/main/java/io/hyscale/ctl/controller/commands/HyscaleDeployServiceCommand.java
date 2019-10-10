package io.hyscale.ctl.controller.commands;

import java.io.File;
import java.util.List;

import io.hyscale.ctl.commons.config.SetupConfig;
import io.hyscale.ctl.commons.exception.HyscaleException;
import io.hyscale.ctl.controller.util.ServiceSpecMapper;
import io.hyscale.ctl.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.ctl.servicespec.commons.model.service.ServiceSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.hyscale.ctl.commons.logger.WorkflowLogger;
import io.hyscale.ctl.commons.models.Manifest;
import io.hyscale.ctl.controller.activity.ControllerActivity;
import io.hyscale.ctl.controller.constants.WorkflowConstants;
import io.hyscale.ctl.controller.model.WorkflowContext;
import io.hyscale.ctl.controller.invoker.DeployComponentInvoker;
import io.hyscale.ctl.controller.invoker.DockerfileGeneratorComponentInvoker;
import io.hyscale.ctl.controller.invoker.ImageBuildComponentInvoker;
import io.hyscale.ctl.controller.invoker.ManifestGeneratorComponentInvoker;
import io.hyscale.ctl.controller.util.CommandUtil;
import picocli.CommandLine;

import javax.annotation.PreDestroy;

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

    @Override
    public void run() {
        for (int i = 0; i < serviceSpecs.length; i++) {

            WorkflowContext workflowContext = new WorkflowContext();
            workflowContext.addAttribute(WorkflowConstants.DEPLOY_START_TIME, System.currentTimeMillis());
            String serviceName = null;
            try {
                File serviceSpecFile = new File(serviceSpecs[i]);
                ServiceSpec serviceSpec = ServiceSpecMapper.from(serviceSpecFile);
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
