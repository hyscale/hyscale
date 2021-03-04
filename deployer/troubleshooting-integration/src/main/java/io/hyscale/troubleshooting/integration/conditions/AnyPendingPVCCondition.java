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
import io.hyscale.troubleshooting.integration.models.*;
import io.hyscale.troubleshooting.integration.actions.ContactClusterAdministratorAction;
import io.hyscale.troubleshooting.integration.actions.PendingPvcAction;
import io.hyscale.troubleshooting.integration.constants.TroubleshootConstants;
import io.kubernetes.client.openapi.models.V1PersistentVolumeClaim;
import io.kubernetes.client.openapi.models.V1Pod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Component
public class AnyPendingPVCCondition extends ConditionNode<TroubleshootingContext> {

    private static final Logger logger = LoggerFactory.getLogger(AnyPendingPVCCondition.class);

    private Predicate<TroubleshootingContext> pendingPvcCondition;

    @Autowired
    private PendingPvcAction pendingPvcAction;

    @Autowired
    private ContactClusterAdministratorAction contactClusterAdministratorAction;

    @PostConstruct
    public void init() {
        this.pendingPvcCondition = context -> {
            List<TroubleshootingContext.ResourceInfo> resourceData = context.getResourceInfos().get(ResourceKind.PERSISTENT_VOLUME_CLAIM.getKind());
            
            // Since there are no pvc's found for the service, there's not pending pvc
            if (resourceData == null || resourceData.isEmpty()) {
                logger.debug("No PVC's found for service {}", context.getServiceMetadata().getServiceName());
                return false;
            }
            
            Object obj = context.getAttribute(FailedResourceKey.FAILED_POD);
            if (obj == null) {
                String describe = describe();
                logger.debug("Cannot find any failed pod for {}", describe);
                return false;
            }
            
            V1Pod pod = (V1Pod) FailedResourceKey.FAILED_POD.getKlazz().cast(obj);
            
            // Get all the pvc names associated to the failed pod
            List<String> podPvcList = pod.getSpec().getVolumes().stream()
                    .map(each -> each.getPersistentVolumeClaim() != null
                            && each.getPersistentVolumeClaim().getClaimName() != null
                                    ? each.getPersistentVolumeClaim().getClaimName()
                                    : null)
                    .collect(Collectors.toList());            
            
            // get all the pvc list for this particular failed pod from context
            List<V1PersistentVolumeClaim> pvcList = resourceData.stream().filter(each -> {
                if (each != null && each.getResource() instanceof V1PersistentVolumeClaim) {
                    V1PersistentVolumeClaim persistentVolumeClaim = (V1PersistentVolumeClaim) each.getResource();
                    return podPvcList.contains(persistentVolumeClaim.getMetadata().getName());
                }
                return false;
            }).map(each -> (V1PersistentVolumeClaim) each.getResource()).collect(Collectors.toList());
            
            // Since there are no pvc's found for the service, there's not pending pvc
            if (pvcList == null || pvcList.isEmpty()) {
                logger.debug("PVC List if found empty for service {}", context.getServiceMetadata().getServiceName());
                return false;
            }
            return pvcList.stream().filter(each -> each instanceof V1PersistentVolumeClaim).anyMatch(each -> {
                V1PersistentVolumeClaim persistentVolumeClaim = each;
                String pvcPhase = persistentVolumeClaim.getStatus().getPhase();
                return TroubleshootConstants.PENDING_PHASE.equals(pvcPhase);
            });
        };
    }

    @Override
    public boolean decide(TroubleshootingContext context) throws HyscaleException {
        return this.pendingPvcCondition.test(context);
    }

    @Override
    public Node<TroubleshootingContext> onSuccess() {
        return pendingPvcAction;
    }

    @Override
    public Node<TroubleshootingContext> onFailure() {
        return contactClusterAdministratorAction;
    }

    @Override
    public String describe() {
        return "Are there any pending pvc's ? ";
    }

}
