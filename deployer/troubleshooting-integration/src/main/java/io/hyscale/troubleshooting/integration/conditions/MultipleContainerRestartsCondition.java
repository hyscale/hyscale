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
import io.hyscale.troubleshooting.integration.actions.FixCrashingApplication;
import io.hyscale.troubleshooting.integration.actions.FixImageNameAction;
import io.hyscale.troubleshooting.integration.models.TroubleshootingContext;
import io.hyscale.troubleshooting.integration.util.ConditionUtil;
import io.kubernetes.client.models.V1Pod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.function.Predicate;

@Component
public class MultipleContainerRestartsCondition implements Node<TroubleshootingContext> {

    private static final Logger logger = LoggerFactory.getLogger(MultipleContainerRestartsCondition.class);

    private Predicate<TroubleshootingContext> multipleContainerRestarts;

    @Autowired
    private FixImageNameAction fixImageNameAction;

    @Autowired
    private FixCrashingApplication fixCrashingApplication;

    @PostConstruct
    public void init() {
        this.multipleContainerRestarts = new Predicate<TroubleshootingContext>() {
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
                List<Object> podList = resourceData.getResource();
                if (podList == null || podList.isEmpty()) {
                    //Log it user
                    return false;
                }
                return podList.stream().anyMatch(each -> {
                    if (each instanceof V1Pod) {
                        V1Pod pod = (V1Pod) each;
                        return pod.getStatus().getContainerStatuses().stream().anyMatch(containerStatus -> {
                            return containerStatus.getRestartCount() > 0;
                        });
                    }
                    return false;
                });
            }
        };
    }

    @Override
    public Node<TroubleshootingContext> next(TroubleshootingContext context) throws HyscaleException {
        return this.multipleContainerRestarts.test(context) ? fixCrashingApplication : fixImageNameAction;
    }

    @Override
    public String describe()  {
        return "Multiple container restarts ?";
    }

    @Override
    public boolean test(TroubleshootingContext context) throws HyscaleException {
        return this.multipleContainerRestarts.test(context);
    }
}
