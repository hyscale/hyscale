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
package io.hyscale.commons.framework.events.publisher;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Scope;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.stereotype.Component;

import io.hyscale.commons.framework.events.model.HyscaleEvent;
import io.hyscale.commons.utils.HyscaleContextUtil;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class EventPublisher {

    private static final Logger logger = LoggerFactory.getLogger(EventPublisher.class);
    private static EventPublisher instanceHolder;

    @Autowired
    private SimpleApplicationEventMulticaster multicaster;

    public void publishEvent(HyscaleEvent event) {
        logger.debug("Publishing event: {}", event);
        multicaster.multicastEvent(event);
    }

    public void removeListener(ApplicationListener<? extends HyscaleEvent> listener) {
        multicaster.removeApplicationListener(listener);
        String[] beanNames = HyscaleContextUtil.getBeanNames(listener.getClass());
        Arrays.stream(beanNames).forEach(each -> multicaster.removeApplicationListenerBean(each));
    }

    public synchronized static EventPublisher getInstance() {
        if (instanceHolder == null) {
            instanceHolder = HyscaleContextUtil.getSpringBean(EventPublisher.class);
        }
        return instanceHolder;
    }
}
