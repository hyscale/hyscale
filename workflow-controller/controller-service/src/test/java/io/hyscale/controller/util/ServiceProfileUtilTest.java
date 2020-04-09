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
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.controller.exception.ControllerErrorCodes;

public class ServiceProfileUtilTest {

    private static final String SERVICE_SPEC_PATH = "/servicespecs/myservice.hspec";

    private static List<File> SERVICE_SPEC_FILES = new ArrayList<File>();

    @BeforeAll
    public static void beforeTest() throws IOException {
        SERVICE_SPEC_FILES.add(new File(ServiceProfileUtilTest.class.getResource(SERVICE_SPEC_PATH).getFile()));
    }
    /*
     * Test cases:
     * 1. No profile found
     * 2. Multiple profiles found
     * 3. Single profile
     */
    
    @Test
    public void noProfile() {
        String profileName = "test";
        try {
            ServiceProfileUtil.getProfilesFromName(SERVICE_SPEC_FILES, profileName);
            fail();
        } catch (HyscaleException e) {
            assertEquals(ControllerErrorCodes.INVALID_PROFILE_NAME, e.getHyscaleErrorCode());
        }
    }
    
    @Test
    public void multipleProfiles() {
        String profileName = "multiple";
        try {
            ServiceProfileUtil.getProfilesFromName(SERVICE_SPEC_FILES, profileName);
            fail();
        } catch (HyscaleException e) {
            assertEquals(ControllerErrorCodes.INVALID_PROFILE_NAME, e.getHyscaleErrorCode());
        }
    }
    
    @Test
    public void successProfiles() {
        String profileName = "success";
        try {
            ServiceProfileUtil.getProfilesFromName(SERVICE_SPEC_FILES, profileName);
        } catch (HyscaleException e) {
            fail(e);
        }
    }
    
    @Test
    public void dirProfiles() {
        String profileName = "dir";
        try {
            ServiceProfileUtil.getProfilesFromName(SERVICE_SPEC_FILES, profileName);
        } catch (HyscaleException e) {
            fail(e);
        }
    }
}
