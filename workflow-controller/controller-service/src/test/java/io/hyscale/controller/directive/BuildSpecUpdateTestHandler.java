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
import io.hyscale.controller.directive.impl.BuildSpecJsonHandler;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.Artifact;
import io.hyscale.servicespec.commons.model.service.BuildSpec;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;

@Component
public class BuildSpecUpdateTestHandler implements IServiceSpecUpdateTestHandler<BuildSpec> {

    private static final String buildSpecUpdateSpec = "/servicespecs/buildSpec_update.hspec.yaml";

    private static final String JSON_PATH = HyscaleSpecFields.getPath(HyscaleSpecFields.image,
            HyscaleSpecFields.buildSpec);

    @Autowired
    private BuildSpecJsonHandler buildSpecHandler;

    @Override
    public String getServiceSpecPath() {
        return buildSpecUpdateSpec;
    }
    
    @Override
    public String getJsonPath() {
        return JSON_PATH;
    }
    
    @Override
    public boolean validate(BuildSpec oldBuildSpec, BuildSpec updatedBuildSpec) {
        assertEquals(WindowsUtil.updateToUnixFileSeparator(oldBuildSpec.getConfigCommandsScript()),
              updatedBuildSpec.getConfigCommandsScript());
      assertEquals(WindowsUtil.updateToUnixFileSeparator(oldBuildSpec.getRunCommandsScript()),
              updatedBuildSpec.getRunCommandsScript());

      for (Artifact updatedArtifact : updatedBuildSpec.getArtifacts()) {
          for (Artifact oldArtifact : oldBuildSpec.getArtifacts()) {
              if (updatedArtifact.getName().equals(oldArtifact.getName())) {
                  assertEquals(oldArtifact.getDestination(), updatedArtifact.getDestination());
                  if (oldArtifact.getSource().contains("\\")) {
                      assertEquals(WindowsUtil.updateToUnixFileSeparator(oldArtifact.getSource()),
                              updatedArtifact.getSource());
                  } else {
                      assertEquals(oldArtifact.getSource(), updatedArtifact.getSource());
                  }
              }
          }
      }
      return true;
    }

    @Override
    public ServiceSpec updateServiceSpec(ObjectNode serviceSpecNode) {
        try {
            buildSpecHandler.update(serviceSpecNode);
            return new ServiceSpec(serviceSpecNode);
        } catch (HyscaleException e) {
            fail();
        }
        return null;
    }

    @Override
    public Class getType() {
        return BuildSpec.class;
    }

}
