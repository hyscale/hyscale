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
package io.hyscale.deployer.services.model;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import io.hyscale.commons.constants.ToolConstants;

/**
 * Cluster Service address
 *
 */
public class ServiceAddress {

    private String serviceIP;
    private List<Integer> ports;
    private String serviceURL;

    public String getServiceIP() {
	return serviceIP;
    }

    public void setServiceIP(String serviceIP) {
	this.serviceIP = serviceIP;
    }

    public List<Integer> getPorts() {
	return ports;
    }

    public void setPorts(List<Integer> ports) {
	this.ports = ports;
    }

    /**
     * @return 
     * null if serviceIP is null or empty
     * else String representing
     * serviceIP:port1, serviceIP:port2
     */
    @Override
    public String toString() {
	if (StringUtils.isBlank(serviceIP)) {
	    return StringUtils.EMPTY;
	}

	StringBuilder address = new StringBuilder(serviceIP);
	if (ports != null && !ports.isEmpty()) {
	    address.append(ToolConstants.COLON);
	    address.append(ports.stream().map(Object::toString)
		    .collect(Collectors.joining(ToolConstants.COMMA + serviceIP + ToolConstants.COLON)));
	}

	return address.toString();
    }

    public String getServiceURL() {
        return serviceURL;
    }

    public void setServiceURL(String serviceURL) {
        this.serviceURL = serviceURL;
    }
}
