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
package io.hyscale.troubleshooting.integration.conditions;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.utils.HyscaleContextUtil;
import io.hyscale.deployer.core.model.ResourceKind;
import io.hyscale.deployer.services.util.K8sPodUtil;
import io.hyscale.troubleshooting.integration.models.Node;
import io.hyscale.troubleshooting.integration.models.PodStatus;
import io.hyscale.troubleshooting.integration.models.TroubleshootingContext;
import io.kubernetes.client.models.V1Pod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * This is a condition to check whether a kubernetes pod
 * has a status in @{@link PodStatus} . The condition is
 * checked based on the pod status from the {@link TroubleshootingContext}.
 * The status of all replicas is determined and aggregated when the first
 * failed state of any replica is encountered. Troubleshooting is
 * performed on this podstatus that is derived.
 */

@Component
public class PodStatusCondition implements Node<TroubleshootingContext> {

    private static final Logger logger = LoggerFactory.getLogger(PodStatusCondition.class);

    @Override
    public Node<TroubleshootingContext> next(TroubleshootingContext context) throws HyscaleException {
        if (context == null || context.getResourceData() == null) {
            logger.debug("Cannot troubleshoot without resource data and context");
            return null;
        }
        TroubleshootingContext.ResourceData resourceData = context.getResourceData().get(ResourceKind.POD.getKind());
        //TODO proper exception handling
        if (resourceData == null || resourceData.getResource() == null || resourceData.getResource().isEmpty()) {
            logger.debug("Cannot troubleshoot without resource details");
            return null;
        }

        List<Object> podsList = resourceData.getResource();
        if (podsList == null || podsList.isEmpty()) {
            //TODO Log about result accuracy
            return null;
        }
        PodStatus effectivePodStatus = PodStatus.DEFAULT;
        for (Object each : podsList) {
            if (each instanceof V1Pod) {
                V1Pod v1Pod = (V1Pod) each;
                String aggregatedStatus = K8sPodUtil.getAggregatedStatusOfContainersForPod(v1Pod);
                if (StringUtils.isEmpty(aggregatedStatus)) {
                    continue;
                }
                effectivePodStatus = PodStatus.get(aggregatedStatus);
                if (!effectivePodStatus.isFailed()) {
                    continue;
                }
                return HyscaleContextUtil.getSpringBean(effectivePodStatus.getNextNode());
            }
        }
        return null;
    }

    @Override
    public String describe() {
        return "Checks for pod status condition";
    }

    @Override
    public boolean test(TroubleshootingContext context) throws HyscaleException {
        return false;
    }
}
