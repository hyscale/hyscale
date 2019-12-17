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
package io.hyscale.controller.manager.impl;

import java.io.*;
import java.util.*;

import javax.annotation.PostConstruct;

import io.hyscale.commons.config.SetupConfig;
import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.commons.models.*;
import io.hyscale.controller.activity.ControllerActivity;
import io.hyscale.controller.builder.ImageRegistryBuilder;
import io.hyscale.controller.config.ControllerConfig;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.utils.ObjectMapperFactory;
import io.hyscale.controller.core.exception.ControllerErrorCodes;
import io.hyscale.controller.manager.RegistryManager;

/**
 * Provides registry credentials.
 * Reads local docker registry config.
 * Using registry name as input, tries to find registry in following order
 * 1. credential helpers if present 
 * 2. credStore, if specified in config file
 * 3. auths map in config file
 */
@Component
public class LocalRegistryManagerImpl implements RegistryManager {

    private static final String SLASH = "/";

    private static final String HTTPS = "https://";

    private static final Logger logger = LoggerFactory.getLogger(LocalRegistryManagerImpl.class);

    private static DockerConfig dockerConfig;

    @Autowired
    private ControllerConfig controllerConfig;

    /**
     * Reads local docker config
     */
    @PostConstruct
    public void init() throws HyscaleException {
        ObjectMapper mapper = ObjectMapperFactory.jsonMapper();
        try {
            TypeReference<DockerConfig> dockerConfigTypeReference = new TypeReference<DockerConfig>() {
            };

            dockerConfig = mapper.readValue(new File(controllerConfig.getDefaultRegistryConf()),
                    dockerConfigTypeReference);

        } catch (IOException e) {
            String dockerConfPath = SetupConfig.getMountOfDockerConf(controllerConfig.getDefaultRegistryConf());
            WorkflowLogger.error(ControllerActivity.ERROR_WHILE_READING, dockerConfPath, e.getMessage());
            HyscaleException ex = new HyscaleException(e, ControllerErrorCodes.DOCKER_CONFIG_NOT_FOUND, dockerConfPath);
            logger.error("Error while deserializing image registries {}", ex.toString());
            throw ex;
        }
    }

    /**
     * Returns image credentials based on docker config in order
     * credHelpers, credsStore, auth map
     * if not found in any, returns null
     * @param registry
     * @return {@link ImageRegistry}
     */
    @Override
    public ImageRegistry getImageRegistry(String registry) throws HyscaleException {
        if (StringUtils.isBlank(registry)) {
            return null;
        }

        List<String> dockerRegistryAliases = DockerHubAliases.getDockerRegistryAliases(registry);
        List<String> registryPatterns = new ArrayList<>();
        for (String registryAlias : dockerRegistryAliases) {
            registryPatterns.addAll(getRegistryPatterns(registryAlias));
        }
        for (String pattern : registryPatterns) {
            ImageRegistryBuilder builder = new ImageRegistryBuilder(pattern);
            DockerCredHelper dockerCredHelper = getDockerCredHelper(pattern);
            ImageRegistry imageRegistry = dockerCredHelper != null ? builder.from(dockerCredHelper) : builder.from(dockerConfig.getAuths());
            if (imageRegistry != null) {
                return imageRegistry;
            }
        }
        return null;
    }

    /**
     * Returns credential helper if it is available for registry,
     * else credsStore if specified otherwise returns null.
     *
     * @param pattern
     * @return docker credential helper if available else returns null
     */
    public DockerCredHelper getDockerCredHelper(String pattern) {
        String helperFunc = dockerConfig.getCredHelpers() != null ? getHelperFunction(pattern) : null;
        if (helperFunc != null) {
            return new DockerCredHelper(helperFunc);
        } else if (dockerConfig.getCredsStore() != null) {
            return new DockerCredHelper(dockerConfig.getCredsStore());
        }
        return null;
    }

    private static String getHelperFunction(String registry) {
        return dockerConfig.getCredHelpers().containsKey(registry) ? dockerConfig.getCredHelpers().get(registry) : null;
    }

    private static List<String> getRegistryPatterns(String registry) {
        String exactMatch = registry;
        String withHttps = HTTPS + registry;
        String withSuffix = registry + SLASH;
        String withHttpsAndSuffix = HTTPS + registry + SLASH;
        return Arrays.asList(exactMatch, withHttps, withSuffix, withHttpsAndSuffix);
    }

}