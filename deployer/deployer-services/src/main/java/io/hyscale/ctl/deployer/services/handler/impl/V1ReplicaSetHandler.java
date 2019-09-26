package io.hyscale.ctl.deployer.services.handler.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.hyscale.ctl.commons.exception.HyscaleException;
import io.hyscale.ctl.deployer.core.model.ResourceKind;
import io.hyscale.ctl.deployer.core.model.ResourceOperation;
import io.hyscale.ctl.deployer.services.exception.DeployerErrorCodes;
import io.hyscale.ctl.deployer.services.handler.ResourceLifeCycleHandler;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.models.V1ReplicaSet;

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
		HyscaleException hyscaleException = new HyscaleException(DeployerErrorCodes.OPERATION_NOT_SUPPORTED,
				ResourceOperation.GET_BY_SELECTOR.getOperation(), getKind());
		logger.error(hyscaleException.getMessage());
		throw hyscaleException;
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

}
