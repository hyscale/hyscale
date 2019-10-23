package io.hyscale.ctl.controller.plugins;

import com.fasterxml.jackson.core.type.TypeReference;
import io.hyscale.ctl.commons.component.InvokerHook;
import io.hyscale.ctl.commons.exception.HyscaleException;
import io.hyscale.ctl.controller.core.exception.ControllerErrorCodes;
import io.hyscale.ctl.controller.model.WorkflowContext;
import io.hyscale.ctl.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.ctl.servicespec.commons.model.service.Port;
import io.hyscale.ctl.servicespec.commons.model.service.ServiceSpec;
import io.hyscale.ctl.servicespec.commons.model.service.Volume;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Hook to validate service spec before manifest generation
 *
 */
@Component
public class ManifestValidatorHook implements InvokerHook<WorkflowContext> {

    private static final Logger logger = LoggerFactory.getLogger(ManifestValidatorHook.class);

    @Override
    public void preHook(WorkflowContext context) throws HyscaleException {
        logger.debug("Executing ManifestValidatorHook");
        ServiceSpec serviceSpec = context.getServiceSpec();
        if (serviceSpec == null) {
            logger.debug("Cannot service spec at manifest plugin ");
            throw new HyscaleException(ControllerErrorCodes.SERVICE_SPEC_REQUIRED);
        }
        TypeReference<List<Port>> listTypeReference = new TypeReference<List<Port>>() {
        };
        List<Port> portList = serviceSpec.get(HyscaleSpecFields.ports, listTypeReference);

        boolean validate = true;
        if (portList != null && !portList.isEmpty()) {
            for (Port port : portList) {
                logger.debug("Port : {}",port.getPort());
                validate = validate && port != null && StringUtils.isNotBlank(port.getPort());
                if (!validate) {
                    logger.debug("Error validating ports of service spec");
                    throw new HyscaleException(ControllerErrorCodes.INVALID_PORTS_FOUND);
                }
            }
        }

        TypeReference<List<Volume>> volumeTypeReference = new TypeReference<List<Volume>>() {
        };
        List<Volume> volumeList = serviceSpec.get(HyscaleSpecFields.volumes, volumeTypeReference);
        if (volumeList != null && !volumeList.isEmpty()) {
            for (Volume volume : volumeList) {
                validate = validate && volume != null && StringUtils.isNotBlank(volume.getName())
                        && StringUtils.isNotBlank(volume.getPath());
                if (!validate) {
                    logger.debug("Error validating volumes of service spec");
                    throw new HyscaleException(ControllerErrorCodes.INVALID_VOLUMES_FOUND);
                }
            }
        }
    }

    @Override
    public void postHook(WorkflowContext context) throws HyscaleException {

    }

    @Override
    public void onError(WorkflowContext context, Throwable th) {
        context.setFailed(true);
    }
}
