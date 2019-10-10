package io.hyscale.ctl.controller.commands;

import io.hyscale.ctl.commons.constants.ToolConstants;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Component;
import picocli.CommandLine;

import java.text.SimpleDateFormat;

@Component
public class HyscaleVersionProvider implements CommandLine.IVersionProvider {

    @Autowired
    BuildProperties buildProperties;

    @Override
    public String[] getVersion() throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append(ToolConstants.VERSION_KEY).append(buildProperties.getVersion());
        sb.append(" ").append(ToolConstants.HYSCALE_PREVIEW);
        sb.append(ToolConstants.LINE_SEPARATOR);
        String buildDate = buildProperties.get(ToolConstants.HYSCALE_BUILD_TIME);
        sb.append(ToolConstants.BUILDDATE_KEY);
        if (StringUtils.isNotBlank(buildDate)) {
            sb.append(buildDate);
        } else {
            sb.append(buildProperties.getTime());
        }
        return new String[]{sb.toString()};
    }
}
