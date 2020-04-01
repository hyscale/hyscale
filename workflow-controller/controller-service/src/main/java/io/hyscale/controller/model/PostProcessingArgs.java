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
package io.hyscale.controller.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.hyscale.servicespec.commons.model.service.ServiceSpec;

public class PostProcessingArgs {

    private List<ServiceSpec> serviceSpecs;
    private Map<String, String> serviceVsProfile;

    public List<ServiceSpec> getServiceSpecs() {
        return serviceSpecs;
    }

    public void setServiceSpecs(List<ServiceSpec> serviceSpecs) {
        this.serviceSpecs = serviceSpecs;
    }

    public void addServiceSpec(ServiceSpec serviceSpec) {
        if (this.serviceSpecs == null) {
            serviceSpecs = new ArrayList<ServiceSpec>();
        }
        this.serviceSpecs.add(serviceSpec);
    }

    public Map<String, String> getServiceVsProfile() {
        return serviceVsProfile;
    }

    public void setServiceVsProfile(Map<String, String> serviceVsProfile) {
        this.serviceVsProfile = serviceVsProfile;
    }

    public void addServiceProfile(String service, String profile) {
        if (serviceVsProfile == null) {
            serviceVsProfile = new HashMap<String, String>();
        }
        serviceVsProfile.put(service, profile);
    }

}
