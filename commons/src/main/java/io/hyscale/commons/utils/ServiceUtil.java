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

public class ServiceUtil {

	public static String getRandomKey(String prefix) {
		String id = IDGenerator.generate(8);
		return prefix + "-" + id;
	}

	public static String getRandomKey(String prefix, int length) {
		String id = IDGenerator.generate(length);
		return prefix + "-" + id;
	}

	public static String getRandomKeyWithNoCaps(String prefix, int length) {
		String id = IDGenerator.generateWithNoCaps(length);
		return prefix + "-" + id;
	}
}
