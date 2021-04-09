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
package io.hyscale.servicespec.commons.util;

import java.io.File;
import java.net.URL;

import org.apache.commons.lang3.StringUtils;

import io.hyscale.commons.config.SetupConfig;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;

public class ServiceSpecTestUtil {
    
    public static ServiceSpec getServiceSpec(String serviceSpecPath, boolean updateAbsPath) throws HyscaleException {
        if (StringUtils.isBlank(serviceSpecPath)) {
            return null;
        }
        File serviceSpecFile = getServiceSpecFile(serviceSpecPath);
        if (updateAbsPath) {
            updateAbsolutePath(serviceSpecFile);
        }
        return new ServiceSpec(serviceSpecFile);
    }

    public static ServiceSpec getServiceSpec(String serviceSpecPath) throws HyscaleException {
        if (StringUtils.isBlank(serviceSpecPath)) {
            return null;
        }
        return getServiceSpec(serviceSpecPath, false);
    }
    
    public static void updateAbsolutePath(File serviceSpecFile) {
        SetupConfig.clearAbsolutePath();
        SetupConfig.setAbsolutePath(serviceSpecFile.getAbsoluteFile().getParent());
    }

    private static File getServiceSpecFile(String serviceSpecPath) {
        URL urlPath = ServiceSpecTestUtil.class.getResource(serviceSpecPath);
        return new File(urlPath.getFile());
    }

}
