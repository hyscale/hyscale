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
 * Provides credential from the docker-credential-helper.Takes inputs @helperFuntion ie..credential helper name and registry name generates
 * the "docker-credential-helperFuntion get" command and gets credentials if exists.
 */
public class DockerCredHelper {

    private static final Logger logger = LoggerFactory.getLogger(CommandExecutor.class);
    private static final String ECHO = "echo";
    private static final String DOCKER_CREDENTIAL = "docker-credential-";
    private static final String PIPE = " | ";
    private static final String GET = "get";

    private String helperFunction;

    public String getHelperFunction() {
        return helperFunction;
    }

    public DockerCredHelper(String helperFunction) {
        this.helperFunction = helperFunction;
    }

    /**
     * Fetches the credentials from credstore given registryUrl
     *
     * @param registryUrl the registry server url
     * @return CredsStoreEntity object containing username and password if found ,else returns null.
     */
    public CredsStoreEntity getCredentials(String registryUrl) {
        String s = null;
        CommandResult result = null;
        StringBuilder stringBuilder = new StringBuilder();
        try {

            String command = stringBuilder.append(ECHO).append(ToolConstants.SPACE).append('"').append(registryUrl).append('"').append(ToolConstants.SPACE).append(PIPE).append(DOCKER_CREDENTIAL).append(getHelperFunction()).append(ToolConstants.SPACE).append(GET).toString();
            CommandExecutor commandExecutor = new CommandExecutor();
            //TODO change command executor
            result = commandExecutor.executeAndGetResults(command);
            if (result == null || result.getExitCode() != 0 || StringUtils.isBlank(result.getCommandOutput())) {
                return null;
            }
            ObjectMapper mapper = ObjectMapperFactory.jsonMapper();
            CredsStoreEntity credsStore = mapper.readValue(result.getCommandOutput(), CredsStoreEntity.class);
            if (!credsStore.getServerURL().isEmpty() && !credsStore.getUsername().isEmpty() && !credsStore.getSecret().isEmpty()) {
                return credsStore;
            }
        } catch (IOException e) {
            logger.error("Error while fetching credentials from {}", helperFunction);
        }
        return null;
    }
}
