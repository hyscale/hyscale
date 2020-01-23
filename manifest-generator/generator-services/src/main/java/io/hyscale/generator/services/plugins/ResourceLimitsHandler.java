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
package io.hyscale.generator.services.plugins;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import io.hyscale.generator.services.constants.ManifestGenConstants;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.commons.models.ManifestContext;
import io.hyscale.generator.services.exception.ManifestErrorCodes;
import io.hyscale.generator.services.model.ManifestGeneratorActivity;
import io.hyscale.generator.services.model.ManifestResource;
import io.hyscale.generator.services.predicates.ManifestPredicates;
import io.hyscale.plugin.framework.annotation.ManifestPlugin;
import io.hyscale.plugin.framework.handler.ManifestHandler;
import io.hyscale.plugin.framework.models.ManifestSnippet;
import io.hyscale.plugin.framework.util.GsonSnippetConvertor;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;
import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.custom.QuantityFormatException;
import io.kubernetes.client.models.V1ResourceRequirements;

@Component
@ManifestPlugin(name = "ResourceLimitsHandler")
public class ResourceLimitsHandler implements ManifestHandler {

    private static final Logger logger = LoggerFactory.getLogger(ResourceLimitsHandler.class);
    private static final String RANGE_REGEX = "(\\d+.*(Ki|Mi|Gi|Ti|Pi|Ei|[numkMGTPE]|))-(\\d+.*((Ki|Mi|Gi|Ti|Pi|Ei|[numkMGTPE]|)))";
    private static final String CPU_REGEX = "(\\d+.*(([.][\\d])[m]|))-(\\d+.*(([.][\\d])[m]|))";
    private static final Pattern cpuRangePattern = Pattern.compile(CPU_REGEX);
    private static final Pattern rangePattern = Pattern.compile(RANGE_REGEX);

    private static final String DEFAULT_MIN_MEMORY = "4Mi";
    private static final String DEFAULT_MIN_CPU = "1m";

    @Override
    public List<ManifestSnippet> handle(ServiceSpec serviceSpec, ManifestContext manifestContext) throws HyscaleException {
        String memory = serviceSpec.get(HyscaleSpecFields.memory, String.class);
        String cpu = serviceSpec.get(HyscaleSpecFields.cpu, String.class);
        ValueRange memoryRange = null;
        ValueRange cpuRange = null;
        if (StringUtils.isNotBlank(memory)) {
            logger.debug("Preparing memory limits.");
            memoryRange = getRange(memory, rangePattern);
        }
        if (StringUtils.isNotBlank(cpu)) {
            logger.debug("Preparing cpu limits.");
            cpuRange = getRange(cpu, cpuRangePattern);
        }
        String podSpecOwner = ((ManifestResource) manifestContext.getGenerationAttribute(ManifestGenConstants.POD_SPEC_OWNER)).getKind();
        List<ManifestSnippet> manifestSnippetList = new ArrayList<>();
        try {
            manifestSnippetList.add(getResourceRequirements(memoryRange, cpuRange, podSpecOwner));
        } catch (JsonProcessingException e) {
            logger.error("Error while building manifest snippet for resource limits {}", e);
        }
        return manifestSnippetList;
    }

    public static Predicate<ValueRange> getRangePredicate() {
        return valueRange -> {
            if (valueRange.getMin() != null) {
                int validRange = valueRange.getMin().getNumber().compareTo(valueRange.getMax().getNumber());
                if (validRange == 1) {
                    String range = valueRange.getMin().toSuffixedString() + "-" + valueRange.getMax().toSuffixedString();
                    WorkflowLogger.warn(ManifestGeneratorActivity.INVALID_RANGE, range);
                    return false;
                }
            }
            return true;
        };
    }

    private ManifestSnippet getResourceRequirements(ValueRange memoryRange, ValueRange cpuRange, String podSpecOwner) throws JsonProcessingException {
        V1ResourceRequirements resourceRequirements = new V1ResourceRequirements();

        validateAndInsert(cpuRange, resourceRequirements, ResourceRequirementType.CPU);
        validateAndInsert(memoryRange, resourceRequirements, ResourceRequirementType.MEMORY);

        ManifestSnippet manifestSnippet = new ManifestSnippet();
        manifestSnippet.setPath("spec.template.spec.containers[0].resources");
        manifestSnippet.setSnippet(GsonSnippetConvertor.serialize(resourceRequirements));
        manifestSnippet.setKind(podSpecOwner);
        return manifestSnippet;
    }

    private ValueRange getRange(String value, Pattern pattern) throws HyscaleException {
        ValueRange range = new ValueRange();
        try {
            if (pattern.matcher(value).matches()) {
                int separatorIndex = value.indexOf("-");
                range.setMin(Quantity.fromString(value.substring(0, separatorIndex)));
                range.setMax(Quantity.fromString(value.substring(separatorIndex + 1)));
            } else {
                range.setMax(Quantity.fromString(value));
            }
        } catch (QuantityFormatException e) {
            WorkflowLogger.persist(ManifestGeneratorActivity.INVALID_SIZE_FORMAT, value);
            throw new HyscaleException(ManifestErrorCodes.INVALID_SIZE_FORMAT, e.getMessage());
        }
        return range;
    }


    public static class ValueRange {

        private Quantity min;
        private Quantity max;

        public Quantity getMin() {
            return min;
        }

        public void setMin(Quantity min) {
            this.min = min;
        }

        public Quantity getMax() {
            return max;
        }

        public void setMax(Quantity max) {
            this.max = max;
        }
    }

    private enum ResourceRequirementType {
        MEMORY("memory") {
            @Override
            public Predicate<ValueRange> getValidationPredicate() {
                return valueRange -> {
                    boolean valid = true;
                    if (valueRange != null && valueRange.getMax() != null) {
                        valid = valueRange.getMax().getNumber().compareTo(Quantity.fromString(DEFAULT_MIN_MEMORY).getNumber()) > 0 ? true : false;
                    }
                    return valid;
                };
            }

            @Override
            public ManifestGeneratorActivity getActivityMessageForValidation() {
                return ManifestGeneratorActivity.INSUFFICIENT_MEMORY;
            }

        },
        CPU("cpu") {
            @Override
            public Predicate<ValueRange> getValidationPredicate() {
                return valueRange -> {
                    boolean valid = true;
                    if (valueRange != null && valueRange.getMax() != null) {
                        valid = valueRange.getMax().getNumber().compareTo(Quantity.fromString(DEFAULT_MIN_CPU).getNumber()) > 0 ? true : false;
                    }
                    return valid;
                };
            }

            @Override
            public ManifestGeneratorActivity getActivityMessageForValidation() {
                return ManifestGeneratorActivity.INSUFFICIENT_CPU;
            }

        };

        private String key;

        ResourceRequirementType(String str) {
            this.key = str;
        }

        public abstract Predicate<ValueRange> getValidationPredicate();

        public abstract ManifestGeneratorActivity getActivityMessageForValidation();

        public String getKey() {
            return key;
        }
    }

    public void validateAndInsert(ValueRange valueRange, V1ResourceRequirements resourceRequirements, ResourceRequirementType requirementType) {
        if (valueRange != null && getRangePredicate().test(valueRange)) {
            if (requirementType.getValidationPredicate().test(valueRange)) {
                addResourceAttribute(resourceRequirements, requirementType.getKey(), valueRange.getMin(), false);
                addResourceAttribute(resourceRequirements, requirementType.getKey(), valueRange.getMax(), true);
            } else {
                WorkflowLogger.warn(requirementType.getActivityMessageForValidation(), valueRange.getMax().toSuffixedString());
            }
        }
    }

    private void addResourceAttribute(V1ResourceRequirements resourceRequirements, String key, Quantity value, boolean limits) {
        if (value == null) {
            return;
        }
        if (limits) {
            resourceRequirements.putLimitsItem(key, value);
        } else {
            resourceRequirements.putRequestsItem(key, value);
        }
    }
}
