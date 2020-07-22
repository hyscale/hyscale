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
package io.hyscale.event.processor;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.hyscale.event.model.HyscaleEvent;
import io.hyscale.event.publisher.EventPublisher;

/**
 * 
 * Performs functions such as:
 * 1. Listeners: Registering and deregistering
 * 2. Calling Publishers
 * 3. Processing Events before publishing
 * 
 *
 */
public class EventProcessor {

    private static final Logger logger = LoggerFactory.getLogger(EventProcessor.class);

    private static List<EventPublisher> publishers = new ArrayList<>();

    //    private static List<InformationEvent> persistedEvents = new LinkedList<>();

    public static void registerPublishers(List<EventPublisher> publishers) {
        if (publishers == null) {
            return;
        }
        EventProcessor.publishers.addAll(publishers);
    }

    public static void addPublisher(EventPublisher publisher) {
        if (publisher == null) {
            return;
        }
        EventProcessor.publishers.add(publisher);
    }

    public static void publishEvent(HyscaleEvent event) {
        logger.debug("Publishing information event: {}", event);
        publishers.stream().forEach(publisher -> publisher.publishEvent(event));
    }

//    public static void publishEvent(ActivityEvent event) {
//        logger.debug("Publishing activity event: {}", event);
//        publishers.stream().forEach(publisher -> publisher.publishEvent(event));
//    }

    //    public static void persistEvent(InformationEvent event) {
    //        persistedEvents.add(event);
    //    }
    //
    //    public static void publishPersistedEvents() {
    //        persistedEvents.stream()
    //                .forEach(event -> publishers.stream().forEach(publisher -> publisher.publishEvent(event)));
    //    }

}
