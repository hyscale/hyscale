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
package io.hyscale.deployer.services.model;

import io.hyscale.commons.models.Activity;

/**
 * Activities list for Deployer Service
 */
public enum DeployerActivity implements Activity {
	DEPLOYING("Applying {}"),
	DELETING("Deleting {}"),
	DEPLOYING_CONFIGMAP("Applying ConfigMap "),
	DEPLOYING_STATEFULSET("Applying StatefulSet "),
	DEPLOYING_DEPLOYMENT("Applying Deployment "),
	DEPLOYING_SECRETS("Applying Secret "),
	DEPLOYING_SERVICE("Applying Service "),
    DEPLOYING_HORIZONTAL_AUTO_SCALER("Applying HorizontalPodAutoScaler "),
	WAITING_FOR_CONTAINER_CREATION("Waiting for container creation"),
	WAITING_FOR_DEPLOYMENT("Waiting for deployment completion "),
	POD_INITIALIZED("Pod initialization "),
	POD_CREATION("Pod creation "),
	POD_READINESS("Pod readiness "),
	WAITING_FOR_SERVICE_IP("Waiting for service IP ..."),
	DELETING_CONFIG_MAP("Deleting ConfigMap "),
	DELETING_SECRETS("Deleting Secret "),
	DELETING_SERVICE("Deleting Service "),
	DELETING_STATEFULSET("Deleting StatefulSet "),
	DELETING_DEPLOYMENT("Deleting Deployment "),
	DELETING_NAMESPACE("Deleting namespace "),
	DELETING_PERSISTENT_VOLUME_CLAIMS("Deleting Persistent Volume Claims "),
    DELETING_HORIZONTAL_POD_AUTOSCALER("Deleting HorizontalPodAutoScaler "),
    LATEST_DEPLOYMENT_NOT_IDENTIFIABLE("Unable to identify latest deployment, displaying all replicas"),
	NO_RESOURCES_TO_UNDEPLOY("No resources to undeploy "),
	STALE_VOLUME_DELETION(
			"Volumes {} seems to have been removed in your hspec. These volumes are retained in your namespace {} as pvc {} for review and manual deletion"),
	STALE_VOLUME_REUSE(
			"These volumes {} are no longer in use. They are retained in your namespace {} as pvc {} for review and manual deletion. To reattach them to service {}, use the same volume name."),
	IGNORING_VOLUME_MODIFICATION("Detected change in \"{}\" hspec for volumes \"{}\". Changes to storage class or size is currently not supported"),
	SERVICE_WITH_ZERO_REPLICAS("Service has zero replicas"),
	SCALING_SERVICE("Scaling service "),
	SCALE_DOWN_VOLUME("The scale operation would leave stale replica volumes [{}] in your namespace {} and are meant for review and manual deletion. If not deleted, when you scale up the service the same volumes are reattached to the new replicas"),
	DESIRED_STATE("Service is already with desired state of replicas {} "),
	DESIRED_STATE_ON_HPA_ENABLED("HPA has been enabled on this service , so the requested scale operation may not result expected replicas. Check existing replicas using 'hyscale get replica status' command");

	private String message;

	DeployerActivity(String message) {
		this.message = message;
	}

	@Override
	public String getActivityMessage() {
		return message;
	}
}
