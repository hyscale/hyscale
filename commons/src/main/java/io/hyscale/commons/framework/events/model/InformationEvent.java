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

import java.io.Serializable;

public class InformationEvent<T extends Serializable> extends HyscaleGenericEvent {

    private Level level;
    private T message;

    public InformationEvent(T message) {
        super(message);
        this.message = message;
    }

    public InformationEvent(T message, Level level) {
        super(message);
        this.message = message;
        this.level = level;
    }

    public InformationEvent(Object source, T message, Level level) {
        super(source);
        this.level = level;
        this.message = message;
    }

    public Level getLevel() {
        return level;
    }

    public T getMessage() {
        return message;
    }

    public enum Level {
        INFO, WARN, DEBUG, ERROR, VERBOSE;
    }

    @Override
    protected Object getType() {
        return message;
    }

}
