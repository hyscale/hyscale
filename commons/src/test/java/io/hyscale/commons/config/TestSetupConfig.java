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

public class TestSetupConfig {
    private static String appName;
    private static String svcName;
    private static StringBuilder APPS_DIR;
    private static StringBuilder SERVICE_DIR;
    private static SetupConfig setupConfig;
    private static String hyscale = "hyscale";
    private static String generatedFilesDir = "generated-files";
    private static String logDir = "logs";
    private static String appsDirectory = "apps";

    @BeforeAll
    public static void init() {
        appName = "myApp";
        svcName = "mySvc";
        APPS_DIR = new StringBuilder();
        APPS_DIR.append(SetupConfig.USER_HOME_DIR).append(ToolConstants.FILE_SEPARATOR).append(hyscale).
                append(ToolConstants.
                        FILE_SEPARATOR).append(appsDirectory).append(ToolConstants.FILE_SEPARATOR).toString();
        SERVICE_DIR = new StringBuilder();
        SERVICE_DIR.append(APPS_DIR).append(appName).append(ToolConstants.FILE_SEPARATOR).append(svcName).append(ToolConstants.FILE_SEPARATOR).toString();
        setupConfig = new SetupConfig();
    }


    @Test
    public void testGetToolLogDir() {
        StringBuilder expected = new StringBuilder();
        expected.append(SetupConfig.USER_HOME_DIR).append(ToolConstants.FILE_SEPARATOR).append(hyscale).append(ToolConstants.FILE_SEPARATOR).append(logDir);
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
        assertEquals(APPS_DIR.toString(), setupConfig.getAppsDir());
    }

    @Test
    public void testGetServiceDir() {
        assertEquals(SERVICE_DIR.toString(), setupConfig.getServiceDir(appName, svcName));
    }

    @Test
    public void testGetGeneratedFilesDir() {
        StringBuilder expected = new StringBuilder();
        expected.append(SERVICE_DIR).append(generatedFilesDir).append(ToolConstants.FILE_SEPARATOR);
        assertEquals(expected.toString(), setupConfig.getGeneratedFilesDir(appName, svcName));
    }

    @Test
    public void testGetLogsDir() {
        StringBuilder expected = new StringBuilder();
        expected.append(SERVICE_DIR).append(logDir).append(ToolConstants.FILE_SEPARATOR);
        assertEquals(expected.toString(), setupConfig.getLogsDir(appName, svcName));
    }

    @Test
    public void testGetMountPathOf() {
        assertEquals(hyscale, SetupConfig.getMountPathOf(hyscale));
    }

    @Test
    public void testGetMountPathOfKubeConf() {
        assertEquals(hyscale, SetupConfig.getMountPathOfKubeConf(hyscale));
    }

    @Test
    public void testGetMountOfDockerConf() {
        assertEquals(hyscale, SetupConfig.getMountOfDockerConf(hyscale));
    }
}
