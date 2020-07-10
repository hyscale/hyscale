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

import java.util.List;

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

}
