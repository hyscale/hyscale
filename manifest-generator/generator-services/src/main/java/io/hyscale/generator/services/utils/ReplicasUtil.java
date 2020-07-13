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
package io.hyscale.generator.services.utils;

import org.apache.commons.lang3.StringUtils;

import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.commons.models.Activity;
import io.hyscale.generator.services.model.ManifestGeneratorActivity;
import io.hyscale.servicespec.commons.model.service.Replicas;

/**
 * Utility class to combine replicas related operations like
 * Does replicas support auto scaling: {@link #isAutoScalingEnabled(Replicas)}
 */
public class ReplicasUtil {

    private static final String CPU_THRESHOLD_REGEX = "[\\d]+%";
    
    private ReplicasUtil() {}

    public static boolean isAutoScalingEnabled(Replicas replicas) {
        return isAutoScalingEnabled(replicas, false);
    }

    /**
     * Auto scaling is enabled when
     * Min, max and cpuThreshold are provided,
     * Min is less than max and
     * CPU threshold matches the {@value #CPU_THRESHOLD_REGEX}
     * @param replicas
     * @param persistMsg whether to persist warn messages
     * @return whether auto scaling is enabled
     */
    public static boolean isAutoScalingEnabled(Replicas replicas, Boolean persistMsg) {
        if (replicas == null) {
            return false;
        }

        if (replicas.getMin() == null || replicas.getMin() < 1) {
            return false;
        }

        if (replicas.getMax() == null) {
            if (StringUtils.isBlank(replicas.getCpuThreshold())) {
                return false;
            }
            persistWarnMsg(persistMsg, ManifestGeneratorActivity.IGNORING_REPLICAS, "Missing field max replicas");
            return false;
        }

        if (replicas.getMax() < replicas.getMin()) {
            persistWarnMsg(persistMsg, ManifestGeneratorActivity.IGNORING_REPLICAS,
                    "Min replicas should be less than max replicas");
            return false;
        }
        if (StringUtils.isBlank(replicas.getCpuThreshold())) {
            persistWarnMsg(persistMsg, ManifestGeneratorActivity.IGNORING_REPLICAS, "Missing field cpuThreshold");
            return false;
        }

        if (!replicas.getCpuThreshold().matches(CPU_THRESHOLD_REGEX)) {
            persistWarnMsg(persistMsg, ManifestGeneratorActivity.IGNORING_REPLICAS,
                    "The field cpuThreshold should match the regex " + CPU_THRESHOLD_REGEX);
            return false;
        }
        int cpuThreshold = Integer.parseInt(replicas.getCpuThreshold().replace("%", ""));
        if (cpuThreshold < 1) {
            persistWarnMsg(persistMsg, ManifestGeneratorActivity.IGNORING_REPLICAS,
                    "The field cpuThreshold should be greater than 0");
            return false;
        }
        
        return true;
    }

    private static void persistWarnMsg(Boolean persist, Activity activity, String msg) {
        if (persist != null && persist) {
            WorkflowLogger.persist(activity, msg);
        }
    }

}
