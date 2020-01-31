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
import io.hyscale.commons.models.DeploymentContext;
import io.hyscale.commons.models.K8sAuthorisation;
import io.hyscale.commons.models.Manifest;
import io.hyscale.deployer.core.model.DeploymentStatus;
import io.hyscale.deployer.core.model.ReplicaInfo;
import io.hyscale.deployer.core.model.ResourceKind;
import io.hyscale.deployer.services.model.Pod;
import io.hyscale.deployer.services.model.ResourceStatus;
import io.hyscale.deployer.services.model.ServiceAddress;
import io.hyscale.deployer.services.progress.ProgressHandler;

/**
 * Interface for service deployments.
 * This class takes the responsibility of handling any deployment operation related to the service.
 *
 * <p>Implementation Notes</p>
 * Should be able to do service deployments from the supplied manifest to the cluster.
 *
 */
public interface Deployer {

	/**
	 * Deploy Service to cluster
	 * @param context
	 * @throws HyscaleException
	 */
	public void deploy(DeploymentContext context) throws HyscaleException;

	/**
	 * Wait for Deployment to complete
	 * Prints deployment status
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
	 * @param context
	 * @throws HyscaleException
	 */
	public void unDeploy(DeploymentContext context) throws HyscaleException;

	/**
	 * Check if User can access cluster
	 * @param authConfig
	 * @return true if user can access cluster
	 * @throws HyscaleException
	 */
	public boolean authenticate(AuthConfig authConfig) throws HyscaleException;

	/**
	 * Get Deployment status for Service
	 * @param context
	 * @return {@link DeploymentStatus} for the service
	 * @throws HyscaleException
	 */
	public DeploymentStatus getServiceDeploymentStatus(DeploymentContext context) throws HyscaleException;

	/**
	 * Get Deployment Status for Services in an App
	 * @param deploymentContext
	 * @return List of {@link DeploymentStatus} for each Service
	 * @throws HyscaleException
	 */
	public List<DeploymentStatus> getDeploymentStatus(DeploymentContext deploymentContext) throws HyscaleException;


	/**
	 * Get replicas info based on pods
	 * <p>
	 * isFilter if disabled return replica info for all pods fetched based on selector
	 * if enabled,
	 * 1. If owner kind for pods is different, warn user and return replica info for all pods
	 * 2. If owner kind is {@link ResourceKind #DEPLOYMENT} or {@link ResourceKind #REPLICA_SET},
	 *     Get Revision from deployment, get replicas set with this revision,
	 *     filter pods based on pod-template-hash from replica set
	 *     return replica info for filtered pods
	 * 3. Else return replica info for all pods
	 * </p>
	 * @param authConfig
	 * @param appName
	 * @param serviceName
	 * @param namespace
	 * @param isFilter
	 * @return List of {@link ReplicaInfo} based on pods fetched
	 * @throws HyscaleException
	 */
	public List<ReplicaInfo> getReplicas(AuthConfig authConfig, String appName, String serviceName, String namespace, 
	        boolean isFilter) throws HyscaleException;
	
	/**
	 * Get Service logs from Pods
	 * tail logs or read specific number of lines
	 * @param deploymentContext
	 * @return Input Stream with logs
	 * @throws HyscaleException
	 */
	public InputStream logs(DeploymentContext deploymentContext) throws HyscaleException;
	
	/**
	 * Get logs of a specific Pod of a Service
	 * tail logs or read specific number of lines
	 * @param deploymentContext
	 * @return Input Stream with logs
	 * @throws HyscaleException
	 */
	public InputStream logs(AuthConfig authConfig, String serviceName, String namespace, String podName, String containerName,
			Integer readLines, boolean tail) throws HyscaleException;
	
	/**
	 * 
	 * @param context
	 * @return ServiceAddress
	 * @throws HyscaleException if service not found or failed to create cluster client
	 */
	public ServiceAddress getServiceAddress(DeploymentContext context) throws HyscaleException;

	/**
	 * Get resource status based on manifest
	 * @param namespace
	 * @param manifest
	 * @param authConfig
	 * @return ResourceStatus, STABLE as default
	 * @throws Exception
	 */
	default ResourceStatus status(String namespace, Manifest manifest, AuthConfig authConfig) throws Exception {
		return ResourceStatus.STABLE;
	}

	List<Pod> getPods(String namespace, String appName, String serviceName, K8sAuthorisation k8sAuthorisation) throws Exception;
}