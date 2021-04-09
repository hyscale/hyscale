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
package io.hyscale.builder.services.provider;

import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import io.hyscale.builder.services.util.ServiceSpecTestUtil;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;

@SpringBootTest
class StackImageProviderTest {

    @Autowired
    private StackImageProvider stackImageProvider;

    private static Stream<Arguments> input() throws HyscaleException {
        return Stream.of(Arguments.of(null, Collections.EMPTY_LIST),
                Arguments.of(ServiceSpecTestUtil.getServiceSpec("/servicespec/no-stack-image.hspec"),
                        Collections.EMPTY_LIST),
                Arguments.of(ServiceSpecTestUtil.getServiceSpec("/servicespec/dockerfile-not-found.hspec"),
                        Collections.EMPTY_LIST),
                Arguments.of(ServiceSpecTestUtil.getServiceSpec("/servicespec/buildSpec.hspec"),
                        Arrays.asList("abc/testimg:1.0")),
                Arguments.of(ServiceSpecTestUtil.getServiceSpec("/servicespec/dockerfile.hspec"),
                        Arrays.asList("testimg:1.0", "testimg:2.0", "testimg:3.0")));
    }

    @ParameterizedTest
    @MethodSource("input")
    void testGetStackImages(ServiceSpec serviceSpec, List<String> stackImages) {
        List<String> availableStackImages = stackImageProvider.getStackImages(serviceSpec);
        if (stackImages == null) {
            assertNull(availableStackImages);
        } else {
            assertThat(stackImages, containsInAnyOrder(availableStackImages.toArray()));
        }

    }
}
