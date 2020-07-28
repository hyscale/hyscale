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
package io.hyscale.generator.services.utils;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.framework.patch.StrategicPatch;
import io.hyscale.commons.io.HyscaleFilesUtil;
import io.hyscale.commons.utils.DataFormatConverter;
import io.hyscale.commons.utils.ObjectMapperFactory;
import io.hyscale.servicespec.commons.builder.MapFieldDataProvider;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Utility class for operations related to Custom K8s Snippets.
 *
 * @author Nishanth Panthangi
 */

@Component
public class CustomSnippetsUtil {
    private static final Logger logger = LoggerFactory.getLogger(CustomSnippetsUtil.class);

    public String mergeYamls(String source, String patch) throws HyscaleException {
        String mergedYaml = null;
        MapFieldDataProvider mapFieldDataProvider = new MapFieldDataProvider(); //NOSONAR
        source = DataFormatConverter.yamlToJson(source);
        patch = DataFormatConverter.yamlToJson(patch);

        String strategicMergeJson = StrategicPatch.apply(source, patch,mapFieldDataProvider);
        try {
            JsonNode jsonNode = ObjectMapperFactory.jsonMapper().readTree(strategicMergeJson); //NOSONAR
            mergedYaml = ObjectMapperFactory.yamlMapper().writeValueAsString(jsonNode);
            return mergedYaml;
        }catch (IOException e){
            logger.error("Error while converting merged json string to yaml ",e);
        }
        return mergedYaml;
    }


    public Multimap<String,String> processCustomSnippetFiles(List<String> k8sSnippetFilePaths) throws HyscaleException {
        Multimap<String,String> kindVsCustomSnippets = ArrayListMultimap.create(); // NOSONAR
        String kind = null;
        for(String snippetFilePath: k8sSnippetFilePaths){
            File snippetFile = new File(snippetFilePath); //NOSONAR
            String yamlSnippet = HyscaleFilesUtil.readFileData(snippetFile);
            kind = validateAndGetKind(yamlSnippet);
            kindVsCustomSnippets.put(kind,yamlSnippet);
        }
        return kindVsCustomSnippets;
    }

    public String validateAndGetKind(String yamlSnippet){
        String jsonSnippet = null;
        try {
            jsonSnippet =  DataFormatConverter.yamlToJson(yamlSnippet);
            return identifyKind(jsonSnippet);
        }catch (HyscaleException e){
            logger.error("Failed to convert YAML Snippet to json, invalid YAML File {}",yamlSnippet,e);
        }
        return null;
    }

    public String identifyKind(String jsonSnippet){
        ObjectMapper objectMapper = ObjectMapperFactory.yamlMapper(); //NOSONAR
        JsonNode jsonNode = null;
        String kind = null;
        try {
            jsonNode = objectMapper.readTree(jsonSnippet);
            kind = jsonNode.get("kind").asText();
            return kind;
        } catch (IOException e) {
            logger.error("Error while trying to identify kind in Snippet {}",jsonSnippet,e);
        }
        return null;
    }
}
