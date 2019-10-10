package io.hyscale.ctl.generator.services.plugins;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.hyscale.ctl.annotation.ManifestPlugin;
import io.hyscale.ctl.commons.exception.HyscaleException;
import io.hyscale.ctl.commons.logger.WorkflowLogger;
import io.hyscale.ctl.commons.models.ManifestContext;
import io.hyscale.ctl.generator.services.model.ManifestGeneratorActivity;
import io.hyscale.ctl.generator.services.model.ManifestResource;
import io.hyscale.ctl.generator.services.exception.ManifestErrorCodes;
import io.hyscale.ctl.generator.services.predicates.ManifestPredicates;
import io.hyscale.ctl.plugin.ManifestHandler;
import io.hyscale.ctl.plugin.ManifestSnippet;
import io.hyscale.ctl.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.ctl.servicespec.commons.model.service.ServiceSpec;
import io.hyscale.ctl.util.GsonSnippetConvertor;
import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.custom.QuantityFormatException;
import io.kubernetes.client.custom.QuantityFormatter;
import io.kubernetes.client.models.V1ResourceRequirements;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
@ManifestPlugin(name = "ResourceLimitsHandler")
public class ResourceLimitsHandler implements ManifestHandler {

    private static final Logger logger = LoggerFactory.getLogger(ResourceLimitsHandler.class);
    private static final String RANGE_REGEX = "(\\d+)-(\\d+)";
    private static final Pattern rangePattern = Pattern.compile(RANGE_REGEX);

    private static final String DEFAULT_MIN_MEMORY = "4Mi";


    private QuantityFormatter formatter;

    @PostConstruct
    public void init() {
        this.formatter = new QuantityFormatter();
    }

    @Override
    public List<ManifestSnippet> handle(ServiceSpec serviceSpec, ManifestContext manifestContext) throws HyscaleException {
        String memory = serviceSpec.get(HyscaleSpecFields.memory, String.class);
        String cpu = serviceSpec.get(HyscaleSpecFields.cpu, String.class);
        ValueRange memoryRange = null;
        ValueRange cpuRange = null;
        if (StringUtils.isNotBlank(memory)) {
            logger.debug("Preparing memory limits.");
            memoryRange = getRange(memory);
        }
        if (StringUtils.isNotBlank(cpu)) {
            logger.debug("Preparing cpu limits.");
            cpuRange = getRange(cpu);
        }
        String podSpecOwner = ManifestPredicates.getVolumesPredicate().test(serviceSpec) ?
                ManifestResource.STATEFUL_SET.getKind() : ManifestResource.DEPLOYMENT.getKind();
        List<ManifestSnippet> manifestSnippetList = new ArrayList<>();
        try {
            manifestSnippetList.add(getResourceRequirements(memoryRange, cpuRange, podSpecOwner));
        } catch (JsonProcessingException e) {
            logger.error("Error while building manifest snippet for resource limits {}", e);
        }
        return manifestSnippetList;
    }

    private boolean validateMinMemory(ValueRange memoryRange) {
        boolean valid = true;
        if (memoryRange != null && memoryRange.getMax() != null) {
            valid = memoryRange.getMax().getNumber().compareTo(Quantity.fromString(DEFAULT_MIN_MEMORY).getNumber()) > 0 ? true : false;
            if (!valid) {
                WorkflowLogger.persist(ManifestGeneratorActivity.INSUFFICIENT_MEMORY, memoryRange.getMax().toSuffixedString());
            }
        }
        return valid;
    }

    private ManifestSnippet getResourceRequirements(ValueRange memoryRange, ValueRange cpuRange, String podSpecOwner) throws JsonProcessingException {
        V1ResourceRequirements resourceRequirements = new V1ResourceRequirements();
        if (cpuRange != null) {
            insertResourceElement(resourceRequirements, "cpu", cpuRange.getMin(), false);
            insertResourceElement(resourceRequirements, "cpu", cpuRange.getMax(), true);
        }

        if (memoryRange != null && validateMinMemory(memoryRange)) {
            insertResourceElement(resourceRequirements, "memory", memoryRange.getMin(), false);
            insertResourceElement(resourceRequirements, "memory", memoryRange.getMax(), true);
        }

        ManifestSnippet manifestSnippet = new ManifestSnippet();
        manifestSnippet.setPath("spec.template.spec.containers[0].resources");
        manifestSnippet.setSnippet(GsonSnippetConvertor.serialize(resourceRequirements));
        manifestSnippet.setKind(podSpecOwner);
        return manifestSnippet;
    }

    private ValueRange getRange(String value) throws HyscaleException {
        ValueRange range = new ValueRange();
        try {
            if (rangePattern.matcher(value).matches()) {
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

    private void insertResourceElement(V1ResourceRequirements resourceRequirements, String key, Quantity value, boolean limits) {
        if (value == null) {
            return;
        }
        if (limits) {
            resourceRequirements.putLimitsItem(key, value);
        } else {
            resourceRequirements.putRequestsItem(key, value);
        }
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
}
