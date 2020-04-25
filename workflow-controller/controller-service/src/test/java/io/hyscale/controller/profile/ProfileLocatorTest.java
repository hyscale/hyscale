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
package io.hyscale.controller.profile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.controller.commands.args.ProfileLocator;
import io.hyscale.controller.validator.impl.HprofSchemaValidator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ProfileLocatorTest {

    @Autowired
    private ProfileLocator profileLocator;

    private static final String SERVICE_SPEC_PATH = "/servicespecs/myservice.hspec";

    private static final String PROFILE_1 = "/servicespecs/multiple-myservice.hprof";
    private static final String PROFILE_2 = "/servicespecs/profiles/multiple-myservice.hprof";
    private static final String PROFILE_3 = "/servicespecs/profiles/multiple-test.hprof";

    private static final String TEST_PROFILE_NAME = "multiple";

    private static List<File> SERVICE_SPEC_FILES = new ArrayList<File>();

    private static List<File> PROFILE_FILES = new ArrayList<File>();


    @BeforeAll
    public static void beforeTest() throws IOException {
        SERVICE_SPEC_FILES.add(new File(ProfileLocatorTest.class.getResource(SERVICE_SPEC_PATH).getFile()));
        PROFILE_FILES.add(new File(ProfileLocatorTest.class.getResource(PROFILE_1).getFile()));
        PROFILE_FILES.add(new File(ProfileLocatorTest.class.getResource(PROFILE_2).getFile()));
        //PROFILE_FILES.add(new File(ProfileLocatorTest.class.getResource(PROFILE_3).getFile()));
    }

    /**
     * Test Cases:
     * 1. getAllProfiles
     * 1. No profiles found
     * 2. profiles irrespective of services should be returned
     * 2. validateAndFilter
     * 1. Positive case : Profile for each service
     * 2. Negative cases:
     * 1. Multiple profiles
     * 2. No profiles
     */

    public static Stream<Arguments> profileListInput() {
        return Stream.of(Arguments.of(null, null), Arguments.of("doesnotexist", new ArrayList<>()),
                Arguments.of(TEST_PROFILE_NAME, PROFILE_FILES));
    }

    @ParameterizedTest
    @MethodSource("profileListInput")
    public void profileListTest(String profileName, List<File> expectedProfiles) {
        List<File> profiles = null;
        try {
            Set<File> profileFiles = profileLocator.getAllProfiles(SERVICE_SPEC_FILES, profileName);
            if (profileFiles != null) {
                profiles = new ArrayList<>();
                profiles.addAll(profileFiles);
            }
        } catch (HyscaleException e) {
            fail();
        }

        if (expectedProfiles == null) {
            assertNull(profiles);
            return;
        }
        Collections.sort(expectedProfiles);
        Collections.sort(profiles);
        assertTrue(expectedProfiles.equals(profiles));
    }
}
