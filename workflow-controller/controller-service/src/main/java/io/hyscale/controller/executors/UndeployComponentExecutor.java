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

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.hyscale.commons.component.ProcessExecutor;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.commons.models.DeploymentContext;
import io.hyscale.controller.activity.ControllerActivity;
import io.hyscale.controller.builder.K8sAuthConfigBuilder;
import io.hyscale.controller.exception.ControllerErrorCodes;
import io.hyscale.controller.model.WorkflowContext;
import io.hyscale.controller.processors.AppDirCleanUpProcessor;
import io.hyscale.controller.processors.ServiceDirCleanUpProcessor;
import io.hyscale.controller.processors.StaleVolumeDetailsProcessor;
import io.hyscale.deployer.services.deployer.Deployer;

/**
 *	Undeploy component acts as a bridge between workflow controller and deployer for undeploy operation
 *	provides link between {@link WorkflowContext} and {@link DeploymentContext}
 */
@Component
public class UndeployComponentExecutor extends ProcessExecutor<WorkflowContext> {

    private static final Logger logger = LoggerFactory.getLogger(UndeployComponentExecutor.class);

    @Autowired
    private Deployer deployer;

    @Autowired
    private K8sAuthConfigBuilder authConfigBuilder;

    @Autowired
    private ServiceDirCleanUpProcessor serviceDirCleanUpHook;

    @Autowired
    private AppDirCleanUpProcessor appDirCleanUpHook;
    
    @Autowired
    private StaleVolumeDetailsProcessor staleVolumeDetailsHook;

    @PostConstruct
    public void init() {
        addProcessor(serviceDirCleanUpHook);
        addProcessor(appDirCleanUpHook);
        addProcessor(staleVolumeDetailsHook);
    }
    
    @Override
    protected void doExecute(WorkflowContext context) throws HyscaleException {
        if (context == null) {
            return;
        }
        String namespace = context.getNamespace();
        String appName = context.getAppName();
        String serviceName = context.getServiceName();
        
        WorkflowLogger.header(ControllerActivity.STARTING_UNDEPLOYMENT);
        DeploymentContext deploymentContext = new DeploymentContext();
        deploymentContext.setAuthConfig(authConfigBuilder.getAuthConfig());
        deploymentContext.setNamespace(namespace);
        deploymentContext.setAppName(appName);
        deploymentContext.setServiceName(serviceName);

        try {
            deployer.unDeploy(deploymentContext);
        } catch (HyscaleException ex) {
            WorkflowLogger.footer();
            WorkflowLogger.error(ControllerActivity.UNDEPLOYMENT_FAILED, ex.getMessage());
            logger.error("Error while undeploying app: {}, service: {}, in namespace: {}", appName, serviceName, namespace, ex);
            throw ex;
        } finally {
            WorkflowLogger.footer();
        }
    }

    @Override
    protected void onError(WorkflowContext context, HyscaleException he) throws HyscaleException {
        WorkflowLogger.header(ControllerActivity.ERROR);
        WorkflowLogger.error(ControllerActivity.CAUSE, he != null ?
                he.getMessage() : ControllerErrorCodes.UNDEPLOYMENT_FAILED.getErrorMessage());
        context.setFailed(true);
        if (he != null) {
            throw he;
        }
    }

}
