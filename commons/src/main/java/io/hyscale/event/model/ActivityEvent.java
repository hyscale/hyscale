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
package io.hyscale.event.model;

import org.apache.commons.lang3.StringUtils;

import io.hyscale.commons.logger.ActivityContext;
import io.hyscale.commons.logger.LoggerTags;
import io.hyscale.commons.models.Activity;

/**
 * Activity related events
 * These events must be created using {@link ActivityEventBuilder}
 *
 */
public class ActivityEvent extends HyscaleEvent {

    private ActivityContext activity;
    private String[] args;
    private LoggerTags tag;
    private ActivityState state;

    private ActivityEvent(ActivityContext activity, ActivityState state, LoggerTags tags, String ...args) {
        super(activity);
        this.activity = activity;
        this.args = args;
        this.state = state;
        this.tag = tag;
    }
    
    public ActivityContext getActivity() {
        return activity;
    }

    public String getActivityMessage() {
        if (activity == null || activity.getStartActivity() == null || activity.getStartActivity().getActivityMessage() == null) {
            return StringUtils.EMPTY;
        }
        return args != null && args.length != 0 ? String.format(getActivity(activity.getStartActivity()), args)
                : activity.getStartActivity().getActivityMessage();
    }
    
    private String getActivity(Activity activity) {
        return activity.getActivityMessage().replaceAll("\\{\\}", "%s");
    }

    public LoggerTags getTag() {
        return tag;
    }

    public ActivityState getState() {
        return state;
    }

    public String[] getArgs() {
        return args;
    }

    public static class ActivityEventBuilder {
        
        private ActivityContext context;
        private String[] args;
        private LoggerTags tag;
        private ActivityState state;
        
        public ActivityEventBuilder withActivity(Activity activity) {
            context = new ActivityContext(activity);
            return this;
        }
        
        public ActivityEventBuilder withArgs(String ...args) {
            this.args = args;
            return this;
        }
        
        public ActivityEventBuilder withLoggerTags(LoggerTags tag) {
            this.tag = tag;
            return this;
        }
        
        public ActivityEventBuilder withActivityState(ActivityState state) {
            this.state = state;
            return this;
        }
        
        public ActivityEvent build() {
            return new ActivityEvent(context, state, tag, args);
        }
    }
}
