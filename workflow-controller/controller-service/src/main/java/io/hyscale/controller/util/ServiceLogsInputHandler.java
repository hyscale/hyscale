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

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.io.HyscaleInputReader;
import io.hyscale.deployer.core.model.ReplicaInfo;
import io.kubernetes.client.openapi.models.V1Pod;

@Component
public class ServiceLogsInputHandler {

    public String getPodFromUser(Map<Integer, ReplicaInfo> indexedReplicasInfo) throws HyscaleException {
        String input = HyscaleInputReader.readInput();
        try {
            int replicaIndex = Integer.parseInt(input);
            if (indexedReplicasInfo.containsKey(replicaIndex)) {
                return indexedReplicasInfo.get(replicaIndex).getName();
            }
        } catch (NumberFormatException e) {
            return input;
        }
        return null;
    }
    
    public boolean isPodValid(List<V1Pod> podList, V1Pod pod) {
        if (podList == null || pod == null) {
            return false;
        }
        return isPodValid(podList, pod.getMetadata().getName());
    }
    
    public boolean isPodValid(List<V1Pod> podList, String podName) {
        if (podList == null || StringUtils.isBlank(podName)) {
            return false;
        }
        return podList.stream().anyMatch(each -> each.getMetadata().getName().equals(podName));
    }
}
