package io.hyscale.ctl.deployer.services.deployer;

import java.io.InputStream;
import java.util.List;

import io.hyscale.ctl.commons.exception.HyscaleException;
import io.hyscale.ctl.commons.models.AuthConfig;
import io.hyscale.ctl.commons.models.DeploymentContext;
import io.hyscale.ctl.commons.models.Manifest;
import io.hyscale.ctl.deployer.core.model.DeploymentStatus;
import io.hyscale.ctl.deployer.services.model.Pod;
import io.hyscale.ctl.deployer.services.model.ResourceStatus;
import io.hyscale.ctl.deployer.services.model.ServiceAddress;
import io.hyscale.ctl.deployer.services.progress.ProgressHandler;
/**
 * Defines Deployer functionalities
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

	List<Pod> getPods(String appName, String serviceName);
}