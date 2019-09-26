package io.hyscale.ctl.servicespec.commons.util;

import io.hyscale.ctl.commons.constants.ToolConstants;
import io.hyscale.ctl.commons.exception.HyscaleException;
import io.hyscale.ctl.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.ctl.servicespec.commons.model.service.ServiceSpec;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

public class ImageUtil {

    private static final String DELIMITER = "/";

    public static String getImage(ServiceSpec serviceSpec) throws HyscaleException {
        String tag = serviceSpec.get(HyscaleSpecFields.getPath(HyscaleSpecFields.image, HyscaleSpecFields.tag), String.class);
        String registryUrl = Objects.requireNonNullElse(serviceSpec.get(HyscaleSpecFields.getPath(HyscaleSpecFields.image, HyscaleSpecFields.registry), String.class), ToolConstants.EMPTY_STRING);
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotBlank(registryUrl)) {
            sb.append(registryUrl);
            sb.append(DELIMITER);
        }
        sb.append(serviceSpec.get(HyscaleSpecFields.image + "." + HyscaleSpecFields.name, String.class));
        if (StringUtils.isNotBlank(tag)) {
            sb.append(":");
            sb.append(tag);
        }
        return sb.toString();
    }
}
