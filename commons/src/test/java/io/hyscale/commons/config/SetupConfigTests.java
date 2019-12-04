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
package io.hyscale.commons.config;

import io.hyscale.commons.constants.ToolConstants;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SetupConfigTests {
    private static final String APP_NAME = "myApp";
    private static final String SVC_NAME = "mySvc";
    private static final String HYSCALE = "hyscale";
    private static final String GENERATED_FILES = "generated-files";
    private static final String LOGS_DIRECTORY = "logs";
    private static final String APPS_DIRECTORY = "apps";
    private static StringBuilder appsDir;
    private static StringBuilder serviceDir;
    private static SetupConfig setupConfig;
    //TODO autowire setup config
    @BeforeAll
    public static void init() {
        appsDir = new StringBuilder();
        appsDir.append(SetupConfig.USER_HOME_DIR).append(ToolConstants.FILE_SEPARATOR).append(HYSCALE).
                append(ToolConstants.
                        FILE_SEPARATOR).append(APPS_DIRECTORY).append(ToolConstants.FILE_SEPARATOR).toString();
        serviceDir = new StringBuilder();
        serviceDir.append(appsDir).append(APP_NAME).append(ToolConstants.FILE_SEPARATOR).append(SVC_NAME).append(ToolConstants.FILE_SEPARATOR).toString();
        setupConfig = new SetupConfig();
    }


    @Test
    public void testGetToolLogDir() {
        StringBuilder expected = new StringBuilder();
        expected.append(SetupConfig.USER_HOME_DIR).append(ToolConstants.FILE_SEPARATOR).append(HYSCALE).append(ToolConstants.FILE_SEPARATOR).append(LOGS_DIRECTORY);
        assertEquals(expected.toString(), SetupConfig.getToolLogDir());
    }

    @Test
    public void testGetInstallationDir() {
        StringBuilder expected = new StringBuilder();
        expected.append(SetupConfig.USER_HOME_DIR).append(ToolConstants.FILE_SEPARATOR);
        assertEquals(expected.toString(), setupConfig.getInstallationDir());
    }

    @Test
    public void testGetAppsDir() {
        assertEquals(appsDir.toString(), setupConfig.getAppsDir());
    }

    @Test
    public void testGetServiceDir() {
        assertEquals(serviceDir.toString(), setupConfig.getServiceDir(APP_NAME, SVC_NAME));
    }

    @Test
    public void testGetGeneratedFilesDir() {
        StringBuilder expected = new StringBuilder();
        expected.append(serviceDir).append(GENERATED_FILES).append(ToolConstants.FILE_SEPARATOR);
        assertEquals(expected.toString(), setupConfig.getGeneratedFilesDir(APP_NAME, SVC_NAME));
    }

    @Test
    public void testGetServiceLogsDir() {
        StringBuilder expected = new StringBuilder();
        expected.append(serviceDir).append(LOGS_DIRECTORY).append(ToolConstants.FILE_SEPARATOR);
        assertEquals(expected.toString(), setupConfig.getLogsDir(APP_NAME, SVC_NAME));
    }

    @Test
    public void testGetMountPathOf() {
        assertEquals(HYSCALE, SetupConfig.getMountPathOf(HYSCALE));
    }

    @Test
    public void testGetMountPathOfKubeConf() {
        assertEquals(HYSCALE, SetupConfig.getMountPathOfKubeConf(HYSCALE));
    }

    @Test
    public void testGetMountOfDockerConf() {
        assertEquals(HYSCALE, SetupConfig.getMountOfDockerConf(HYSCALE));
    }
}
