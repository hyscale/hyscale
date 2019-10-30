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
package io.hyscale.controller.commands;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Component;

import io.hyscale.commons.constants.ToolConstants;
import picocli.CommandLine;

/**
 * Provides version when 'hyscale version' command
 * is executed
 */
@Component
public class HyscaleVersionProvider implements CommandLine.IVersionProvider {

    @Autowired
    BuildProperties buildProperties;

    @Override
    public String[] getVersion() throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append(ToolConstants.VERSION_KEY).append(buildProperties.getVersion());
        sb.append(ToolConstants.LINE_SEPARATOR);
        String buildDate = buildProperties.get(ToolConstants.HYSCALE_BUILD_TIME);
        sb.append(ToolConstants.BUILDDATE_KEY);
        if (StringUtils.isNotBlank(buildDate)) {
            sb.append(buildDate);
        } else {
            sb.append(buildProperties.getTime());
        }
        String buildName = buildProperties.get(ToolConstants.HYSCALE_RELASE_NAME);
        if (StringUtils.isNotBlank(buildName)) {
        	sb.append(ToolConstants.LINE_SEPARATOR);
        	sb.append(ToolConstants.RELEASE_NAME_KEY);
        	sb.append(buildName);
        }
        return new String[]{sb.toString()};
    }
}
