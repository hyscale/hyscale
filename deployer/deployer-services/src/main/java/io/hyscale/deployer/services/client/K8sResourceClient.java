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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.commons.models.AnnotationKey;
import io.hyscale.commons.models.Status;
import io.hyscale.commons.utils.GsonProviderUtil;
import io.hyscale.commons.utils.ThreadPoolUtil;
import io.hyscale.deployer.services.constants.DeployerConstants;
import io.hyscale.deployer.services.exception.DeployerErrorCodes;
import io.hyscale.deployer.services.model.CustomListObject;
import io.hyscale.deployer.services.model.CustomObject;
import io.hyscale.deployer.services.model.DeployerActivity;
import io.hyscale.deployer.services.util.K8sResourcePatchUtil;
import io.kubernetes.client.custom.V1Patch;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.util.generic.KubernetesApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
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
        Map<String,Object> metaMap = (Map) resource.get("metadata");
        Map<String,String> annotations = new HashMap<>();
        annotations.put(AnnotationKey.K8S_HYSCALE_LAST_APPLIED_CONFIGURATION.getAnnotation(),
                GsonProviderUtil.getPrettyGsonBuilder().toJson(resource));
        metaMap.put("annotations",annotations);

        KubernetesApiResponse<CustomObject> response = genericClient.create(resource);
        if(response!=null){
            if(response.isSuccess()){
                logger.info("Successfully created resource "+kind);
                return;
            }else{
                logger.error("Failed to create, reason: "+response.getStatus().getReason()+"\n" +
                        "Message: "+response.getStatus().getMessage());
            }
        }
        throw new HyscaleException(DeployerErrorCodes.FAILED_TO_CREATE_RESOURCE);
    }

    @Override
    public void update(CustomObject resource) throws HyscaleException {
        if(resource == null){
            return;
        }
        String kind = resource.getKind();
        Map<String,Object> metaMap = (Map) resource.get("metadata");
        Map<String,String> annotations = new HashMap<>();
        annotations.put(AnnotationKey.K8S_HYSCALE_LAST_APPLIED_CONFIGURATION.getAnnotation(),
                GsonProviderUtil.getPrettyGsonBuilder().toJson(resource));
        metaMap.put("annotations",annotations);

        KubernetesApiResponse<CustomObject> response = genericClient.update(resource);
        if(response!=null){
            if(response.isSuccess()){
                logger.info("Successfully updated resource "+kind);
                return;
            }else{
                logger.error("Failed reason: "+response.getStatus().getReason()+"\n" +
                        "Message: "+response.getStatus().getMessage());
            }
        }
        throw new HyscaleException(DeployerErrorCodes.FAILED_TO_CREATE_RESOURCE);
    }

    @Override
    public boolean patch(CustomObject resource) throws HyscaleException {
        if(resource == null){
            return false;
        }
        String kind = resource.getKind();
        String name = resource.getMetadata().getName();
        CustomObject customObject = get(resource);
        if(customObject != null){
            String lastAppliedConfig = customObject.getMetadata().getAnnotations()
                    .get(AnnotationKey.K8S_HYSCALE_LAST_APPLIED_CONFIGURATION.getAnnotation());
            Object patchObject = null;
            try {
                patchObject = K8sResourcePatchUtil.getJsonPatch(GsonProviderUtil.getPrettyGsonBuilder().fromJson(lastAppliedConfig, CustomObject.class),
                        resource, CustomObject.class);
                V1Patch v1Patch = new V1Patch(patchObject.toString());
                KubernetesApiResponse<CustomObject> response = genericClient.patch(name,null,v1Patch);
                if(response!=null){
                    if(response.isSuccess()){
                        logger.info("Successfully patched resource "+kind);
                        return true;
                    }else{
                        logger.error("Failed to Patch, reason: "+response.getStatus().getReason()+"\n" +
                                "Message: "+response.getStatus().getMessage());
                    }
                }
            } catch (HyscaleException e) {
                throw new HyscaleException(DeployerErrorCodes.FAILED_TO_CREATE_RESOURCE);
            }

        }
        return false;
    }

    @Override
    public boolean delete(CustomObject resource) {
        if(resource == null){
            return false;
        }
        String kind = resource.getKind();
        String name = resource.getMetadata().getName();
        KubernetesApiResponse<CustomObject> response = genericClient.delete(namespace,name);
        if(response!=null){
            if(response.isSuccess()){
                logger.info("Successfully deleted resource "+kind);
                waitForResourceDeletion(resource);
                return true;
            }else{
                logger.error("Failed to delete resource "+kind+"\nReason : "+response.getStatus().getReason());
            }
        }
        return false;
    }

    private void waitForResourceDeletion(CustomObject resource) {
        if(resource == null){
            return;
        }
        long startTime = System.currentTimeMillis();
        while((System.currentTimeMillis() - startTime < DeployerConstants.MAX_WAIT_TIME_IN_MILLISECONDS)){
            CustomObject customObject = get(resource);
            if(customObject == null){ return; }
            logger.debug("Resource {} found to be existing, waiting for resource deletion", resource);
            ThreadPoolUtil.sleepSilently(DeployerConstants.DELETE_SLEEP_INTERVAL_IN_MILLIS);
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
            return getResourceByName(name);
        }
        return null;
    }

    @Override
    public CustomObject getResourceByName(String name) {
        if(name == null || name.isBlank()){
            return null;
        }
        KubernetesApiResponse<CustomObject> response = genericClient.get(namespace,name);
        if(response != null && response.getObject()!=null){
            return response.getObject();
        }
        return null;
    }

    @Override
    public List<CustomObject> getAll(){
        KubernetesApiResponse<CustomListObject> response = genericClient.list(namespace);
        if(response!=null){
            return response.getObject().getItems();
        }
        return null;
    }

}
