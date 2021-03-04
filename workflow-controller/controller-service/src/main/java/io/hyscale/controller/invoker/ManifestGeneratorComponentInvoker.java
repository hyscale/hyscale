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
package io.hyscale.controller.invoker;

import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.commons.models.Manifest;
import io.hyscale.commons.models.ManifestContext;
import io.hyscale.controller.activity.ControllerActivity;
import io.hyscale.controller.builder.ManifestContextBuilder;
import io.hyscale.controller.constants.WorkflowConstants;
import io.hyscale.controller.model.WorkflowContext;
import io.hyscale.controller.hooks.ManifestCleanUpHook;
import io.hyscale.generator.services.config.ManifestConfig;
import io.hyscale.generator.services.exception.ManifestErrorCodes;
import io.hyscale.generator.services.generator.ManifestGenerator;
import io.hyscale.servicespec.commons.exception.ServiceSpecErrorCodes;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.hyscale.commons.component.ComponentInvoker;
import io.hyscale.commons.exception.HyscaleException;

import javax.annotation.PostConstruct;
import java.util.List;

/**
 *	Manifest generator component acts as a bridge between workflow controller and manifest generator
 *	to generate K8s manifests provides link between {@link WorkflowContext} and {@link ManifestContext}
 */
@Component
public class ManifestGeneratorComponentInvoker extends ComponentInvoker<WorkflowContext> {

    private static final Logger logger = LoggerFactory.getLogger(ManifestGeneratorComponentInvoker.class);

    @Autowired
    private ManifestGenerator manifestGenerator;

    @Autowired
    private ManifestContextBuilder manifestContextBuilder;

    @Autowired
    private ManifestConfig manifestConfig;

    @Autowired
    private ManifestCleanUpHook manifestCleanUpHook;

    @PostConstruct
    public void init() {
        super.addHook(manifestCleanUpHook);
    }

    protected void doExecute(WorkflowContext context) throws HyscaleException {
        if (context == null || context.isFailed()) {
            return;
        }
        ServiceSpec serviceSpec = context.getServiceSpec();
        if (serviceSpec == null) {
            context.setFailed(true);
            logger.error("Cannot generate manifests for empty service spec.");
            throw new HyscaleException(ServiceSpecErrorCodes.SERVICE_SPEC_REQUIRED);
        }
        String serviceName = context.getServiceName();

        WorkflowLogger.header(ControllerActivity.STARTING_MANIFEST_GENERATION);
        ManifestContext manifestContext = manifestContextBuilder.build(context);

        List<Manifest> manifestList = null;
        try {
            manifestList = manifestGenerator.generate(serviceSpec, manifestContext);
        } catch (HyscaleException e) {
            logger.error("Error while generating manifest for service: {}", serviceName, e);
            context.setFailed(true);
            throw e;
        } finally {
            context.addAttribute(WorkflowConstants.MANIFESTS_PATH,
                    manifestConfig.getManifestDir(context.getAppName(), serviceName));
        }
        context.addAttribute(WorkflowConstants.GENERATED_MANIFESTS, manifestList);
        context.addAttribute(WorkflowConstants.OUTPUT, manifestList);
    }

    @Override
    protected void onError(WorkflowContext context, HyscaleException he) throws HyscaleException {
        WorkflowLogger.header(ControllerActivity.ERROR);
        WorkflowLogger.error(ControllerActivity.CAUSE, he != null ?
                he.getMessage() : ManifestErrorCodes.ERROR_WHILE_CREATING_MANIFEST.getMessage());
        context.addAttribute(WorkflowConstants.ERROR_MESSAGE, (he != null) ? he.getMessage() : ManifestErrorCodes.ERROR_WHILE_CREATING_MANIFEST.getMessage());
        context.setFailed(true);
        if (he != null) {
            throw he;
        }
    }
}
