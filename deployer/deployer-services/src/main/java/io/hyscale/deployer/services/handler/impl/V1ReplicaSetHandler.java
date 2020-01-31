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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.models.AnnotationKey;
import io.hyscale.deployer.core.model.ResourceKind;
import io.hyscale.deployer.core.model.ResourceOperation;
import io.hyscale.deployer.services.exception.DeployerErrorCodes;
import io.hyscale.deployer.services.handler.ResourceLifeCycleHandler;
import io.hyscale.deployer.services.util.ExceptionHelper;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.apis.AppsV1Api;
import io.kubernetes.client.models.V1ReplicaSet;
import io.kubernetes.client.models.V1ReplicaSetList;

public class V1ReplicaSetHandler implements ResourceLifeCycleHandler<V1ReplicaSet> {

	private static final Logger logger = LoggerFactory.getLogger(V1ReplicaSetHandler.class);

	@Override
	public V1ReplicaSet create(ApiClient apiClient, V1ReplicaSet resource, String namespace) throws HyscaleException {
		HyscaleException hyscaleException = new HyscaleException(DeployerErrorCodes.OPERATION_NOT_SUPPORTED,
				ResourceOperation.CREATE.getOperation(), getKind());
		logger.error(hyscaleException.getMessage());
		throw hyscaleException;
	}

	@Override
	public boolean update(ApiClient apiClient, V1ReplicaSet resource, String namespace) throws HyscaleException {
		HyscaleException hyscaleException = new HyscaleException(DeployerErrorCodes.OPERATION_NOT_SUPPORTED,
				ResourceOperation.UPDATE.getOperation(), getKind());
		logger.error(hyscaleException.getMessage());
		throw hyscaleException;
	}

	@Override
	public V1ReplicaSet get(ApiClient apiClient, String name, String namespace) throws HyscaleException {
		HyscaleException hyscaleException = new HyscaleException(DeployerErrorCodes.OPERATION_NOT_SUPPORTED,
				ResourceOperation.GET.getOperation(), getKind());
		logger.error(hyscaleException.getMessage());
		throw hyscaleException;
	}

	@Override
	public List<V1ReplicaSet> getBySelector(ApiClient apiClient, String selector, boolean label, String namespace)
			throws HyscaleException {
	    AppsV1Api appsV1Api = new AppsV1Api(apiClient);
        List<V1ReplicaSet> v1ReplicaSets = null;
        try {
            String labelSelector = label ? selector : null;
            String fieldSelector = label ? null : selector;

            V1ReplicaSetList v1DeploymentList = appsV1Api.listNamespacedReplicaSet(namespace, TRUE,
                    null, fieldSelector, labelSelector, null, null, null, null);

            v1ReplicaSets = v1DeploymentList != null ? v1DeploymentList.getItems() : null;
        } catch (ApiException e) {
            HyscaleException ex = ExceptionHelper.buildGetException(getKind(), e, ResourceOperation.GET_BY_SELECTOR);
            logger.error("Error while listing ReplicaSets in namespace {}, with selectors {}, error {} ", namespace,
                    selector, ex.toString());
            throw ex;
        }
        return v1ReplicaSets;
	}

	@Override
	public boolean patch(ApiClient apiClient, String name, String namespace, V1ReplicaSet body)
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
		return ResourceKind.REPLICA_SET.getKind();
	}

	@Override
	public boolean cleanUp() {
		return false;
	}

	@Override
	public int getWeight() {
		return ResourceKind.REPLICA_SET.getWeight();
	}
	
	public V1ReplicaSet getReplicaSetByRevision(ApiClient apiClient, String namespace, String selector, boolean label,
            String revision) throws HyscaleException {
        List<V1ReplicaSet> replicaSetList = getBySelector(apiClient, selector, label, namespace);

        if (replicaSetList == null) {
            return null;
        }
        
        for (V1ReplicaSet replicaSet : replicaSetList) {
            if (replicaSet.getMetadata().getAnnotations() == null) {
                continue;
            }
            if (revision.equals(replicaSet.getMetadata().getAnnotations()
                    .get(AnnotationKey.K8S_DEPLOYMENT_REVISION.getAnnotation()))) {
                return replicaSet;
            }
        }

        return null;
    }
	
}
