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
package io.hyscale.generator.services.processor;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Multimap;
import io.hyscale.commons.models.*;
import io.hyscale.generator.services.builder.DefaultLabelBuilder;
import io.hyscale.generator.services.provider.CustomSnippetsProvider;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.generator.services.config.ManifestConfig;
import io.hyscale.generator.services.generator.ManifestFileGenerator;
import io.hyscale.generator.services.model.ManifestGeneratorActivity;
import io.hyscale.generator.services.model.ManifestNode;
import io.hyscale.generator.services.utils.ManifestTreeUtils;
import io.hyscale.generator.services.utils.PluginHandlers;
import io.hyscale.plugin.framework.handler.ManifestHandler;
import io.hyscale.plugin.framework.models.ManifestMeta;
import io.hyscale.plugin.framework.models.ManifestSnippet;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;
import org.yaml.snakeyaml.Yaml;

@Component
public class PluginProcessor {

    private static final Logger logger = LoggerFactory.getLogger(PluginProcessor.class);

    @Autowired
    private ManifestTreeUtils manifestTreeUtils;

    @Autowired
    private ManifestFileGenerator manifestFileGenerator;

    @Autowired
    private PluginHandlers pluginHandlers;

    @Autowired
    private ManifestConfig manifestConfig;

    @Autowired
    private CustomSnippetsProvider customSnippetsProvider;

    @Autowired
    private CustomSnippetsProcessor customSnippetsProcessor;



    public List<Manifest> getManifests(ServiceSpec serviceSpec, ManifestContext manifestContext)
            throws HyscaleException {
        YAMLMapper yamlMapper = new YAMLMapper();
        String serviceName = serviceSpec.get(HyscaleSpecFields.name, String.class);
        List<Manifest> manifestList = new ArrayList<>();
        TypeReference<List<String>> listTypeReference = new TypeReference<List<String>>() {};
        List<String> k8sSnippetFilePaths= serviceSpec.get(HyscaleSpecFields.k8sSnippets,listTypeReference);
        // Fetching Custom Snippets for various kinds
        Multimap<String,String> kindVsCustomSnippets  = customSnippetsProcessor.processCustomSnippetFiles(k8sSnippetFilePaths);

        Map<ManifestMeta, ManifestNode> manifestMetavsNodeMap = process(serviceSpec, manifestContext);
        if (manifestMetavsNodeMap == null || manifestMetavsNodeMap.isEmpty()) {
            logger.debug("Found empty processed manifests ");
            return manifestList;
        }
        String manifestDir = manifestConfig.getManifestDir(manifestContext.getAppName(), serviceName);

        manifestMetavsNodeMap.entrySet().stream().forEach(each -> {
            ManifestMeta manifestMeta = each.getKey();
            ManifestNode manifestNode = each.getValue();
            try {
                String yamlString = null;
                if (manifestNode != null && manifestNode.getObjectNode() != null) {
                    yamlString = yamlMapper.writeValueAsString(manifestNode.getObjectNode());
                    String kind = manifestMeta.getKind();
                    if(kindVsCustomSnippets!= null && !kindVsCustomSnippets.isEmpty()){
                        yamlString = applyCustomSnippets(kindVsCustomSnippets,kind,yamlString);
                        kindVsCustomSnippets.removeAll(kind);
                    }
                }
                WorkflowLogger.startActivity(ManifestGeneratorActivity.GENERATING_MANIFEST, each.getKey().getKind());
                YAMLManifest yamlManifest = manifestFileGenerator.getYamlManifest(manifestDir, yamlString,
                        each.getKey());
                manifestList.add(yamlManifest);
                WorkflowLogger.endActivity(Status.DONE);
            } catch (HyscaleException e) {
                logger.error("Failed to process manifest {}", each.getKey(), e);
                WorkflowLogger.endActivity(Status.FAILED);
            } catch (JsonProcessingException e) {
                logger.error("Failed to process manifest during yaml conversion {}", each.getKey(), e);
                WorkflowLogger.endActivity(Status.FAILED);
            }
        });
        ServiceMetadata serviceMetadata = new ServiceMetadata();
        serviceMetadata.setAppName(manifestContext.getAppName());
        serviceMetadata.setEnvName(manifestContext.getEnvName());
        serviceMetadata.setServiceName(serviceName);
        List<Manifest> customSnippetsManifestList = getManifestsFromCustomSnippets(serviceMetadata, manifestDir, kindVsCustomSnippets);
        if(customSnippetsManifestList != null && !customSnippetsManifestList.isEmpty()){
             manifestList.addAll(customSnippetsManifestList);
        }
        return manifestList;
    }

    private String applyCustomSnippets(Multimap<String,String> kindVsCustomSnippets, String kind, String yamlString){
        Collection<String> customSnippets = kindVsCustomSnippets.get(kind);
        if(customSnippets != null && !customSnippets.isEmpty()){
            WorkflowLogger.startActivity(ManifestGeneratorActivity.APPLYING_CUSTOM_SNIPPET,kind);
            List<String> customSnippetsList = customSnippets.stream().collect(Collectors.toList());
            try {
                yamlString = customSnippetsProvider.mergeCustomSnippets(yamlString, customSnippetsList);
                WorkflowLogger.endActivity(Status.DONE);
                return yamlString;
            }catch (HyscaleException e){
                logger.error("Error while applying Custom Snippets on kind {}",kind, e);
                WorkflowLogger.endActivity(Status.FAILED);
            }
        }
        return yamlString;
    }

    private List<Manifest> getManifestsFromCustomSnippets(ServiceMetadata serviceMetaData, String manifestDir, Multimap<String,String> kindVsCustomSnippets){
        if(kindVsCustomSnippets == null || kindVsCustomSnippets.isEmpty()){
            return Collections.emptyList();
        }
        List<Manifest> manifestList = new ArrayList<>();
        Yaml yaml = new Yaml();
        kindVsCustomSnippets.forEach((kind,customSnippet)->{
            WorkflowLogger.startActivity(ManifestGeneratorActivity.GENERATING_MANIFEST, kind);
            ManifestMeta manifestMeta = new ManifestMeta(kind);
            Map<String, Object> data = (Map) yaml.load(customSnippet);
            Map<String,Object> metadata = (Map) data.get("metadata");
            String name = (String) metadata.get("name");
            manifestMeta.setIdentifier(name);
            YAMLManifest yamlManifest = null;
            customSnippet = addHyscaleLabelsForCustomSnippets(customSnippet,serviceMetaData);
            try {
                yamlManifest = manifestFileGenerator.getYamlManifest(manifestDir, customSnippet,
                        manifestMeta);
                WorkflowLogger.endActivity(Status.DONE);
            } catch (HyscaleException e) {
                logger.error("Failed to process manifest {}", kind, e);
                WorkflowLogger.endActivity(Status.FAILED);
            }
            manifestList.add(yamlManifest);
        });
        return manifestList;
    }

    private String addHyscaleLabelsForCustomSnippets(String snippet, ServiceMetadata serviceMetadata){
        Yaml yaml = new Yaml();
        Map<String, Object> data = (Map) yaml.load(snippet);
        Map<String,Object> metadata = (Map) data.get("metadata");
        Map<String,Object> spec = (Map) data.get("spec");
        if(metadata.get("labels")==null){
            metadata.put("labels",new HashMap<String,String>());
        }
        Map<String,String> labels = (Map) metadata.get("labels");
        labels.putAll(DefaultLabelBuilder.build(serviceMetadata));
        if(spec.get("selector") == null){
            spec.put("selector",new HashMap<String,String>());
        }
        Map<String,String> selector = (Map) spec.get("selector");
        selector.putAll(DefaultLabelBuilder.build(serviceMetadata));
        return yaml.dump(data);
    }

    public Map<ManifestMeta, ManifestNode> process(ServiceSpec serviceSpec, ManifestContext manifestContext) {
        List<ManifestHandler> manifestHandlerList = pluginHandlers.getAllPlugins();
        if (manifestHandlerList == null || manifestHandlerList.isEmpty()) {
            return null;
        }
        Map<ManifestMeta, ManifestNode> manifestMetavsNodeMap = new LinkedHashMap<>();
        manifestHandlerList.stream().filter(Objects::nonNull).forEach(each -> {
            List<ManifestSnippet> manifestSnippetList = null;
            try {
                logger.debug("Executing plugin handler of : {}", each.getClass().getCanonicalName());
                manifestSnippetList = each.handle(serviceSpec, manifestContext);
                if (validateSnippets(manifestSnippetList)) {
                    logger.debug("Updating plugins snippets of {} plugin handler ", each.getClass().getCanonicalName());
                    updateManifests(manifestSnippetList, manifestMetavsNodeMap);
                }
                logger.debug("Completed execution of {} plugin handler ", each.getClass().getCanonicalName());
            } catch (HyscaleException e) {
                logger.error("Error while executing manifest plugin {} ", each.getClass().getName(), e);
            }
        });
        return manifestMetavsNodeMap;
    }

    private void updateManifests(List<ManifestSnippet> manifestSnippetList,
                                 Map<ManifestMeta, ManifestNode> manifestMetavsNodeMap) {
        if (manifestSnippetList == null || manifestSnippetList.isEmpty()) {
            return;
        }

        List<String> failedSnippets = new ArrayList<>();
        manifestSnippetList.stream().filter(each -> {
            return each != null && StringUtils.isNotBlank(each.getSnippet());
        }).forEach(each -> {
            logger.debug("Processing Snippet Kind{} :: Path at {} ", each.getKind(), each.getPath());
            ManifestMeta manifestMeta = new ManifestMeta(each.getKind());
            if (each.getName() != null) {
                manifestMeta.setIdentifier(each.getName());
            }
            ManifestNode manifestNode = manifestMetavsNodeMap.get(manifestMeta);
            ObjectNode rootNode = null;
            if (manifestNode == null || manifestNode.getObjectNode() == null) {
                rootNode = JsonNodeFactory.instance.objectNode();
                manifestNode = new ManifestNode(rootNode);
            } else {
                rootNode = manifestNode.getObjectNode();
            }
            try {
                rootNode = (ObjectNode) manifestTreeUtils.injectSnippet(each.getSnippet(), each.getPath(), rootNode);
                // updating the root node back in the manifests
                manifestNode.setObjectNode(rootNode);
                manifestMetavsNodeMap.put(manifestMeta, manifestNode);

            } catch (IOException e) {
                failedSnippets.add(each.getPath());
                logger.error("Error while Injecting manifest snippet for {} ", each, e);
            } catch (HyscaleException e) {
                failedSnippets.add(each + ": " + e.getMessage());
                logger.error("Error while Injecting manifest snippet for {} ", each, e);
            }
        });
        if (!failedSnippets.isEmpty()) {
            WorkflowLogger.warn(ManifestGeneratorActivity.ERROR_WHILE_PROCESSING_MANIFEST_PLUGINS,
                    failedSnippets.toString());
        }
    }

    private boolean validateSnippets(List<ManifestSnippet> manifestSnippetList) {
        if (manifestSnippetList != null && !manifestSnippetList.isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

}
