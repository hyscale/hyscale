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
package io.hyscale.commons.logger;

import io.hyscale.commons.models.Activity;


public class ActivityContext {

    private Activity startActivity;
    private long startTime;

    private int remaining;

    public ActivityContext(Activity startActivity) {
        this.startActivity = startActivity;
        this.remaining = WorkflowLogger.LEFT_ALIGNED_PADDING - startActivity.getActivityMessage().length();
    }

    public Activity getStartActivity() {
        return startActivity;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public int getRemaining() {
        return remaining;
    }

    public void setRemaining(int remaining) {
        this.remaining = remaining;
    }

}
