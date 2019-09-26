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

public interface Deployer {

	public void deploy(DeploymentContext context) throws HyscaleException;

	public void waitForDeployment(DeploymentContext context) throws HyscaleException;

	public default void waitForDeployment(DeploymentContext context, ProgressHandler progressHandler)
			throws HyscaleException {
		waitForDeployment(context);
	}

	public void unDeploy(DeploymentContext context) throws HyscaleException;

	public boolean authenticate(AuthConfig authConfig) throws HyscaleException;

	public DeploymentStatus getServiceDeploymentStatus(DeploymentContext context) throws HyscaleException;

	public List<DeploymentStatus> getDeploymentStatus(DeploymentContext deploymentContext) throws HyscaleException;

	public InputStream logs(DeploymentContext deploymentContext) throws HyscaleException;

	public ServiceAddress getServiceAddress(DeploymentContext context) throws HyscaleException;

	default ResourceStatus status(String namespace, Manifest manifest, AuthConfig authConfig) throws Exception {
		return ResourceStatus.STABLE;
	}

	List<Pod> getPods(String appName, String serviceName);
}