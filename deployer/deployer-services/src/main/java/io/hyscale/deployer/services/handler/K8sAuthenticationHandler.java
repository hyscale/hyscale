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
package io.hyscale.deployer.services.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.hyscale.commons.exception.CommonErrorCode;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.models.AuthConfig;
import io.hyscale.commons.models.K8sAuthorisation;
import io.hyscale.deployer.core.model.ResourceOperation;
import io.hyscale.deployer.services.provider.K8sClientProvider;
import io.hyscale.deployer.services.util.ExceptionHelper;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.apis.AuthenticationV1Api;
import io.kubernetes.client.openapi.models.V1APIResourceList;

@Component
public class K8sAuthenticationHandler implements AuthenticationHandler {
	private static final Logger logger = LoggerFactory.getLogger(K8sAuthenticationHandler.class);

	@Autowired
	private K8sClientProvider clientProvider;

	public boolean authenticate(AuthConfig authConfig) throws HyscaleException {
		ApiClient apiClient = null;
		apiClient = clientProvider.get((K8sAuthorisation) authConfig);
		AuthenticationV1Api apiInstance = new AuthenticationV1Api(apiClient);
		try {
			V1APIResourceList result = apiInstance.getAPIResources();
			return result != null ? true : false;
		} catch (ApiException e) {
			logger.error("Exception when calling AuthenticationV1Api#createTokenReview");
			HyscaleException ex = new HyscaleException(e, CommonErrorCode.ERROR_OCCURED_WHILE_CONNECTING_TO_CLUSTER,
					ExceptionHelper.getExceptionMessage("Falied to validate cluster", e, ResourceOperation.DELETE));
			throw ex; 
		}
	}

}
