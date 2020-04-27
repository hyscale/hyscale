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
package io.hyscale.controller.exception;

import org.springframework.stereotype.Component;

import io.hyscale.commons.constants.ToolConstants;
import io.hyscale.commons.exception.HyscaleException;
import picocli.CommandLine.IExitCodeExceptionMapper;

@Component
public class ExitCodeExceptionMapper implements IExitCodeExceptionMapper {

    @Override
    public int getExitCode(Throwable exception) {
        if (exception == null) {
            return ToolConstants.HYSCALE_SUCCESS_CODE;
        }
        if (exception instanceof HyscaleException) {
            HyscaleException he = (HyscaleException) exception;
            return he.getCode();
        }
        return ToolConstants.HYSCALE_FAILURE_CODE;
    }

}
