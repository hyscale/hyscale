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
import io.hyscale.commons.models.AnnotationKey;
import io.hyscale.commons.models.Status;
import io.hyscale.commons.utils.GsonProviderUtil;
import io.hyscale.deployer.services.exception.DeployerErrorCodes;
import io.hyscale.deployer.services.model.CustomObject;
import io.hyscale.deployer.services.model.DeployerActivity;
import io.hyscale.deployer.services.util.K8sResourcePatchUtil;
import io.kubernetes.client.custom.V1Patch;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.util.generic.KubernetesApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

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
        Map<String,Object> metaMap = (Map) resource.get("metadata");
        Map<String,String> annotations = new HashMap<>();
        annotations.put(AnnotationKey.K8S_HYSCALE_LAST_APPLIED_CONFIGURATION.getAnnotation(),
                GsonProviderUtil.getPrettyGsonBuilder().toJson(resource));
        metaMap.put("annotations",annotations);

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
    public void update(CustomObject resource) throws HyscaleException {
        if(resource == null){
            return;
        }
        String kind = resource.getKind();
        WorkflowLogger.startActivity(DeployerActivity.DEPLOYING,kind);

        KubernetesApiResponse<CustomObject> response = genericClient.update(resource);
        if(response!=null){
            if(response.isSuccess()){
                logger.info("Successfully updated resource "+kind);
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
    public void patch(CustomObject resource) throws HyscaleException {
        if(resource == null){
            return;
        }
        String kind = resource.getKind();
        WorkflowLogger.startActivity(DeployerActivity.DEPLOYING,kind);
        String name = resource.getMetadata().getName();
        CustomObject customObject = get(resource);
        if(customObject != null){
            logger.info("CO - "+customObject);
            String lastAppliedConfig = customObject.getMetadata().getAnnotations()
                    .get(AnnotationKey.K8S_HYSCALE_LAST_APPLIED_CONFIGURATION.getAnnotation());
            Object patchObject = K8sResourcePatchUtil.getJsonPatch(GsonProviderUtil.getPrettyGsonBuilder().fromJson(lastAppliedConfig, CustomObject.class),
                    resource, CustomObject.class);
            V1Patch v1Patch = new V1Patch(patchObject.toString());
            String patchType = "jsonPatch";
            KubernetesApiResponse<CustomObject> response = genericClient.patch(name,patchType,v1Patch);
            if(response!=null){
                if(response.isSuccess()){
                    logger.info("Successfully patched resource "+kind);
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
    }

    @Override
    public void delete(CustomObject resource) {
        if(resource == null){
            return;
        }
        String kind = resource.getKind();
        // Deleting activity
        //TODO Add workflow loggers
        String name = resource.getMetadata().getName();
        KubernetesApiResponse<CustomObject> response = genericClient.delete(namespace,name);
        if(response!=null){
            if(response.isSuccess()){
                logger.info("Successfully deleted resource "+kind);
            }else{
                logger.debug("Failed reason: "+response.getStatus().getReason());
                logger.debug(response.getStatus().getMessage());
            }
        }
    }

    @Override
    public CustomObject get(CustomObject resource) {
        if(resource == null){
            return null;
        }
        logger.debug("Fetching "+resource.getKind());
        if(resource.getMetadata() != null){
            String name = resource.getMetadata().getName();
            KubernetesApiResponse<CustomObject> response = genericClient.get(namespace,name);
            if(response != null && response.getObject()!=null){
                logger.debug("Custom object - "+response.getObject());
                return response.getObject();
            }
        }
        return null;
    }
}
