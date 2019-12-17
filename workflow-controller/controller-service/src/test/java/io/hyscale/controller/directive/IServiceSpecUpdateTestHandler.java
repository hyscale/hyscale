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
package io.hyscale.controller.directive;

import static org.junit.jupiter.api.Assertions.fail;

import com.fasterxml.jackson.databind.node.ObjectNode;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;

public interface IServiceSpecUpdateTestHandler<T> {

    public String getServiceSpecPath();
    
    public boolean validate(T oldObject, T updatedObject);
    
    public ServiceSpec updateServiceSpec(ObjectNode serviceSpecNode);

    default boolean validate(ServiceSpec oldServiceSpec, ServiceSpec updatedServiceSpec) {
        T oldObject = null;
        T updatedObject = null;
        try {
            oldObject = oldServiceSpec.get(getJsonPath(), getType());
            updatedObject = updatedServiceSpec.get(getJsonPath(), getType());
        } catch (HyscaleException e) {
            fail();
        }
        return validate(oldObject, updatedObject);
    }
    
    public String getJsonPath();
    
    public Class<T> getType();
}
