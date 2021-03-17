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
package io.hyscale.commons.models;

import java.util.List;

/**
 * Match conditions of a load balancer.
 */
public class LoadBalancerMapping {
    List<String> contextPaths;
    String port;
    Integer portNumber;

    public List<String> getContextPaths() {
        return contextPaths;
    }

    public void setContextPaths(List<String> contextPaths) {
        this.contextPaths = contextPaths;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
        setPortNumber(port);
    }

    public void setPortNumber(String port) {
        this.portNumber = Integer.parseInt(port.substring(0, port.indexOf('/')));
    }

    public Integer getPortNumber(String port) {
        return Integer.parseInt(port.substring(0, port.indexOf('/')));
    }
}
