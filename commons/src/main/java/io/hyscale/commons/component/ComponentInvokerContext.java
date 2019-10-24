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

import io.hyscale.commons.exception.HyscaleException;

/**
 *  This class provides the invocation context
 *  to invoke any component 
 *
 */

public class ComponentInvokerContext {

    private boolean failed;
    private HyscaleException hyscaleException;

    public boolean isFailed() {
        return failed;
    }

    public void setFailed(boolean failed) {
        this.failed = failed;
    }

    public HyscaleException getHyscaleException() {
        return hyscaleException;
    }

    public void setHyscaleException(HyscaleException hyscaleException) {
        this.hyscaleException = hyscaleException;
    }
}
