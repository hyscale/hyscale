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

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;

import io.hyscale.commons.models.*;
import io.hyscale.troubleshooting.integration.models.DiagnosisReport;
import io.hyscale.troubleshooting.integration.service.TroubleshootService;

import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;

import io.hyscale.commons.component.ComponentInvoker;
import io.hyscale.commons.constants.ToolConstants;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.io.LogProcessor;
import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.controller.activity.ControllerActivity;
import io.hyscale.controller.builder.DeploymentContextBuilder;
import io.hyscale.controller.constants.WorkflowConstants;
import io.hyscale.controller.exception.ControllerErrorCodes;
import io.hyscale.controller.hooks.K8SResourcesCleanUpHook;
import io.hyscale.controller.hooks.VolumeCleanUpHook;
import io.hyscale.controller.model.WorkflowContext;
import io.hyscale.controller.util.TroubleshootUtil;
import io.hyscale.deployer.services.config.DeployerConfig;
import io.hyscale.deployer.services.deployer.Deployer;
import io.hyscale.deployer.services.exception.DeployerErrorCodes;
import io.hyscale.deployer.services.model.ServiceAddress;
import io.hyscale.servicespec.commons.exception.ServiceSpecErrorCodes;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.Port;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;

/**
 * Deployer component acts as a bridge between workflow controller and deployer for deploy operation
 * provides link between {@link WorkflowContext} and {@link DeploymentContext}
 */
@Component
public class DeployComponentInvoker extends ComponentInvoker<WorkflowContext> {

    private static final Logger logger = LoggerFactory.getLogger(DeployComponentInvoker.class);

    @Autowired
    private Deployer deployer;

    @Autowired
    private DeploymentContextBuilder deploymentContextBuilder;

    @Autowired
    private DeployerConfig deployerConfig;

    @Autowired
    private LogProcessor logProcessor;

    @Autowired
    private K8SResourcesCleanUpHook k8sResourcesCleanUpHook;

    @Autowired
    private VolumeCleanUpHook volumeCleanUpHook;

    @Autowired
    private TroubleshootService troubleshootService;

    @PostConstruct
    public void init() {
        super.addHook(k8sResourcesCleanUpHook);
        super.addHook(volumeCleanUpHook);
    }

    /**
     * Deploys the service to the kubernetes cluster
     * <p>
     * 1. Deploying the service to the cluster
     * 2. Wait for deployment completion
     * 3. Get service address from the cluster if externalized
     */
    @Override
    protected void doExecute(WorkflowContext context) throws HyscaleException {
        if (context == null || context.isFailed()) {
            return;
        }
        DeploymentContext deploymentContext = deploymentContextBuilder.build(context);

        ServiceSpec serviceSpec = context.getServiceSpec();
        if (serviceSpec == null) {
            context.setFailed(true);
            logger.error("Service Spec is required for deployment");
            throw new HyscaleException(ServiceSpecErrorCodes.SERVICE_SPEC_REQUIRED);
        }

        List<Manifest> mainfestList = deploymentContext.getManifests();
        if (mainfestList == null || mainfestList.isEmpty()) {
            context.setFailed(true);
            logger.error("Manifest is required for deployment");
            throw new HyscaleException(ControllerErrorCodes.MANIFEST_REQUIRED);
        }

        String serviceName = deploymentContext.getServiceName();

        /*
         * Deploys and waits for the deployment completion
         */

        try {
            deployer.deploy(deploymentContext);
            deployer.waitForDeployment(deploymentContext);

        } catch (HyscaleException e) {
            logger.error("Deployment failed with error: {}, running troubleshoot", e.toString());
            String troubleshootMessage = TroubleshootUtil.getTroubleshootMessage(troubleshoot(deploymentContext));
            context.addAttribute(WorkflowConstants.TROUBLESHOOT_MESSAGE, troubleshootMessage);
            throw e;
        } finally {
            writeDeployLogs(context, deploymentContext);
        }
        context.addAttribute(WorkflowConstants.OUTPUT, true);

        boolean external = BooleanUtils.toBoolean(serviceSpec.get(HyscaleSpecFields.external, Boolean.class));
        logger.debug("Checking whether service {} is external {}", serviceName, external);
        if (external) {
            TypeReference<List<Port>> typeReference = new TypeReference<List<Port>>() {
            };
            List<Port> servicePorts = serviceSpec.get(HyscaleSpecFields.ports, typeReference);
            if (servicePorts != null) {
                try {
                    ServiceAddress serviceAddress = deployer.getServiceAddress(deploymentContext);
                    if (serviceAddress != null) {
                        context.addAttribute(WorkflowConstants.SERVICE_IP, serviceAddress.toString());
                        context.addAttribute(WorkflowConstants.SERVICE_URL, serviceAddress.getServiceURL());
                    }
                } catch (HyscaleException e) {
                    logger.error("Error while getting service IP address {}, running troubleshoot", e.getMessage());
                    String troubleshootMessage = TroubleshootUtil.getTroubleshootMessage(troubleshoot(deploymentContext));
                    context.addAttribute(WorkflowConstants.TROUBLESHOOT_MESSAGE, troubleshootMessage);
                    throw e;
                }
            }
        }
    }

    private List<DiagnosisReport> troubleshoot(DeploymentContext deploymentContext) {
        ServiceMetadata serviceMetadata = new ServiceMetadata();
        serviceMetadata.setAppName(deploymentContext.getAppName());
        serviceMetadata.setServiceName(deploymentContext.getServiceName());
        try {
            return troubleshootService.troubleshoot(serviceMetadata, (K8sAuthorisation) deploymentContext.getAuthConfig(), deploymentContext.getNamespace());
        } catch (HyscaleException e) {
            logger.error("Error while executing troubleshooot serice {}",deploymentContext.getServiceName(), e);
        }
        return Collections.emptyList();
    }

    /**
     * Write deployment logs to file for later access
     *
     * @param context
     * @param deploymentContext
     */
    private void writeDeployLogs(WorkflowContext context, DeploymentContext deploymentContext) {
        String serviceName = deploymentContext.getServiceName();
        try (InputStream is = deployer.logs(deploymentContext.getAuthConfig(), serviceName,
                deploymentContext.getNamespace(), null, serviceName, deploymentContext.getReadLines(), deploymentContext.isTailLogs())) {
            String deploylogFile = deployerConfig.getDeployLogDir(deploymentContext.getAppName(),
                    serviceName);
            logProcessor.writeLogFile(is, deploylogFile);
            context.addAttribute(WorkflowConstants.DEPLOY_LOGS,
                    deployerConfig.getDeployLogDir(deploymentContext.getAppName(), deploymentContext.getServiceName()));
        } catch (IOException e) {
            logger.error("Failed to get deploy logs {}", deploymentContext.getServiceName(), e);
        } catch (HyscaleException ex) {
            logger.error("Failed to get deploy logs {}", deploymentContext.getServiceName(), ex);
        }
    }

    @Override
    protected void onError(WorkflowContext context, HyscaleException he) throws HyscaleException {
        WorkflowLogger.header(ControllerActivity.ERROR);
        Object troubleshootMsgObj = context.getAttribute(WorkflowConstants.TROUBLESHOOT_MESSAGE);
        StringBuilder errorMessage = new StringBuilder();
        errorMessage.append(he != null ? he.getMessage() : DeployerErrorCodes.FAILED_TO_APPLY_MANIFEST.getMessage());
        if (troubleshootMsgObj != null) {
            String troubleshootMessage = (String) troubleshootMsgObj;
            logger.error("Troubleshoot message: {}", troubleshootMessage);
            errorMessage.append(ToolConstants.NEW_LINE).append(troubleshootMessage);
        }
        WorkflowLogger.error(ControllerActivity.TROUBLESHOOT, errorMessage.toString());
        context.addAttribute(WorkflowConstants.ERROR_MESSAGE, errorMessage.toString());
        context.setFailed(true);
        if (he != null) {
            throw he;
        }
    }
}
