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

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.deployer.core.model.ResourceKind;
import io.hyscale.troubleshooting.integration.models.Node;
import io.hyscale.troubleshooting.integration.models.ActionMessage;
import io.hyscale.troubleshooting.integration.models.TroubleshootingContext;
import io.hyscale.troubleshooting.integration.util.ConditionUtil;
import io.hyscale.troubleshooting.integration.util.TroubleshootUtil;
import io.kubernetes.client.models.V1Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

@Component
public class PendingPvcAction implements Node<TroubleshootingContext> {

    private static final Logger logger = LoggerFactory.getLogger(PendingPvcAction.class);

    private static final String PROVISIONING_FAILED = "ProvisioningFailed";
    private static final String STORAGE_CLASS_NOTFOUND = "storageclass[\\w\\.\\s\\\"]*not found";
    private static final Pattern pattern = Pattern.compile(STORAGE_CLASS_NOTFOUND);

    @Override
    public Node<TroubleshootingContext> next(TroubleshootingContext context) throws HyscaleException {
        if (TroubleshootUtil.validateContext(context)) {
            logger.debug("Cannot troubleshoot without resource data and context");
            return null;
        }

        TroubleshootingContext.ResourceData resourceData = context.getResourceData().get(ResourceKind.PERSISTENT_VOLUME_CLAIM.getKind());
        //TODO proper error handling
        if (ConditionUtil.isResourceInValid(resourceData)) {
            logger.error("Cannot proceed with incomplete resource data {}");
            return null;
        }

        List<V1Event> eventList = resourceData.getEvents();
        if (eventList == null || eventList.isEmpty()) {
            return null;
        }

        // ProvisioningFailed
        AtomicReference<String> volume = null;
        boolean provsioningFailed = eventList.stream().anyMatch(each -> {
            volume.set(each.getMetadata().getName());
            return PROVISIONING_FAILED.equals(each.getReason()) && pattern.matcher(each.getMessage()).matches();
        });

        WorkflowLogger.debug(ActionMessage.INVALID_STORAGE_CLASS, volume.get(), context.getServiceInfo().getServiceName());

        return null;
    }

    @Override
    public String describe()  {
        return null;
    }

    @Override
    public boolean test(TroubleshootingContext context) throws HyscaleException {
        return false;
    }
}
