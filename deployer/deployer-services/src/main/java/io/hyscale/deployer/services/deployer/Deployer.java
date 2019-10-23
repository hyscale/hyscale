package io.hyscale.deployer.services.deployer;

import java.io.InputStream;
import java.util.List;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.models.AuthConfig;
import io.hyscale.commons.models.DeploymentContext;
import io.hyscale.commons.models.K8sAuthorisation;
import io.hyscale.commons.models.Manifest;
import io.hyscale.deployer.core.model.DeploymentStatus;
import io.hyscale.deployer.services.model.Pod;
import io.hyscale.deployer.services.model.ResourceStatus;
import io.hyscale.deployer.services.model.ServiceAddress;
import io.hyscale.deployer.services.progress.ProgressHandler;

/**
 * Interface to service deployments.
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
	 * Get Service logs from Pods
	 * tail logs or read specific number of lines
	 * @param deploymentContext
	 * @return Input Stream with logs
	 * @throws HyscaleException
	 */
	public InputStream logs(DeploymentContext deploymentContext) throws HyscaleException;
	
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