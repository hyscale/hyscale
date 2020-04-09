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

import io.hyscale.generator.services.model.ServiceMetadata;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;

public class EffectiveServiceSpec {

    private ServiceMetadata serviceMetadata;
    private ServiceSpec serviceSpec;
    private File serviceSpecFile;

    public ServiceMetadata getServiceMetadata() {
        return serviceMetadata;
    }

    public void setServiceMetadata(ServiceMetadata serviceMetadata) {
        this.serviceMetadata = serviceMetadata;
    }

    public ServiceSpec getServiceSpec() {
        return serviceSpec;
    }

    public void setServiceSpec(ServiceSpec serviceSpec) {
        this.serviceSpec = serviceSpec;
    }

    public File getServiceSpecFile() {
        return serviceSpecFile;
    }

    public void setServiceSpecFile(File serviceSpecFile) {
        this.serviceSpecFile = serviceSpecFile;
    }

}
