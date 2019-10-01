package io.hyscale.ctl.generator.services.plugins;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Lists;
import io.hyscale.ctl.annotation.ManifestPlugin;
import io.hyscale.ctl.commons.exception.HyscaleException;
import io.hyscale.ctl.commons.models.ManifestContext;
import io.hyscale.ctl.generator.services.model.ManifestResource;
import io.hyscale.ctl.generator.services.predicates.ManifestPredicates;
import io.hyscale.ctl.plugin.ManifestHandler;
import io.hyscale.ctl.plugin.ManifestSnippet;
import io.hyscale.ctl.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.ctl.servicespec.commons.model.service.ServiceSpec;
import io.hyscale.ctl.util.JsonSnippetConvertor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
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
            return null;
        }
        String podSpecOwner = ManifestPredicates.getVolumesPredicate().test(serviceSpec) ?
                ManifestResource.STATEFUL_SET.getKind() : ManifestResource.DEPLOYMENT.getKind();
        return getCommandAndArgsSnippet(startCommand, podSpecOwner);
    }

    private List<ManifestSnippet> getCommandAndArgsSnippet(String startCommand, String podSpecOwner) {
        List<ManifestSnippet> snippetList = new ArrayList<>();
        try {
            List<String> commandWithArgsList = Lists.newArrayList();
            String[] commandWithArgsArray = startCommand.split("\\s*,\\s*");
            for (String arg : commandWithArgsArray) {
                String trimmedArg = arg.replaceAll("^\"|\"$", "");
                commandWithArgsList.add(trimmedArg);
            }
            // TODO split on " "? for command
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
            logger.debug("Prepared args snippet {}.",argsSnippet.getSnippet());

        } catch (JsonProcessingException e) {
            logger.error("Error while processing command and args snippet ", e);
            return null;
        }
        return snippetList;
    }
}
