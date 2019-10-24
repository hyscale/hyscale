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
package io.hyscale.servicespec;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.utils.ObjectMapperFactory;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;
import io.hyscale.servicespec.commons.util.ImageUtil;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;

public class ImageUtilTest {

    static ObjectMapper objectMapper = ObjectMapperFactory.yamlMapper();



    public static Stream<Arguments> input() {
        return Stream.of(Arguments.of("/servicespecs/myservice.hspec.yaml", "x.y.z/myServiceImage:1.2.3"),
                Arguments.of("/servicespecs/test1.hspec.yaml", "x.y.z/myServiceImage"),
                Arguments.of("/servicespecs/test2.hspec.yaml", "myServiceImage"));
    }

    @ParameterizedTest
    @MethodSource(value = "input")
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
