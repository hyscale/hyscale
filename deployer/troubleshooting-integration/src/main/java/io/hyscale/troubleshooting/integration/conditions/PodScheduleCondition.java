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
import io.hyscale.deployer.services.model.PodCondition;
import io.hyscale.deployer.services.util.K8sPodUtil;
import io.hyscale.troubleshooting.integration.errors.TroubleshootErrorCodes;
import io.hyscale.troubleshooting.integration.models.*;
import io.hyscale.troubleshooting.integration.util.ConditionUtil;
import io.hyscale.troubleshooting.integration.util.DiagnosisReportUtil;
import io.kubernetes.client.openapi.models.V1Pod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * This class checks for any Unschedulable pod or any pod with {@link PodCondition#POD_SCHEDULED}
 * condition = true . In either of the cases the pod is not scheduled to any node. To know more
 * about <a href="https://kubernetes.io/docs/concepts/workloads/pods/pod-lifecycle/#pod-conditions">pod condition</a>
 */

@Component
public class PodScheduleCondition extends ConditionNode<TroubleshootingContext> {

    private static final Logger logger = LoggerFactory.getLogger(PodScheduleCondition.class);

    @Override
    public boolean decide(TroubleshootingContext context) throws HyscaleException {
        String serviceName = context.getServiceMetadata().getServiceName();

        List<V1Pod> podsList = ConditionUtil.getPods(context);

        if (podsList == null || podsList.isEmpty()) {
            logger.debug("No pods found for service: {}", serviceName);
            context.addReport(DiagnosisReportUtil.getServiceNotDeployedReport(serviceName));
            throw new HyscaleException(TroubleshootErrorCodes.SERVICE_IS_NOT_DEPLOYED, serviceName);
        }

        return podsList.stream().anyMatch(pod -> !K8sPodUtil.checkForPodCondition(pod, PodCondition.POD_SCHEDULED)
                || K8sPodUtil.checkForPodCondition(pod, PodCondition.UNSCHEDULABLE));
    }
    
    @Override
    public Node<TroubleshootingContext> onSuccess() {
        return null;
    }

    @Override
    public Node<TroubleshootingContext> onFailure() {
        return null;
    }


    @Override
    public String describe() {
        return "Are all pods scheduled ?";
    }

}
