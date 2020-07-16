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
package io.hyscale.generator.services.utils;

import java.util.HashMap;
import java.util.Map;

import io.hyscale.commons.models.AnnotationKey;

public class AnnotationBuilder {
    
    private AnnotationBuilder() {}

	// TODO CHECK THIS LIMIT
	private static final int MAX_ANNOTATION_KEY_SIZE = 1024;

	public static Map<AnnotationKey, String> volumeClaimTemplate(String storageClass) {
		Map<AnnotationKey, String> annotations = new HashMap<>();
		annotations.put(AnnotationKey.PVC_TEMPLATE_STORAGE_CLASS, normalize(storageClass));
		return annotations;
	}

	public static Map<AnnotationKey, String> serviceSpec(String serviceSpec) {
		Map<AnnotationKey, String> annotations = new HashMap<>();
		annotations.put(AnnotationKey.HYSCALE_SERVICE_SPEC,serviceSpec);
		return annotations;
	}

	public static String normalize(String name) {
		String normalized = name.replaceAll("[\\.]+", "-").replaceAll("[ ]+", "-").replaceAll("[^a-zA-Z0-9-_]", "");
		return normalized.substring(0, Integer.min(MAX_ANNOTATION_KEY_SIZE - 1, name.length()));
	}
}
