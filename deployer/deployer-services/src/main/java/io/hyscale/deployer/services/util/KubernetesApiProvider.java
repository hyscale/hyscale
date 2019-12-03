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

import io.kubernetes.client.ApiClient;
import io.kubernetes.client.apis.AppsV1beta2Api;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.apis.StorageV1Api;

/**
 * Helper Class to provide different kubernetes api
 * such as CoreV1APi, AppsV1 among others
 * @author tushart
 *
 */
public class KubernetesApiProvider {

    public static AppsV1beta2Api getAppsV1beta2Api(ApiClient apiClient) {
        if (apiClient == null) {
            return new AppsV1beta2Api();
        }
        return new AppsV1beta2Api(apiClient);
    }

    public static CoreV1Api getCoreV1Api(ApiClient apiClient) {
        if (apiClient == null) {
            return new CoreV1Api();
        }
        return new CoreV1Api(apiClient);
    }

    public static StorageV1Api getStorageV1Api(ApiClient apiClient) {
        if (apiClient == null) {
            return new StorageV1Api();
        }
        return new StorageV1Api(apiClient);
    }

}
