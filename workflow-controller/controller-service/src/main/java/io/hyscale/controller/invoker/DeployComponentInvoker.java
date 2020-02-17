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
import java.util.List;

import javax.annotation.PostConstruct;

import io.hyscale.commons.models.K8sAuthorisation;
import io.hyscale.troubleshooting.integration.models.DiagnosisReport;
import io.hyscale.troubleshooting.integration.models.ServiceInfo;
import io.hyscale.troubleshooting.integration.service.TroubleshootService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;

import io.hyscale.commons.component.ComponentInvoker;
import io.hyscale.commons.constants.ToolConstants;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.commons.models.DeploymentContext;
import io.hyscale.commons.models.Manifest;
import io.hyscale.commons.utils.LogProcessor;
import io.hyscale.controller.activity.ControllerActivity;
import io.hyscale.controller.builder.K8sAuthConfigBuilder;
import io.hyscale.controller.constants.WorkflowConstants;
import io.hyscale.controller.exception.ControllerErrorCodes;
import io.hyscale.controller.hooks.K8SResourcesCleanUpHook;
import io.hyscale.controller.hooks.VolumeCleanUpHook;
import io.hyscale.controller.hooks.VolumeValidatorHook;
import io.hyscale.controller.model.WorkflowContext;
import io.hyscale.controller.util.LoggerUtility;
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
    private LoggerUtility loggerUtility;

    @Autowired
    private K8sAuthConfigBuilder authConfigBuilder;

    @Autowired
    private DeployerConfig deployerConfig;

    @Autowired
    private LogProcessor logProcessor;

    @Autowired
    private K8SResourcesCleanUpHook k8sResourcesCleanUpHook;

    @Autowired
    private VolumeCleanUpHook volumeCleanUpHook;

    @Autowired
    private VolumeValidatorHook volumeValidatorHook;

    @Autowired
    private TroubleshootService troubleshootService;

    @PostConstruct
    public void init() {
        super.addHook(volumeValidatorHook);
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

        ServiceSpec serviceSpec = context.getServiceSpec();
        if (serviceSpec == null) {
            context.setFailed(true);
            logger.error("Service Spec is required for deployment");
            throw new HyscaleException(ServiceSpecErrorCodes.SERVICE_SPEC_REQUIRED);
        }

        List<Manifest> mainfestList = (List<Manifest>) context.getAttribute(WorkflowConstants.GENERATED_MANIFESTS);
        if (mainfestList == null || mainfestList.isEmpty()) {
            context.setFailed(true);
            logger.error("Manifest is required for deployment");
            throw new HyscaleException(ControllerErrorCodes.MANIFEST_REQUIRED);
        }
        Boolean verbose = (Boolean) context.getAttribute(WorkflowConstants.VERBOSE);

        verbose = (verbose != null) ? verbose : false;

        DeploymentContext deploymentContext = new DeploymentContext();
        deploymentContext.setAuthConfig(authConfigBuilder.getAuthConfig());
        deploymentContext.setNamespace(context.getNamespace());
        deploymentContext.setAppName(context.getAppName());
        deploymentContext.setManifests(mainfestList);

        String serviceName;
        try {
            serviceName = serviceSpec.get(HyscaleSpecFields.name, String.class);
        } catch (HyscaleException e) {
            logger.error("Failed to get service name, error {}", e.toString());
            return;
        }

        deploymentContext.setServiceName(serviceName);
        context.setServiceName(serviceName);
        WorkflowLogger.header(ControllerActivity.STARTING_DEPLOYMENT);
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
        if (verbose) {
            loggerUtility.deploymentLogs(context);
        }

        Boolean external = serviceSpec.get(HyscaleSpecFields.external, Boolean.class);
        external = external == null ? false : external;
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
        ServiceInfo serviceInfo = new ServiceInfo();
        serviceInfo.setAppName(deploymentContext.getAppName());
        serviceInfo.setServiceName(deploymentContext.getServiceName());
        try {
            return troubleshootService.troubleshoot(serviceInfo, (K8sAuthorisation) deploymentContext.getAuthConfig(), deploymentContext.getNamespace());
        } catch (HyscaleException e) {
            logger.error("Error while executing troubleshooot serice {}", e);
        }
        return null;
    }
        
    /**
     * Write deployment logs to file for later access
     *
     * @param context
     * @param deploymentContext
     */
    private void writeDeployLogs(WorkflowContext context, DeploymentContext deploymentContext) {
        try (InputStream is = deployer.logs(deploymentContext)) {
            String deploylogFile = deployerConfig.getDeployLogDir(deploymentContext.getAppName(),
                    deploymentContext.getServiceName());
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
        errorMessage.append(he != null ? he.getMessage() : DeployerErrorCodes.FAILED_TO_APPLY_MANIFEST.getErrorMessage());
        if (troubleshootMsgObj != null) {
            String troubleshootMessage = (String)troubleshootMsgObj;
            logger.error("Troubleshoot message: {}", troubleshootMessage);
            errorMessage.append(ToolConstants.NEW_LINE).append(troubleshootMessage);
        }
        WorkflowLogger.error(ControllerActivity.TROUBLESHOOT, errorMessage.toString());
        context.setFailed(true);
        if (he != null) {
            throw he;
        }
    }
}
