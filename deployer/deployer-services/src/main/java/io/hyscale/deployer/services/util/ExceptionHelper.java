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
package io.hyscale.deployer.services.util;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.deployer.services.exception.DeployerErrorCodes;
import io.hyscale.deployer.core.model.ResourceOperation;
import io.kubernetes.client.ApiException;

/**
 * Helper class for {@link HyscaleException}
 *
 */
public class ExceptionHelper {

    private static final String FAILED_WITH_MESSAGE = "failed with status: ";
    private static final String CAUSE_MESSAGE = ", cause: ";
    private static final String UNAUTHORIZED_MESSAGE = "Unauthorized access";

    /**
     * @param resourceKind
     * @param ApiException
     * @param operation
     * @return Array of String - resource kind, fail message, ApiException code, cause message
     */
    public static String[] getExceptionArgs(String resourceKind, ApiException ex, ResourceOperation operation) {
        String[] args = new String[]{resourceKind, FAILED_WITH_MESSAGE, getCode(ex),
                CAUSE_MESSAGE, getResponseMessage(ex)};
        return args;
    }

    public static String[] getResourceDetails(String resourceKind, String name, ResourceOperation operation) {
        String[] args = new String[]{resourceKind, name, operation.getOperation()};

        return args;
    }

    /**
     * Get operation exception could be if the resource is not found or some other exception
     * @param resourceKind
     * @param e - K8s cluster exception
     * @param operation {@link ResourceOperation}
     * @return HyscaleException
     */
    public static HyscaleException buildGetException(String resourceKind, ApiException e, ResourceOperation operation) {
        HyscaleException ex = null;
        if (e.getCode() != 404) {
            ex = new HyscaleException(e, DeployerErrorCodes.FAILED_TO_GET_RESOURCE,
                    getExceptionArgs(resourceKind, e, operation));
        } else {
            ex = new HyscaleException(e, DeployerErrorCodes.RESOURCE_NOT_FOUND,
                    getExceptionArgs(resourceKind, e, operation));
        }
        return ex;
    }

    private static String getCode(ApiException e) {
        return Integer.toString(e.getCode());
    }

    private static String getResponseMessage(ApiException e) {
        int code = e.getCode();
        if (code == 401) {
            return UNAUTHORIZED_MESSAGE;
        }
        return e.getResponseBody();
    }
}
