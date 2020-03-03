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
import io.hyscale.deployer.services.model.PodStatus;
import io.hyscale.deployer.services.model.PodStatusUtil;
import io.hyscale.troubleshooting.integration.actions.FixCrashingApplication;
import io.hyscale.troubleshooting.integration.models.ConditionNode;
import io.hyscale.troubleshooting.integration.models.FailedResourceKey;
import io.hyscale.troubleshooting.integration.models.Node;
import io.hyscale.troubleshooting.integration.models.TroubleshootingContext;
import io.kubernetes.client.openapi.models.V1Pod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class IsApplicationCrashing extends ConditionNode<TroubleshootingContext> {

    @Autowired
    private FixCrashingApplication fixCrashingApplication;

    @Autowired
    private IsPodsReadinessFailing isPodsReadinessFailing;


    @Override
    public boolean decide(TroubleshootingContext context) throws HyscaleException {
        Object obj = context.getAttribute(FailedResourceKey.FAILED_POD);
        String lastState = null;
        if (obj != null) {
            V1Pod pod = (V1Pod) FailedResourceKey.FAILED_POD.getKlazz().cast(obj);
            lastState = pod != null ? PodStatusUtil.lastStateOf(pod) : null;
        }

        if (lastState != null) {
            return lastState.equals(PodStatus.OOMKILLED.getStatus()) || lastState.equals(PodStatus.COMPLETED.getStatus());
        }
        return false;
    }

    @Override
    public Node<TroubleshootingContext> onSuccess() {
        return fixCrashingApplication;
    }

    @Override
    public Node<TroubleshootingContext> onFailure() {
        return isPodsReadinessFailing;
    }

    @Override
    public String describe() {
        return "Is Application crashing";
    }
}
