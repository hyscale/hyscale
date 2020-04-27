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
package io.hyscale.controller.commands.args;

import io.hyscale.commons.constants.ToolConstants;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.io.HyscaleFilesUtil;
import io.hyscale.controller.util.ServiceSpecUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.*;

/*
 * Locates the profile files with the given profile name
 * The directory of search if with respect to the relative
 * directory of servicespec or the relative directory of servicespec's
 * profiles directory.
 *
 * Example : /a/b/c/myservice.hspec is my service spec directory.
 * Profile is searched under
 *      1.  /a/b/c
 *      2.  /a/b/c/profiles directory
 */

@Component
public class ProfileLocator {

    private static final Logger logger = LoggerFactory.getLogger(ProfileLocator.class);

    public List<File> locateProfiles(String profileName, List<File> serviceSpecs) throws HyscaleException {
        List<File> profiles = new ArrayList<>();
        if (profileName != null) {
            profiles.addAll(getAllProfiles(serviceSpecs, profileName));
        }
        return profiles;
    }

    /**
     * Fetches the profile files of service specs relative to the servicespec directory
     * and dir(servicespec)/profiles directory.
     *
     * @param serviceSpecs
     * @param profileName
     * @return
     */
    public Set<File> getAllProfiles(List<File> serviceSpecs, String profileName) throws HyscaleException {
        if (serviceSpecs == null || StringUtils.isBlank(profileName)) {
            return null;
        }
        Set<File> profileFiles = new HashSet<File>();
        for (File serviceSpec : serviceSpecs) {
            String serviceSpecPath = serviceSpec.getAbsoluteFile().getParent();
            String profileFilePattern = getProfileNamePattern(profileName, ServiceSpecUtil.getServiceName(serviceSpec));

            // Search for profile relative to servicespec directory
            List<File> serviceProfileFiles = HyscaleFilesUtil.listFilesWithPattern(serviceSpecPath, profileFilePattern);
            if (serviceProfileFiles != null && !serviceProfileFiles.isEmpty()) {
                profileFiles.addAll(serviceProfileFiles);
            }

            // Search for profile relative to servicespec/profiles directory
            serviceSpecPath = serviceSpecPath.concat(ToolConstants.FILE_SEPARATOR)
                    .concat(ToolConstants.PROFILES_DIR_NAME);
            serviceProfileFiles = HyscaleFilesUtil.listFilesWithPattern(serviceSpecPath, profileFilePattern);
            if (serviceProfileFiles != null && !serviceProfileFiles.isEmpty()) {
                profileFiles.addAll(serviceProfileFiles);
            }
        }
        return profileFiles;
    }

    /**
     * @param profileName
     * @return ^(profileName-).*({@value ToolConstants#HPROF_EXTENSION})$
     */
    public String getProfileNamePattern(String profileName) {
        if (StringUtils.isBlank(profileName)) {
            return null;
        }
        StringBuilder profilePattern = new StringBuilder();
        profilePattern.append("^(").append(profileName).append(ToolConstants.DASH).append(")").append(".*").append("(")
                .append(ToolConstants.HPROF_EXTENSION).append(")$");
        return profilePattern.toString();
    }

    /**
     * @param profileName
     * @return ^(profileName-).*({@value ToolConstants#HPROF_EXTENSION})$
     */
    public String getProfileNamePattern(String profileName, String serviceName) {
        if (StringUtils.isBlank(profileName)) {
            return null;
        }
        StringBuilder profilePattern = new StringBuilder();
        profilePattern.append("^(").append(profileName).append(ToolConstants.DASH).append(serviceName).append(")").append(".*").append("(")
                .append(ToolConstants.HPROF_EXTENSION).append(")$");
        return profilePattern.toString();
    }

}
