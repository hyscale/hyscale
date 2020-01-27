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
package io.hyscale.deployer.services.processor.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.commons.models.DeploymentContext;
import io.hyscale.commons.models.K8sAuthorisation;
import io.hyscale.commons.models.KubernetesResource;
import io.hyscale.commons.models.Manifest;
import io.hyscale.commons.utils.ResourceSelectorUtil;
import io.hyscale.deployer.core.model.ResourceKind;
import io.hyscale.deployer.services.exception.DeployerErrorCodes;
import io.hyscale.deployer.services.handler.ResourceHandlers;
import io.hyscale.deployer.services.handler.ResourceLifeCycleHandler;
import io.hyscale.deployer.services.handler.impl.V1PersistentVolumeClaimHandler;
import io.hyscale.deployer.services.model.DeployerActivity;
import io.hyscale.deployer.services.processor.DeployerInterceptorProcessor;
import io.hyscale.deployer.services.provider.K8sClientProvider;
import io.hyscale.deployer.services.util.KubernetesResourceUtil;
import io.hyscale.deployer.services.util.KubernetesVolumeUtil;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.models.V1PersistentVolumeClaim;

/**
 * Processor to handle volumes post deployment
 * Lists all the volumes no longer in use after current deployment
 * 
 * @author tushar
 *
 */
@Component
public class DeployStaleVolumeProcessor extends DeployerInterceptorProcessor {

    private static final Logger logger = LoggerFactory.getLogger(DeployStaleVolumeProcessor.class);

    @Autowired
    private K8sClientProvider clientProvider;

    @Override
    protected void _preProcess(DeploymentContext context) throws HyscaleException {
    }

    @Override
    protected void _postProcess(DeploymentContext context) throws HyscaleException {
        logger.debug("Cleaning up stale volumes ");
        ApiClient apiClient = clientProvider.get((K8sAuthorisation) context.getAuthConfig());
        String serviceName = context.getServiceName();
        String appName = context.getAppName();
        String namespace = context.getNamespace();
        String envName = context.getEnvName();
        List<Manifest> mainfestList = context.getManifests();
        if (mainfestList == null || mainfestList.isEmpty()) {
            logger.debug("No resource to cleanup");
            return;
        }
        String selector = ResourceSelectorUtil.getSelector(appName, envName, serviceName);
        for (Manifest manifest : mainfestList) {
            try {
                KubernetesResource k8sResource = KubernetesResourceUtil.getKubernetesResource(manifest, namespace);
                ResourceLifeCycleHandler lifeCycleHandler = ResourceHandlers.getHandlerOf(k8sResource.getKind());
                if (lifeCycleHandler != null) {
                    if (ResourceKind.STATEFUL_SET.getKind().equalsIgnoreCase(lifeCycleHandler.getKind())) {
                        cleanUpOldVolumes(false, apiClient, selector, namespace);
                    } else if (ResourceKind.DEPLOYMENT.getKind().equalsIgnoreCase(lifeCycleHandler.getKind())) {
                        // Delete all pvcs
                        cleanUpOldVolumes(true, apiClient, selector, namespace);
                    }
                }
            } catch (Exception e) {
                HyscaleException ex = new HyscaleException(e, DeployerErrorCodes.FAILED_TO_READ_MANIFEST);
                logger.error("Error while cleaning old pvcs", ex);
                return;
            }
        }
    }

    @Override
    protected void _onError(DeploymentContext context, Throwable th) throws HyscaleException {
        if (th != null && th instanceof HyscaleException) {
            HyscaleException hex = (HyscaleException) th;
            logger.error("Inside on error method in {}", getClass().toString(), hex.getMessage());
            throw hex;
        }
    }

    /**
     * 1. Delete All - based on selector
     * 2. Fetch pods based on selector
     * Create list of pvc from pods
     * Fetch pvc based on selector
     * Delete pvc not found in previous list
     *
     * @param deleteAll
     * @param apiClient
     * @param selector
     * @param namespace
     */
    private void cleanUpOldVolumes(boolean deleteAll, ApiClient apiClient, String selector, String namespace) {
        try {
            V1PersistentVolumeClaimHandler pvcHandler = (V1PersistentVolumeClaimHandler) ResourceHandlers
                    .getHandlerOf(ResourceKind.PERSISTENT_VOLUME_CLAIM.getKind());

            List<V1PersistentVolumeClaim> pvcItemsList = pvcHandler.getBySelector(apiClient, selector, true, namespace);
            if (pvcItemsList == null || pvcItemsList.isEmpty()) {
                return;
            }
            if (deleteAll) {
                printCleaningMsg();
                deleteAllPVC(pvcHandler, apiClient, namespace, pvcItemsList);
                return;
            }
            Set<String> podsVolumes = KubernetesVolumeUtil.getPodVolumes(apiClient, selector, namespace);

            if (podsVolumes == null || podsVolumes.isEmpty()) {
                printCleaningMsg();
                deleteAllPVC(pvcHandler, apiClient, namespace, pvcItemsList);
                return;
            }
            Set<String> staleVolumes = new HashSet<String>();
            Set<String> stalePVCs = new HashSet<String>();
            pvcItemsList.stream().filter(pvc -> !podsVolumes.contains(pvc.getMetadata().getName())).forEach(pvc -> {
                String pvcName = pvc.getMetadata().getName();
                staleVolumes.add(KubernetesVolumeUtil.getVolumeName(pvc));
                stalePVCs.add(pvcName);
                //              try {
                //                  logger.debug("Deleting PVC: {} in namespace: {}", pvcName, namespace);
                //                  pvcHandler.delete(apiClient, pvcName, namespace, false);
                //                  
                //              } catch (HyscaleException e) {
                //                  logger.error("Error while deleting pvc: {}, ignoring", pvcName);
                //              }
            });
            if (!staleVolumes.isEmpty()) {
                WorkflowLogger.persist(DeployerActivity.STALE_VOLUME_DELETION, staleVolumes.toString(), namespace,
                        stalePVCs.toString());
            }
        } catch (HyscaleException e) {
            logger.error("Error while cleaning up pvcs, error {}", e.getMessage());
            return;
        }

    }

    private void deleteAllPVC(V1PersistentVolumeClaimHandler pvcHandler, ApiClient apiClient, String namespace,
            List<V1PersistentVolumeClaim> pvcItemsList) {
        Set<String> staleVolumes = new HashSet<String>();
        Set<String> stalePVCs = new HashSet<String>();
        pvcItemsList.stream().forEach(pvc -> {
            staleVolumes.add(KubernetesVolumeUtil.getVolumeName(pvc));
            stalePVCs.add(pvc.getMetadata().getName());
            /*try {
                pvcHandler.delete(apiClient, name, namespace, false);
            } catch (HyscaleException e) {
                logger.error("Error while deleting PVC: {}, error: {}, ignoring", name, e.getMessage());
            }*/
        });
        if (!staleVolumes.isEmpty()) {
            WorkflowLogger.persist(DeployerActivity.STALE_VOLUME_DELETION, staleVolumes.toString(), namespace,
                    stalePVCs.toString());
        }

    }

    private void printCleaningMsg() {
        //WorkflowLogger.header(ControllerActivity.CLEANING_UP_VOLUMES);
    }

}
