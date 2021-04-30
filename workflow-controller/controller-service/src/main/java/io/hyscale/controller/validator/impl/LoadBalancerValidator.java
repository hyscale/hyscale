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
import io.hyscale.commons.io.StructuredOutputHandler;
import io.hyscale.commons.logger.LoggerTags;
import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.commons.models.LBType;
import io.hyscale.commons.models.LoadBalancer;
import io.hyscale.commons.models.LoadBalancerMapping;
import io.hyscale.commons.validator.Validator;
import io.hyscale.controller.activity.ValidatorActivity;
import io.hyscale.controller.model.WorkflowContext;
import io.hyscale.controller.provider.PortsProvider;
import io.hyscale.generator.services.builder.DefaultPortsBuilder;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class LoadBalancerValidator implements Validator<WorkflowContext> {

    @Autowired
    private PortsProvider portsProvider;

    @Autowired
    DefaultPortsBuilder defaultPortsBuilder;

    @Autowired
    private StructuredOutputHandler structuredOutputHandler;


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

        List<String> portNumbersList = portsProvider.getExposedPorts(serviceSpec, true);
        List<String> lbPorts = new ArrayList<>();
        loadBalancer.getMapping().forEach(e -> lbPorts.add(e.getPort()));
        for (String lbPort : lbPorts) {
            lbPort = defaultPortsBuilder.updatePortProtocol(lbPort);
            if (!portNumbersList.contains(lbPort)) {
                WorkflowLogger.persist(ValidatorActivity.PORTS_MISMATCH, LoggerTags.ERROR, lbPort);
                addErrorMessage(ValidatorActivity.PORTS_MISMATCH.getActivityMessage(), lbPort);
                return false;
            }
        }
        return true;
    }


    /**
     * @param serviceSpec
     * @throws HyscaleException
     */
    private boolean checkForExternalTrue(ServiceSpec serviceSpec) throws HyscaleException {
        Boolean isExternal = serviceSpec.get(HyscaleSpecFields.external, Boolean.class);
        if (!BooleanUtils.toBoolean(isExternal)) {
            WorkflowLogger.persist(ValidatorActivity.EXTERNAL_CONFIGURED, LoggerTags.ERROR);
            addErrorMessage(ValidatorActivity.EXTERNAL_CONFIGURED.getActivityMessage(), (String[]) null);
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
        if (LBType.getByProvider(loadBalancer.getProvider()) == LBType.ISTIO) {
            isMandatoryFieldsExists = validateEachField(loadBalancer.getLabels(), ValidatorActivity.LB_GATEWAY_LABEL_REQUIRED);
        }
        if (!validateEachField(loadBalancer.getHost(), ValidatorActivity.LB_HOST_REQUIRED) ||
                !validateEachField(loadBalancer.getProvider(), ValidatorActivity.LB_PROVIDER_REQUIRED) ||
                !validateEachField(loadBalancer.getMapping(), ValidatorActivity.LB_MAPPING_REQUIRED)) {
            isMandatoryFieldsExists = false;
        }
        if (loadBalancer.getMapping() != null && !loadBalancer.getMapping().isEmpty()) {
            boolean isMappingFieldsExists = validateLoadBalancerMapping(loadBalancer.getMapping());
            isMandatoryFieldsExists = isMappingFieldsExists && isMandatoryFieldsExists;
        }
        return isMandatoryFieldsExists;
    }

    public boolean validateEachField(Object object, ValidatorActivity activityMessage) {
        if (object == null) {
            WorkflowLogger.persist(activityMessage, LoggerTags.ERROR);
            addErrorMessage(activityMessage.getActivityMessage(), (String[]) null);
            return false;
        }
        return true;
    }

    /**
     * validate port and contextPaths in the loadBalancer mapping.
     * @param mappings
     */
    public boolean validateLoadBalancerMapping(List<LoadBalancerMapping> mappings) {
        boolean isMappingFieldsExists = true;
        String errorMessage;
        for (LoadBalancerMapping mapping : mappings) {
            if (mapping.getPort() == null) {
                isMappingFieldsExists = false;
                errorMessage = mapping.getContextPaths() != null ? "for contextPaths: " + String.join(",", mapping.getContextPaths()) : "";
                WorkflowLogger.persist(ValidatorActivity.LB_PORT_REQUIRED, LoggerTags.ERROR, errorMessage);
                addErrorMessage(ValidatorActivity.LB_PORT_REQUIRED.getActivityMessage(), errorMessage);
            }
            if (mapping.getContextPaths() == null || mapping.getContextPaths().isEmpty()) {
                isMappingFieldsExists = false;
                errorMessage = mapping.getPort() != null ? "for port:" + mapping.getPort() : "";
                WorkflowLogger.persist(ValidatorActivity.LB_CONTEXT_PATH_REQUIRED, LoggerTags.ERROR, errorMessage);
                addErrorMessage(ValidatorActivity.LB_CONTEXT_PATH_REQUIRED.getActivityMessage(), errorMessage);
            }
        }
        return isMappingFieldsExists;
    }

    public void addErrorMessage(String errorMessage, String... args) {
        if (WorkflowLogger.isDisabled()) {
            structuredOutputHandler.addErrorMessage(errorMessage, args);
        }
    }
}

