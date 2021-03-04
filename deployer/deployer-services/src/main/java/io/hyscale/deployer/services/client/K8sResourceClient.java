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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.models.AnnotationKey;
import io.hyscale.commons.utils.GsonProviderUtil;
import io.hyscale.commons.utils.ThreadPoolUtil;
import io.hyscale.deployer.services.constants.DeployerConstants;
import io.hyscale.deployer.services.exception.DeployerErrorCodes;
import io.hyscale.deployer.services.model.CustomListObject;
import io.hyscale.deployer.services.model.CustomObject;
import io.hyscale.deployer.services.util.K8sResourcePatchUtil;
import io.kubernetes.client.custom.V1Patch;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.util.generic.KubernetesApiResponse;
import io.kubernetes.client.util.generic.options.DeleteOptions;
import io.kubernetes.client.util.generic.options.ListOptions;

public class K8sResourceClient extends GenericK8sClient {
    private static final Logger logger = LoggerFactory.getLogger(K8sResourceClient.class);
    private static final String annotations = "annotations";

    public K8sResourceClient(ApiClient apiClient) {
        super(apiClient);
    }

    @Override
    public void create(CustomObject resource) throws HyscaleException {
        if(resource == null){
            return;
        }
        String kind = resource.getKind();
        String name = resource.getMetadata().getName();
        Map<String,Object> metaMap = (Map) resource.get("metadata");

        if(metaMap.get(annotations) == null){
            metaMap.put(annotations,new HashMap<String,String>());
        }
        Map<String,String> annotationsMap = (Map) metaMap.get(K8sResourceClient.annotations);
        annotationsMap.put(AnnotationKey.K8S_HYSCALE_LAST_APPLIED_CONFIGURATION.getAnnotation(),
                GsonProviderUtil.getPrettyGsonBuilder().toJson(resource));

        KubernetesApiResponse<CustomObject> response = genericClient.create(resource);
        if(response!=null){
            if(response.isSuccess()){
                logger.info("Successfully created resource : {}, name : {} ",kind,name);
                return;
            }else{
                logger.error("Failed to create, reason: {}\n Message: {}",response.getStatus().getReason(),response.getStatus().getMessage());
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
        String name = resource.getMetadata().getName();
        Map<String,Object> metaMap = (Map) resource.get("metadata");
        if(metaMap.get(annotations) == null){
            metaMap.put(annotations,new HashMap<String,String>());
        }
        Map<String,String> annotationsMap = (Map) metaMap.get(K8sResourceClient.annotations);
        annotationsMap.put(AnnotationKey.K8S_HYSCALE_LAST_APPLIED_CONFIGURATION.getAnnotation(),
                GsonProviderUtil.getPrettyGsonBuilder().toJson(resource));

        KubernetesApiResponse<CustomObject> response = genericClient.update(resource);
        if(response!=null){
            if(response.isSuccess()){
                logger.info("Successfully updated resource : {} name : {}",kind,name);
                return;
            }else{
                logger.error("Failed to update, reason: {}\n Message: {}",response.getStatus().getReason(),response.getStatus().getMessage());
            }
        }
        throw new HyscaleException(DeployerErrorCodes.FAILED_TO_CREATE_RESOURCE);
    }

    @Override
    public boolean patch(CustomObject resource) {
        if(resource == null){
            return false;
        }
        String kind = resource.getKind();
        String name = resource.getMetadata().getName();
        CustomObject customObject = get(resource);
        if(customObject != null && customObject.getMetadata() != null && customObject.getMetadata().getAnnotations() != null){
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
                        logger.info("Successfully patched resource {}",kind);
                        return true;
                    }else{
                        logger.error("Failed to patch, reason: {}\n Message: {}",response.getStatus().getReason(),response.getStatus().getMessage());
                    }
                }
            } catch (Exception e) {
                logger.error("Error while creating patch for resource kind : {}, name : {}, error : {}", kind,
                        name, e.toString());
                return false;
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
        DeleteOptions deleteOptions = new DeleteOptions();
        deleteOptions.setKind("DeleteOptions");
        deleteOptions.setApiVersion("v1");
        deleteOptions.setPropagationPolicy("Foreground");
        KubernetesApiResponse<CustomObject> response = genericClient.delete(namespace,name, deleteOptions);
        if(response!=null){
            if(response.isSuccess()){
                logger.info("Successfully deleted resource : {}, name : {}",kind,name);
                waitForResourceDeletion(resource);
                return true;
            }else{
                logger.error("Failed to delete resource {} \nReason : {}",kind,response.getStatus().getReason());
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
        logger.debug("Fetching {}",resource.getKind());
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
        return Collections.emptyList();
    }

    @Override
    public List<CustomObject> getBySelector(String selector) {
        ListOptions listOptions = new ListOptions();
        listOptions.setLabelSelector(selector);
        KubernetesApiResponse<CustomListObject> response = genericClient.list(namespace,listOptions);
        if(response!=null){
            return response.getObject().getItems();
        }
        return Collections.emptyList();
    }

}
