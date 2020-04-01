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
import java.util.List;

public class PreProcessingArgs {

    private List<String> serviceSpecPaths;
    private List<String> profilePaths;
    private String profileName;

    public List<String> getServiceSpecPaths() {
        return serviceSpecPaths;
    }

    public void setServiceSpecPaths(List<String> serviceSpecPaths) {
        this.serviceSpecPaths = serviceSpecPaths;
    }

    public void addServiceSpecPath(String serviceSpecPath) {
        if (serviceSpecPaths == null) {
            serviceSpecPaths = new ArrayList<String>();
        }
        serviceSpecPaths.add(serviceSpecPath);
    }

    public List<String> getProfilePaths() {
        return profilePaths;
    }

    public void setProfilePaths(List<String> profilePaths) {
        this.profilePaths = profilePaths;
    }

    public void addProfilePath(String profilePath) {
        if (profilePaths == null) {
            profilePaths = new ArrayList<String>();
        }
        profilePaths.add(profilePath);
    }

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

}
