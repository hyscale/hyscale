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
package io.hyscale.deployer.services.config;

import io.hyscale.deployer.services.handler.impl.V1ServiceHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

public class NonBeanDeployerConfig {

    private static final Logger logger = LoggerFactory.getLogger(NonBeanDeployerConfig.class);

    private static Properties properties;

    private static final String MAX_LB_READY_TIMEOUT_PROPERTY = "hyscale.ctl.k8s.max.lb.ready.timeout.ms";

    static {
        URL url = V1ServiceHandler.class.getResource("/config/deployer-config.props");
        properties = new Properties();
        try (InputStream is = url.openStream()) {
            properties.load(is);
        } catch (IOException e) {
            logger.error("Error while loading deployer properties ", e);
        }
    }

    public static long getMaxLBReadyTimeout() {
        return Long.parseLong(String.valueOf(properties.getOrDefault(MAX_LB_READY_TIMEOUT_PROPERTY, 90000)));
    }
}
