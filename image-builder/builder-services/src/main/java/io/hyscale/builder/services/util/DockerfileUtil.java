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
package io.hyscale.builder.services.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import io.hyscale.builder.services.constants.DockerImageConstants;
import io.hyscale.commons.config.SetupConfig;
import io.hyscale.commons.constants.ToolConstants;
import io.hyscale.servicespec.commons.model.service.Dockerfile;

@Component
public class DockerfileUtil {

    public String getDockerfileAbsolutePath(Dockerfile dockerfile) {
        if (dockerfile == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        String path = dockerfile.getPath();
        if (StringUtils.isNotBlank(path)) {
            sb.append(path);
            if (!path.endsWith(ToolConstants.FILE_SEPARATOR)) {
                sb.append(ToolConstants.FILE_SEPARATOR);
            }
        }
        String dockerfileDir = dockerfile.getDockerfilePath();
        if (StringUtils.isNotBlank(dockerfileDir)) {
            sb.append(dockerfileDir);
        }
        String dockerfilePath = sb.toString();
        dockerfilePath = StringUtils.isNotBlank(dockerfilePath) ? SetupConfig.getAbsolutePath(dockerfilePath)
                : SetupConfig.getAbsolutePath(".");
        return dockerfilePath.endsWith(ToolConstants.FILE_SEPARATOR)
                ? dockerfilePath + DockerImageConstants.DOCKERFILE_NAME
                : dockerfilePath + ToolConstants.FILE_SEPARATOR + DockerImageConstants.DOCKERFILE_NAME;
    }
}
