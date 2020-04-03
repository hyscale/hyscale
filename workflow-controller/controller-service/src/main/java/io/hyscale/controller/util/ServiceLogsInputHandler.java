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
package io.hyscale.controller.util;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.hyscale.commons.io.HyscaleInputReader;
import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.controller.activity.ControllerActivity;
import io.hyscale.controller.exception.ControllerErrorCodes;
import io.hyscale.controller.service.ReplicaProcessingService;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.deployer.services.model.ReplicaInfo;

@Component
public class ServiceLogsInputHandler {

    @Autowired
    private ReplicaProcessingService replicaProcessingService;

    public static final Integer MAX_RETRIES = 2;

    public String getPodFromUser(List<ReplicaInfo> replicaInfoList) throws HyscaleException {
        if (replicaInfoList == null || replicaInfoList.isEmpty()) {
            return null;
        }
        Optional<Map<Integer, ReplicaInfo>> optionalMap = replicaProcessingService.logReplicas(replicaInfoList, true);

        if (!optionalMap.isPresent() || optionalMap.isEmpty()) {
            return null;
        }

        Map<Integer, ReplicaInfo> replicaInfoMap = null;
        if (optionalMap.isPresent()) {
            replicaInfoMap = optionalMap.get();
        }
        WorkflowLogger.action(ControllerActivity.INPUT_REPLICA_DETAIL);
        int inputAttempt = 0;
        String replicaName = null;
        do {
            inputAttempt++;
            String input = HyscaleInputReader.readInput();
            boolean indexedInput = NumberUtils.isCreatable(input);
            try {
                if (indexedInput) {
                    int replicaIndex = Integer.parseInt(input);
                    ReplicaInfo replicaInfo = replicaInfoMap.get(replicaIndex);
                    replicaName = replicaInfo != null ? replicaInfo.getName() : null;
                } else {
                    replicaName = input;
                }
            } catch (NumberFormatException | IndexOutOfBoundsException e) {
            }
            if (replicaName == null) {
                WorkflowLogger.action(ControllerActivity.INVALID_INPUT_RETRY, input);
            }
        } while (inputAttempt < MAX_RETRIES && replicaName == null);

        if (inputAttempt >= MAX_RETRIES && replicaName == null) {
            throw new HyscaleException(ControllerErrorCodes.INVALID_REPLICA_SELECTED_REACHED_MAX_RETRIES);
        }
        return replicaName;
    }
}
