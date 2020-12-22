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
package io.hyscale.commons.models;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.hyscale.commons.commands.CommandExecutor;
import io.hyscale.commons.constants.ToolConstants;
import io.hyscale.commons.utils.ObjectMapperFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Provides credential from the docker-credential-helper.
 * Takes inputs @helperFuntion i.e..credential helper name and registry name.
 * Generates the "docker-credential-helperFuntion get" command 
 * and gets credentials if exists.
 */
public class DockerCredHelper {
    private static final Logger logger = LoggerFactory.getLogger(DockerCredHelper.class);
    private static final String DOCKER_CREDENTIAL = "docker-credential-";
    private static final String GET = "get";

    private String helperFunction;

    public String getHelperFunction() {
        return helperFunction;
    }

    public DockerCredHelper(String helperFunction) {
        this.helperFunction = helperFunction;
    }

    /**
     * Fetches the credentials from credstore given registryUrl, Helper name.
     * <p>
     * Takes image registry creates command "docker-credential-{@literal<helper>} get"
     * executes with registry url as input and fetches credentials from the
     * credsStore entity pattern if found.
     *
     * @param registryUrl the registry server url
     * @return CredsStoreEntity object containing username and password if found,
     *         else returns null.
     */
    public CredsStoreEntity get(String registryUrl) {
        StringBuilder stringBuilder = new StringBuilder();
        String command = stringBuilder.append(DOCKER_CREDENTIAL).append(getHelperFunction()).append(ToolConstants.SPACE)
                .append(GET).toString();
        try {
            CommandResult result = CommandExecutor.executeAndGetResults(command, registryUrl);
            ObjectMapper mapper = ObjectMapperFactory.jsonMapper();
            if (result == null || StringUtils.isBlank(result.getCommandOutput()) || result.getExitCode() != 0) {
                logger.debug("Could not fetch credentials for registry {} from helper function {}.", registryUrl,
                        getHelperFunction());
                return null;
            }
            CredsStoreEntity credsStore = mapper.readValue(result.getCommandOutput(), CredsStoreEntity.class);
            if (StringUtils.isNotBlank(credsStore.getServerURL()) && StringUtils.isNotBlank(credsStore.getUsername())
                    && StringUtils.isNotBlank(credsStore.getSecret())) {
                logger.debug("Found credentials for registry {} from helper function {}.", registryUrl,
                        getHelperFunction());
                return credsStore;
            }
        } catch (IOException e) {
            logger.error("Error while fetching credentials from {}", helperFunction, e);
        }
        return null;
    }
}
