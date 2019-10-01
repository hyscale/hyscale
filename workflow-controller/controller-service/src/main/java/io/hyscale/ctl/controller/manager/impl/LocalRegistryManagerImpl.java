package io.hyscale.ctl.controller.manager.impl;

import java.io.File;
import java.io.IOException;
import java.util.Base64;

import javax.annotation.PostConstruct;

import io.hyscale.ctl.commons.config.SetupConfig;
import io.hyscale.ctl.commons.logger.WorkflowLogger;
import io.hyscale.ctl.controller.activity.ControllerActivity;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.hyscale.ctl.commons.constants.ToolConstants;
import io.hyscale.ctl.commons.exception.HyscaleException;
import io.hyscale.ctl.commons.models.Auth;
import io.hyscale.ctl.commons.models.DockerConfig;
import io.hyscale.ctl.commons.models.ImageRegistry;
import io.hyscale.ctl.commons.utils.ObjectMapperFactory;
import io.hyscale.ctl.controller.config.ControllerConfig;
import io.hyscale.ctl.controller.core.exception.ControllerErrorCodes;
import io.hyscale.ctl.controller.manager.RegistryManager;

@Component
public class LocalRegistryManagerImpl implements RegistryManager {

    private static final Logger logger = LoggerFactory.getLogger(LocalRegistryManagerImpl.class);

    private static DockerConfig dockerConfig = new DockerConfig();

    @Autowired
    private ControllerConfig controllerConfig;

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

    @Override
    public ImageRegistry getImageRegistry(String registry) throws HyscaleException {
        if (StringUtils.isBlank(registry)) {
            logger.debug("Image push not required");
            return null;
        }
        Auth auth = dockerConfig.getAuths().get(registry);
        if (auth == null) {
            return null;
        }
        return getPrivateRegistry(auth, registry);
    }

    private ImageRegistry getPrivateRegistry(Auth auth, String url) {
        String encodedAuth = auth.getAuth();
        if (StringUtils.isBlank(encodedAuth)) {
            return null;
        }
        // Format username:password
        String decodedAuth = new String(Base64.getDecoder().decode(encodedAuth));
        String[] authArray = decodedAuth.split(ToolConstants.COLON);
        ImageRegistry imageRegistry = new ImageRegistry();
        imageRegistry.setUrl(url);
        imageRegistry.setUserName(authArray[0]);
        imageRegistry.setPassword(authArray[1]);

        return imageRegistry;
    }

}
