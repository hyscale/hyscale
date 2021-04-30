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
package io.hyscale.builder.services.predicates;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import io.hyscale.builder.core.models.BuildContext;
import io.hyscale.builder.services.util.ServiceSpecTestUtil;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.models.DockerfileEntity;
import io.hyscale.servicespec.commons.model.service.Dockerfile;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;

class ImageBuilderPredicatesTest {
    
    private static BuildContext contextWithDockerfile;
    private static BuildContext contextWithDockerEntity;
    

    @BeforeAll
    public static void init() {
        contextWithDockerfile = new BuildContext();
        DockerfileEntity entity = new DockerfileEntity();
        entity.setDockerfile(new File("Dockerfile"));
        contextWithDockerfile.setDockerfileEntity(entity);
        contextWithDockerEntity = new BuildContext();
        contextWithDockerEntity.setDockerfileEntity(new DockerfileEntity());
    }
    
    private static Stream<Arguments> skipBuildInput() {
        return Stream.of(Arguments.of(null, null, true),
                Arguments.of(null, new BuildContext(), true),
                Arguments.of(null, contextWithDockerEntity, true),
                Arguments.of(new Dockerfile(), null, false),
                Arguments.of(null, contextWithDockerfile, false));
    }

    @ParameterizedTest
    @MethodSource("skipBuildInput")
    void testSkipBuild(Dockerfile dockerfile, BuildContext context, boolean result) {
        assertEquals(result, ImageBuilderPredicates.getSkipBuildPredicate().test(dockerfile, context));
    }
    
    private static Stream<Arguments> buildPushRequiredInput() throws HyscaleException {
        BuildContext stackImageContext = new BuildContext();
        stackImageContext.setStackAsServiceImage(true);
        return Stream.of(Arguments.of(null, null, false),
                Arguments.of(ServiceSpecTestUtil.getServiceSpec("/servicespec/buildSpec.hspec"), null, false),
                Arguments.of(ServiceSpecTestUtil.getServiceSpec("/servicespec/dockerfile.hspec"), null, true),
                Arguments.of(null, new BuildContext(), false),
                Arguments.of(null, stackImageContext, true),
                Arguments.of(null, contextWithDockerfile, true),
                Arguments.of(null, contextWithDockerEntity, false));
    }
    
    @ParameterizedTest
    @MethodSource("buildPushRequiredInput")
    void testBuildPushRequired(ServiceSpec serviceSpec, BuildContext context, boolean result) {
        assertEquals(result, ImageBuilderPredicates.getBuildPushRequiredPredicate().test(serviceSpec, context));
    }
    
}
