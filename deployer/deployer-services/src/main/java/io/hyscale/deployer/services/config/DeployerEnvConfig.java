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

import io.hyscale.deployer.services.constants.DeployerConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeployerEnvConfig {

    private static Logger logger = LoggerFactory.getLogger(DeployerEnvConfig.class);

    public static long getLBReadyTimeout() {
        try {
            return Long.parseLong(getEnv(DeployerConstants.LB_READY_TIMEOUT));
        } catch (NumberFormatException e) {
            logger.error("Error while parsing max Lb ready timeout {} so defaulting to {}", DeployerConstants.LB_READY_TIMEOUT, DeployerConstants.DEFAULT_LB_READY_TIMEOUT);
            return DeployerConstants.DEFAULT_LB_READY_TIMEOUT;
        }
    }
    
    
    public static long getPodRestartCount() {
        try {
            return Long.parseLong(getEnv(DeployerConstants.POD_RESTART_COUNT));
        } catch (NumberFormatException e) {
            logger.error("Error while parsing pod restart count {} so defaulting to {}", DeployerConstants.POD_RESTART_COUNT, DeployerConstants.DEFAULT_POD_RESTART_COUNT);
            return DeployerConstants.DEFAULT_POD_RESTART_COUNT;
        }
    }

    private static String getEnv(String key) {
        return System.getenv(key);
    }
}
