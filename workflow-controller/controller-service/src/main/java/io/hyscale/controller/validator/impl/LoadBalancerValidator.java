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
import io.hyscale.commons.models.LoadBalancerMapping;
import io.hyscale.commons.validator.Validator;
import io.hyscale.controller.activity.ValidatorActivity;
import io.hyscale.controller.model.WorkflowContext;
import io.hyscale.generator.services.model.LBType;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.Agent;
import io.hyscale.servicespec.commons.model.service.Port;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class LoadBalancerValidator implements Validator<WorkflowContext> {

    private static final Logger logger = LoggerFactory.getLogger(LoadBalancerValidator.class);

    /**
     * Checks for the required mandatory fields.
     * Checks for Ports mismatch
     * Checks for external true.
     * @param workflowContext
     * @return
     * @throws HyscaleException
     */
    @Override
    public boolean validate(WorkflowContext workflowContext) throws HyscaleException {
        logger.debug("Validating load balancer details from the service spec");
        ServiceSpec serviceSpec = workflowContext.getServiceSpec();
        TypeReference<LoadBalancer> loadBalancerTypeReference = new TypeReference<LoadBalancer>() {
        };

        LoadBalancer loadBalancer = serviceSpec.get(HyscaleSpecFields.loadBalancer, loadBalancerTypeReference);
        if (loadBalancer != null) {
            if (!validateMandatoryFields(loadBalancer)) {
                return false;
            }
            if (!portValidation(serviceSpec, loadBalancer)) {
                //message for validator?
                return false;
            }
            if (!checkForExternalTrue(serviceSpec)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks for Ports mismatch
     * @param serviceSpec
     * @param loadBalancer
     * @return
     * @throws HyscaleException
     */
    private boolean portValidation(ServiceSpec serviceSpec, LoadBalancer loadBalancer) throws HyscaleException {
        TypeReference<List<Port>> portsListTypeReference = new TypeReference<List<Port>>() {
        };
        // Fetching service ports
        List<Port> portList = serviceSpec.get(HyscaleSpecFields.ports, portsListTypeReference);
        List<String> portNumbersList = new ArrayList<>();
        portList.forEach(each -> portNumbersList.add(each.getPort()));
        // Fetching agents ports
       // portNumbersList.addAll(getAgentPorts(serviceSpec));
        List<String> lbPorts = new ArrayList<>();
        loadBalancer.getMapping().forEach(e -> lbPorts.add(e.getPort()));
        for (String lbPort : lbPorts) {
            if (!portNumbersList.contains(lbPort)) {
                WorkflowLogger.persist(ValidatorActivity.PORTS_MISMATCH, LoggerTags.ERROR, lbPort);
                return false;
            }
        }
        return true;
    }

    private List<String> getAgentPorts(ServiceSpec serviceSpec) {
        TypeReference<List<Agent>> agentsList = new TypeReference<List<Agent>>() {
        };
        List<String> portNumbersList = new ArrayList<>();
        try {
            List<Agent> agents = serviceSpec.get(HyscaleSpecFields.agents, agentsList);
            if(agents == null || agents.isEmpty()){
                Collections.emptyList();
            }
            agents.forEach((agent -> {
                List<Port> ports = agent.getPorts();
                if(ports!=null && !ports.isEmpty()){
                    ports.forEach((port)->{
                        portNumbersList.add(port.getPort());
                    });
                }
            }));
            return portNumbersList;
        } catch (HyscaleException e) {
            logger.error("Error while fetching agents from service spec, returning null.",e);
            return Collections.emptyList();
        }
    }


    /**
     * @param serviceSpec
     * @throws HyscaleException
     */
    private boolean checkForExternalTrue(ServiceSpec serviceSpec) throws HyscaleException {
        TypeReference<Boolean> booleanTypeReference = new TypeReference<>() {
        };
        Boolean isExternal = serviceSpec.get(HyscaleSpecFields.external, booleanTypeReference);
        if (isExternal != null && !isExternal) {
            WorkflowLogger.persist(ValidatorActivity.EXTERNAL_CONFIGURED, LoggerTags.ERROR);
            return false;
        }
        return true;
    }

    /**
     * Checks for the required mandatory fields.
     * @param loadBalancer
     */
    private boolean validateMandatoryFields(LoadBalancer loadBalancer) {
        boolean isMandatoryFieldsExists = true;
        if (LBType.getByProvider(loadBalancer.getProvider()) == LBType.INGRESS && loadBalancer.getClassName() == null) {
            isMandatoryFieldsExists = false;
            WorkflowLogger.persist(ValidatorActivity.LB_CLASS_NAME_REQUIRED, LoggerTags.ERROR);
        }
        if (LBType.getByProvider(loadBalancer.getProvider()) == LBType.ISTIO && loadBalancer.getLabels() == null) {
            isMandatoryFieldsExists = false;
            WorkflowLogger.persist(ValidatorActivity.LB_GATEWAY_LABEL_REQUIRED, LoggerTags.ERROR);
        }
        if (loadBalancer.getHost() == null) {
            isMandatoryFieldsExists = false;
            WorkflowLogger.persist(ValidatorActivity.LB_HOST_REQUIRED, LoggerTags.ERROR);
        }
        if (loadBalancer.getProvider() == null) {
            isMandatoryFieldsExists = false;
            WorkflowLogger.persist(ValidatorActivity.LB_TYPE_REQUIRED, LoggerTags.ERROR);
        }
        if (loadBalancer.getMapping() == null || loadBalancer.getMapping().isEmpty()) {
            isMandatoryFieldsExists = false;
            WorkflowLogger.persist(ValidatorActivity.LB_MAPPING_REQUIRED, LoggerTags.ERROR);
        }
        if (loadBalancer.getMapping() != null && !loadBalancer.getMapping().isEmpty()) {
            boolean isMappingFieldsExists = validateLoadBalancerMapping(loadBalancer.getMapping());
            isMandatoryFieldsExists = isMappingFieldsExists && isMandatoryFieldsExists;
        }
        return isMandatoryFieldsExists;
    }

    /**
     * validate port and contextPaths in the loadBalancer mapping.
     * @param mappings
     */
    public boolean validateLoadBalancerMapping(List<LoadBalancerMapping> mappings) {
        boolean isMappingFieldsExists = true;
        for (LoadBalancerMapping mapping : mappings) {
            if (mapping.getPort() == null) {
                isMappingFieldsExists = false;
                WorkflowLogger.persist(ValidatorActivity.LB_PORT_REQUIRED, LoggerTags.ERROR, mapping.getContextPaths() != null ? "for contextPaths: " + String.join(",", mapping.getContextPaths()) : "");
            }
            if (mapping.getContextPaths() == null || mapping.getContextPaths().isEmpty()) {
                isMappingFieldsExists = false;
                WorkflowLogger.persist(ValidatorActivity.LB_CONTEXT_PATH_REQUIRED, LoggerTags.ERROR, mapping.getPort() != null ? "for port:" + mapping.getPort() : "");
            }
        }
        return isMappingFieldsExists;
    }
}

