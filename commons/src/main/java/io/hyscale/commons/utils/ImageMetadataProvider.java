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
package io.hyscale.commons.utils;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import io.hyscale.commons.commands.provider.ImageCommandProvider;
import io.hyscale.commons.constants.ToolConstants;

@Component
public class ImageMetadataProvider {
    
    private static final String HYSCALE_IO_URL = "hyscale.io";
    private static final String SLASH = "/";

    public String getBuildImageName(String appName, String serviceName) {
        StringBuilder sb = new StringBuilder();
        sb.append(HYSCALE_IO_URL).append(SLASH).append(appName).append(SLASH).append(serviceName);
        return NormalizationUtil.normalizeImageName(sb.toString());
    }

    public String getBuildImageNameWithTag(String appName, String serviceName, String tag) {
        StringBuilder sb = new StringBuilder();
        sb.append(HYSCALE_IO_URL).append(SLASH).append(appName).append(SLASH).append(serviceName);
        if (StringUtils.isNotBlank(tag)) {
            sb.append(ToolConstants.COLON).append(tag);
        }
        return NormalizationUtil.normalizeImageName(sb.toString());
    }
    
    public Map<String, String> getImageOwnerLabel(){
        Map<String, String> labels = new HashMap<>();
        labels.put(ImageCommandProvider.IMAGE_OWNER, ImageCommandProvider.HYSCALE);
        return labels;
    }
}
