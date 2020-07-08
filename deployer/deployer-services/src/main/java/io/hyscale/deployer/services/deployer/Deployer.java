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
package io.hyscale.deployer.services.deployer;

import java.io.InputStream;
import java.util.List;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.models.AuthConfig;
import io.hyscale.commons.models.ClusterVersionInfo;
import io.hyscale.commons.models.DeploymentContext;
import io.hyscale.commons.models.Manifest;
import io.hyscale.deployer.core.model.AppMetadata;
import io.hyscale.deployer.core.model.DeploymentStatus;
import io.hyscale.deployer.services.model.*;
import io.hyscale.deployer.services.progress.ProgressHandler;

/**
 * Interface for service deployments.
 * This class takes the responsibility of handling any deployment operation related to the service.
 *
 * <p>Implementation Notes</p>
 * Should be able to do service deployments from the supplied manifest to the cluster.
 */
public interface Deployer<T extends AuthConfig> {

    /**
     * Deploy Service to cluster
     *
     * @param context
     * @throws HyscaleException
     */
    public void deploy(DeploymentContext context) throws HyscaleException;

    /**
     * Wait for Deployment to complete
     * Prints deployment status
     *
     * @param context
     * @throws HyscaleException
     */
    public void waitForDeployment(DeploymentContext context) throws HyscaleException;

    public default void waitForDeployment(DeploymentContext context, ProgressHandler progressHandler)
            throws HyscaleException {
        waitForDeployment(context);
    }

    /**
     * Undeploy service from cluster
     *
     * @param context
     * @throws HyscaleException
     */
    public void unDeploy(DeploymentContext context) throws HyscaleException;

    /**
     * Check if User can access cluster
     *
     * @param authConfig
     * @return true if user can access cluster else false
     * @throws HyscaleException
     */
    public boolean authenticate(T authConfig) throws HyscaleException;

    /**
     * Get Deployment status for Service
     *
     * @param context
     * @return {@link DeploymentStatus} for the service
     * @throws HyscaleException
     */
    public DeploymentStatus getServiceDeploymentStatus(DeploymentContext context) throws HyscaleException;

    /**
     * Get Deployment ServiceStatus for Services in an App
     *
     * @param deploymentContext
     * @return List of {@link DeploymentStatus} for each Service
     * @throws HyscaleException
     */
    public List<DeploymentStatus> getDeploymentStatus(DeploymentContext deploymentContext) throws HyscaleException;


    /**
     * Get replicas info based on pods
     * isFilter restricts which pods for which replica info is required
     *
     * @param authConfig
     * @param appName
     * @param serviceName
     * @param namespace
     * @param isFilter    TODO Enable predicate based filter
     * @return List of {@link ReplicaInfo} based on pods fetched
     * @throws HyscaleException
     */
    public List<ReplicaInfo> getReplicas(T authConfig, String appName, String serviceName, String namespace,
                                         boolean isFilter) throws HyscaleException;


    /**
     * Get logs of a specific Pod of a Service
     * tail logs or read specific number of lines
     *
     * @return Input Stream with logs
     * @throws HyscaleException
     */
    public InputStream logs(T authConfig, String serviceName, String namespace, String podName, String containerName,
                            Integer readLines, boolean tail) throws HyscaleException;

    /**
     * @param context
     * @return ServiceAddress
     * @throws HyscaleException if service not found or failed to create cluster client
     */
    public ServiceAddress getServiceAddress(DeploymentContext context) throws HyscaleException;


    /**
     * @param authConfig
     * @return List of {@link AppMetadata} containing details of deployed apps
     * @throws HyscaleException
     */
    public List<AppMetadata> getAppsMetadata(T authConfig) throws HyscaleException;

    /**
     * Get resource status based on manifest
     *
     * @param namespace
     * @param manifest
     * @param authConfig
     * @return ResourceStatus, STABLE as default
     * @throws Exception
     */
    default ResourceStatus status(String namespace, Manifest manifest, T authConfig) throws Exception {
        return ResourceStatus.STABLE;
    }


    List<Pod> getPods(String namespace, String appName, String serviceName, T k8sAuthorisation) throws Exception;

    /**
     * Fetches the replicas of the latest deployment of a service
     *
     * @param authConfig  Auth of connecting to the cluster
     * @param appName     Name of the application
     * @param serviceName service name
     * @param namespace   namespace of the deployed application
     * @return
     * @throws HyscaleException
     */

    public List<ReplicaInfo> getLatestReplicas(T authConfig, String appName, String serviceName, String namespace) throws HyscaleException;

    /**
     * Scales a service by its operation
     * @param authConfig Auth of cluster
     * @param appName Name of the application
     * @param serviceName Name of the service to be scaled
     * @param namespace namespace of the deployed application
     * @param scaleSpec the spec to scale the service
     * @return ScaleStatus
     * @throws HyscaleException
     */

    public ScaleStatus scale(T authConfig, String appName, String serviceName, String namespace, ScaleSpec scaleSpec) throws HyscaleException;
    
    public ClusterVersionInfo getVersion(T authConfig) throws HyscaleException;
}