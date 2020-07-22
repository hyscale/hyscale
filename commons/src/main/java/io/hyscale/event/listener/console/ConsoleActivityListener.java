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
package io.hyscale.event.listener.console;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.commons.models.Status;
import io.hyscale.event.model.ActivityEvent;

@Component
public class ConsoleActivityListener implements ApplicationListener<ActivityEvent> {

    private static final Logger logger = LoggerFactory.getLogger(ConsoleActivityListener.class);

    @Override
    public void onApplicationEvent(ActivityEvent event) {
        logger.debug("Listening Activity event: {}", event.getState());
        switch (event.getState()) {
        case HEADER:
            WorkflowLogger.header(event.getActivity().getStartActivity(), event.getArgs());
            break;
        case DONE:
            WorkflowLogger.endActivity(event.getActivity(), Status.DONE);
            break;
        case FAILED:
            WorkflowLogger.endActivity(event.getActivity(), Status.FAILED);
            break;
        case PROGRESS:
            WorkflowLogger.continueActivity(event.getActivity());
            break;
        case SKIPPING:
            WorkflowLogger.endActivity(event.getActivity(), Status.SKIPPING);
            break;
        case STARTED:
            WorkflowLogger.startActivity(event.getActivity(), event.getArgs());
            break;
        default:
            break;

        }
    }

}
