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

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.controller.commands.input.ProfileArg;
import io.hyscale.controller.exception.ControllerErrorCodes;
import io.hyscale.controller.model.HyscaleInputSpec;
import io.hyscale.controller.validator.impl.HprofSchemaValidator;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class ProfileSpecProcessorTest {

    @Autowired
    private ProfileSpecProcessor profileSpecProcessor;

    @MockBean
    private HprofSchemaValidator hprofSchemaValidator;

    private static final String SERVICE_SPEC_1 = "/servicespecs/myservice.hspec";
    private static final String SERVICE_SPEC_2 = "/servicespecs/myprofileservice.hspec";

    private static final String PROFILE_1 = "/servicespecs/multiple-myservice.hprof";
    private static final String PROFILE_2 = "/servicespecs/profiles/multiple-myservice.hprof";
    private static final String PROFILE_3 = "/servicespecs/profiles/multiple-test.hprof";
    private static final String PROFILE_4 = "/servicespecs/test-myprofileservice.hprof";
    private static final String PROFILE_5 = "/servicespecs/profiles/invalid.hprof";
    private List<File> serviceSpecList = new ArrayList<>();
    private List<File> profileList = new ArrayList<>();

     /*

   T1 & T2:  with strict with missing profile &   with strict with multiple profile
   T3 & T4:  with no strict with missing profile - success case & with no strict multiple profile
   T5:  with no strict invalid hprof - ex
   T6:  with no strict invalid file path - ex
 */


    @Test
    public void testWithMissingProfiles() {
        serviceSpecList.clear();
        serviceSpecList.add(new File(ProfileLocatorTest.class.getResource(SERVICE_SPEC_1).getFile()));
        serviceSpecList.add(new File(ProfileLocatorTest.class.getResource(SERVICE_SPEC_2).getFile()));
        profileList.clear();
        File testFile = new File(ProfileLocatorTest.class.getResource(PROFILE_1).getFile());

        profileList.add(testFile);
        try {
            Mockito.when(hprofSchemaValidator.validate(testFile)).thenReturn(true);
            File testFile4 = new File(ProfileLocatorTest.class.getResource(PROFILE_4).getFile());
            Mockito.when(hprofSchemaValidator.validate(testFile4)).thenReturn(true);
            HyscaleInputSpec hyscaleInputSpec = profileSpecProcessor.process(withName("test"), serviceSpecList);
            assertFalse(hyscaleInputSpec != null);
        } catch (HyscaleException e) {
            assertTrue(ControllerErrorCodes.PROFILE_NOT_PROVIDED_FOR_SERVICES.equals(e.getHyscaleErrorCode()));
        } catch (IllegalAccessException | NoSuchFieldException e) {
            fail();
        }
        try {
            HyscaleInputSpec hyscaleInputSpec = profileSpecProcessor.process(withProfileFiles(profileList), serviceSpecList);
            assertTrue(hyscaleInputSpec != null);
        } catch (HyscaleException e) {
            assertFalse(true);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            fail();
        }
    }

    @Test
    public void testWithMultipleProfiles() {
        serviceSpecList.clear();
        serviceSpecList.add(new File(ProfileLocatorTest.class.getResource(SERVICE_SPEC_1).getFile()));
        profileList.clear();
        File testProfile1 = new File(ProfileLocatorTest.class.getResource(PROFILE_1).getFile());
        File testProfile2 = new File(ProfileLocatorTest.class.getResource(PROFILE_2).getFile());
        profileList.add(testProfile1);
        profileList.add(testProfile2);
        try {
            Mockito.when(hprofSchemaValidator.validate(testProfile1)).thenReturn(true);
            Mockito.when(hprofSchemaValidator.validate(testProfile2)).thenReturn(true);
            HyscaleInputSpec hyscaleInputSpec = profileSpecProcessor.process(withProfileFiles(profileList), serviceSpecList);
            assertFalse(hyscaleInputSpec != null);
        } catch (HyscaleException e) {
            assertTrue(ControllerErrorCodes.UNIQUE_PROFILE_REQUIRED.equals(e.getHyscaleErrorCode()));
        } catch (IllegalAccessException | NoSuchFieldException e) {
            fail();
        }

        try {
            HyscaleInputSpec hyscaleInputSpec = profileSpecProcessor.process(withName("multiple"), serviceSpecList);
            assertFalse(hyscaleInputSpec != null);
        } catch (HyscaleException e) {
            assertTrue(ControllerErrorCodes.UNIQUE_PROFILE_REQUIRED.equals(e.getHyscaleErrorCode()));
        } catch (IllegalAccessException | NoSuchFieldException e) {
            fail();
        }
    }

    @Test
    public void testWithInvalidProfile() {
        serviceSpecList.clear();
        serviceSpecList.add(new File(ProfileLocatorTest.class.getResource(SERVICE_SPEC_1).getFile()));
        profileList.clear();
        File testFile = new File(ProfileLocatorTest.class.getResource(PROFILE_5).getFile());
        profileList.add(testFile);
        try {
            Mockito.when(hprofSchemaValidator.validateData(testFile)).thenReturn(true);
            HyscaleInputSpec hyscaleInputSpec = profileSpecProcessor.process(withProfileFiles(profileList), serviceSpecList);
            assertFalse(hyscaleInputSpec != null);
        } catch (HyscaleException e) {
            assertTrue(ControllerErrorCodes.INPUT_VALIDATION_FAILED.equals(e.getHyscaleErrorCode()));
        } catch (IllegalAccessException | NoSuchFieldException e) {
            fail();
        }
    }

    private ProfileArg withName(String profileName) throws NoSuchFieldException, IllegalAccessException {
        ProfileArg profileArg = new ProfileArg();
        Field field = profileArg.getClass().getDeclaredField("profileName");
        field.setAccessible(true);
        field.set(profileArg, profileName);
        return profileArg;
    }

    private ProfileArg withProfileFiles(List<File> profileFiles) throws NoSuchFieldException, IllegalAccessException {
        ProfileArg profileArg = new ProfileArg();
        Field field = profileArg.getClass().getDeclaredField("profiles");
        field.setAccessible(true);
        field.set(profileArg, profileFiles);
        return profileArg;
    }


}

