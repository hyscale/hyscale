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
package io.hyscale.commons.constants;

public class ToolConstants {

    public static final String CHARACTER_ENCODING = "UTF-8";

    public static final String AT_SIGN = "@";

    public static final String COMMAND_SEPARATOR = "&&";

    public static final String COLON = ":";

    public static final String SEMI_COLON = ";";

    public static final String DOUBLE_COLON = "::";

    public static final String COMMA = ",";

    public static final String HEADER_CONCATENATOR = "|";

    public static final String DOT = ".";

    public static final String DASH = "-";

    public static final String HYSCALECTL_LOGS_DIR_PROPERTY = "HYSCALECTL_LOGS_DIR";

    public static final String EMPTY_STRING = "";

	public static final String EQUALS_SYMBOL="=";

    public static final String QUOTES = "\"";

    public static final String SPACE = " ";

    public static final String NEW_LINE = "\n";

    public static final String LINUX_FILE_SEPARATOR = "/";

    public static final String HYSCALE = "HyScale";

    public static final String VERSION_KEY = "Version";

    public static final String BUILD = "build";

    public static final String BUILDDATE_KEY = "BuildDate : ";

    public static final String RELEASE_NAME_KEY = "ReleaseName: ";

    public static final String HYSCALE_HOST_FS_PROPERTY = "HYSCALE_HOST_FS";

    public static final String HYSCALE_BUILD_TIME = "hyscale.build.time";

    public static final String HYSCALE_RELEASE_NAME = "hyscale.release.name";

    public static final String LINE_SEPARATOR = System.getProperty("line.separator");

    public static final String FILE_SEPARATOR = System.getProperty("file.separator");

    public static final Integer HYSCALE_ERROR_CODE = 11;

    public static final Integer SCHEMA_VALIDATION_FAILURE_ERROR_CODE = 22;

    public static final Integer INVALID_INPUT_ERROR_CODE = 2;

    public static final Integer HYSCALE_SUCCESS_CODE = 0;

    public static final Integer HYSCALE_FAILURE_CODE = 1;

    public static final String HSPEC_VERSION = "hspec.version";

    public static final String NASHORNS_ARGS = "nashorn.args";

    public static final String NASHORNS_DEPRECATION_WARNING_FLAG = "--no-deprecation-warning";

    public static final String JDK_TLS_CLIENT_PROTOCOLS_ARGS = "jdk.tls.client.protocols";

    public static final String JDK_TLS_CLIENT_VERSION = "TLSv1.2";

    public static final String HSPEC_EXTENSION = ".hspec";

    public static final String HPROF_EXTENSION = ".hprof";

    public static final String PROFILES_DIR_NAME = "profiles";

    public static final String WORKFLOW_LOGGER_DISABLED = "WORKFLOW_LOGGER_DISABLED";

    public static final String PORTS_PROTOCOL_SEPARATOR = "/";

    private ToolConstants() {}

}