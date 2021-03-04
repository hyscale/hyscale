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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import io.hyscale.servicespec.commons.model.service.Replicas;

class ReplicasUtilTest {

    private static Stream<Arguments> input() {
        return Stream.of(Arguments.of(null, false), Arguments.of(getReplica(null, null, null), false),
                Arguments.of(getReplica(1, null, "10"), false), Arguments.of(getReplica(null, 2, "10"), false),
                Arguments.of(getReplica(1, 2, null), false), Arguments.of(getReplica(1, 2, "test"), false),
                Arguments.of(getReplica(1, 2, "10"), false), Arguments.of(getReplica(1, 2, "-10%"), false),
                Arguments.of(getReplica(-1, 2, "10"), false), Arguments.of(getReplica(3, 2, "10"), false),
                Arguments.of(getReplica(1, 2, "10%"), true));
    }

    @ParameterizedTest
    @MethodSource("input")
    void autoScalingTest(Replicas replica, boolean autoScalingEnabled) {
        assertEquals(ReplicasUtil.isAutoScalingEnabled(replica), autoScalingEnabled);
    }

    private static Replicas getReplica(Integer min, Integer max, String cpuThreshold) {
        Replicas replicas = new Replicas();
        replicas.setMin(min);
        replicas.setMax(max);
        replicas.setCpuThreshold(cpuThreshold);
        return replicas;
    }
}
