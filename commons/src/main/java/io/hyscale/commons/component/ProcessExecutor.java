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
package io.hyscale.commons.component;

import java.util.ArrayList;
import java.util.List;

import io.hyscale.commons.exception.HyscaleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Any class can extend this class to process.
 * Hooks can be implemented using @see {@link PrePostProcessors} and register
 * with component @see {@link #addProcessor(PrePostProcessors)}
 *
 * @param <I> <p> Implementation Notes </p>
 * @see #doExecute(ProcessingError) to execute any process . This method
 * will be invoked after all @see {@link PrePostProcessors#preProcess(Object)}.
 * After successful execution all @see {@link PrePostProcessors#postProcess(Object)}
 * are executed. In case of error the execution is terminated and the
 * @see {@link #onError(ProcessingError, HyscaleException)} is invoked.
 */

public abstract class ProcessExecutor<I extends ProcessingError> {

    private static final Logger logger = LoggerFactory.getLogger(ProcessExecutor.class);

    private List<PrePostProcessors> prePostProcessors = new ArrayList<PrePostProcessors>();

    protected void addProcessor(PrePostProcessors prePostProcessor) {
        this.prePostProcessors.add(prePostProcessor);
    }

    public void execute(I processInput) throws HyscaleException {
        try {
            if (prePostProcessors == null || prePostProcessors.isEmpty()) {
                operate(processInput);
            } else {
                executePrePostProcessors(true, processInput);
                operate(processInput);
                executePrePostProcessors(false, processInput);
            }
        } catch (HyscaleException e) {
            onError(processInput, e);
        }
    }

    private void executePrePostProcessors(boolean before, I processInput) {
        for (PrePostProcessors prePostProcessor : prePostProcessors) {
            if (processInput == null || processInput.isFailed()) {
                logger.error("Cannot execute the PrePostProcessor {}", prePostProcessor.getClass());
                return;
            }
            try {
                if (before) {
                    prePostProcessor.preProcess(processInput);
                } else {
                    prePostProcessor.postProcess(processInput);
                }
            } catch (HyscaleException he) {
                processInput.setHyscaleException(he);
                prePostProcessor.onError(processInput, he);
            }
        }
    }

    protected abstract void doExecute(I processInput) throws HyscaleException;

    protected abstract void onError(I processInput, HyscaleException th) throws HyscaleException;

    private boolean operate(I processInput) throws HyscaleException {
        if (processInput == null || processInput.isFailed()) {
            logger.error("Cannot execute the component {}", getClass());
            if (processInput.getHyscaleException() != null) {
                throw processInput.getHyscaleException();
            }
            return false;
        }
        doExecute(processInput);
        return true;
    }

}
