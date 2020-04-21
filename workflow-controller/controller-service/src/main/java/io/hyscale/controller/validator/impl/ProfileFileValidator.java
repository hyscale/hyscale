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
package io.hyscale.controller.validator.impl;

import org.springframework.stereotype.Component;

import io.hyscale.commons.constants.ValidationConstants;
import io.hyscale.controller.validator.FileValidator;
import io.hyscale.servicespec.commons.activity.ServiceSpecActivity;

/**
 * Provides profile specific implementation to {@link FileValidator}
 *
 */
@Component
public class ProfileFileValidator extends FileValidator {

    @Override
    public String getFilePattern() {
        return ValidationConstants.PROFILE_FILENAME_REGEX;
    }

    @Override
    public ServiceSpecActivity getWarnMessage() {
        return ServiceSpecActivity.IMPROPER_PROFILE_FILE_NAME;
    }

}
