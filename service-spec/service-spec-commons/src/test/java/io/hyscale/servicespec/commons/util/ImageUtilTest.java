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
package io.hyscale.servicespec.commons.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.hyscale.commons.constants.ToolConstants;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.utils.ObjectMapperFactory;
import io.hyscale.servicespec.BaseFieldsTest;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;

public class ImageUtilTest {
    
    private static ObjectMapper objectMapper = ObjectMapperFactory.yamlMapper();

    private static List<ServiceSpec> BUILD_PUSH_REQUIRED = new ArrayList<ServiceSpec>();

    private static List<ServiceSpec> BUILD_PUSH_NOT_REQUIRED = new ArrayList<ServiceSpec>();

    private static final List<String> BUILD_PUSH_REQUIRED_SPEC = Arrays
            .asList(new String[] { "/servicespecs/buildspec.hspec", "/servicespecs/dockerfile.hspec" });

    private static final List<String> BUILD_PUSH_NOT_REQUIRED_SPEC = Arrays
            .asList(new String[] { "/servicespecs/nobuildpush.hspec" });

    @BeforeAll
    public static void beforeTest() throws IOException, HyscaleException {
        for (String spec : BUILD_PUSH_REQUIRED_SPEC) {
            BUILD_PUSH_REQUIRED.add(new ServiceSpec(FileUtils.readFileToString(
                    new File(ImageUtilTest.class.getResource(spec).getFile()), ToolConstants.CHARACTER_ENCODING)));
        }
        for (String spec : BUILD_PUSH_NOT_REQUIRED_SPEC) {
            BUILD_PUSH_NOT_REQUIRED.add(new ServiceSpec(FileUtils.readFileToString(
                    new File(ImageUtilTest.class.getResource(spec).getFile()), ToolConstants.CHARACTER_ENCODING)));
        }
    }

    public static Stream<Arguments> input() {
        List<Arguments> args1 = BUILD_PUSH_REQUIRED.stream().map(each -> Arguments.of(each, true)).collect(Collectors.toList());
        args1.addAll(
                BUILD_PUSH_NOT_REQUIRED.stream().map(each -> Arguments.of(each, false)).collect(Collectors.toList()));
        args1.add(Arguments.of(null, false));
        return args1.stream();
    }

    @ParameterizedTest
    @MethodSource(value = "input")
    public void testIsImageBuildPushRequired(ServiceSpec serviceSpec, boolean expectedResult) {
        assertEquals(expectedResult, ImageUtil.isImageBuildPushRequired(serviceSpec));
    }
    
    public static Stream<Arguments> getImageInput() {
        return Stream.of(Arguments.of("/servicespecs/myservice.hspec", "x.y.z/myServiceImage:1.2.3"),
                Arguments.of("/servicespecs/test1.hspec", "x.y.z/myServiceImage"),
                Arguments.of("/servicespecs/test2.hspec", "myServiceImage"));
    }

    @ParameterizedTest
    @MethodSource(value = "getImageInput")
    public void testImage(String serviceSpecFile,String expectedResult) throws HyscaleException, IOException {
        InputStream resourceAsStream = BaseFieldsTest.class.getResourceAsStream(serviceSpecFile);
        String testData = IOUtils.toString(resourceAsStream, "UTF-8");
        ObjectNode rootNode = (ObjectNode) objectMapper.readTree(testData);
        ServiceSpec serviceSpec = new ServiceSpec(rootNode);
        String image = ImageUtil.getImage(serviceSpec);
        Assertions.assertNotNull(image);
        Assertions.assertEquals(expectedResult, image);
        resourceAsStream.close();
    }
}
