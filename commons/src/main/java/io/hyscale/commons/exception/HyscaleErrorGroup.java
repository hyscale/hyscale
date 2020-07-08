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
    SERVICE_SPEC_PROCESSING(15),
    SERVICE_PROFILE_PROCESSING(30),
    UPFRONT_VALIDATION (45),
    DOCKER_FILE_GENERATION (60),
    IMAGE_BUILD (75),
    IMAGE_PUSH (90),
    MANIFEST_GENERATION (105),
    DEPLOYER_APPLY (120),
    WAIT_FOR_DEPLOYMENT (135),
    GET_SERVICE_IP (201),
    GET_API_CLIENT(202),
    GET_LOGS(203);

    Integer groupCode;

    HyscaleErrorGroup(Integer groupCode) {
        this.groupCode=groupCode;
    }

    public Integer getGroupCode() {
        return groupCode;
    }
}
