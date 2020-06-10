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
package io.hyscale.commons.exception;

public enum HyscaleErrorGroup{
    SERVICE_SPEC_PROCESSING(12),
    SERVICE_PROFILE_PROCESSING(13),
    CLUSTER_VALIDATION (14),
    DOCKER_FILE_GENERATION (15),
    IMAGE_BUILD (16),
    IMAGE_PUSH (17),
    MANIFEST_GENERATION (18),
    DEPLOYER_APPLY (19),
    WAIT_FOR_DEPLOYMENT (20),
    GET_SERVICE_IP (21),
    GET_API_CLIENT(22),
    GET_LOGS(23);

    Integer groupCode;

    HyscaleErrorGroup(Integer groupCode) {
        this.groupCode=groupCode;
    }

    public Integer getGroupCode() {
        return groupCode;
    }
}
