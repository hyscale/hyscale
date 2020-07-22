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
package io.hyscale.event.publisher;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.stereotype.Component;

import io.hyscale.event.model.HyscaleEvent;

@Component
public class EventPublisher {

    @Autowired
    private SimpleApplicationEventMulticaster multicaster;

    public void publishEvent(HyscaleEvent event) {
        multicaster.multicastEvent(event);
    }
    
    // Remove Listener methods
    public void removeListener(ApplicationListener listener) {
        multicaster.removeApplicationListener(listener);
    }
}
