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
import io.hyscale.troubleshooting.integration.models.Node;
import io.hyscale.troubleshooting.integration.actions.FixHealthCheckAction;
import io.hyscale.troubleshooting.integration.models.TroubleshootingContext;
import io.hyscale.troubleshooting.integration.util.ConditionUtil;
import io.kubernetes.client.models.V1Pod;
import io.kubernetes.client.models.V1PodCondition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.function.Predicate;

@Component
public class ArePodsReady implements Node<TroubleshootingContext> {

    private static final Logger logger = LoggerFactory.getLogger(ArePodsReady.class);

    private Predicate<TroubleshootingContext> podsReadyCondition;

    @Autowired
    private FixHealthCheckAction fixHealthCheckAction;

    @PostConstruct
    public void init() {
        this.podsReadyCondition = new Predicate<TroubleshootingContext>() {
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

                List<Object> podsList = resourceData.getResource();
                if (podsList == null && podsList.isEmpty()) {
                    return false;
                }
                return podsList.stream().filter(each -> {
                    return each instanceof V1Pod;
                }).allMatch(each -> {
                    V1Pod pod = (V1Pod) each;
                    for (V1PodCondition condition : pod.getStatus().getConditions()) {
                        if (condition.getType().equals(PodCondition.READY.getPodCondition())) {
                            return condition.getStatus().equals("True");
                        }
                    }
                    return false;
                });
            }
        };
    }

    @Override
    public Node<TroubleshootingContext> next(TroubleshootingContext context) throws HyscaleException {
        return this.podsReadyCondition.test(context) ? null : fixHealthCheckAction;
    }

    @Override
    public String describe() {
        return "Are all pods ready ?";
    }

    @Override
    public boolean test(TroubleshootingContext context) throws HyscaleException {
        return this.podsReadyCondition.test(context);
    }
}
