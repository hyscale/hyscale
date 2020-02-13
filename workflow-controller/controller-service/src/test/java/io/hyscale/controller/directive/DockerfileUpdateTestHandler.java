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
package io.hyscale.controller.directive;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.utils.WindowsUtil;
import io.hyscale.controller.directive.impl.DockerfileJsonHandler;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.Dockerfile;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;

@Component
public class DockerfileUpdateTestHandler implements IServiceSpecUpdateTestHandler<Dockerfile> {

    private static final String JSON_PATH = HyscaleSpecFields.getPath(HyscaleSpecFields.image,
            HyscaleSpecFields.dockerfile);

    private static final String dockerfileUpdateSpec = "/servicespecs/dockerfile_update.hspec";

    @Autowired
    private DockerfileJsonHandler dockerfileHandler;

    @Override
    public String getServiceSpecPath() {
        return dockerfileUpdateSpec;
    }

    @Override
    public boolean validate(Dockerfile oldDockerfile, Dockerfile updatedDockerfile) {
        assertEquals(WindowsUtil.updateToUnixFileSeparator(oldDockerfile.getPath()), updatedDockerfile.getPath());
        assertEquals(WindowsUtil.updateToUnixFileSeparator(oldDockerfile.getDockerfilePath()),
                updatedDockerfile.getDockerfilePath());
        return true;
    }

    @Override
    public ServiceSpec updateServiceSpec(ObjectNode serviceSpecNode) {
        try {
            dockerfileHandler.update(serviceSpecNode);
            return new ServiceSpec(serviceSpecNode);
        } catch (HyscaleException e) {
            fail();
        }
        return null;
    }

    @Override
    public String getJsonPath() {
        return JSON_PATH;
    }

    @Override
    public Class<Dockerfile> getType() {
        return Dockerfile.class;
    }
}
