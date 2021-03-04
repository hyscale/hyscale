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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import io.hyscale.commons.exception.CommonErrorCode;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.framework.patch.StrategicPatch;
import io.hyscale.commons.io.HyscaleFilesUtil;
import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.commons.utils.DataFormatConverter;
import io.hyscale.commons.utils.ObjectMapperFactory;
import io.hyscale.generator.services.exception.ManifestErrorCodes;
import io.hyscale.generator.services.model.ManifestGeneratorActivity;
import io.hyscale.generator.services.provider.CustomSnippetsFieldDataProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * This class is responsible for processing Custom K8s Snippets and related operations .
 *
 * @author Nishanth Panthangi
 */

@Component
public class CustomSnippetsProcessor {
    private static final Logger logger = LoggerFactory.getLogger(CustomSnippetsProcessor.class);

    public Multimap<String,String> processCustomSnippetFiles(List<String> k8sSnippetFilePaths) throws HyscaleException {
        if(k8sSnippetFilePaths == null){
            return null;
        }
        Multimap<String,String> kindVsCustomSnippets = ArrayListMultimap.create();
        String kind = null;
        String yamlSnippet = null;
        for(String snippetFilePath: k8sSnippetFilePaths){
            File snippetFile = new File(snippetFilePath);
            try{
                yamlSnippet = HyscaleFilesUtil.readFileData(snippetFile);
                if(yamlSnippet == null){
                    throw new HyscaleException(CommonErrorCode.FAILED_TO_READ_FILE);
                }
            }catch (HyscaleException e){
                logger.error("Failed to read Custom Snippet file from path {}",snippetFilePath);
                WorkflowLogger.error(ManifestGeneratorActivity.FAILED_TO_READ_CUSTOM_SNIPPETS,snippetFilePath);
                throw new HyscaleException(CommonErrorCode.FAILED_TO_READ_FILE,snippetFilePath);
            }
            try {
                // Validate YAML
                String jsonSnippet =  DataFormatConverter.yamlToJson(yamlSnippet);
                kind = identifyKind(jsonSnippet);
                if(kind == null){
                    throw new HyscaleException(CommonErrorCode.FAILED_TO_READ_FILE);
                }
            }catch(HyscaleException e){
                logger.error("Failed to convert YAML Snippet to json, invalid YAML File {}",yamlSnippet,e);
                WorkflowLogger.error(ManifestGeneratorActivity.INVALID_CUSTOM_SNIPPET,snippetFilePath);
                throw new HyscaleException(CommonErrorCode.FAILED_TO_READ_FILE,snippetFilePath);
            }
            kindVsCustomSnippets.put(kind,yamlSnippet);
        }
        return kindVsCustomSnippets;
    }

    public String mergeYamls(String source, String patch) throws HyscaleException {
        if(patch == null){
            return source;
        }
        source = DataFormatConverter.yamlToJson(source);
        patch = DataFormatConverter.yamlToJson(patch);
        String strategicMergeJson = StrategicPatch.apply(source, patch, new CustomSnippetsFieldDataProvider());
        try {
            JsonNode jsonNode = ObjectMapperFactory.jsonMapper().readTree(strategicMergeJson);
            return ObjectMapperFactory.yamlMapper().writeValueAsString(jsonNode);
        }catch (IOException e){
            logger.error("Error while converting merged json string to yaml ",e);
        }
        throw new HyscaleException(ManifestErrorCodes.ERROR_WHILE_APPLYING_CUSTOM_SNIPPETS);
    }

    public String identifyKind(String jsonSnippet){
        if(jsonSnippet == null) {
            return null;
        }
        try {
            ObjectMapper objectMapper = ObjectMapperFactory.yamlMapper();
            JsonNode jsonNode  = objectMapper.readTree(jsonSnippet);
            return jsonNode.get("kind").asText();
        } catch (Exception e) {
            logger.error("Error while trying to identify kind in Snippet {}",jsonSnippet,e);
        }
        return null;
    }
}
