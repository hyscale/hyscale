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
package io.hyscale.builder.services.command;

import io.hyscale.builder.services.ImageCommandGenerator;
import io.hyscale.commons.constants.ToolConstants;
import io.hyscale.commons.models.ImageRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ImageCommandGeneratorTests {
    private static String appName ;
    private static String svcName ;
    private static String tag;
    private static String expectedApp;
    private static String expectedSvc;
    private static String dockerFilePath;
    private static String imageName;
    private static String imageFullPath;
    private static ImageCommandGenerator imageCommandGenerator;
    private ImageRegistry imageRegistry;

    @BeforeEach
    public void performBeforeAll() {
        appName = "sampleApp";
        svcName = "sampleSvc";
        tag = "1.0";
        expectedApp = "sampleapp";
        expectedSvc = "samplesvc";
        imageFullPath = "sample"+ToolConstants.FILE_SEPARATOR+"Path";
        imageCommandGenerator = new ImageCommandGenerator();
        URL dockerFileUrl = ImageCommandGeneratorTests.class.getClassLoader().getResource("Dockerfile");
        dockerFilePath = dockerFileUrl.getPath();
        imageName = TestImageConstants.HYSCALE_IO_URL + TestImageConstants.SLASH + expectedApp + TestImageConstants.SLASH + expectedSvc;
        imageRegistry = new ImageRegistry();
    }

    @Test
    public void testGetBuildImageName() {
        StringBuilder expected = new StringBuilder();
        expected.append(imageName);
        assertEquals(expected.toString(), imageCommandGenerator.getBuildImageName(appName, svcName));
    }

    public static Stream<Arguments> getImageGeneratorCommand() {
        StringBuilder expected = new StringBuilder();
        return Stream.of(Arguments.of("", expected.append(imageName).toString()),
                Arguments.of(tag, expected.append(ToolConstants.COLON).append(tag).toString()));
    }

    @ParameterizedTest
    @MethodSource(value = "getImageGeneratorCommand")
    public void testGetBuildImageNameWithTag(String tag, String expected) {
        assertEquals(expected, imageCommandGenerator.getBuildImageNameWithTag(appName, svcName, tag));
    }

    public static Stream<Arguments> build() {
        Map<String, String> buildArgs = new HashMap<String, String>();
        buildArgs.put("flag", "true");
        StringBuilder expected = new StringBuilder();
        expected.append(TestImageConstants.DOCKER_BUILD).append(TestImageConstants.TAG_ARG).append(imageName).append(ToolConstants.COLON).append(tag).append(TestImageConstants.SPACE).append(dockerFilePath).append(ToolConstants.FILE_SEPARATOR);
        StringBuilder buildArgsCmd = new StringBuilder();
        buildArgsCmd.append(TestImageConstants.BUILD_ARGS).append("flag").append(TestImageConstants.EQUALS).append(buildArgs.get("flag"));
        return Stream.of(Arguments.of(null, expected.toString()),
                Arguments.of(buildArgs, expected.insert(12, buildArgsCmd).toString()));
    }

    @ParameterizedTest
    @MethodSource(value = "build")
    public void testDockerBuildCommand(Map<String, String> buildArgs, String expected) {
        assertEquals(expected, imageCommandGenerator.dockerBuildCommand(appName, svcName, tag, dockerFilePath, buildArgs));
    }

    @Test
    public void testGetDockerInstalledCommand() {
        StringBuilder expected = new StringBuilder();
        expected.append(TestImageConstants.dockerCommand).append(TestImageConstants.VERSION_COMMAND);
        assertEquals(expected.toString(), imageCommandGenerator.getDockerInstalledCommand());
    }

    @Test
    public void testGetDockerDaemonRunningCommand() {
        StringBuilder expected = new StringBuilder();
        expected.append(TestImageConstants.dockerCommand).append(TestImageConstants.IMAGES);
        assertEquals(expected.toString(), imageCommandGenerator.getDockerDaemonRunningCommand());
    }

    @Test
    public void testGetLoginCommand() {
        StringBuilder expected = new StringBuilder();
        expected.append(TestImageConstants.DOCKER_COMMAND).append(TestImageConstants.SPACE).append(TestImageConstants.LOGIN_COMMAND).append(TestImageConstants.SPACE).append(imageRegistry.getUrl()).append(TestImageConstants.USER_ARG)
                .append(imageRegistry.getUserName()).append(TestImageConstants.PASSWORD_ARG).append(imageRegistry.getPassword());
        assertEquals(expected.toString(), imageCommandGenerator.getLoginCommand(imageRegistry));
    }

    @Test
    public void testGetImagePushCommand() {
        StringBuilder expected = new StringBuilder();
        expected.append(TestImageConstants.DOCKER_COMMAND).append(TestImageConstants.SPACE).append(TestImageConstants.PUSH_COMMAND).append(TestImageConstants.SPACE).append(imageFullPath.toLowerCase());
        assertEquals(expected.toString(), imageCommandGenerator.getImagePushCommand(imageFullPath));
    }

    @Test
    public void testGetImageTagCommand() {
        String source = "baseImage";
        String target = "baseImage:2";
        StringBuilder expected = new StringBuilder();
        expected.append(TestImageConstants.DOCKER_COMMAND).append(TestImageConstants.SPACE).append(TestImageConstants.TAG_COMMAND).append(TestImageConstants.SPACE).append(source.toLowerCase()).append(TestImageConstants.SPACE).append(target.toLowerCase());
        assertEquals(expected.toString(), imageCommandGenerator.getImageTagCommand(source, target));
    }

    public static Stream<Arguments> pull() {
        StringBuilder expected = new StringBuilder();
        expected.append(TestImageConstants.DOCKER_COMMAND).append(TestImageConstants.SPACE).append(TestImageConstants.PULL_COMMAND).append(TestImageConstants.SPACE).append(imageFullPath.toLowerCase());
        return Stream.of(Arguments.of(null, null),
                Arguments.of(imageFullPath, expected.toString()));
    }

    @ParameterizedTest
    @MethodSource(value = "pull")
    public void testGetImagePullCommand(String image, String expected) {
        assertEquals(expected, imageCommandGenerator.getImagePullCommand(image));
    }

    @Test
    public void testGetImageCleanUpCommand() {
        StringBuilder expected = new StringBuilder();
        expected.append(TestImageConstants.DOCKER_COMMAND).append(TestImageConstants.SPACE).append(TestImageConstants.REMOVE_IMAGE).append(TestImageConstants.SPACE).append(imageCommandGenerator.getBuildImageNameWithTag(expectedApp, expectedSvc, tag));
        assertEquals(expected.toString(), imageCommandGenerator.getImageCleanUpCommand(appName, svcName, tag));
    }

    @Test
    public void testGetImageInspectCommand() {
        StringBuilder expected = new StringBuilder();
        expected.append(TestImageConstants.DOCKER_COMMAND).append(TestImageConstants.SPACE).append(TestImageConstants.INSPECT_COMMAND).append(TestImageConstants.SPACE).append(imageFullPath.toLowerCase());
        assertEquals(expected.toString(), imageCommandGenerator.getImageInspectCommand(imageFullPath));
    }
}
