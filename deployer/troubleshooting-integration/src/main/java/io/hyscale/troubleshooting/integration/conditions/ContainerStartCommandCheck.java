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
package io.hyscale.troubleshooting.integration.conditions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.hyscale.commons.commands.CommandExecutor;
import io.hyscale.commons.commands.provider.ImageCommandProvider;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.models.CommandResult;
import io.hyscale.commons.utils.ObjectMapperFactory;
import io.hyscale.deployer.core.model.ResourceKind;
import io.hyscale.troubleshooting.integration.models.Node;
import io.hyscale.troubleshooting.integration.actions.DockerfileCMDMissingAction;
import io.hyscale.troubleshooting.integration.actions.FixCrashingApplication;
import io.hyscale.troubleshooting.integration.models.TroubleshootingContext;
import io.hyscale.troubleshooting.integration.util.ConditionUtil;
import io.hyscale.troubleshooting.integration.util.TroubleshootUtil;
import io.kubernetes.client.models.V1Pod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;
import java.util.function.Predicate;

@Component
public class ContainerStartCommandCheck implements Node<TroubleshootingContext> {

    private static final Logger logger = LoggerFactory.getLogger(ContainerStartCommandCheck.class);

    private Predicate<TroubleshootingContext> containerStartCommandCheck;

    @Autowired
    private ImageCommandProvider commandProvider;

    @Autowired
    private DockerfileCMDMissingAction dockerfileCMDMissingAction;

    @Autowired
    private FixCrashingApplication fixCrashingApplication;


    @PostConstruct
    public void init() {
        this.containerStartCommandCheck = new Predicate<TroubleshootingContext>() {
            @Override
            public boolean test(TroubleshootingContext context) {
                if (TroubleshootUtil.validateContext(context)) {
                    logger.debug("Cannot troubleshoot without resource data and context");
                    return false;
                }

                TroubleshootingContext.ResourceData resourceData = context.getResourceData().get(ResourceKind.POD.getKind());
                //TODO proper error handling
                if (ConditionUtil.isResourceInValid(resourceData)) {
                    logger.error("Cannot proceed with incomplete resource data {}");
                    return false;
                }
                List<Object> podsList = resourceData.getResource();

                if (podsList == null || podsList.isEmpty()) {
                    // TODO talk about result accuracy without events
                    return false;
                }
                V1Pod pod = (V1Pod) podsList.get(0);

                // Add tracing on verbose logging
                if (checkForStartCommands(pod)) {
                    return true;
                }

                String image = getImageFromPods(pod);
                if (image == null) {
                    // TODO indicate about result
                    return false;
                }

                String dockerInstallCommand = commandProvider.getDockerInstalledCommand();
                if (!CommandExecutor.execute(dockerInstallCommand)) {
                    // Throw that result cannot be obtained because docker is not installed,
                    // Still show this can be the possbility of error
                    return false;
                }


                CommandResult result = CommandExecutor.executeAndGetResults(commandProvider.getImageInspectCommand(image));
                if (result == null || result.getExitCode() != 0) {
                    // Throw that result cannot be obtained because docker is not installed,
                    return true;
                }

                return checkForDockerfileCMD(result.getCommandOutput());
            }

            private boolean checkForDockerfileCMD(String commandOutput) {
                ObjectMapper mapper = ObjectMapperFactory.jsonMapper();
                try {
                    JsonNode node = mapper.readTree(commandOutput);
                    JsonNode cmdNode = null;
                    if (node.isArray()) {
                        cmdNode = node.get(0).get("Config").get("Cmd");
                    } else {
                        cmdNode = node.get("Config").get("Cmd");
                    }
                    if (cmdNode == null) {
                        return true;
                    }

                    /* If CMD is an array and is not empty, then it means CMD is
                        present in the dockerfile.
                     */
                    if (cmdNode.isArray()) {
                        ArrayNode arrayNode = (ArrayNode) cmdNode;
                        return arrayNode.isEmpty();
                    } else {
                        // CMD is not present in Dockerfile
                        return true;
                    }
                } catch (IOException e) {
                    logger.error("Error while processing image inspect results ", e);
                    //TODO Stop the troubleshooting and inform user to do check this condition
                    return false;
                }
            }
        };

    }

    private boolean checkForStartCommands(V1Pod pod) {
        if (pod == null) {
            return false;
        }
        return StringUtils.isEmpty(pod.getSpec().getContainers().get(0).getArgs());
    }

    private String getImageFromPods(V1Pod pod) {
        if (pod == null) {
            return null;
        }
        return pod.getSpec().getContainers().get(0).getImage();
    }


    @Override
    public Node<TroubleshootingContext> next(TroubleshootingContext context) throws HyscaleException {
        return test(context) ? dockerfileCMDMissingAction : fixCrashingApplication;
    }

    @Override
    public String describe()  {
        return "Is either of Dockerfile CMD or Args in kubernetes yaml defined ?";
    }

    @Override
    public boolean test(TroubleshootingContext context) throws HyscaleException {
        return this.containerStartCommandCheck.test(context);
    }
}
