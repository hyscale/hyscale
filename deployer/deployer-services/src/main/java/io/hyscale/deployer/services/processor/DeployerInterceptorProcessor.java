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
package io.hyscale.deployer.services.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.hyscale.commons.component.IInterceptorProcessor;
import io.hyscale.commons.exception.CommonErrorCode;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.models.DeploymentContext;

public abstract class DeployerInterceptorProcessor implements IInterceptorProcessor {

    private static final Logger logger = LoggerFactory.getLogger(DeployerInterceptorProcessor.class);

    private static final String COMPONENT = "Deployer";

    abstract protected void _preProcess(DeploymentContext context) throws HyscaleException;

    abstract protected void _postProcess(DeploymentContext context) throws HyscaleException;

    abstract protected void _onError(DeploymentContext context) throws HyscaleException;

    @Override
    public void preProcess(Object... args) throws HyscaleException {
        if (!isInputValid(args)) {
            HyscaleException ex = new HyscaleException(CommonErrorCode.FAILED_TO_EXECUTE_PROCESSOR,
                    getFailureMsg(COMPONENT, "pre processing"));
            logger.error(ex.getMessage());
            throw ex;
        }
        _preProcess((DeploymentContext) args[0]);
    }

    @Override
    public void postProcess(Object... args) throws HyscaleException {
        if (!isInputValid(args)) {
            HyscaleException ex = new HyscaleException(CommonErrorCode.FAILED_TO_EXECUTE_PROCESSOR,
                    getFailureMsg(COMPONENT, "post processing"));
            logger.error(ex.getMessage());
            throw ex;
        }
        _postProcess((DeploymentContext) args[0]);
    }

    @Override
    public void onError(Object... args) throws HyscaleException {
        if (!isInputValid(args)) {
            HyscaleException ex = new HyscaleException(CommonErrorCode.FAILED_TO_EXECUTE_PROCESSOR,
                    getFailureMsg(COMPONENT, "error processing"));
            logger.error(ex.getMessage());
            throw ex;
        }
        _onError((DeploymentContext) args[0]);
    }

    private boolean isInputValid(Object... args) {
        if (args == null || args.length < 1) {
            return false;
        }
        if (args[0] == null || !(args[0] instanceof DeploymentContext)) {
            return false;
        }
        return true;
    }

}
