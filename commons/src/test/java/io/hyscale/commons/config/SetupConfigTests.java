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
    private static String appName;
    private static String svcName;
    private static StringBuilder appsDir;
    private static StringBuilder serviceDir;
    private static SetupConfig setupConfig;
    private static String hyscale;
    private static String generatedFilesDir;
    private static String logDir;
    private static String appsDirectory;
    //TODO autowire setup config
    @BeforeAll
    public static void init() {
        appName = "myApp";
        svcName = "mySvc";
        hyscale = "hyscale";
        generatedFilesDir = "generated-files";
        logDir = "logs";
        appsDirectory = "apps";
        appsDir = new StringBuilder();
        appsDir.append(SetupConfig.USER_HOME_DIR).append(ToolConstants.FILE_SEPARATOR).append(hyscale).
                append(ToolConstants.
                        FILE_SEPARATOR).append(appsDirectory).append(ToolConstants.FILE_SEPARATOR).toString();
        serviceDir = new StringBuilder();
        serviceDir.append(appsDir).append(appName).append(ToolConstants.FILE_SEPARATOR).append(svcName).append(ToolConstants.FILE_SEPARATOR).toString();
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
        assertEquals(appsDir.toString(), setupConfig.getAppsDir());
    }

    @Test
    public void testGetServiceDir() {
        assertEquals(serviceDir.toString(), setupConfig.getServiceDir(appName, svcName));
    }

    @Test
    public void testGetGeneratedFilesDir() {
        StringBuilder expected = new StringBuilder();
        expected.append(serviceDir).append(generatedFilesDir).append(ToolConstants.FILE_SEPARATOR);
        assertEquals(expected.toString(), setupConfig.getGeneratedFilesDir(appName, svcName));
    }

    @Test
    public void testGetServiceLogsDir() {
        StringBuilder expected = new StringBuilder();
        expected.append(serviceDir).append(logDir).append(ToolConstants.FILE_SEPARATOR);
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
