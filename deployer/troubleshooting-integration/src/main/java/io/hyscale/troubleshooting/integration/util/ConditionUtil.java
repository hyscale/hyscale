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
package io.hyscale.troubleshooting.integration.util;

import io.hyscale.deployer.core.model.ResourceKind;
import io.hyscale.troubleshooting.integration.models.TroubleshootingContext;
import io.kubernetes.client.openapi.models.V1Pod;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ConditionUtil {
    
    private ConditionUtil() {}
    
    /**
     * Pod parent can be deployment or statefulset
     * Preference is given to statefulset if both are present
     * 
     * @param context
     * @return Pod parent kind
     */
    public static ResourceKind getPodParent(TroubleshootingContext context) {

        if (context == null || context.getResourceInfos() == null) {
            return null;
        }
        List<TroubleshootingContext.ResourceInfo> resourceInfos = context.getResourceInfos()
                .getOrDefault(ResourceKind.STATEFUL_SET.getKind(), null);

        if (resourceInfos != null && !resourceInfos.isEmpty()) {
            return ResourceKind.STATEFUL_SET;
        }

        resourceInfos = context.getResourceInfos().getOrDefault(ResourceKind.DEPLOYMENT.getKind(), null);

        if (resourceInfos != null && !resourceInfos.isEmpty()) {
            return ResourceKind.DEPLOYMENT;
        }

        return null;
    }
    
    /**
     * @param context
     * @return list of pods from context resource info
     */
    public static List<V1Pod> getPods(TroubleshootingContext context) {
        List<V1Pod> podList = Collections.emptyList();
        if (context == null || context.getResourceInfos() == null) {
            return podList;
        }
        List<TroubleshootingContext.ResourceInfo> resourceInfos = context.getResourceInfos()
                .get(ResourceKind.POD.getKind());
        if (resourceInfos == null || resourceInfos.isEmpty()) {
            return podList;
        }
        podList = resourceInfos.stream().filter(each -> each != null && each.getResource() instanceof V1Pod)
                .map(pod -> (V1Pod) pod.getResource()).collect(Collectors.toList());

        return podList;
    }

}
