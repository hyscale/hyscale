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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class HyscaleCommandSpecBuilder {

    private String appName;
    private String namespace;
    private List<File> serviceSpecFiles;
    private List<File> profileFiles;
    private String profileName;

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public List<File> getServiceSpecFiles() {
        return serviceSpecFiles;
    }

    public void setServiceSpecFiles(List<File> serviceSpecFiles) {
        this.serviceSpecFiles = serviceSpecFiles;
    }
    
    public void addServiceSpecFile(File serviceSpecFile) {
        if (serviceSpecFiles == null) {
            serviceSpecFiles = new ArrayList<File>();
        }
        serviceSpecFiles.add(serviceSpecFile);
    }

    public List<File> getProfileFiles() {
        return profileFiles;
    }

    public void setProfileFiles(List<File> profileFiles) {
        this.profileFiles = profileFiles;
    }
    
    public void addProfileFile(File profileFile) {
        if (profileFiles == null) {
            profileFiles = new ArrayList<File>();
        }
        profileFiles.add(profileFile);
    }

    public String getProfileName() {
        return profileName;
    }

    public void setProfileName(String profileName) {
        this.profileName = profileName;
    }

}
