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

import io.hyscale.commons.exception.CommonErrorCode;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.commons.models.ConfigTemplate;
import io.hyscale.commons.models.ManifestContext;
import io.hyscale.commons.utils.MustacheTemplateResolver;
import io.hyscale.generator.services.exception.ManifestErrorCodes;
import io.hyscale.generator.services.model.AppMetaData;
import io.hyscale.generator.services.model.ManifestGeneratorActivity;
import io.hyscale.generator.services.model.ManifestResource;
import io.hyscale.generator.services.predicates.ManifestPredicates;
import io.hyscale.generator.services.provider.PluginTemplateProvider;
import io.hyscale.plugin.framework.annotation.ManifestPlugin;
import io.hyscale.plugin.framework.handler.ManifestHandler;
import io.hyscale.plugin.framework.models.ManifestSnippet;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.Replicas;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * {@link AutoScalingPluginHandler} creates a HorizontalPodAutoScaler
 * manifest snippet based on the replicas field from the servicespec.
 * <p>
 * It perpetually translates the "replicas" field from service spec to
 * HorizontalPodAutoScaler
 */

@Component
@ManifestPlugin(name = "AutoScalingPluginHandler")
public class AutoScalingPluginHandler implements ManifestHandler {

    private static final Logger logger = LoggerFactory.getLogger(AutoScalingPluginHandler.class);
    private static final String TARGET_APIVERSION = "TARGET_APIVERSION";
    private static final String TARGET_KIND = "TARGET_KIND";
    private static final String TARGET_NAME = "TARGET_NAME";
    private static final String MIN_REPLICAS = "MIN_REPLICAS";
    private static final String MAX_REPLICAS = "MAX_REPLICAS";
    private static final String AVERAGE_UTILIZATION = "AVERAGE_UTILIZATION";
    private static final String CPU_THRESHOLD_REGEX = "[\\d\\.]+%";

    @Autowired
    private PluginTemplateProvider templateProvider;

    @Autowired
    private MustacheTemplateResolver templateResolver;

    @Override
    public List<ManifestSnippet> handle(ServiceSpec serviceSpec, ManifestContext manifestContext) throws HyscaleException {
        if (serviceSpec == null) {
            throw new HyscaleException(CommonErrorCode.SERVICE_SPEC_REQUIRED);
        }
        if (manifestContext == null) {
            throw new HyscaleException(ManifestErrorCodes.CONTEXT_REQUIRED);
        }
        if (!ManifestPredicates.isAutoScalingEnabled().test(serviceSpec)) {
            logger.debug("Skipping AutoScaling handler");
            return null;
        }
        Replicas replicas = serviceSpec.get(HyscaleSpecFields.replicas, Replicas.class);

        if (replicas == null || !validate(replicas)) {
            logger.debug("Cannot handle replicas as the field is not declared");
            return null;
        }
        ConfigTemplate hpaTemplate = templateProvider.get(PluginTemplateProvider.PluginTemplateType.HPA);
        if (hpaTemplate == null) {
            WorkflowLogger.persist(ManifestGeneratorActivity.FAILED_TO_PROCESS_REPLICAS);
            return null;
        }
        String yamlString = templateResolver.resolveTemplate(hpaTemplate.getTemplatePath(), getContext(replicas, serviceSpec, manifestContext));
        ManifestSnippet snippet = new ManifestSnippet();
        snippet.setKind(ManifestResource.HORIZONTAL_POD_AUTOSCALER.getKind());
        snippet.setPath("spec");
        snippet.setSnippet(yamlString);
        List<ManifestSnippet> snippetList = new LinkedList<>();
        snippetList.add(snippet);
        return snippetList;
    }

    private boolean validate(Replicas replicas) {
        if (replicas == null) {
            return false;
        }
        if (replicas.getMin() > 0 && replicas.getMax() < replicas.getMin()) {
            WorkflowLogger.persist(ManifestGeneratorActivity.IGNORING_REPLICAS, "Min replicas should be less than max replicas");
            return false;
        }
        if (StringUtils.isBlank(replicas.getCpuThreshold())) {
            WorkflowLogger.persist(ManifestGeneratorActivity.IGNORING_REPLICAS, "Missing field cpuThreshold");
            return false;
        }

        if (!replicas.getCpuThreshold().matches(CPU_THRESHOLD_REGEX)) {
            WorkflowLogger.persist(ManifestGeneratorActivity.IGNORING_REPLICAS, "The field cpuThreshold should match the regex " + CPU_THRESHOLD_REGEX);
            return false;
        }

        return true;
    }

    private Map<String, Object> getContext(Replicas replicas, ServiceSpec serviceSpec, ManifestContext manifestContext) throws HyscaleException {
        Map<String, Object> context = new HashMap<>();
        AppMetaData appMetaData = new AppMetaData();
        appMetaData.setAppName(manifestContext.getAppName());
        appMetaData.setEnvName(manifestContext.getEnvName());
        appMetaData.setServiceName(serviceSpec.get(HyscaleSpecFields.name, String.class));
        ManifestResource podSpecOwner = ManifestPredicates.getVolumesPredicate().test(serviceSpec) ? ManifestResource.STATEFUL_SET :
                ManifestResource.DEPLOYMENT;
        context.put(TARGET_KIND, podSpecOwner.getKind());
        context.put(TARGET_APIVERSION, podSpecOwner.getApiVersion());
        context.put(TARGET_NAME, podSpecOwner.getName(appMetaData));
        context.put(MIN_REPLICAS, replicas.getMin());
        context.put(MAX_REPLICAS, replicas.getMax());
        context.put(AVERAGE_UTILIZATION, normalizeThreshold(replicas.getCpuThreshold()));
        return context;
    }

    private Integer normalizeThreshold(String cpuThresholdPercentage) throws HyscaleException {
        String cpuThreshold = cpuThresholdPercentage.replaceAll("%", "");
        try {
            return Integer.valueOf(cpuThreshold);
        } catch (NumberFormatException e) {
            throw new HyscaleException(ManifestErrorCodes.INVALID_FORMAT_CPUTHRESHOLD);
        }
    }
}
