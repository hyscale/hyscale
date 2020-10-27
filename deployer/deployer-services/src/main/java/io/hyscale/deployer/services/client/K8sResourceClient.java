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
package io.hyscale.deployer.services.client;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.commons.models.Status;
import io.hyscale.deployer.core.model.ResourceOperation;
import io.hyscale.deployer.services.exception.DeployerErrorCodes;
import io.hyscale.deployer.services.model.CustomListObject;
import io.hyscale.deployer.services.model.CustomObject;
import io.hyscale.deployer.services.model.DeployerActivity;
import io.hyscale.deployer.services.util.ExceptionHelper;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.util.generic.KubernetesApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class K8sResourceClient extends GenericK8sClient {
    private static final Logger logger = LoggerFactory.getLogger(K8sResourceClient.class);

    public K8sResourceClient(ApiClient apiClient) {
        super(apiClient);
    }

    @Override
    public void create(CustomObject resource) throws HyscaleException {
        if(resource == null){
            return;
        }
        String kind = resource.getKind();
        WorkflowLogger.startActivity(DeployerActivity.DEPLOYING,kind);

        KubernetesApiResponse<CustomObject> response = genericClient.create(resource);
        if(response!=null){
            if(response.isSuccess()){
                logger.info("Successfully created resource "+kind);
                WorkflowLogger.endActivity(Status.DONE);
                return;
            }else{
                logger.error("Failed reason: "+response.getStatus().getReason()+"\n" +
                        "Message: "+response.getStatus().getMessage());
            }
        }
        WorkflowLogger.endActivity(Status.FAILED);
        throw new HyscaleException(DeployerErrorCodes.FAILED_TO_CREATE_RESOURCE);
    }

    @Override
    public void update(CustomObject resource) {

    }

    @Override
    public void delete(CustomObject resource) {

    }

    @Override
    public CustomObject get(CustomObject resource) {
        return null;
    }
}
