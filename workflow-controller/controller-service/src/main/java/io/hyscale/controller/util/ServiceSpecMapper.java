package io.hyscale.controller.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.hyscale.commons.constants.ToolConstants;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.utils.ObjectMapperFactory;
import io.hyscale.controller.core.exception.ControllerErrorCodes;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * Get {@link ServiceSpec} from input which could be a file or filepath
 *
 */
public class ServiceSpecMapper {

    private static final Logger logger = LoggerFactory.getLogger(ServiceSpecMapper.class);

    public static ServiceSpec from(File serviceSpecFile) throws HyscaleException {
        checkForFile(serviceSpecFile);
        ObjectMapper mapper = ObjectMapperFactory.yamlMapper();
        try {
            ObjectNode rootNode = (ObjectNode) mapper.readTree(serviceSpecFile);
            ServiceSpec serviceSpec = new ServiceSpec(rootNode);
            return serviceSpec;
        } catch (IOException e) {
            logger.error("Error while processing service spec ", e);
            throw new HyscaleException(ControllerErrorCodes.SERVICE_SPEC_PROCESSING_FAILED, e.getMessage());
        }
    }

    public static ServiceSpec from(String filepath) throws HyscaleException {
        File serviceSpecFile = new File(filepath);
        checkForFile(serviceSpecFile);
        return from(serviceSpecFile);
    }

    private static boolean checkForFile(File serviceSpecFile) throws HyscaleException {
        if (serviceSpecFile == null || !serviceSpecFile.exists()) {
            throw new HyscaleException(ControllerErrorCodes.CANNOT_FIND_SERVICE_SPEC,
                    serviceSpecFile != null ? serviceSpecFile.getName() : ToolConstants.EMPTY_STRING);
        }
        return true;
    }
}
