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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.deployer.core.model.ResourceKind;
import io.hyscale.deployer.services.factory.PodParentFactory;
import io.hyscale.deployer.services.handler.PodParentHandler;
import io.hyscale.troubleshooting.integration.actions.ParentFailureAction;
import io.hyscale.troubleshooting.integration.actions.ServiceNotDeployedAction;
import io.hyscale.troubleshooting.integration.actions.ServiceWithZeroReplicasAction;
import io.hyscale.troubleshooting.integration.actions.TryAfterSometimeAction;
import io.hyscale.troubleshooting.integration.models.AbstractedErrorMessage;
import io.hyscale.troubleshooting.integration.models.DiagnosisReport;
import io.hyscale.troubleshooting.integration.models.FailedResourceKey;
import io.hyscale.troubleshooting.integration.models.Node;
import io.hyscale.troubleshooting.integration.models.TroubleshootingContext;
import io.hyscale.troubleshooting.integration.models.TroubleshootingContext.ResourceInfo;
import io.hyscale.troubleshooting.integration.util.ConditionUtil;
import io.kubernetes.client.openapi.models.CoreV1Event;

@Component
public class ParentStatusCondition implements Node<TroubleshootingContext> {
    
    private static final Logger logger = LoggerFactory.getLogger(ParentStatusCondition.class);
    
    public static final String FAILED_CREATE_EVENT = "FailedCreate";
    
    @Autowired
    private ServiceNotDeployedAction serviceNotDeployedAction;
    
    @Autowired
    private ServiceWithZeroReplicasAction serviceWithZeroReplicasAction;
    
    @Autowired
    private ParentFailureAction parentFailureAction;
    
    @Autowired
    private TryAfterSometimeAction tryAfterSometimeAction;

    @Override
    public Node<TroubleshootingContext> next(TroubleshootingContext context) throws HyscaleException {
        if (context.getResourceInfos() == null) {
            return serviceNotDeployedAction;
        }
        
        ResourceKind podParent = ConditionUtil.getPodParent(context);
        if (context.isTrace()) {
            String describe = describe();
            logger.debug("{}, pod parent {}", describe, podParent);
        }
        if (podParent == null) {
            return serviceNotDeployedAction;
        }
        List<TroubleshootingContext.ResourceInfo> resourceInfos = context.getResourceInfos()
                .getOrDefault(podParent.getKind(), null);
        if (resourceInfos == null || resourceInfos.isEmpty()) {
            logger.debug("Pod owner {} not found in context", podParent);
            return serviceNotDeployedAction;
        }
        // Only one resource of parent should exist
        TroubleshootingContext.ResourceInfo parentInfo = resourceInfos.get(0);
        if (hasZeroReplicas(parentInfo, podParent)) {
            return serviceWithZeroReplicasAction;
        }
        DiagnosisReport report = new DiagnosisReport();
        List<CoreV1Event> events = parentInfo.getEvents();
        if (events == null || events.isEmpty()) {
            report.setReason(AbstractedErrorMessage.CANNOT_FIND_EVENTS.getReason());
            report.setRecommendedFix(AbstractedErrorMessage.CANNOT_FIND_EVENTS.getMessage());
            context.addReport(report);
            logger.debug("{} no events found", podParent);
            return null;
        }
        CoreV1Event event = getFilteredEvent(events);
        if (event == null) {
            logger.debug("{} no failure event found to process", podParent);
            return tryAfterSometimeAction;
        }
        
        context.addAttribute(FailedResourceKey.FAILED_PARENT_EVENT, event);
        return parentFailureAction;
    }
    
    private boolean hasZeroReplicas(ResourceInfo parentInfo, ResourceKind podParent) {
        PodParentHandler podParentHandler = PodParentFactory.getHandler(podParent.getKind());
        
        Integer replicas = podParentHandler.getReplicas(parentInfo.getResource());
        
        return replicas != null && replicas == 0;
    }

    // TODO filter events wrt latest deployment
    private CoreV1Event getFilteredEvent(List<CoreV1Event> events) {
        CoreV1Event filteredEvent = null;
        for (CoreV1Event event : events) {
            if (FAILED_CREATE_EVENT.equals(event.getReason())) {
                filteredEvent = event;
            }
        }
        return filteredEvent;
    }

    @Override
    public String describe() {
        return "Checks for pod parent status and process events";
    }

}
