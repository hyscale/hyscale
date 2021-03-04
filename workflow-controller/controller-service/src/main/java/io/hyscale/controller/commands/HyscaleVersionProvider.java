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

import io.hyscale.controller.constants.GitPropertyConstants;
import io.hyscale.controller.util.GitPropertyProvider;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Component;

import io.hyscale.commons.constants.ToolConstants;
import picocli.CommandLine;

/**
 * Provides version when 'hyscale --version' command
 * is executed
 * <p>
 * HyScale Version {@literal<version>} {@literal<release-name>}, build {@literal<commitID>}
 */
@Component
public class HyscaleVersionProvider implements CommandLine.IVersionProvider {

    @Autowired
    BuildProperties buildProperties;

    @Autowired
    GitPropertyProvider gitPropertyProvider;

    @Override
    public String[] getVersion() throws Exception {
        StringBuilder sb = new StringBuilder();
        String buildName = buildProperties.get(ToolConstants.HYSCALE_RELEASE_NAME);
        sb.append(ToolConstants.HYSCALE).append(ToolConstants.SPACE);
        sb.append(ToolConstants.VERSION_KEY).append(ToolConstants.SPACE);
        sb.append(buildProperties.getVersion()).append(ToolConstants.SPACE);
        if (StringUtils.isNotBlank(buildName)) {
            sb.append(ToolConstants.QUOTES).append(buildName).append(ToolConstants.QUOTES);
        }

        String gitLastCommitId =gitPropertyProvider.getGitProperty(GitPropertyConstants.GIT_COMMIT_ID);
        if(StringUtils.isNotBlank(gitLastCommitId)){
            sb.append(ToolConstants.COMMA).append(ToolConstants.SPACE);
            sb.append(ToolConstants.BUILD).append(ToolConstants.SPACE);
            sb.append(gitLastCommitId.substring(0,10));
        }
        return new String[]{sb.toString()};
    }
}
