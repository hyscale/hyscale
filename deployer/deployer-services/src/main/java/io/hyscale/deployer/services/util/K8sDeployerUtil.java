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
package io.hyscale.deployer.services.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.hyscale.commons.constants.K8SRuntimeConstants;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.commons.utils.ResourceSelectorUtil;
import io.hyscale.deployer.core.model.ResourceKind;
import io.hyscale.deployer.services.handler.ResourceHandlers;
import io.hyscale.deployer.services.handler.impl.V1DeploymentHandler;
import io.hyscale.deployer.services.handler.impl.V1PodHandler;
import io.hyscale.deployer.services.handler.impl.V1ReplicaSetHandler;
import io.hyscale.deployer.services.model.DeployerActivity;
import io.hyscale.deployer.services.predicates.PodPredicates;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.models.V1Deployment;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1ReplicaSet;

public class K8sDeployerUtil {

    private static final Logger logger = LoggerFactory.getLogger(K8sDeployerUtil.class);

    public static List<V1Pod> getExistingPods(ApiClient apiClient, String appName, String serviceName, String namespace)
            throws HyscaleException {

        V1PodHandler v1PodHandler = (V1PodHandler) ResourceHandlers.getHandlerOf(ResourceKind.POD.getKind());
        String selector = ResourceSelectorUtil.getServiceSelector(appName, serviceName);
        return v1PodHandler.getBySelector(apiClient, selector, true, namespace);
    }

    public static List<V1Pod> getLatestPods(ApiClient apiClient, String appName, String serviceName,
                                            String namespace) {
        List<V1Pod> podList = null;
        V1PodHandler v1PodHandler = (V1PodHandler) ResourceHandlers.getHandlerOf(ResourceKind.POD.getKind());
        try {
            String selector = ResourceSelectorUtil.getServiceSelector(appName, serviceName);
            podList = v1PodHandler.getBySelector(apiClient, selector, true, namespace);
        } catch (HyscaleException e) {

        }

        if (podList == null || podList.isEmpty()) {
            return podList;
        }

        if (!PodPredicates.isPodAmbiguous().test(podList)) {
            return podList;
        }
        String podOwner = K8sPodUtil.getPodsUniqueOwner(podList);
        ResourceKind podOwnerKind = ResourceKind.fromString(podOwner);

        // Unknown parent
        if (StringUtils.isBlank(podOwner)) {
            logger.debug("Unable to determine latest deployment, displaying all replicas");
            WorkflowLogger.warn(DeployerActivity.LATEST_DEPLOYMENT_NOT_IDENTIFIABLE);
            return podList;
        }

        // Deployment
        if (ResourceKind.REPLICA_SET.equals(podOwnerKind) || ResourceKind.DEPLOYMENT.equals(podOwnerKind)) {
            // Get deployment, get revision, get RS with the revision, get all labels and filter pods
            return filterPodsByDeployment(apiClient, appName, serviceName, namespace, podList);
        }

        // TODO do we need to handle STS cases ??
        logger.debug("Replicas info:: unhandled case, pod owner: {}", podOwner);
        return podList;
    }

    /**
     * Get {@link V1Deployment} from cluster based on namespace, appname and service name.
     * Deployment provides the revision for corresponding replica set
     * Get {@link V1ReplicaSet} from cluster based on namespace, appname, service name and revision.
     * Replica set provides pod-template-hash label(cluster internal) for corresponding pods
     * From the provided pods return the ones which contains pod-template-hash in label 
     *
     * @param apiClient
     * @param appName
     * @param serviceName
     * @param namespace
     * @param podList
     * @return pods from pod list which refer to deployment for the app and service in namespace
     */
    public static List<V1Pod> filterPodsByDeployment(ApiClient apiClient, String appName, String serviceName, String namespace, List<V1Pod> podList) {
        String selector = ResourceSelectorUtil.getServiceSelector(appName, serviceName);
        V1DeploymentHandler v1DeploymentHandler = (V1DeploymentHandler) ResourceHandlers
                .getHandlerOf(ResourceKind.DEPLOYMENT.getKind());
        List<V1Deployment> deploymentList = null;
        try {
            deploymentList = v1DeploymentHandler.getBySelector(apiClient, selector, true, namespace);
        } catch (HyscaleException e) {
            logger.error("Error fetching deployment for pod filtering, ignoring", e);
            return podList;
        }
        if (deploymentList == null || deploymentList.isEmpty()) {
            logger.debug("No deployment found for filtering pods, returning empty list");
            return new ArrayList<V1Pod>();
        }

        String revision = V1DeploymentHandler.getDeploymentRevision(deploymentList.get(0));

        if (StringUtils.isBlank(revision)) {
            return podList;
        }
        V1ReplicaSetHandler v1ReplicaSetHandler = (V1ReplicaSetHandler) ResourceHandlers
                .getHandlerOf(ResourceKind.REPLICA_SET.getKind());

        V1ReplicaSet replicaSet = null;

        try {
            replicaSet = v1ReplicaSetHandler.getReplicaSetByRevision(apiClient, namespace, selector, true, revision);
        } catch (HyscaleException e) {
            logger.error("Error fetching replica set with revision {} for pod filtering, ignoring", revision, e);
            return podList;
        }
        if (replicaSet == null) {
            logger.debug("No Replica set found with revision: {} for filtering pods, returning empty list", revision);
            return new ArrayList<V1Pod>();
        }

        Map<String, String> replicaLabels = replicaSet.getMetadata().getLabels();
        String podTemplateHash = replicaLabels != null
                ? replicaLabels.get(K8SRuntimeConstants.K8S_DEPLOYMENT_POD_TEMPLATE_HASH)
                : null;

        Map<String, String> searchLabel = new HashMap<String, String>();
        searchLabel.put(K8SRuntimeConstants.K8S_DEPLOYMENT_POD_TEMPLATE_HASH, podTemplateHash);

        return K8sPodUtil.filterPods(podList, PodPredicates.podContainsLabel(), searchLabel);

    }

}
