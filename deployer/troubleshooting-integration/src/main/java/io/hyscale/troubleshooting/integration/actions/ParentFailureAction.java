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
package io.hyscale.troubleshooting.integration.actions;

import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import io.hyscale.deployer.core.model.ResourceKind;
import io.hyscale.troubleshooting.integration.models.AbstractedErrorMessage;
import io.hyscale.troubleshooting.integration.models.ActionNode;
import io.hyscale.troubleshooting.integration.models.DiagnosisReport;
import io.hyscale.troubleshooting.integration.models.TroubleshootingContext;
import io.hyscale.troubleshooting.integration.util.PodParentTroubleshootUtil;
import io.kubernetes.client.openapi.models.V1Event;
import io.kubernetes.client.openapi.models.V1ObjectReference;

/**
 * Action node handles case when pods are not available but pod owner is available
 * @author tushar
 *
 */
@Component
public class ParentFailureAction extends ActionNode<TroubleshootingContext> {

    private static final Logger logger = LoggerFactory.getLogger(ParentFailureAction.class);

    public static final String NORMAL_EVENT = "Normal";

    public static final String FAILED_CREATE_EVENT = "FailedCreate";

    private static final String INVALID_VOLUME_NAME = "spec\\.volumes\\[\\d\\]\\.name";

    private static final String INVALID_RESOURCE_NAME = "metadata\\.labels: Invalid value";

    private static final Pattern invalidVolumeNamePattern = Pattern.compile(INVALID_VOLUME_NAME);

    private static final Pattern invalidResourceNamePattern = Pattern.compile(INVALID_RESOURCE_NAME);

    @Override
    public void process(TroubleshootingContext context) {
        ResourceKind podParent = PodParentTroubleshootUtil.getPodParent(context);
        if (podParent == null) {
            return;
        }
        List<TroubleshootingContext.ResourceInfo> resourceInfos = context.getResourceInfos()
                .getOrDefault(podParent.getKind(), null);
        if (resourceInfos == null || resourceInfos.isEmpty()) {
            logger.debug("Pod owner {} not found in context", podParent);
            return;
        }

        // Only one resource of parent should exist
        TroubleshootingContext.ResourceInfo parentInfo = resourceInfos.get(0);
        List<V1Event> events = parentInfo.getEvents();
        if (events == null || events.isEmpty()) {
            logger.debug(podParent + " no events found");
            return;
        }
        V1Event event = getFilteredEvent(events);
        if (event == null) {
            logger.debug(podParent + " no failure event found to process");
            return;
        }
        DiagnosisReport report = new DiagnosisReport();
        if (invalidVolumeNamePattern.matcher(event.getMessage()).find()) {
            report.setReason(AbstractedErrorMessage.INVALID_VOLUME_NAME.getReason());
            report.setRecommendedFix(AbstractedErrorMessage.INVALID_VOLUME_NAME.getMessage());
            context.addReport(report);
            return;
        }
        if (invalidResourceNamePattern.matcher(event.getMessage()).find()) {
            V1ObjectReference eventRefObj = event.getInvolvedObject();
            String resourceDetails = eventRefObj.getKind() + " " + eventRefObj.getName();
            report.setReason(AbstractedErrorMessage.INVALID_RESOURCE_NAME.formatReason(resourceDetails));
            report.setRecommendedFix(AbstractedErrorMessage.INVALID_RESOURCE_NAME.getMessage());
            context.addReport(report);
            return;
        }
    }

    private V1Event getFilteredEvent(List<V1Event> events) {
        V1Event filteredEvent = null;
        for (V1Event event : events) {
            if (FAILED_CREATE_EVENT.equals(event.getReason())) {
                filteredEvent = event;
            }
        }
        return filteredEvent;
    }

    @Override
    public String describe() {
        return "Kubernetes controller failed to create pods";
    }

}
