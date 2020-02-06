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
import io.hyscale.deployer.core.model.ResourceKind;
import io.hyscale.deployer.services.model.PodCondition;
import io.hyscale.deployer.services.util.K8sPodUtil;
import io.hyscale.troubleshooting.integration.models.Node;
import io.hyscale.troubleshooting.integration.models.TroubleshootingContext;
import io.kubernetes.client.models.V1Pod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.function.Predicate;

/**
 * This class checks for any Unschedulable pod or any pod with {@link PodCondition#POD_SCHEDULED}
 * condition = true . In either of the cases the pod is not scheduled to any node. To know more
 * about pod condition, refer {@see https://kubernetes.io/docs/concepts/workloads/pods/pod-lifecycle/#pod-conditions}
 */

@Component
public class PodScheduleCondition implements Node<TroubleshootingContext> {

    private static final Logger logger = LoggerFactory.getLogger(PodScheduleCondition.class);

    private Predicate<TroubleshootingContext> podSchedulePredicate;

    @PostConstruct
    public void init() {
        this.podSchedulePredicate = new Predicate<TroubleshootingContext>() {

            @Override
            public boolean test(TroubleshootingContext context) {
                if (context == null || context.getResourceData() == null) {
                    logger.debug("Cannot troubleshoot without resource data and context");
                    return false;
                }
                TroubleshootingContext.ResourceData resourceData = context.getResourceData().get(ResourceKind.POD.getKind());
                //TODO proper exception handling
                if (resourceData == null || resourceData.getResource() == null || resourceData.getResource().isEmpty()) {
                    logger.debug("Cannot troubleshoot without resource details");
                    return false;
                }


                List<Object> podList = resourceData.getResource();
                return podList.stream().anyMatch(pod -> {
                    if (pod instanceof V1Pod) {
                        V1Pod v1Pod = (V1Pod) pod;
                        return !K8sPodUtil.checkForPodCondition((V1Pod) pod, PodCondition.POD_SCHEDULED) ||
                                K8sPodUtil.checkForPodCondition(v1Pod, PodCondition.UNSCHEDULABLE);
                    }
                    return false;
                });
            }
        };
    }

    @Override
    public Node<TroubleshootingContext> next(TroubleshootingContext context) throws HyscaleException {
        return test(context) ? null : null;
    }

    @Override
    public String describe() {
        return "Are all pods scheduled ?";
    }

    @Override
    public boolean test(TroubleshootingContext context) throws HyscaleException {
        return this.podSchedulePredicate.test(context);
    }
}
