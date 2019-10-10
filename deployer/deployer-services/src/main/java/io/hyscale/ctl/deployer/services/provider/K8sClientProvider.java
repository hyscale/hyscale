package io.hyscale.ctl.deployer.services.provider;

import io.hyscale.ctl.commons.exception.HyscaleException;
import io.hyscale.ctl.commons.models.K8sAuthorisation;
import io.kubernetes.client.ApiClient;

public interface K8sClientProvider {

	public ApiClient get(K8sAuthorisation authConfig) throws HyscaleException;

}
