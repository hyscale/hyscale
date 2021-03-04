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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Utility class to provide different types of GsonBuilder to build json objects.
 */
public class GsonProviderUtil {

    private static final Gson PRETTY_JSON_BUILDER = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private GsonProviderUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * used for building json objects in pretty format.
     * @return PRETTY_JSON_BUILDER.
     */
    public static Gson getPrettyGsonBuilder() {
        return PRETTY_JSON_BUILDER;
    }

}
