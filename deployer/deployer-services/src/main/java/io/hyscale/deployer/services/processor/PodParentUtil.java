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
package io.hyscale.deployer.services.processor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.hyscale.commons.models.AnnotationKey;
import io.hyscale.commons.utils.HyscaleContextUtil;
import io.hyscale.commons.utils.ResourceSelectorUtil;
import io.hyscale.deployer.core.model.CustomResourceKind;
import io.hyscale.deployer.services.client.GenericK8sClient;
import io.hyscale.deployer.services.client.K8sResourceClient;
import io.hyscale.deployer.services.model.CustomObject;
import io.hyscale.deployer.services.model.PodParent;
import io.kubernetes.client.openapi.ApiClient;

public class PodParentUtil {

    private ApiClient apiClient;
    private String namespace;

    public PodParentUtil(ApiClient apiClient, String namespace){
        this.apiClient = apiClient;
        this.namespace = namespace;
        
    }

    public PodParent getPodParentForService(String serviceName){
        for(CustomResourceKind customResourceKind : HyscaleContextUtil.getSpringBean(WorkloadKinds.class).get()) {
            GenericK8sClient genericK8sClient = new K8sResourceClient(apiClient).
                    withNamespace(namespace).forKind(customResourceKind);
            CustomObject resource = genericK8sClient.getResourceByName(serviceName);
            if(resource!=null){
                return new PodParent(resource.getKind(),resource);
            }
        }
        return null;
    }

    public Map<String,PodParent> getServiceVsPodParentMap(String appName){
        String selector = ResourceSelectorUtil.getServiceSelector(appName,null);
        Map<String,PodParent> serviceVsPodParents = new HashMap<>();
        GenericK8sClient genericK8sClient = null;
        List<CustomObject> workloadResources = new ArrayList<>();
        
		for (CustomResourceKind customResourceKind : HyscaleContextUtil.getSpringBean(WorkloadKinds.class).get()) {
			genericK8sClient = new K8sResourceClient(apiClient).withNamespace(namespace).forKind(customResourceKind);
			// Fetch Deployments
			workloadResources.addAll(genericK8sClient.getBySelector(selector));
		}
        
        if(workloadResources != null && !workloadResources.isEmpty()){
            for(CustomObject customObject :workloadResources){
                if(customObject.getMetadata() != null){
                    String serviceName = customObject.getMetadata().getName();
                    PodParent podParent = new PodParent(customObject.getKind(),customObject);
                    serviceVsPodParents.put(serviceName,podParent);
                }
            }
            return serviceVsPodParents;
        }
        return null;
    }

    public List<CustomResourceKind> getAppliedKindsList(PodParent podParent){
        if(podParent!= null && podParent.getParent()!=null){
            CustomObject resource = (CustomObject) podParent.getParent();
            if(resource.getMetadata() != null){
                Map<String,String> annotations = resource.getMetadata().getAnnotations();
                String appliedKindsStr = annotations.get(AnnotationKey.HYSCALE_APPLIED_KINDS.getAnnotation());
                List<String> appliedKindsList = Arrays.asList(appliedKindsStr.substring(1,appliedKindsStr.length()-1).replaceAll("\\s","").split(","));
                List<CustomResourceKind> customResourceKinds = new ArrayList<>();
                for(String appliedKind : appliedKindsList){
                    String[] csr = appliedKind.split(":");
                    String kind = csr[0];
                    String apiVersion = csr[1];
                    CustomResourceKind customResourceKind = new CustomResourceKind(kind,apiVersion);
                    customResourceKinds.add(customResourceKind);
                }
                return customResourceKinds;
            }
        }
        return Collections.emptyList();
    }
}
