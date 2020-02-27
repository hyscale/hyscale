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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.deployer.core.model.ResourceKind;
import io.hyscale.troubleshooting.integration.actions.ParentFailureAction;
import io.hyscale.troubleshooting.integration.actions.ServiceNotDeployedAction;
import io.hyscale.troubleshooting.integration.models.Node;
import io.hyscale.troubleshooting.integration.models.TroubleshootingContext;
import io.hyscale.troubleshooting.integration.util.PodParentTroubleshootUtil;

@Component
public class ParentStatusCondition implements Node<TroubleshootingContext> {
    
    private static final Logger logger = LoggerFactory.getLogger(ParentStatusCondition.class);
    
    @Autowired
    private ServiceNotDeployedAction serviceNotDeployedAction;
    
    @Autowired
    private ParentFailureAction parentFailureAction;

    @Override
    public Node<TroubleshootingContext> next(TroubleshootingContext context) throws HyscaleException {
        if (context.getResourceInfos() == null) {
            return serviceNotDeployedAction;
        }
        
        ResourceKind podParent = PodParentTroubleshootUtil.getPodParent(context);
        if (context.isTrace()) {
            logger.debug(describe() + ", pod parent {}", podParent);
        }
        if (podParent == null) {
            return serviceNotDeployedAction;
        }
        return parentFailureAction;
    }

    @Override
    public String describe() {
        return "Checks for pod parent status and process events";
    }

}
