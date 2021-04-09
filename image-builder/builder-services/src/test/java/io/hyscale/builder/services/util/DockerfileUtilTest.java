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
package io.hyscale.builder.services.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import io.hyscale.builder.services.constants.DockerImageConstants;
import io.hyscale.commons.config.SetupConfig;
import io.hyscale.commons.constants.ToolConstants;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.servicespec.commons.model.service.Dockerfile;

@SpringBootTest
class DockerfileUtilTest {

    private static final String PATH = "path";
    private static final String DOCKERFILE_PATH = "dockerfilePath";

    @Autowired
    private DockerfileUtil dockerfileUtil;

    @Autowired
    private static SetupConfig setupConfig;

    private static Stream<Arguments> input() {
        Dockerfile dockerfileOnlyPath = new Dockerfile();
        dockerfileOnlyPath.setPath(PATH);
        Dockerfile dockerfile = new Dockerfile();
        dockerfile.setDockerfilePath(DOCKERFILE_PATH);
        Dockerfile dockerfileComplete = new Dockerfile();
        dockerfileComplete.setPath(PATH);
        dockerfileComplete.setDockerfilePath(DOCKERFILE_PATH);
        return Stream.of(Arguments.of(null, null),
                Arguments.of(new Dockerfile(), getPath(setupConfig.getAbsolutePath("."))),
                Arguments.of(dockerfileOnlyPath, getPath(setupConfig.getAbsolutePath(PATH))),
                Arguments.of(dockerfile, getPath(setupConfig.getAbsolutePath(DOCKERFILE_PATH))),
                Arguments.of(dockerfileComplete,
                        getPath(setupConfig.getAbsolutePath(PATH + ToolConstants.FILE_SEPARATOR + DOCKERFILE_PATH))));
    }

    @ParameterizedTest
    @MethodSource("input")
    void testDockerfilePath(Dockerfile dockerfile, String dockerfilePath) {
        assertEquals(dockerfilePath, dockerfileUtil.getDockerfileAbsolutePath(dockerfile));
    }

    private static String getPath(String path) {
        return path + ToolConstants.LINUX_FILE_SEPARATOR + DockerImageConstants.DOCKERFILE_NAME;
    }

}
