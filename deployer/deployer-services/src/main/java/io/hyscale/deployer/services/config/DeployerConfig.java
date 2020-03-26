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

import io.hyscale.commons.config.SetupConfig;
import io.hyscale.deployer.services.model.ResourceUpdatePolicy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Deployment related config properties
 *
 */
@Component
@PropertySource("classpath:config/deployer-config.props")
public class DeployerConfig {

    private static final String DEPLOY_LOG = "deploy.log";

    private static final String SERVICE_LOG = "service.log";

    @Value(("${hyscale.ctl.k8s.pod.log.tail.lines:100}"))
    private int defaultTailLines;

    @Autowired
    private SetupConfig setupConfig;

    public int getDefaultTailLines() {
        return defaultTailLines;
    }

    /**
     * @param appName
     * @param serviceName
     * @return deploy logs directory
     */
    public String getDeployLogDir(String appName, String serviceName) {
        StringBuilder sb = new StringBuilder(setupConfig.getLogsDir(appName, serviceName));
        sb.append(DEPLOY_LOG);
        return sb.toString();
    }

    /**
     *
     * @param appName
     * @param serviceName
     * @return service logs directory
     */
    public String getServiceLogDir(String appName, String serviceName) {
        StringBuilder sb = new StringBuilder(setupConfig.getLogsDir(appName, serviceName));
        sb.append(SERVICE_LOG);
        return sb.toString();
    }


}
