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
import io.hyscale.troubleshooting.integration.models.Node;
import io.hyscale.troubleshooting.integration.actions.ContactClusterAdministratorAction;
import io.hyscale.troubleshooting.integration.actions.PendingPvcAction;
import io.hyscale.troubleshooting.integration.constants.TroubleshootConstants;
import io.hyscale.troubleshooting.integration.models.TroubleshootingContext;
import io.hyscale.troubleshooting.integration.util.ConditionUtil;
import io.hyscale.troubleshooting.integration.util.TroubleshootUtil;
import io.kubernetes.client.models.V1Event;
import io.kubernetes.client.models.V1PersistentVolumeClaim;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.function.Predicate;

@Component
public class AnyPendingPVCCondition implements Node<TroubleshootingContext> {

    private static final Logger logger = LoggerFactory.getLogger(AnyPendingPVCCondition.class);

    private Predicate<TroubleshootingContext> pendingPvcCondition;

    @Autowired
    private PendingPvcAction pendingPvcAction;

    @Autowired
    private ContactClusterAdministratorAction contactClusterAdministratorAction;

    @PostConstruct
    public void init() {
        this.pendingPvcCondition = new Predicate<TroubleshootingContext>() {
            @Override
            public boolean test(TroubleshootingContext context) {
                if (TroubleshootUtil.validateContext(context)) {
                    logger.debug("Cannot troubleshoot without resource data and context");
                    return false;
                }

                TroubleshootingContext.ResourceData resourceData = context.getResourceData().get(ResourceKind.PERSISTENT_VOLUME_CLAIM.getKind());
                //TODO proper error handling
                if (ConditionUtil.isResourceInValid(resourceData)) {
                    logger.error("Cannot proceed with incomplete resource data {}");
                    return false;
                }

                List<Object> pvcList = resourceData.getResource();
                if (pvcList != null || pvcList.isEmpty()) {
                    logger.debug("PVC List if found empty for service {}", context.getServiceInfo().getServiceName());
                    return false;
                }
                boolean anyPendingPvc = pvcList.stream().anyMatch(each -> {
                    if (each instanceof V1PersistentVolumeClaim) {
                        V1PersistentVolumeClaim persistentVolumeClaim = (V1PersistentVolumeClaim) each;
                        String pvcPhase = persistentVolumeClaim.getStatus().getPhase();
                        return pvcPhase != null ? pvcPhase.equals(TroubleshootConstants.PENDING_PHASE) : false;
                    }
                    return false;
                });
                return anyPendingPvc;
            }
        };
    }

    @Override
    public Node<TroubleshootingContext> next(TroubleshootingContext context) throws HyscaleException {
        return this.pendingPvcCondition.test(context) ? pendingPvcAction : contactClusterAdministratorAction;
    }

    @Override
    public String describe() {
        return "Are there any pending pvc's ?? ";
    }

    @Override
    public boolean test(TroubleshootingContext context) throws HyscaleException {
        return this.pendingPvcCondition.test(context);
    }
}
