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
package io.hyscale.servicespec.model;

import java.lang.reflect.Field;

import io.hyscale.servicespec.annotations.StrategicMergePatch;

/**
 * Get field metadata details based on annotation
 * @author tushart
 *
 */
public class AnnotationFieldDataProvider implements FieldMetaDataProvider {

	private Class klazz;

	public AnnotationFieldDataProvider(Class klazz) {
		super();
		this.klazz = klazz;
	}

	@Override
	public FieldMetaData getMetaData(String field) {
		// For given field, return merge key information
		FieldMetaData fieldMetaData = new FieldMetaData();
		try {
			Field keyObjectField = klazz.getDeclaredField(field);
			StrategicMergePatch patchMergeKey = keyObjectField.getAnnotation(StrategicMergePatch.class);
			fieldMetaData.setKey(patchMergeKey.key());
		} catch (NoSuchFieldException | SecurityException e) {
			return null;
		}
		return fieldMetaData;
	}

}
