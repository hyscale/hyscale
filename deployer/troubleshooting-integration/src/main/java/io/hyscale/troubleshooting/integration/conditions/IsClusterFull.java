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
import io.hyscale.troubleshooting.integration.errors.TroubleshootErrorCodes;
import io.hyscale.troubleshooting.integration.models.*;
import io.hyscale.troubleshooting.integration.actions.ClusterFullAction;
import io.kubernetes.client.openapi.models.V1Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

//TODO JAVADOC
// Exception Handling
@Component
public class IsClusterFull extends ConditionNode<TroubleshootingContext> {

    private static final Logger logger = LoggerFactory.getLogger(IsClusterFull.class);

    @Autowired
    private ClusterFullAction clusterFullAction;

    @Autowired
    private AnyPendingPVCCondition pendingPVCCondition;

    private static final String INSUFFICIENT_MEMORY_REGEX = "\\d\\/\\d nodes are available: \\d+ Insufficient memory.";
    private static final String FAILED_SCHEDULING = "FailedScheduling";
    private static final Pattern pattern = Pattern.compile(INSUFFICIENT_MEMORY_REGEX);


    @Override
    public boolean decide(TroubleshootingContext context) throws HyscaleException {
        List<TroubleshootingContext.ResourceInfo> resourceInfos = context.getResourceInfos().get(ResourceKind.POD.getKind());
        DiagnosisReport report = new DiagnosisReport();
        if (resourceInfos == null || resourceInfos.isEmpty()) {
            report.setReason(AbstractedErrorMessage.SERVICE_NOT_DEPLOYED.formatReason(context.getServiceMetadata().getServiceName()));
            report.setRecommendedFix(AbstractedErrorMessage.SERVICE_NOT_DEPLOYED.getMessage());
            context.addReport(report);
            throw new HyscaleException(TroubleshootErrorCodes.SERVICE_IS_NOT_DEPLOYED, context.getServiceMetadata().getServiceName());
        }

        Object obj = context.getAttribute(FailedResourceKey.FAILED_POD_EVENTS);
        if (obj == null) {
            String describe = describe();
            logger.debug("Cannot find any failed pod for node: {}", describe);
            return false;
        }

        List<V1Event> eventList = (List<V1Event>) FailedResourceKey.FAILED_POD_EVENTS.getKlazz().cast(obj);

        if (eventList == null || eventList.isEmpty()) {
            report.setReason(AbstractedErrorMessage.CANNOT_FIND_EVENTS.getReason());
            report.setRecommendedFix(AbstractedErrorMessage.CANNOT_FIND_EVENTS.getMessage());
            context.addReport(report);
            return false;
        }

        return eventList.stream().anyMatch(
                event -> FAILED_SCHEDULING.equals(event.getReason()) && pattern.matcher(event.getMessage()).find());
    }


    @Override
    public Node<TroubleshootingContext> onSuccess() {
        return clusterFullAction;
    }

    @Override
    public Node<TroubleshootingContext> onFailure() {
        return pendingPVCCondition;
    }

    @Override
    public String describe() {
        return "Is Cluster Full ?";
    }

}
