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

import java.io.StringReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.json.*;
import javax.json.JsonValue.ValueType;
import javax.json.stream.JsonParsingException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.hyscale.commons.exception.CommonErrorCode;
import io.hyscale.commons.exception.HyscaleException;

/**
 * Strategic Patch to update source with patch values
 * Source and Patch should be in JSON format 
 * 
 * @author tushar
 *
 */

public class StrategicPatch {

    private static final Logger logger = LoggerFactory.getLogger(StrategicPatch.class);

    private static final List<ValueType> replacementValueTypes = Arrays.asList(ValueType.NUMBER, ValueType.STRING,
            ValueType.TRUE, ValueType.FALSE);

    private StrategicPatch() {}

    /**
     * Convert String input for source and patch to Json 
     * and calls {@link #mergeJsonObjects(JsonObject, JsonObject, FieldMetaDataProvider)}
     * Convert result of {@link #mergeJsonObjects(JsonObject, JsonObject, FieldMetaDataProvider)} to String
     * 
     * @param source
     * @param patch
     * @param fieldDataProvider
     * @return String - Json representation after merging source and patch
     * @throws HyscaleException
     */
    public static String apply(String source, String patch, FieldMetaDataProvider fieldDataProvider)
            throws HyscaleException {

        if (StringUtils.isBlank(source)) {
            return patch;
        }

        if (StringUtils.isBlank(patch)) {
            return source;
        }
        JsonObject mergedJsonObject = null;
        try (JsonReader sourceJsonReader = Json.createReader(new StringReader(source)); JsonReader patchJsonReader = Json.createReader(new StringReader(patch));) {
            // Convert String to Json Object
            JsonObject sourceJson = sourceJsonReader.readObject();
            JsonObject patchJson = patchJsonReader.readObject();

            // Convert JSON Object to String
            mergedJsonObject = mergeJsonObjects(sourceJson, patchJson, fieldDataProvider);
            if (mergedJsonObject != null) {
                return mergedJsonObject.toString();
            }

        } catch (JsonParsingException e) {
            HyscaleException ex = new HyscaleException(e, CommonErrorCode.INVALID_JSON_FORMAT);
            logger.error("Invalid Json format for source or patch", ex);
            throw ex;
        }

        return null;

    }

    /**
     * Creates new source map and recursively updates it with patch values
     * Returns the map as JsonObject
     * 
     * <p>
     * Implementation:
     * Updates patch values into source, for each value in patch
     * 1. If null or empty, ignore (doesn't delete value in source)
     * 2. If it is replacement value type, replace value in source
     * 3. If arrays type
     *  3.a. If replacement value type elements, merge the values without duplication
     *  3.b. If merge strategy is replace, replace all items in the list
     *  3.c. Else merge based on key provided, throws exception if key is not available
     * 4. For Others(JSONObject):
     *  4.a. If value in source is absent or replacement type, replace with patch value(Model change)
     *  4.b. Else Recursively call with JsonObject of source and patch
     * </p>
     * 
     * @param source
     * @param patch
     * @param fieldDataProvider - provides keys while merging array entities
     * @return merged JsonObject
     * @throws HyscaleException
     */
    public static JsonObject mergeJsonObjects(JsonObject source, JsonObject patch,
            FieldMetaDataProvider fieldDataProvider) throws HyscaleException {

        if (patch == null && source == null) {
            return null;
        }
        if (patch == null) {
            return source;
        }

        if (source == null) {
            return patch;
        }

        Map<String, JsonValue> effectiveMap = new HashMap<>();

        source.entrySet().stream().forEach(each -> effectiveMap.put(each.getKey(), each.getValue()));

        for (Entry<String, JsonValue> entrySet : patch.entrySet()) {
            String key = entrySet.getKey();
            JsonValue value = entrySet.getValue();
            if (value.getValueType() == ValueType.NULL) {
                // Empty/Null value field are ignored
                continue;
            }

            // if absent in source or different type than patch, put patch value
            JsonValue sourceJsonValue = source.get(key);
            if (sourceJsonValue == null || value.getValueType() != sourceJsonValue.getValueType()) {
                effectiveMap.put(key, value);
                continue;
            }

            if (replacementValueTypes.contains(value.getValueType())) {
                // if primitive, String or Boolean replace value in map
                effectiveMap.put(key, value);
            } else if (value.getValueType() == ValueType.ARRAY) {

                JsonArray patchArray = value.asJsonArray();
                JsonValue sourceJsonVal = effectiveMap.get(key);
                JsonArray effectiveArray = handleArrayMerge(sourceJsonVal, patchArray, key, fieldDataProvider);
                effectiveMap.put(key, effectiveArray);
            } else {
                effectiveMap.put(key,
                        mergeJsonObjects(sourceJsonValue.asJsonObject(), value.asJsonObject(), fieldDataProvider));
            }
        }
        JsonObjectBuilder objBuilder = Json.createObjectBuilder();
        effectiveMap.entrySet().stream().forEach(each -> objBuilder.add(each.getKey(), each.getValue()));
        return objBuilder.build();
    }
    
    private static JsonArray handleArrayMerge(JsonValue sourceJsonVal, JsonArray patchArray, String key,
            FieldMetaDataProvider fieldDataProvider) throws HyscaleException {
        Set<JsonValue> updatedJsonObj = new HashSet<>();
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        PatchStrategy strategy = getPatchStrategy(fieldDataProvider, key);
        
        if (strategy == PatchStrategy.REPLACE) {
            patchArray.stream().forEach(arrayBuilder::add);
            return arrayBuilder.build();
        }
        JsonArray sourceArray = null;
        if (sourceJsonVal != null) {
            sourceArray = sourceJsonVal.asJsonArray();
            // Add pre-existing elements
            sourceArray.stream().forEach(updatedJsonObj::add);
        }

        for (JsonValue patchValue : patchArray) {
            if (patchValue instanceof JsonObject) {
                String objectKey = getObjectKey(fieldDataProvider, key);
                JsonObject patchObj = patchValue.asJsonObject();
                JsonObject sourceObj = getKeyObject(sourceArray, objectKey,
                        patchObj.get(objectKey));

                if (sourceObj != null) {
                    // remove element and add updated one
                    updatedJsonObj.remove(sourceObj);
                }
                updatedJsonObj.add(mergeJsonObjects(sourceObj, patchValue.asJsonObject(), fieldDataProvider));
            } else {
                // replacement value type
                updatedJsonObj.add(patchValue);
            }
        }
        // update list items
        updatedJsonObj.stream().forEach(arrayBuilder::add);
        return arrayBuilder.build();
    }
    
    private static String getObjectKey(FieldMetaDataProvider fieldDataProvider, String key) throws HyscaleException {
        if (fieldDataProvider == null) {
            throw getException(key);
        }
        FieldMetaData fieldMetaData = fieldDataProvider.getMetaData(key);
        if (fieldMetaData == null
                || (StringUtils.isBlank(fieldMetaData.getKey()))) {
            throw getException(key);
        }
        return fieldMetaData.getKey();
    }

    private static PatchStrategy getPatchStrategy(FieldMetaDataProvider fieldDataProvider, String key) {
        if (fieldDataProvider == null) {
            return PatchStrategy.getDefault();
        }
        FieldMetaData fieldMetadata = fieldDataProvider.getMetaData(key);
        if (fieldMetadata != null && fieldMetadata.getPatchStrategy() != null) {
            return fieldMetadata.getPatchStrategy();
        }
        return PatchStrategy.getDefault();
    }
    
    private static JsonObject getKeyObject(JsonArray objList, String key, JsonValue value) {
        if (objList == null || StringUtils.isBlank(key)) {
            return null;
        }
        for (JsonValue object : objList) {
            JsonObject asJsonObject = object.asJsonObject();
            JsonValue sourceVal = asJsonObject.get(key);
            if (value.equals(sourceVal)) {
                return asJsonObject;
            }
        }
        return null;
    }
    
    private static HyscaleException getException(String key) {
        HyscaleException ex = new HyscaleException(CommonErrorCode.STRATEGIC_MERGE_KEY_NOT_FOUND,
                key);
        logger.error("Error while performing strategic patch", ex);
        return ex;
    }

}