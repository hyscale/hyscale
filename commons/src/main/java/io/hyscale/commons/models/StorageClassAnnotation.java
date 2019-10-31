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
package io.hyscale.commons.models;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class StorageClassAnnotation {

	private static List<String> defaultAnnotations = Arrays.asList(
			AnnotationKey.DEFAULT_STORAGE_CLASS.getAnnotation(),
			AnnotationKey.DEFAULT_BETA_STORAGE_CLASS.getAnnotation());

	public static String getDefaultAnnotaionValue(Map<String, String> annotations) {
		if (annotations == null) {
			return null;
		}
		for (Entry<String, String> entry : annotations.entrySet()) {
			if (defaultAnnotations.contains(entry.getKey())) {
				return entry.getValue();
			}
		}
		return null;
	}
}
