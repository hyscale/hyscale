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
package io.hyscale.builder.services.config;

import io.hyscale.commons.utils.WindowsUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import io.hyscale.commons.config.SetupConfig;

@Component
@PropertySource("classpath:config/image-builder.props")
public class ImageBuilderConfig {

    public static final String IMAGE_BUILDER_PROP = "hyscale.image.builder";
    public static final String DOCKER_HOST = "DOCKER_HOST";
    private static final String PUSH_LOG = "push.log";
    private static final String BUILD_LOG = "build.log";
    private static final String IMAGE_CLEAN_UP_POLICY_PROPERTY = "HYS_IMAGE_CLEANUP_POLICY";
    private static final String UNIX_DOCKER_SOCK = "unix:///var/run/docker.sock";

    
    @Autowired
    private SetupConfig setupConfig;

    @Value("${HYS.PRESERVE_N_RECENTLY_USED:3}")
    private Integer noOfPreservedImages;

    public String getImageCleanUpPolicy() {
        return System.getenv(IMAGE_CLEAN_UP_POLICY_PROPERTY);
    }

    public Integer getNoOfPreservedImages() {
        return noOfPreservedImages;
    }

    public String getDockerBuildlog(String appName, String serviceName) {
        StringBuilder sb = new StringBuilder(setupConfig.getLogsDir(appName, serviceName));
        sb.append(BUILD_LOG);
        return sb.toString();
    }

    public String getDockerPushLogDir(String appName, String serviceName) {
        StringBuilder sb = new StringBuilder(setupConfig.getLogsDir(appName, serviceName));
        sb.append(PUSH_LOG);
        return sb.toString();
    }

    public String getDockerHost(){
        String host = System.getenv(DOCKER_HOST);
        if(StringUtils.isNotBlank(host)){
            return host;
        }
        return WindowsUtil.isHostWindows() ? WindowsUtil.dockerHost() : UNIX_DOCKER_SOCK;
    }

}
