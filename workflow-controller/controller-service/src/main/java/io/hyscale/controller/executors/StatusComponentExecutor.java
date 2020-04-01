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
package io.hyscale.controller.executors;

import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.hyscale.commons.component.ProcessExecutor;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.models.DeploymentContext;
import io.hyscale.commons.models.K8sAuthorisation;
import io.hyscale.controller.builder.K8sAuthConfigBuilder;
import io.hyscale.controller.constants.WorkflowConstants;
import io.hyscale.controller.model.WorkflowContext;
import io.hyscale.controller.processors.K8SClusterValidatorProcessor;
import io.hyscale.controller.util.TroubleshootUtil;
import io.hyscale.deployer.core.model.DeploymentStatus;
import io.hyscale.deployer.services.deployer.Deployer;
import io.hyscale.troubleshooting.integration.models.DiagnosisReport;
import io.hyscale.troubleshooting.integration.models.ServiceInfo;
import io.hyscale.troubleshooting.integration.service.TroubleshootService;

/**
 * Status component acts as a bridge between workflow controller and deployer for status operation
 * provides link between {@link WorkflowContext} and {@link DeploymentContext}
 * Responsible for calling troubleshooting in case service is in Not running state
 * @author tushar
 *
 */
@Component
public class StatusComponentExecutor extends ProcessExecutor<WorkflowContext> {
    
    private static final Logger logger = LoggerFactory.getLogger(StatusComponentExecutor.class);

    @Autowired
    private Deployer deployer;
    
    @Autowired
    private K8sAuthConfigBuilder authConfigBuilder;
    
    @Autowired
    private TroubleshootService troubleshootService;
    
    @Autowired
    private K8SClusterValidatorProcessor k8SClusterValidatorHook;

    @PostConstruct
    public void init() {
        super.addProcessor(k8SClusterValidatorHook);
    }
    
    @Override
    protected void doExecute(WorkflowContext context) throws HyscaleException {
        String serviceName = context.getServiceName();
        
        DeploymentContext deploymentContext = new DeploymentContext();
        deploymentContext.setAuthConfig(authConfigBuilder.getAuthConfig());
        deploymentContext.setNamespace(context.getNamespace());
        deploymentContext.setAppName(context.getAppName());
        deploymentContext.setServiceName(serviceName);
        deploymentContext.setWaitForReadiness(false);
        
        if (StringUtils.isNotBlank(serviceName)) {
            // Service status command
            DeploymentStatus serviceStatus = deployer.getServiceDeploymentStatus(deploymentContext);
            if (serviceStatus != null) {
                serviceStatus.setMessage(getServiceMessage(serviceStatus, deploymentContext));
            }
            context.addAttribute(WorkflowConstants.DEPLOYMENT_STATUS, serviceStatus);
            return;
        }
        // App status command
        List<DeploymentStatus> deploymentStatusList = deployer.getDeploymentStatus(deploymentContext);
        if (deploymentStatusList != null) {
            for (DeploymentStatus serviceStatus : deploymentStatusList) {
                if (serviceStatus != null) {
                    serviceStatus.setMessage(getServiceMessage(serviceStatus, deploymentContext));
                }
            }
            context.addAttribute(WorkflowConstants.DEPLOYMENT_STATUS_LIST, deploymentStatusList);
        }
        
    }
    
    private String getServiceMessage(DeploymentStatus serviceStatus, DeploymentContext context) {
        if (serviceStatus == null) {
            return null;
        }
        String message = null;
        /*
         * Fetch service name from service status as
         * context can have the previous service name in case of app deploy
         */
        context.setServiceName(serviceStatus.getServiceName());
        if (!DeploymentStatus.Status.RUNNING.equals(serviceStatus.getStatus())) {
            List<DiagnosisReport> diagnosisReports = troubleshoot(context);
            message = TroubleshootUtil.getTroubleshootMessage(diagnosisReports);
        }
        
        return message;
    }
    
    private List<DiagnosisReport> troubleshoot(DeploymentContext deploymentContext) {
        ServiceInfo serviceInfo = new ServiceInfo();
        serviceInfo.setAppName(deploymentContext.getAppName());
        serviceInfo.setServiceName(deploymentContext.getServiceName());
        try {
            return troubleshootService.troubleshoot(serviceInfo, (K8sAuthorisation) deploymentContext.getAuthConfig(), 
                    deploymentContext.getNamespace());
        } catch (HyscaleException e) {
            logger.error("Error while executing troubleshooot serice {}", e);
        }
        return null;
    }
    
    @Override
    protected void onError(WorkflowContext context, HyscaleException th) throws HyscaleException {
        if (th != null) {
            logger.error("Error while getting status", th.getMessage());
            throw th;
        }
    }

}
