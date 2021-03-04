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
package io.hyscale.servicespec.commons.predicates;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.function.Predicate;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.BuildSpec;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;
import io.hyscale.servicespec.commons.util.ServiceSpecTestUtil;

class ServiceSpecPredicatesTest {

    private static Stream<Arguments> inputServiceSpec() {
        return Stream.of(Arguments.of(ServiceSpecPredicates.stackAsServiceImage(), null, false),
                Arguments.of(ServiceSpecPredicates.stackAsServiceImage(), "/servicespecs/stackAsImage/dockerfile.hspec",
                        false),
                Arguments.of(ServiceSpecPredicates.stackAsServiceImage(), "/servicespecs/stackAsImage/only-image.hspec",
                        false),
                Arguments.of(ServiceSpecPredicates.stackAsServiceImage(), "/servicespecs/stackAsImage/dont-skip.hspec",
                        false),
                Arguments.of(ServiceSpecPredicates.stackAsServiceImage(),
                        "/servicespecs/stackAsImage/invalid-spec.hspec", false),
                Arguments.of(ServiceSpecPredicates.stackAsServiceImage(),
                        "/servicespecs/stackAsImage/stack-as-service.hspec", true));
    }

    @ParameterizedTest
    @MethodSource("inputServiceSpec")
    void testPredicate(Predicate<ServiceSpec> predicate, String serviceSpecPath, boolean result) {
        try {
            ServiceSpec serviceSpec = ServiceSpecTestUtil.getServiceSpec(serviceSpecPath);
            assertEquals(predicate.test(serviceSpec), result);
        } catch (HyscaleException e) {
            fail(e);
        }
    }

    private static Stream<Arguments> inputBuildSpec() {
        return Stream.of(
                Arguments.of(ServiceSpecPredicates.haveArtifacts(), "/servicespecs/stackAsImage/only-image.hspec",
                        false),
                Arguments.of(ServiceSpecPredicates.haveConfigCommands(), "/servicespecs/stackAsImage/only-image.hspec",
                        false),
                Arguments.of(ServiceSpecPredicates.haveRunCommands(), "/servicespecs/stackAsImage/only-image.hspec",
                        false),
                Arguments.of(ServiceSpecPredicates.haveConfigScript(), "/servicespecs/stackAsImage/only-image.hspec",
                        false),
                Arguments.of(ServiceSpecPredicates.haveRunScript(), "/servicespecs/stackAsImage/only-image.hspec",
                        false));
    }

    @ParameterizedTest
    @MethodSource("inputBuildSpec")
    void testBuildSpecPredicate(Predicate<BuildSpec> predicate, String serviceSpecPath, boolean result) {
        try {
            ServiceSpec serviceSpec = ServiceSpecTestUtil.getServiceSpec(serviceSpecPath);
            BuildSpec buildSpec = serviceSpec.get(
                    HyscaleSpecFields.getPath(HyscaleSpecFields.image, HyscaleSpecFields.buildSpec), BuildSpec.class);
            assertEquals(predicate.test(buildSpec), result);
        } catch (HyscaleException e) {
            fail(e);
        }
    }
}
