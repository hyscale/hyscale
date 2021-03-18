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

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.hyscale.commons.models.AnnotationKey;
import io.hyscale.commons.models.LoadBalancer;
import io.hyscale.commons.utils.GsonProviderUtil;
import io.hyscale.commons.utils.HyscaleContextUtil;
import io.hyscale.commons.utils.ResourceSelectorUtil;
import io.hyscale.deployer.core.model.CustomResourceKind;
import io.hyscale.deployer.core.model.ResourceKind;
import io.hyscale.deployer.services.client.GenericK8sClient;
import io.hyscale.deployer.services.client.K8sResourceClient;
import io.hyscale.deployer.services.model.CustomObject;
import io.hyscale.deployer.services.model.PodParent;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.models.V1Deployment;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1StatefulSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class PodParentUtil {

    private static final Logger logger = LoggerFactory.getLogger(PodParentUtil.class);

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

    /**
     * This method is responsible for retrieving loadBalancer spec from 'hyscale.io/service-spec' annotation present in deployed Pod Parent resource.
     */
    public static LoadBalancer getLoadBalancerSpec(PodParent podParent) {
        try {
            V1ObjectMeta metadata = null;
            if (ResourceKind.DEPLOYMENT.getKind().equalsIgnoreCase(podParent.getKind())) {
                metadata = ((V1Deployment) podParent.getParent()).getMetadata();
            }
            if (ResourceKind.STATEFUL_SET.getKind().equalsIgnoreCase(podParent.getKind())) {
                metadata = ((V1StatefulSet) podParent.getParent()).getMetadata();
            }
            if (metadata != null) {
                JsonParser jsonParser = new JsonParser();
                JsonObject serviceSpec = (JsonObject) jsonParser.parse(metadata.getAnnotations().get(AnnotationKey.HYSCALE_SERVICE_SPEC.getAnnotation()));
                if (serviceSpec.get("loadBalancer") != null) {
                    return GsonProviderUtil.getPrettyGsonBuilder().fromJson(serviceSpec.get("loadBalancer"), LoadBalancer.class);
                }
            }
        } catch (Exception e) {
            logger.error("Error while retrieving loadBalancer configuration in service spec. Reason :", e);
        }
        return null;
    }
}
