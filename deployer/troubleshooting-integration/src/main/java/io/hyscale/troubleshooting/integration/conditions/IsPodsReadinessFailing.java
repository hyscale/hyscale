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
import io.hyscale.troubleshooting.integration.actions.FixHealthCheckAction;
import io.hyscale.troubleshooting.integration.models.TroubleshootingContext;
import io.hyscale.troubleshooting.integration.util.ConditionUtil;
import io.kubernetes.client.models.V1Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.function.Predicate;

@Component
public class IsPodsReadinessFailing implements Node<TroubleshootingContext> {

    private static final Logger logger = LoggerFactory.getLogger(IsPodsReadinessFailing.class);

    private Predicate<TroubleshootingContext> isPodReadinessFailing;

    private static final String UNHEALTHY_REASON = "Unhealthy";

    @Autowired
    private FixHealthCheckAction fixHealthCheckAction;

    @Autowired
    private MultipleContainerRestartsCondition multipleContainerRestartsCondition;

    @PostConstruct
    public void init() {
        this.isPodReadinessFailing = new Predicate<TroubleshootingContext>() {
            @Override
            public boolean test(TroubleshootingContext context) {
                if (context == null || context.getResourceData() == null) {
                    logger.debug("Cannot troubleshoot without resource data and context");
                    return false;
                }

                TroubleshootingContext.ResourceData resourceData = context.getResourceData().get(ResourceKind.POD.getKind());
                //TODO proper error handling
                if (ConditionUtil.isResourceInValid(resourceData)) {
                    logger.error("Cannot proceed with incomplete resource data {}");
                    return false;
                }

                List<V1Event> eventList = resourceData.getEvents();
                if (eventList == null || eventList.isEmpty()) {
                    //TODO mention that results may not be appropriate because events are lost.
                    return false;
                }

                return eventList.stream().anyMatch(each -> {
                    return UNHEALTHY_REASON.equals(each.getReason());
                });
            }
        };
    }

    @Override
    public Node<TroubleshootingContext> next(TroubleshootingContext context) throws HyscaleException {
        return this.isPodReadinessFailing.test(context) ? fixHealthCheckAction : multipleContainerRestartsCondition;
    }

    @Override
    public String describe()  {
        return "Readiness failing ?";
    }

    @Override
    public boolean test(TroubleshootingContext context) throws HyscaleException {
        return this.isPodReadinessFailing.test(context);
    }
}
