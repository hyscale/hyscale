package io.hyscale.ctl.deployer.services.model;

/**
 * Status of resource on cluster
 *
 */
public enum ResourceStatus {
	PENDING,	// Resource not yet deployed
	STABLE,		// Resource deployed successfully
	PAUSED,		// Resource not yet deployed
	FAILED		// Resource failed
}
