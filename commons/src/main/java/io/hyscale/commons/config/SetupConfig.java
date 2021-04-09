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

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import io.hyscale.commons.utils.HyscaleStringUtil;

@Component
public class SetupConfig {

    public static final String HYSCALE_CTL_HOME = "HYSCALECTL_HOME";
    public static final String USER_HOME_DIR = System.getProperty("user.home");
    public static final String TEMP_DIR = System.getProperty("java.io.tmpdir");
    public static final String CURRENT_WORKING_DIR = System.getProperty("user.dir");
    public static final String FILE_SEPARATOR = System.getProperty("file.separator");
    public static final String INSTALLATION_DIR = USER_HOME_DIR;
    public static final String HYSCALECTL_HOME_DIR = System.getenv(HYSCALE_CTL_HOME);
    public static final String KUBECONF_PATH_PROP = "HYSCALECTL_KUBECONF";
    public static final String KUBECONF_PATH_DIR = System.getenv(KUBECONF_PATH_PROP);
    public static final String DOCKERCONF_PATH_PROP = "HYSCALECTL_DOCKERCONF";
    public static final String DOCKERCONF_PATH_DIR = System.getenv(DOCKERCONF_PATH_PROP);
    public static final String HYS_REGISTRY_CONFIG_ENV = "HYS_REGISTRY_CONFIG";


    private static final String GENERATED_FILES_DIR = "generated-files";
    private static final String LOGS_DIR = "logs";
    private static final String APPS_DIR = "apps";
    private static final String HYSCALE_DIR = "hyscale";
    private static final String TEMPORARY_CONFIG_DIR = "hyscale-docker-config";

    private static final ThreadLocal<String> absolutePathTL = new ThreadLocal<>();

    public static void setAbsolutePath(String path) {
        if (StringUtils.isNotBlank(path) && absolutePathTL.get() == null) {
            absolutePathTL.set(path);
        }
    }
    
    public static String getToolLogDir() {
        return INSTALLATION_DIR + FILE_SEPARATOR + HYSCALE_DIR + FILE_SEPARATOR + LOGS_DIR;
    }

    private static String getAbsolutePath() {
        if (absolutePathTL.get() != null) {
            return absolutePathTL.get() + FILE_SEPARATOR;
        }
        return absolutePathTL.get();
    }

    public static void clearAbsolutePath() {
        absolutePathTL.remove();
    }

    /*
     * 1. When the path is absolute return the absolute path itself 2. When the path
     * is not absolute and use service spec absolute path , the source path is
     * expected to be relative to the service spec 3. When the path is not absolute
     * and service spec absolute path does not exist, the source path is expected to
     * be relative to the current working directory
     */

    public static String getAbsolutePath(String source) {
        if (StringUtils.isBlank(source)) {
            return getAbsolutePath() != null ? getAbsolutePath() : CURRENT_WORKING_DIR;
        }
        if (isAbsolute(source)) {
            return source;
        } else if (StringUtils.isNotBlank(getAbsolutePath())) {
            return getAbsolutePath() + source;
        }
        return CURRENT_WORKING_DIR + FILE_SEPARATOR + source;
    }
    
    /**
     * Source path is absolute if
     * 1. It starts with absolute path, given absolute path is fixed
     * 2. If absolute path not fixed then {@link File#isAbsolute()}
     * @param source
     * @return
     */
    private static boolean isAbsolute(String source) {
        if (StringUtils.isBlank(source)) {
            return false;
        }
        if (getAbsolutePath() != null) {
            return source.startsWith(HyscaleStringUtil.removeSuffixStr(getAbsolutePath(), FILE_SEPARATOR));
        }
        return new File(source).isAbsolute();
    }

    public String getInstallationDir() {
        return INSTALLATION_DIR + FILE_SEPARATOR;
    }

    public String getAppsDir() {
        return getInstallationDir() + HYSCALE_DIR + FILE_SEPARATOR + APPS_DIR + FILE_SEPARATOR;
    }

    public String getServiceDir(String appName, String serviceName) {
        StringBuilder sb = new StringBuilder();
        sb.append(getAppsDir()).append(appName).append(FILE_SEPARATOR).append(serviceName).append(FILE_SEPARATOR);
        return sb.toString();
    }

    public String getGeneratedFilesDir(String appName, String serviceName) {
        return getServiceDir(appName, serviceName) + GENERATED_FILES_DIR + FILE_SEPARATOR;
    }

    public String getLogsDir(String appName, String serviceName) {
        return getServiceDir(appName, serviceName) + LOGS_DIR + FILE_SEPARATOR;
    }

    public static String getMountPathOf(String dir) {
       return getMountPathOf(dir, HYSCALECTL_HOME_DIR);

    }

    public static String getMountPathOfKubeConf(String dir) {
        return StringUtils.isNotBlank(KUBECONF_PATH_DIR) ? KUBECONF_PATH_DIR : dir;
    }

    public static String getMountOfDockerConf(String dir) {
        return StringUtils.isNotBlank(DOCKERCONF_PATH_DIR) ? DOCKERCONF_PATH_DIR : dir;
    }

    private static String getMountPathOf(String dir, String source) {
        if (StringUtils.isNotBlank(dir) && StringUtils.isNotBlank(source)) {
            String sourceDir = source;
            if (!sourceDir.endsWith(FILE_SEPARATOR)) {
                sourceDir += FILE_SEPARATOR;
            }
            String userHomeDir = USER_HOME_DIR;
            if (!userHomeDir.endsWith(FILE_SEPARATOR)) {
                userHomeDir += FILE_SEPARATOR;
            }
            if (dir.contains(userHomeDir)) {
                return dir.replace(userHomeDir, sourceDir);
            }
        }
        return dir;
    }

    public static boolean hasExternalRegistryConf() {
        String hyscaleregistryConf = System.getenv(HYS_REGISTRY_CONFIG_ENV);
        if (StringUtils.isBlank(hyscaleregistryConf)) {
            return false;
        }
        return Boolean.valueOf(hyscaleregistryConf);
    }
    
    public static String getTemporaryDockerConfigDir() {
        return TEMP_DIR + FILE_SEPARATOR + TEMPORARY_CONFIG_DIR;
    }
}
