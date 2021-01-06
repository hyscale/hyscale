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
package io.hyscale.controller.validator.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.logger.LoggerTags;
import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.commons.models.LoadBalancer;
import io.hyscale.commons.validator.Validator;
import io.hyscale.controller.activity.ValidatorActivity;
import io.hyscale.controller.model.WorkflowContext;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.Port;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class LoadBalancerValidator implements Validator<WorkflowContext> {

    private static final Logger logger = LoggerFactory.getLogger(LoadBalancerValidator.class);

    @Override
    public boolean validate(WorkflowContext workflowContext) throws HyscaleException {
        logger.debug("Validating volumes from the service spec");
        ServiceSpec serviceSpec = workflowContext.getServiceSpec();
        TypeReference<List<Port>> portsListTypeReference = new TypeReference<List<Port>>() {
        };
        TypeReference<LoadBalancer> loadBalancerTypeReference = new TypeReference<LoadBalancer>() {
        };
        TypeReference<Boolean> booleanTypeReference = new TypeReference<Boolean>() {
        };
        List<Port> portList = serviceSpec.get(HyscaleSpecFields.ports, portsListTypeReference);
        List<String> portNumbersList = new ArrayList<>();
        portList.forEach(each -> portNumbersList.add(each.getPort()));
        LoadBalancer loadBalancer = serviceSpec.get(HyscaleSpecFields.loadBalancer, loadBalancerTypeReference);
        if (loadBalancer != null && loadBalancer.getMapping() != null) {
            List<String> lbPorts = new ArrayList<>();
            loadBalancer.getMapping().forEach(e -> lbPorts.add(e.getPort()));
            for (String lbPort : lbPorts) {
                if (!portNumbersList.contains(lbPort)) {
                    WorkflowLogger.persist(ValidatorActivity.PORTS_MISMATCH, LoggerTags.ERROR, lbPort);
                    return false;
                }
            }
        }
        Boolean isExternal = serviceSpec.get(HyscaleSpecFields.external, booleanTypeReference);
        if (isExternal != null && isExternal) {
            WorkflowLogger.persist(ValidatorActivity.EXTERNAL_CONFIGURED, LoggerTags.WARN);
        }
        return true;
    }
}
