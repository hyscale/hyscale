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
package io.hyscale.controller.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.controller.exception.ControllerErrorCodes;

public class ServiceProfileUtilTest {

    private static final String SERVICE_SPEC_PATH = "/servicespecs/myservice.hspec";
    private static final String SERVICE_SPEC_PATH_FILTER = "/servicespecs/myprofileservice.hspec";

    private static final String PROFILE_1 = "/servicespecs/multiple-myservice.hprof";
    private static final String PROFILE_2 = "/servicespecs/profiles/multiple-myservice.hprof";
    private static final String PROFILE_3 = "/servicespecs/profiles/multiple-test.hprof";
    private static final String PROFILE_4 = "/servicespecs/test-myprofileservice.hprof";

    private static final String MULTIPLE_PROFILE_NAME = "multiple";

    private static List<File> SERVICE_SPEC_FILES = new ArrayList<File>();
    private static List<File> FILTER_SERVICE_SPEC_FILES = new ArrayList<File>();

    private static List<File> PROFILE_FILES = new ArrayList<File>();

    private static List<File> FILTER_PROFILE_FILES = new ArrayList<File>();

    @BeforeAll
    public static void beforeTest() throws IOException {
        SERVICE_SPEC_FILES.add(new File(ServiceProfileUtilTest.class.getResource(SERVICE_SPEC_PATH).getFile()));
        FILTER_SERVICE_SPEC_FILES
                .add(new File(ServiceProfileUtilTest.class.getResource(SERVICE_SPEC_PATH_FILTER).getFile()));
        PROFILE_FILES.add(new File(ServiceProfileUtilTest.class.getResource(PROFILE_1).getFile()));
        PROFILE_FILES.add(new File(ServiceProfileUtilTest.class.getResource(PROFILE_2).getFile()));
        PROFILE_FILES.add(new File(ServiceProfileUtilTest.class.getResource(PROFILE_3).getFile()));
        FILTER_PROFILE_FILES.addAll(PROFILE_FILES);
        FILTER_PROFILE_FILES.add(new File(ServiceProfileUtilTest.class.getResource(PROFILE_4).getFile()));
    }

    /**
     * Test Cases:
     * 1. getAllProfiles
     *      1. No profiles found
     *      2. profiles irrespective of services should be returned
     * 2. validateAndFilter
     *      1. Positive case : Profile for each service
     *      2. Negative cases:
     *          1. Multiple profiles
     *          2. No profiles
     */

    public static Stream<Arguments> profileListInput() {
        return Stream.of(Arguments.of(null, null), Arguments.of("doesnotexist", new ArrayList<File>()),
                Arguments.of(MULTIPLE_PROFILE_NAME, PROFILE_FILES));
    }

    @ParameterizedTest
    @MethodSource("profileListInput")
    public void profileListTest(String profileName, List<File> expectedProfiles) {
        List<File> profiles = null;
        profiles = ServiceProfileUtil.getAllProfiles(SERVICE_SPEC_FILES, profileName);

        if (expectedProfiles == null) {
            assertNull(profiles);
            return;
        }
        Collections.sort(expectedProfiles);
        Collections.sort(profiles);
        assertTrue(expectedProfiles.equals(profiles));
    }

    public static Stream<Arguments> profilefilterInput() {
        List<File> expectedProfiles = new ArrayList<File>();
        expectedProfiles.add(new File(ServiceProfileUtilTest.class.getResource(PROFILE_4).getFile()));
        return Stream.of(Arguments.of(null, null, null, null), Arguments.of(null, PROFILE_FILES, null, null),
                Arguments.of(SERVICE_SPEC_FILES, null, null,
                        new HyscaleException(ControllerErrorCodes.ERROR_WHILE_PROCESSING_PROFILE)),
                Arguments.of(SERVICE_SPEC_FILES, PROFILE_FILES, null,
                        new HyscaleException(ControllerErrorCodes.ERROR_WHILE_PROCESSING_PROFILE)),
                Arguments.of(FILTER_SERVICE_SPEC_FILES, FILTER_PROFILE_FILES, expectedProfiles, null));
    }

    @ParameterizedTest
    @MethodSource("profilefilterInput")
    public void profileFilterTest(List<File> serviceSpec, List<File> profiles, List<File> expectedProfiles,
            HyscaleException expectedException) {
        List<File> filteredProfiles = null;
        try {
            filteredProfiles = ServiceProfileUtil.validateAndFilter(serviceSpec, profiles, "test");
        } catch (HyscaleException e) {
            if (expectedException == null) {
                fail(e);
            }
            assertEquals(expectedException.getHyscaleErrorCode(), e.getHyscaleErrorCode());
            return;
        }
        if (expectedException != null) {
            fail();
        }

        if (expectedProfiles == null) {
            assertNull(filteredProfiles);
            return;
        }

    }
}
