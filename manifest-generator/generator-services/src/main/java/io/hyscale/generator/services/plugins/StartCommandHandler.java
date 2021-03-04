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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Lists;
import io.hyscale.generator.services.constants.ManifestGenConstants;
import io.hyscale.plugin.framework.annotation.ManifestPlugin;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.models.ManifestContext;
import io.hyscale.plugin.framework.handler.ManifestHandler;
import io.hyscale.plugin.framework.models.ManifestSnippet;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;
import io.hyscale.plugin.framework.util.JsonSnippetConvertor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@ManifestPlugin(name = "StartCommandHandler")
public class StartCommandHandler implements ManifestHandler {

    private static final Logger logger = LoggerFactory.getLogger(StartCommandHandler.class);

    @Override
    public List<ManifestSnippet> handle(ServiceSpec serviceSpec, ManifestContext manifestContext) throws HyscaleException {
        String startCommand = serviceSpec.get(HyscaleSpecFields.startCommand, String.class);
        if (StringUtils.isBlank(startCommand)) {
            logger.debug("Found empty start command.");
            return Collections.emptyList();
        }
        String podSpecOwner = ((String) manifestContext.getGenerationAttribute(ManifestGenConstants.POD_SPEC_OWNER));
        return getCommandAndArgsSnippet(startCommand, podSpecOwner);
    }

    private List<ManifestSnippet> getCommandAndArgsSnippet(String startCommand, String podSpecOwner) {
        List<ManifestSnippet> snippetList = new ArrayList<>();
        try {
            List<String> commandWithArgsList = Lists.newArrayList();
            String[] commandWithArgsArray = startCommand.split("\\s*,\\s*");
            for (String arg : commandWithArgsArray) {
                // Replace starting and ending '"' from command
                String trimmedArg = arg.replaceAll("(?:^\")|(?:\"$)", "");
                commandWithArgsList.add(trimmedArg);
            }
            List<String> command = Lists.newArrayList();
            command.add(commandWithArgsList.get(0));
            List<String> args = Lists.newArrayList(commandWithArgsList);
            args.remove(0);

            // k8s command snippet
            ManifestSnippet commandSnippet = new ManifestSnippet();
            commandSnippet.setPath("spec.template.spec.containers[0].command");
            commandSnippet.setKind(podSpecOwner);
            commandSnippet.setSnippet(JsonSnippetConvertor.serialize(command));
            snippetList.add(commandSnippet);
            logger.debug("Prepared command snippet {}",commandSnippet.getSnippet());


            // Args snippet
            ManifestSnippet argsSnippet = new ManifestSnippet();
            argsSnippet.setPath("spec.template.spec.containers[0].args");
            argsSnippet.setKind(podSpecOwner);
            argsSnippet.setSnippet(JsonSnippetConvertor.serialize(args));
            snippetList.add(argsSnippet);
            logger.debug("Prepared command args snippet {}.",argsSnippet.getSnippet());

        } catch (JsonProcessingException e) {
            logger.error("Error while creating command and args snippet ", e);
            return Collections.emptyList();
        }
        return snippetList;
    }
}
