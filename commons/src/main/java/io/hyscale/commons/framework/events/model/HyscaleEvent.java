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
package io.hyscale.commons.framework.events.model;

import org.springframework.context.ApplicationEvent;

public abstract class HyscaleEvent extends ApplicationEvent {

    /**
     * @param source HyscaleEvents implements {@link ApplicationEvent}
     * which requires a source object {@link ApplicationEvent#getSource()}.
     * Implementing class should take care of how they define source. 
     * It could be based the object on which the event initially occurred
     * or with which the event is associated (never {@code null})
     */
    public HyscaleEvent(Object source) {
        super(source);
    }

}
