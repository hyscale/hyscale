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
package io.hyscale.commons.utils;

import java.util.Map;

import io.hyscale.commons.models.ResourceLabelKey;

public class ResourceLabelUtil {

	public static String getServiceName(Map<String, String> labels) {
		if (labels == null) {
			return null;
		}
		return labels.get(ResourceLabelKey.SERVICE_NAME.getLabel());
	}

	public static String getAppName(Map<String, String> labels) {
		if (labels == null) {
			return null;
		}
		return labels.get(ResourceLabelKey.APP_NAME.getLabel());
	}
	
	public static String getEnvName(Map<String, String> labels) {
		if (labels == null) {
			return null;
		}
		return labels.get(ResourceLabelKey.ENV_NAME.getLabel());
	}
	
	public static String getVolumeName(Map<String, String> labels) {
        if (labels == null) {
            return null;
        }
        return labels.get(ResourceLabelKey.VOLUME_NAME.getLabel());
    }

}
