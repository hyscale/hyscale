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

import org.apache.commons.lang3.StringUtils;

import io.hyscale.commons.constants.ToolConstants;

public class OperatingSystemHelper {
	
	private static final String WINDOWS_FS_MATCHER = "\\\\";

	/**
     * Replace windows file separator(\) with unix file separator(/)
     * @param fileSeperator
     * @return unix file separator based file path
     */
    public static String modifyWindowsFileSeparator(String filePath) {
    	if (StringUtils.isBlank(filePath)) {
			return filePath;
		}
		return filePath.replaceAll(WINDOWS_FS_MATCHER, ToolConstants.LINUX_FILE_SEPARATOR);
    }
}
