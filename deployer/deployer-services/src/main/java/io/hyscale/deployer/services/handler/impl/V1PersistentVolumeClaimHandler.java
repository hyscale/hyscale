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
package io.hyscale.deployer.services.handler.impl;

import java.util.List;

import io.hyscale.deployer.services.model.DeployerActivity;
import io.hyscale.deployer.services.exception.DeployerErrorCodes;
import io.hyscale.deployer.services.handler.ResourceLifeCycleHandler;
import io.hyscale.deployer.services.util.ExceptionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.gson.JsonSyntaxException;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.logger.ActivityContext;
import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.commons.models.Status;
import io.hyscale.deployer.core.model.ResourceKind;
import io.hyscale.deployer.core.model.ResourceOperation;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1DeleteOptions;
import io.kubernetes.client.openapi.models.V1PersistentVolumeClaim;
import io.kubernetes.client.openapi.models.V1PersistentVolumeClaimList;

public class V1PersistentVolumeClaimHandler implements ResourceLifeCycleHandler<V1PersistentVolumeClaim> {

	private static final Logger logger = LoggerFactory.getLogger(V1PersistentVolumeClaimHandler.class);

	@Override
	public V1PersistentVolumeClaim create(ApiClient apiClient, V1PersistentVolumeClaim resource, String namespace)
			throws HyscaleException {
		HyscaleException hyscaleException = new HyscaleException(DeployerErrorCodes.OPERATION_NOT_SUPPORTED,
				ResourceOperation.CREATE.getOperation(), getKind());
		logger.error(hyscaleException.getMessage());
		throw hyscaleException;
	}

	@Override
	public boolean update(ApiClient apiClient, V1PersistentVolumeClaim resource, String namespace)
			throws HyscaleException {
		HyscaleException hyscaleException = new HyscaleException(DeployerErrorCodes.OPERATION_NOT_SUPPORTED,
				ResourceOperation.UPDATE.getOperation(), getKind());
		logger.error(hyscaleException.getMessage());
		throw hyscaleException;
	}

	@Override
	public V1PersistentVolumeClaim get(ApiClient apiClient, String name, String namespace) throws HyscaleException {
		CoreV1Api coreV1Api = new CoreV1Api(apiClient);
		V1PersistentVolumeClaim v1PersistentVolumeClaim = null;
		try {
			v1PersistentVolumeClaim = coreV1Api.readNamespacedPersistentVolumeClaim(name, namespace, TRUE, null, null);
		} catch (ApiException e) {
			HyscaleException ex = ExceptionHelper.buildGetException(getKind(), e, ResourceOperation.GET);
			logger.error("Error while fetching Persistent Volume claim {} in namespace {}, error {}", name, namespace,
					ex.toString());
			throw ex;
		}
		return v1PersistentVolumeClaim;
	}

	@Override
	public List<V1PersistentVolumeClaim> getBySelector(ApiClient apiClient, String selector, boolean label,
			String namespace) throws HyscaleException {
		CoreV1Api coreV1Api = new CoreV1Api(apiClient);
		List<V1PersistentVolumeClaim> v1PersistentVolumeClaims = null;
		try {
			String labelSelector = label ? selector : null;
			String fieldSelector = label ? null : selector;

			V1PersistentVolumeClaimList v1PersistentVolumeClaimList = coreV1Api.listNamespacedPersistentVolumeClaim(
					namespace, TRUE, null, null, fieldSelector, labelSelector, null, null, null, null);
			v1PersistentVolumeClaims = v1PersistentVolumeClaimList != null ? v1PersistentVolumeClaimList.getItems()
					: null;
		} catch (ApiException e) {
			HyscaleException ex = ExceptionHelper.buildGetException(getKind(), e, ResourceOperation.GET_BY_SELECTOR);
			logger.error("Error while listing Persistent volume claims in namespace {}, with selectors {}, error {} ",
					namespace, selector, ex.toString());
			throw ex;
		}
		return v1PersistentVolumeClaims;
	}

	@Override
	public boolean patch(ApiClient apiClient, String name, String namespace, V1PersistentVolumeClaim body)
			throws HyscaleException {
		HyscaleException hyscaleException = new HyscaleException(DeployerErrorCodes.OPERATION_NOT_SUPPORTED,
				ResourceOperation.PATCH.getOperation(), getKind());
		logger.error(hyscaleException.getMessage());
		throw hyscaleException;
	}

	@Override
	public boolean delete(ApiClient apiClient, String name, String namespace, boolean wait) throws HyscaleException {
		CoreV1Api coreV1Api = new CoreV1Api(apiClient);

		V1DeleteOptions deleteOptions = getDeleteOptions();
		ActivityContext activityContext = new ActivityContext(DeployerActivity.DELETING_PERSISTENT_VOLUME_CLAIMS);
		WorkflowLogger.startActivity(activityContext);
		try {
		    try {
				coreV1Api.deleteNamespacedPersistentVolumeClaim(name, namespace, TRUE, null, null, null, null, deleteOptions);
		    } catch (JsonSyntaxException e) {
			// K8s Exception ignore
		    }
			List<String> persistentVolumeClaims = Lists.newArrayList();
			persistentVolumeClaims.add(name);
			if (wait) {
				waitForResourceDeletion(apiClient, persistentVolumeClaims, namespace, activityContext);
			}
		} catch (ApiException e) {
			if (e.getCode() == 404) {
				WorkflowLogger.endActivity(activityContext, Status.NOT_FOUND);
				return false;
			}
			HyscaleException ex = new HyscaleException(e, DeployerErrorCodes.FAILED_TO_DELETE_RESOURCE,
					ExceptionHelper.getExceptionMessage(getKind(), e, ResourceOperation.DELETE));
			logger.error("Error while deleting persistent volume claims {} in namespace {}, error {}", name, namespace,
					ex.toString());
			WorkflowLogger.endActivity(activityContext, Status.FAILED);
			throw ex;
		}
		WorkflowLogger.endActivity(activityContext, Status.DONE);
		return true;

	}

	@Override
	public boolean deleteBySelector(ApiClient apiClient, String selector, boolean label, String namespace, boolean wait)
			throws HyscaleException {
		try {
			List<V1PersistentVolumeClaim> persistentVolumeClaimList = getBySelector(apiClient, selector, label,
					namespace);
			if (persistentVolumeClaimList == null || persistentVolumeClaimList.isEmpty()) {
			    return false;
			}
			for (V1PersistentVolumeClaim persistentVolumeClaim : persistentVolumeClaimList) {
				delete(apiClient, persistentVolumeClaim.getMetadata().getName(), namespace, wait);
			}
		} catch (HyscaleException e) {
			if (DeployerErrorCodes.RESOURCE_NOT_FOUND.equals(e.getHyscaleError())) {
				logger.error("Error while deleting Persistent Volume claims for selector {} in namespace {}, error {}",
						selector, namespace, e.toString());
				return false;
			}
			throw e;
		}
		return true;

	}

	@Override
	public String getKind() {
		return ResourceKind.PERSISTENT_VOLUME_CLAIM.getKind();
	}

	@Override
	public boolean cleanUp() {
		return false;
	}

	@Override
	public int getWeight() {
	    return ResourceKind.PERSISTENT_VOLUME_CLAIM.getWeight();
	}
}
