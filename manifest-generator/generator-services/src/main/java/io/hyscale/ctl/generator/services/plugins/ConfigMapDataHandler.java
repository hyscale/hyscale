package io.hyscale.ctl.generator.services.plugins;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.hyscale.ctl.annotation.ManifestPlugin;
import io.hyscale.ctl.commons.config.SetupConfig;
import io.hyscale.ctl.commons.constants.ToolConstants;
import io.hyscale.ctl.commons.exception.HyscaleException;
import io.hyscale.ctl.commons.models.ManifestContext;
import io.hyscale.ctl.commons.utils.HyscaleFilesUtil;
import io.hyscale.ctl.generator.services.model.ManifestResource;
import io.hyscale.ctl.generator.services.model.MetaDataContext;
import io.hyscale.ctl.generator.services.predicates.ManifestPredicates;
import io.hyscale.ctl.generator.services.provider.PropsProvider;
import io.hyscale.ctl.plugin.ManifestHandler;
import io.hyscale.ctl.plugin.ManifestSnippet;
import io.hyscale.ctl.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.ctl.servicespec.commons.model.PropType;
import io.hyscale.ctl.servicespec.commons.model.service.Props;
import io.hyscale.ctl.servicespec.commons.model.service.ServiceSpec;
import io.hyscale.ctl.util.JsonSnippetConvertor;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@ManifestPlugin(name = "ConfigMapDataHandler")
public class ConfigMapDataHandler implements ManifestHandler {

    private static final Logger logger = LoggerFactory.getLogger(ConfigMapDataHandler.class);

    @Autowired
    private HyscaleFilesUtil filesUtil;

    @Override
    public List<ManifestSnippet> handle(ServiceSpec serviceSpec, ManifestContext manifestContext) throws HyscaleException {
        Props props = PropsProvider.getProps(serviceSpec);
        if (!ManifestPredicates.getPropsPredicate().test(serviceSpec)) {
            logger.debug("Props found to be empty while processing configmap data.");
            return null;
        }
        MetaDataContext metaDataContext = new MetaDataContext();
        metaDataContext.setAppName(manifestContext.getAppName());
        metaDataContext.setEnvName(manifestContext.getEnvName());
        metaDataContext.setServiceName(serviceSpec.get(HyscaleSpecFields.name, String.class));


        String propsVolumePath = serviceSpec.get(HyscaleSpecFields.propsVolumePath, String.class);

        List<ManifestSnippet> manifestSnippetList = new ArrayList<>();
        try {
            manifestSnippetList.add(getConfigMapData(props, propsVolumePath, metaDataContext));
            logger.debug("Added config map data to the manifest snippet list");
        } catch (JsonProcessingException e) {
            logger.error("Error while generating manifest for props of service {}", metaDataContext.getServiceName(), e);
        }
        return manifestSnippetList;
    }

    private ManifestSnippet getConfigMapData(Props props, String propsVolumePath, MetaDataContext metaDataContext)
            throws JsonProcessingException, HyscaleException {
        Map<String, String> configProps = new HashMap<>();
        StringBuilder sb = new StringBuilder();
        props.getProps().entrySet().stream().forEach(each -> {
            String value = each.getValue();
            if (PropType.FILE.getPatterMatcher().matcher(value).matches()) {
                String fileContent = null;
                try (InputStream is = new FileInputStream(SetupConfig.getAbsolutePath(PropType.FILE.extractPropValue(value)))) {
                    fileContent = IOUtils.toString(is, ToolConstants.CHARACTER_ENCODING);
                    logger.debug(" Adding file {} to config props." , value);
                    configProps.put(each.getKey(), Base64.encodeBase64String(fileContent.getBytes()));
                } catch (IOException e) {
                    logger.error("Error while reading file content of config prop {}", each.getKey(), e);
                }
            } else if (PropType.ENDPOINT.getPatterMatcher().matcher(value).matches()) {
                String propValue = PropType.ENDPOINT.extractPropValue(each.getValue());
                configProps.put(each.getKey(), propValue);
                logger.debug(" Adding endpoint {} to config props." , value);
                sb.append(each.getKey()).append("=").append(propValue).append("\n");
            } else {
                String propValue = PropType.STRING.extractPropValue(each.getValue());
                configProps.put(each.getKey(), propValue);
                logger.debug(" Adding prop {} to config props." ,value);
                sb.append(each.getKey()).append("=").append(propValue).append("\n");
            }
        });

        String fileData = sb.toString();
        if (StringUtils.isNotBlank(fileData) && StringUtils.isNotBlank(propsVolumePath)) {
            logger.debug("Processing props file data.");
            configProps.put(filesUtil.getFileName(propsVolumePath), fileData);
        }

        ManifestSnippet configMapDataSnippet = new ManifestSnippet();
        configMapDataSnippet.setKind(ManifestResource.CONFIG_MAP.getKind());
        configMapDataSnippet.setPath("data");
        configMapDataSnippet.setSnippet(JsonSnippetConvertor.serialize(configProps));
        logger.debug("Generated Config map snippet.");
        return configMapDataSnippet;
    }

}
