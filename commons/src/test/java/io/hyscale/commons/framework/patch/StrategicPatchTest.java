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
package io.hyscale.commons.framework.patch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.StringReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import javax.json.Json;
import javax.json.JsonObject;

import org.json.JSONException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.skyscreamer.jsonassert.JSONAssert;

import io.hyscale.commons.exception.CommonErrorCode;
import io.hyscale.commons.exception.HyscaleError;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.io.HyscaleFilesUtil;

public class StrategicPatchTest {

    private static String sourceData;
    private static String patchData;
    private static String mergedData;

    @BeforeAll
    public static void init() throws HyscaleException {
        sourceData = getData("/patch/source.json");
        patchData = getData("/patch/patch.json");
        mergedData = getData("/patch/merged.json");
    }

    public static Stream<Arguments> getApplyInput() {
        TestFieldDataProvider fieldDataProvider = new TestFieldDataProvider();
        return Stream.of(Arguments.of(sourceData, patchData, fieldDataProvider, mergedData),
                Arguments.of(null, null, null, null),
                Arguments.of(null, patchData, null, patchData),
                Arguments.of(sourceData, null, null, sourceData));
    }

    @ParameterizedTest
    @MethodSource(value = "getApplyInput")
    public void testApply(String source, String patch, TestFieldDataProvider fieldDataMap, String expectedMergedData) {
        try {
            String actualMergedData = StrategicPatch.apply(source, patch, fieldDataMap);
            if (expectedMergedData == null) {
                assertNull(actualMergedData);
            } else {
                JSONAssert.assertEquals(expectedMergedData, actualMergedData, false);
            }
        } catch (JSONException | HyscaleException e) {
            fail();
        }
    }

    public static Stream<Arguments> getApplyExceptionInput() {
        TestFieldDataProvider fieldDataProvider = new TestFieldDataProvider();
        return Stream.of(Arguments.of("{test:abc}", "{test:def test1:abc}", null, CommonErrorCode.INVALID_JSON_FORMAT),
                Arguments.of("{test:def test1:abc}", "{test:abc}", null, CommonErrorCode.INVALID_JSON_FORMAT),
                Arguments.of(sourceData, patchData, null, CommonErrorCode.STRATEGIC_MERGE_KEY_NOT_FOUND),
                Arguments.of("{\"test\":[{\"test\":\"abc\"}]}", "{\"test\":[{\"test\":\"abc\"}]}", fieldDataProvider, CommonErrorCode.STRATEGIC_MERGE_KEY_NOT_FOUND));
    }

    @ParameterizedTest
    @MethodSource(value = "getApplyExceptionInput")
    public void testException(String source, String patch, TestFieldDataProvider fieldDataProvider,
            HyscaleError errorCode) {
        try {
            StrategicPatch.apply(source, patch, fieldDataProvider);
            fail();
        } catch (HyscaleException e) {
            assertEquals(errorCode, e.getHyscaleError());
        }

    }

    public static Stream<Arguments> getMergeNullInput() {
        JsonObject input = Json.createReader(new StringReader("{\"test\":\"abc\"}")).readObject();
        return Stream.of(Arguments.of(null, null, null),
                Arguments.of(input, null, input),
                Arguments.of(null, input, input));
    }
    
    @ParameterizedTest
    @MethodSource(value = "getMergeNullInput")
    public void nullMergeChecks(JsonObject source, JsonObject patch, JsonObject expected) {
        try {
            JsonObject merged = StrategicPatch.mergeJsonObjects(source, patch, null);
            if (expected == null) {
                assertNull(merged);
            } else {
                assertEquals(expected,merged);
            }
        } catch (HyscaleException e) {
            fail();
        }
    }

    private static String getData(String path) throws HyscaleException {
        URL urlPath = StrategicPatchTest.class.getResource(path);
        return HyscaleFilesUtil.readFileData(new File(urlPath.getFile()));
    }
    
    private static class TestFieldDataProvider implements FieldMetaDataProvider{
        
        private static Map<String, String> fieldMap = new HashMap<String, String>();
        private static Map<String, String> strategyMap = new HashMap<>();
        static {
            fieldMap.put("patchTestModelList", "key");
            strategyMap.put("testListReplace", "replace");
            strategyMap.put("testListReplaceEmpty", "replace");
            strategyMap.put("testListReplaceObject", "replace");
        }
        @Override
        public FieldMetaData getMetaData(String field) {
            FieldMetaData fieldMetaData = new FieldMetaData();
            fieldMetaData.setKey(fieldMap.get(field));
            fieldMetaData.setPatchStrategy(PatchStrategy.fromString(strategyMap.get(field)));
            return fieldMetaData;
        }
        
    }
}
