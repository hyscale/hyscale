package io.hyscale.ctl.controller.invoker;

import io.hyscale.ctl.commons.logger.WorkflowLogger;
import io.hyscale.ctl.commons.models.Manifest;
import io.hyscale.ctl.commons.models.ManifestContext;
import io.hyscale.ctl.controller.activity.ControllerActivity;
import io.hyscale.ctl.controller.constants.WorkflowConstants;
import io.hyscale.ctl.controller.core.exception.ControllerErrorCodes;
import io.hyscale.ctl.controller.manager.RegistryManager;
import io.hyscale.ctl.controller.plugins.ManifestCleanUpPlugin;
import io.hyscale.ctl.controller.plugins.ManifestValidatorPlugin;
import io.hyscale.ctl.generator.services.config.ManifestConfig;
import io.hyscale.ctl.generator.services.constants.ManifestGenConstants;
import io.hyscale.ctl.generator.services.exception.ManifestErrorCodes;
import io.hyscale.ctl.generator.services.generator.ManifestGenerator;
import io.hyscale.ctl.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.ctl.servicespec.commons.model.service.ServiceSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.hyscale.ctl.commons.component.ComponentInvoker;
import io.hyscale.ctl.commons.exception.HyscaleException;
import io.hyscale.ctl.controller.model.WorkflowContext;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 *	Manifest generator component
 *	acts as a bridge between workflow controller and manifest generator to generate K8s manifests
 *	provides link between {@link WorkflowContext} and {@link ManifestContext}
 */
@Component
public class ManifestGeneratorComponentInvoker extends ComponentInvoker<WorkflowContext> {

    private static final Logger logger = LoggerFactory.getLogger(ManifestGeneratorComponentInvoker.class);

    @Autowired
    private ManifestGenerator manifestGenerator;

    @Autowired
    private RegistryManager registryManager;

    @Autowired
    private ManifestConfig manifestConfig;

    @Autowired
    private ManifestCleanUpPlugin cleanUpPlugin;

    @Autowired
    private ManifestValidatorPlugin manifestValidatorPlugin;

    @PostConstruct
    public void init() {
        super.addPlugin(manifestValidatorPlugin);
        super.addPlugin(cleanUpPlugin);
    }

    protected void doExecute(WorkflowContext context) throws HyscaleException {
        if (context == null || context.isFailed()) {
            return;
        }
        ServiceSpec serviceSpec = context.getServiceSpec();
        if (serviceSpec == null) {
            context.setFailed(true);
            logger.error(" Cannot generate manifests with empty service specs ");
            throw new HyscaleException(ControllerErrorCodes.SERVICE_SPEC_REQUIRED);
        }
        String serviceName = serviceSpec.get(HyscaleSpecFields.name, String.class);

        String registryUrl = serviceSpec
                .get(HyscaleSpecFields.getPath(HyscaleSpecFields.image, HyscaleSpecFields.registry), String.class);
        WorkflowLogger.header(ControllerActivity.STARTING_MANIFEST_GENERATION);
        ManifestContext manifestContext = new ManifestContext();
        manifestContext.setAppName(context.getAppName());
        manifestContext.setEnvName(context.getEnvName());
        manifestContext.setNamespace(context.getNamespace());
        manifestContext.setImageRegistry(registryManager.getImageRegistry(registryUrl));
        manifestContext.addGenerationAttribute(ManifestGenConstants.IMAGE_SHA_SUM, context.getAttribute(WorkflowConstants.IMAGE_SHA_SUM));

        List<Manifest> manifestList = null;
        try {
            manifestList = manifestGenerator.generate(serviceSpec, manifestContext);
        } catch (HyscaleException e) {
            context.setFailed(true);
            WorkflowLogger.header(ControllerActivity.MANIFEST_GENERATION_FAILED, e.getMessage());
            throw e;
        } finally {
            context.addAttribute(WorkflowConstants.MANIFESTS_PATH,
                    manifestConfig.getManifestDir(context.getAppName(), serviceName));
        }
        context.addAttribute(WorkflowConstants.GENERATED_MANIFESTS, manifestList);
        context.addAttribute(WorkflowConstants.OUTPUT, manifestList);
    }

    @Override
    protected void onError(WorkflowContext context, HyscaleException he) {
        WorkflowLogger.header(ControllerActivity.ERROR);
        WorkflowLogger.error(ControllerActivity.CAUSE, he != null ?
                he.getMessage() : ManifestErrorCodes.ERROR_WHILE_CREATING_MANIFEST.getErrorMessage());
        context.setFailed(true);
    }
}
