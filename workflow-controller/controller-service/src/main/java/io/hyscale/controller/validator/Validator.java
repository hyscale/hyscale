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
package io.hyscale.controller.validator;

import io.hyscale.commons.component.PrePostProcessors;

public abstract class Validator<I> implements PrePostProcessors<I> {
    
    public abstract boolean preValidate(I processInput);

    public abstract boolean postValidate(I processInput);

    public void preHook(I processInput) {
        if (!preValidate(processInput))
            onError(processInput, null);
    }

    public void postHook(I processInput) {
        if (!postValidate(processInput))
            onError(processInput, null);
    }

}
