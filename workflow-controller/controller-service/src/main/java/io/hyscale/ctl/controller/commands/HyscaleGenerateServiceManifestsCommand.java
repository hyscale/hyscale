package io.hyscale.ctl.controller.commands;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.hyscale.ctl.commons.config.SetupConfig;
import io.hyscale.ctl.commons.exception.HyscaleException;
import io.hyscale.ctl.commons.logger.WorkflowLogger;
import io.hyscale.ctl.controller.activity.ControllerActivity;
import io.hyscale.ctl.controller.constants.WorkflowConstants;
import io.hyscale.ctl.controller.model.WorkflowContext;
import io.hyscale.ctl.controller.invoker.ManifestGeneratorComponentInvoker;
import io.hyscale.ctl.controller.util.CommandUtil;
import io.hyscale.ctl.controller.util.ServiceSpecMapper;
import io.hyscale.ctl.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.ctl.servicespec.commons.model.service.ServiceSpec;
import picocli.CommandLine;

import javax.annotation.PreDestroy;

/**
 * Command to generate Manifest from service specs
 *
 */
@CommandLine.Command(name = "manifests", aliases = {"manifest"},
        description = {"Generates manifests from the given service specs"})
@Component
public class HyscaleGenerateServiceManifestsCommand implements Runnable {

    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "Display help message about the specified command")
    private boolean helpRequested = false;

    @CommandLine.Option(names = {"-a", "--app"}, required = true, description = "Application name")
    private String appName;

    @CommandLine.Option(names = {"-f", "--files"}, required = true, description = "Service specs files.", split = ",")
    private String[] serviceSpecs;

    @Autowired
    private ManifestGeneratorComponentInvoker manifestGeneratorComponentInvoker;

    @Override
    public void run() {

        for (int i = 0; i < serviceSpecs.length; i++) {

            WorkflowContext workflowContext = new WorkflowContext();
            String serviceName = null;
            try {
                File serviceSpecFile = new File(serviceSpecs[i]);
                ServiceSpec serviceSpec = ServiceSpecMapper.from(serviceSpecFile);
                serviceName = serviceSpec.get(HyscaleSpecFields.name, String.class);
                workflowContext.setServiceSpec(serviceSpec);
                workflowContext.setServiceName(serviceName);

                SetupConfig.clearAbsolutePath();
                SetupConfig.setAbsolutePath(serviceSpecFile.getAbsoluteFile().getParent());

            } catch (HyscaleException e) {
                WorkflowLogger.error(ControllerActivity.CANNOT_PROCESS_SERVICE_SPEC, e.getMessage());
                return;
            }

            workflowContext.setAppName(appName.trim());
            workflowContext.setEnvName(CommandUtil.getEnvName(null, appName.trim()));

            if (!workflowContext.isFailed()) {
                manifestGeneratorComponentInvoker.execute(workflowContext);
            }
            WorkflowLogger.footer();
            CommandUtil.logMetaInfo(SetupConfig.getMountPathOf((String) workflowContext.getAttribute(WorkflowConstants.MANIFESTS_PATH)),
                    ControllerActivity.MANIFESTS_GENERATION_PATH);
        }

    }

    @PreDestroy
    public void clear() {
        SetupConfig.clearAbsolutePath();
    }
}
