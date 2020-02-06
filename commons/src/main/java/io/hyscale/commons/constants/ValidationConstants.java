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

public class ValidationConstants {

    public static final String APP_NAME_REGEX = "([a-z0-9-]){2,30}";

    public static final String SERVICE_NAME_REGEX = "[a-z]([-a-z0-9]*[a-z0-9])?";
    
    public static final String SERVICE_SPEC_NAME_REGEX = "(.*)" + SERVICE_NAME_REGEX + "(\\.hspec\\.yaml)$";
    
    public static final String PROFILE_NAME_REGEX = "([-a-zA-Z0-9]){2,30}";
    
    public static final String PROFILE_FILENAME_REGEX = "(.*)" + PROFILE_NAME_REGEX + ToolConstants.DASH + SERVICE_NAME_REGEX + "(\\.hprof\\.yaml)$";

    public static final String NAMESPACE_REGEX = "([a-z0-9-]){2,30}";
    
    public static final long MIN_LOG_LINES = 1;
    
    public static final long MAX_LOG_LINES = 501;

    public static final String INVALID_APP_NAME_MSG = "Application name \"{}\" is invalid. It must consist of lower case alphanumeric characters or '-', "
            + "its length should be between 2 and 30."
            + "(regex used for validation is '"
            + APP_NAME_REGEX + "')";

    public static final String INVALID_SERVICE_NAME_MSG = "Service name \"{}\" is invalid. A service name must consist of lower case alphanumeric characters or '-',"
            + " start with an alphabetic character, and end with an alphanumeric character (e.g. 'my-name',  or 'abc-123', "
            + "regex used for validation is '"
            + SERVICE_NAME_REGEX + "')";

    public static final String INVALID_NAMESPACE_MSG = "Namespace \"{}\" is invalid. It must consist of lower case alphanumeric characters or '-', "
            + "its length should be between 2 and 30."
            + "(regex used for validation is '"
            + NAMESPACE_REGEX + "')";
    
    public static final String MAX_LOG_LINES_ERROR_MSG = "Logs lines must be less than " + MAX_LOG_LINES;
    
    public static final String MIN_LOG_LINES_ERROR_MSG = "Logs lines must be more than " + MIN_LOG_LINES;
    
}
