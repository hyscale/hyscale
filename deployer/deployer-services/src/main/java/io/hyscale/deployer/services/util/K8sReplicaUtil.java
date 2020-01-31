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
package io.hyscale.deployer.services.util;

import java.util.List;
import java.util.stream.Collectors;

import io.hyscale.deployer.core.model.ReplicaInfo;
import io.kubernetes.client.models.V1Pod;

public class K8sReplicaUtil {


    /**
     * @param podList
     * @return list of {@link ReplicaInfo} for each pod
     */
    public static List<ReplicaInfo> getReplicaInfo(List<V1Pod> podList) {
        if (podList == null) {
            return null;
        }
        return podList.stream().map(each -> getReplicaInfo(each)).collect(Collectors.toList());
    }
    
    /**
     * 
     * @param pod
     * @return {@link ReplicaInfo} for the pod
     */
    public static ReplicaInfo getReplicaInfo(V1Pod pod) {
        if (pod == null) {
            return null;
        }
        ReplicaInfo replicaInfo = new ReplicaInfo();
        replicaInfo.setName(pod.getMetadata().getName());
        replicaInfo.setAge(pod.getStatus().getStartTime());
        replicaInfo.setStatus(K8sPodUtil.getAggregatedStatusOfContainersForPod(pod));
        
        return replicaInfo;
    }
    
}
