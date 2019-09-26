package io.hyscale.ctl.servicespec.util;

import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;
import javax.json.JsonValue.ValueType;

import org.apache.commons.lang3.StringUtils;

import io.hyscale.ctl.servicespec.model.FieldMetaData;
import io.hyscale.ctl.servicespec.model.FieldMetaDataProvider;

/**
 * Strategic Merge In case of list patch item having same annotations Items
 * extra in patch list are added to the source list
 * 
 * @author tushart
 *
 */

public class StrategicPatch {

	public static String apply(String source, String patch, FieldMetaDataProvider fieldDataProvider) {

		// Convert String to Json Object
		JsonObject sourceJson = Json.createReader(new StringReader(source)).readObject();

		JsonObject patchJson = Json.createReader(new StringReader(patch)).readObject();

		// Convert JSON Object to String
		return mergeJsonObjects(sourceJson, patchJson, fieldDataProvider).toString();

	}

	/**
	 * Creates new source map and recursively updates it with patch values
	 * 
	 * @param source
	 * @param patch
	 * @param fieldDataProvider
	 * @return merged object
	 */
	public static JsonObject mergeJsonObjects(JsonObject source, JsonObject patch,
			FieldMetaDataProvider fieldDataProvider) {

		if (patch == null && source == null) {
			return null;
		}
		if (patch == null) {
			return source;
		}

		if (source == null) {
			return patch;
		}

		Map<String, JsonValue> effectiveMap = new HashMap<String, JsonValue>();

		source.entrySet().stream().forEach(each -> {
			effectiveMap.put(each.getKey(), each.getValue());
		});

		for (Entry<String, JsonValue> entrySet : patch.entrySet()) {
			String key = entrySet.getKey();
			JsonValue value = entrySet.getValue();

			if (value.getValueType() == ValueType.NUMBER || value.getValueType() == ValueType.STRING) {
				// if primitive or String replace value in map
				effectiveMap.put(key, value);
			} else if (value.getValueType() == ValueType.ARRAY) {
				// if list / array
				// find key for the element
				// find source object with same key

				// list object type
				JsonArray patchArray = value.asJsonArray();
				JsonValue sourceJsonVal = effectiveMap.get(key);
				Set<JsonObject> updatedJsonObj = new HashSet<JsonObject>();
				JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
				JsonArray sourceArray = null;
				if (sourceJsonVal != null) {
					sourceArray = sourceJsonVal.asJsonArray();
					// Add pre-existing elements
					sourceArray.stream().forEach(each -> {
						updatedJsonObj.add(each.asJsonObject());
					});
				}

				for (JsonValue patchValue : patchArray) {
					if (patchValue instanceof JsonObject) {
						JsonObject patchObj = patchValue.asJsonObject();
						FieldMetaData mergeKey = fieldDataProvider.getMetaData(key);
						JsonObject sourceObj = getKeyObject(sourceArray, mergeKey.getKey(),
								patchObj.get(mergeKey.getKey()));

						if (sourceObj != null) {
							// remove element and add updated one
							updatedJsonObj.remove(sourceObj);
						}
						updatedJsonObj.add(mergeJsonObjects(sourceObj, patchValue.asJsonObject(), fieldDataProvider));
					}
				}
				// update list items
				updatedJsonObj.stream().forEach(each -> {
					arrayBuilder.add(each);
				});

				effectiveMap.put(key, arrayBuilder.build());
			} else {
				// if object, Get JsonObject and merge
				effectiveMap.put(key,
						mergeJsonObjects(source.get(key).asJsonObject(), value.asJsonObject(), fieldDataProvider));
			}
		}

		JsonObjectBuilder objBuilder = Json.createObjectBuilder();
		effectiveMap.entrySet().stream().forEach(each -> {
			objBuilder.add(each.getKey(), each.getValue());
		});
		return objBuilder.build();
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

}