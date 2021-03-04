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

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.commons.models.ConfigTemplate;
import io.hyscale.commons.models.ManifestContext;
import io.hyscale.commons.models.ServiceMetadata;
import io.hyscale.commons.utils.MustacheTemplateResolver;
import io.hyscale.generator.services.constants.ManifestGenConstants;
import io.hyscale.generator.services.exception.ManifestErrorCodes;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

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

    @Autowired
    private PluginTemplateProvider templateProvider;

    @Autowired
    private MustacheTemplateResolver templateResolver;

    @Override
    public List<ManifestSnippet> handle(ServiceSpec serviceSpec, ManifestContext manifestContext) throws HyscaleException {
        Object podSpecOwner = manifestContext.getGenerationAttribute(ManifestGenConstants.POD_SPEC_OWNER);
        if (!ManifestPredicates.isAutoScalingEnabledWithPrint().test(serviceSpec, true)
                || !(ManifestResource.STATEFUL_SET.getKind().equals(podSpecOwner)
                        || ManifestResource.DEPLOYMENT.getKind().equals(podSpecOwner))) {
            logger.debug("Skipping AutoScaling handler");
            return Collections.emptyList();
        }

        Replicas replicas = serviceSpec.get(HyscaleSpecFields.replicas, Replicas.class);
        if (replicas == null) {
            logger.debug("Cannot handle replicas as the field is not declared");
            return Collections.emptyList();
        }
        ConfigTemplate hpaTemplate = templateProvider.get(PluginTemplateProvider.PluginTemplateType.HPA);
        if (hpaTemplate == null) {
            WorkflowLogger.persist(ManifestGeneratorActivity.FAILED_TO_PROCESS_REPLICAS);
            return Collections.emptyList();
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

    private Map<String, Object> getContext(Replicas replicas, ServiceSpec serviceSpec, ManifestContext manifestContext) throws HyscaleException {
        Map<String, Object> context = new HashMap<>();
        ServiceMetadata serviceMetadata = new ServiceMetadata();
        serviceMetadata.setAppName(manifestContext.getAppName());
        serviceMetadata.setEnvName(manifestContext.getEnvName());
        serviceMetadata.setServiceName(serviceSpec.get(HyscaleSpecFields.name, String.class));
        ManifestResource podSpecOwner = ManifestResource
                .fromString((String) manifestContext.getGenerationAttribute(ManifestGenConstants.POD_SPEC_OWNER));
        context.put(TARGET_KIND, podSpecOwner.getKind());
        context.put(TARGET_APIVERSION, podSpecOwner.getApiVersion());
        context.put(TARGET_NAME, podSpecOwner.getName(serviceMetadata));
        context.put(MIN_REPLICAS, replicas.getMin());
        context.put(MAX_REPLICAS, replicas.getMax());
        context.put(AVERAGE_UTILIZATION, normalizeThreshold(replicas.getCpuThreshold()));
        return context;
    }

    private Integer normalizeThreshold(String cpuThresholdPercentage) throws HyscaleException {
        String cpuThreshold = cpuThresholdPercentage.replace("%", "");
        try {
            return Integer.valueOf(cpuThreshold);
        } catch (NumberFormatException e) {
            throw new HyscaleException(ManifestErrorCodes.INVALID_FORMAT_CPUTHRESHOLD);
        }
    }
}
