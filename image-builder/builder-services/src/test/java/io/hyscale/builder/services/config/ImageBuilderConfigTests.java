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

import io.hyscale.commons.config.SetupConfig;
import io.hyscale.commons.constants.ToolConstants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;

public class ImageBuilderConfigTests {
    @Mock
    private SetupConfig setupConfig;

    @InjectMocks
    private ImageBuilderConfig imageBuilderConfig;

    private String appName = "myApp";
    private String serviceName = "mySvc";
    private String pushLog = "push.log";
    private String buildLog = "build.log";
    private StringBuilder expectedPath;


    @BeforeEach
    public void init() {
        expectedPath = new StringBuilder();
        expectedPath.append(SetupConfig.USER_HOME_DIR).append(ToolConstants.FILE_SEPARATOR).append("hyscale").append(ToolConstants.FILE_SEPARATOR).append("apps").append(
                ToolConstants.FILE_SEPARATOR).append(appName).append(ToolConstants.FILE_SEPARATOR).append(serviceName)
                .append(ToolConstants.FILE_SEPARATOR).append("logs").append(ToolConstants.FILE_SEPARATOR);
        MockitoAnnotations.initMocks(this);
        Mockito.when(setupConfig.getLogsDir(appName,serviceName)).thenReturn(expectedPath.toString());


    }

    @Test
    public void getDockerBuildlogTest() {
        String buildLogPath = imageBuilderConfig.getDockerBuildlog(appName, serviceName);
        expectedPath.append(buildLog);
        Assertions.assertNotNull(buildLogPath);
        Assertions.assertEquals(expectedPath, buildLogPath);
    }

    @Test
    public void getDockerPushlogTest() {
        String pushLogPath = imageBuilderConfig.getDockerBuildlog(appName, serviceName);
        expectedPath.append(pushLog);
        Assertions.assertNotNull(pushLogPath);
        Assertions.assertEquals(expectedPath, pushLogPath);
    }
}
