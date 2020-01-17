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
package io.hyscale.controller.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.hyscale.commons.constants.ToolConstants;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.controller.core.exception.ControllerErrorCodes;

public class ServiceProfileUtil {

    private static final Logger logger = LoggerFactory.getLogger(ServiceProfileUtil.class);

    private static final String DOT_REGEX = "\\.";

    public static Map<String, String> getServiceProfileMap(List<String> profiles) throws HyscaleException {
        Map<String, String> serviceProfileMap = new HashMap<String, String>();
        if (profiles == null || profiles.isEmpty()) {
            return serviceProfileMap;
        }
        for (String profilePath : profiles) {
            String serviceName = getServiceNameFromProfile(profilePath);
            if (serviceProfileMap.get(serviceName) == null) {
                serviceProfileMap.put(serviceName, profilePath);
            } else {
                // multiple profile for single service - fail
                HyscaleException hyscaleException = new HyscaleException(ControllerErrorCodes.UNIQUE_PROFILE_REQUIRED,
                        serviceName);
                logger.error("Service: {}, have more than 1 profile", serviceName, hyscaleException);
                throw hyscaleException;
            }
        }
        return serviceProfileMap;
    }

    /**
     * 
     * @param serviceSpecPath .../<service-name>.hspec.yaml
     * @return
     */
    public static String getServiceName(String serviceSpecPath) {
        if (StringUtils.isBlank(serviceSpecPath)) {
            return null;
        }
        String serviceSpec = FilenameUtils.getBaseName(serviceSpecPath);
        return serviceSpec.split(DOT_REGEX)[0];
    }

    /**
     * @param profilePath .../<profile-name>-<service-name>.hprof.yaml
     * @return
     */
    public static String getServiceNameFromProfile(String profilePath) {
        if (StringUtils.isBlank(profilePath)) {
            return null;
        }
        String serviceAndProfile = FilenameUtils.getBaseName(profilePath);
        int dashIndex = serviceAndProfile.indexOf(ToolConstants.DASH);
        if (dashIndex < 0) {
            return null;
        }
        int lastIndex = serviceAndProfile.indexOf(".hprof");
        return serviceAndProfile.substring(dashIndex + 1, lastIndex);

    }

    /**
     * @param profilePath .../<profile-name>-<service-name>.hprof.yaml
     * @return
     */
    public static String getProfileName(String profilePath) {
        if (StringUtils.isBlank(profilePath)) {
            return null;
        }
        String serviceAndProfile = FilenameUtils.getBaseName(profilePath);

        return serviceAndProfile.split(ToolConstants.DASH)[0];

    }
}
