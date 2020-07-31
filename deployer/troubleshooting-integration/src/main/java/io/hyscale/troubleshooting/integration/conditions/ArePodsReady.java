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
import io.hyscale.troubleshooting.integration.errors.TroubleshootErrorCodes;
import io.hyscale.troubleshooting.integration.models.*;
import io.hyscale.troubleshooting.integration.util.ConditionUtil;
import io.hyscale.troubleshooting.integration.util.DiagnosisReportUtil;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodCondition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

//TODO JAVADOC
@Component
public class ArePodsReady extends ConditionNode<TroubleshootingContext> {

    private static final Logger logger = LoggerFactory.getLogger(ArePodsReady.class);

    @Autowired
    private MultipleContainerRestartsCondition multipleContainerRestartsCondition;

    @Override
    public boolean decide(TroubleshootingContext context) throws HyscaleException {

        List<V1Pod> podsList = ConditionUtil.getPods(context);
        String serviceName = context.getServiceMetadata().getServiceName();

        if (podsList == null || podsList.isEmpty()) {
            logger.debug("No pods found for service: {}", serviceName);
            context.addReport(DiagnosisReportUtil.getServiceNotDeployedReport(serviceName));
            throw new HyscaleException(TroubleshootErrorCodes.SERVICE_IS_NOT_DEPLOYED, serviceName);
        }

        // check if all pods are in ready state with the pod condition ready
        return podsList.stream().filter(Objects::nonNull).allMatch(pod -> {
            boolean ready = true;
            for (V1PodCondition condition : pod.getStatus().getConditions()) {
                if (condition.getType().equals(PodCondition.READY.getPodCondition())
                        && condition.getStatus().equals("False")) {
                    ready = false;
                    context.addAttribute(FailedResourceKey.FAILED_POD, pod);
                    context.addAttribute(FailedResourceKey.UNREADY_POD, pod);
                    break;
                }
            }
            return ready;
        });
    }


    @Override
    public Node<TroubleshootingContext> onSuccess() {
        return null;
    }

    @Override
    public Node<TroubleshootingContext> onFailure() {
        return multipleContainerRestartsCondition;
    }

    @Override
    public String describe() {
        return "Are all pods ready ?";
    }

}
