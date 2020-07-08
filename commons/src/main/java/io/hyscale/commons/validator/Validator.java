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
package io.hyscale.commons.validator;

import io.hyscale.commons.exception.HyscaleException;

/**
 * Provides methods to be implemented by validators
 * These validators are used to validate information related to
 * service spec, cluster among others
 * @author tushar
 *
 * @param <T>
 */
public interface Validator<T> {
    
    public boolean validate(T t) throws HyscaleException;

}
