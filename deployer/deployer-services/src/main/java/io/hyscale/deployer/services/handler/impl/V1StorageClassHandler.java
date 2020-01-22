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

import io.hyscale.deployer.services.exception.DeployerErrorCodes;
import io.hyscale.deployer.services.handler.ResourceLifeCycleHandler;
import io.hyscale.deployer.services.util.ExceptionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.deployer.core.model.ResourceKind;
import io.hyscale.deployer.core.model.ResourceOperation;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.apis.StorageV1Api;
import io.kubernetes.client.models.V1StorageClass;
import io.kubernetes.client.models.V1StorageClassList;

public class V1StorageClassHandler implements ResourceLifeCycleHandler<V1StorageClass> {

	private static final Logger logger = LoggerFactory.getLogger(V1StorageClassHandler.class);

	@Override
	public V1StorageClass create(ApiClient apiClient, V1StorageClass resource, String namespace)
			throws HyscaleException {
		HyscaleException hyscaleException = new HyscaleException(DeployerErrorCodes.OPERATION_NOT_SUPPORTED,
				ResourceOperation.CREATE.getOperation(), getKind());
		logger.error(hyscaleException.getMessage());
		throw hyscaleException;
	}

	@Override
	public boolean update(ApiClient apiClient, V1StorageClass resource, String namespace) throws HyscaleException {
		HyscaleException hyscaleException = new HyscaleException(DeployerErrorCodes.OPERATION_NOT_SUPPORTED,
				ResourceOperation.UPDATE.getOperation(), getKind());
		logger.error(hyscaleException.getMessage());
		throw hyscaleException;
	}

	@Override
	public V1StorageClass get(ApiClient apiClient, String name, String namespace) throws HyscaleException {
		HyscaleException hyscaleException = new HyscaleException(DeployerErrorCodes.OPERATION_NOT_SUPPORTED,
				ResourceOperation.GET.getOperation(), getKind());
		logger.error(hyscaleException.getMessage());
		throw hyscaleException;
	}

	@Override
	public List<V1StorageClass> getBySelector(ApiClient apiClient, String selector, boolean label, String namespace)
			throws HyscaleException {
		HyscaleException hyscaleException = new HyscaleException(DeployerErrorCodes.OPERATION_NOT_SUPPORTED,
				ResourceOperation.GET_BY_SELECTOR.getOperation(), getKind());
		logger.error(hyscaleException.getMessage());
		throw hyscaleException;
	}

	@Override
	public boolean patch(ApiClient apiClient, String name, String namespace, V1StorageClass body)
			throws HyscaleException {
		HyscaleException hyscaleException = new HyscaleException(DeployerErrorCodes.OPERATION_NOT_SUPPORTED,
				ResourceOperation.PATCH.getOperation(), getKind());
		logger.error(hyscaleException.getMessage());
		throw hyscaleException;
	}

	@Override
	public boolean delete(ApiClient apiClient, String name, String namespace, boolean wait) throws HyscaleException {
		HyscaleException hyscaleException = new HyscaleException(DeployerErrorCodes.OPERATION_NOT_SUPPORTED,
				ResourceOperation.DELETE.getOperation(), getKind());
		logger.error(hyscaleException.getMessage());
		throw hyscaleException;
	}

	@Override
	public boolean deleteBySelector(ApiClient apiClient, String selector, boolean label, String namespace, boolean wait)
			throws HyscaleException {
		HyscaleException hyscaleException = new HyscaleException(DeployerErrorCodes.OPERATION_NOT_SUPPORTED,
				ResourceOperation.DELETE_BY_SELECTOR.getOperation(), getKind());
		logger.error(hyscaleException.getMessage());
		throw hyscaleException;
	}

	@Override
	public String getKind() {
		return ResourceKind.STORAGE_CLASS.getKind();
	}

	@Override
	public int getWeight() {
		return ResourceKind.STORAGE_CLASS.getWeight();
	}

	@Override
	public boolean cleanUp() {
		return false;
	}

	public List<V1StorageClass> getAll(ApiClient apiClient) throws HyscaleException {
		StorageV1Api storageV1Api = new StorageV1Api(apiClient);
		List<V1StorageClass> v1StorageList = null;
		try {
			V1StorageClassList v1StorageClassList = storageV1Api.listStorageClass(TRUE, null, null, null, null,
					null, null, null);

			v1StorageList = v1StorageClassList != null ? v1StorageClassList.getItems() : null;
		} catch (ApiException e) {
			HyscaleException ex = ExceptionHelper.buildGetException(getKind(), e, ResourceOperation.GET_BY_SELECTOR);
			logger.error("Error while listing Storage class, error {} ", ex.getMessage());
			throw ex;
		}
		return v1StorageList;
	}

}
