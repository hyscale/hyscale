package io.hyscale.deployer.services.model;

import io.hyscale.commons.models.Activity;

/**
 * Activities list for Deployer Service
 */
public enum DeployerActivity implements Activity {
	DEPLOYING_CONFIGMAP("Applying ConfigMap "),
	DEPLOYING_STATEFULSET("Applying StatefulSet "),
	DEPLOYING_DEPLOYMENT("Applying Deployment "),
	DEPLOYING_SECRETS("Applying Secret "),
	DEPLOYING_SERVICE("Applying Service "),
	WAITING_FOR_CONTAINER_CREATION("Waiting for container creation"),
	WAITING_FOR_DEPLOYMENT("Waiting for deployment completion "),
	POD_INITIALIZED("Pod initialization "),
	POD_CREATION("Pod creation "),
	POD_READINESS("Pod readiness "),
	WAITING_FOR_SERVICE_IP("Waiting for service IP..."),
	DELETING_CONFIG_MAP("Deleting ConfigMap "),
	DELETING_SECRETS("Deleting Secret "),
	DELETING_SERVICE("Deleting Service "),
	DELETING_STATEFULSET("Deleting StatefulSet "),
	DELETING_DEPLOYMENT("Deleting Deployment "),
	DELETING_NAMESPACE("Deleting namespace "),
	DELETING_PERSISTENT_VOLUME_CLAIMS("Deleting Persistent Volume Claims "),
	NO_RESOURCES_TO_UNDEPLOY("No resources to undeploy "),
	STALE_VOLUME_DELETION(
			"Volume {} seems to have been removed in your hspec. This volume is retained in your namespace {} as pvc {} for review and manual deletion"),
	STALE_VOLUME_REUSE(
			"These volumes {} are no longer in use. They are retained in your namespace {} as pvc {} for review and manual deletion. To reattach them to service {}, use the same volume name."),
	IGNORING_VOLUME_MODIFICATION("Detected change in hspec for volumes {}. Changes to storage class or size is currently not supported");

	private String message;

	DeployerActivity(String message) {
		this.message = message;
	}

	@Override
	public String getActivityMessage() {
		return message;
	}
}
