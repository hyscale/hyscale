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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.hyscale.commons.utils.ObjectMapperFactory;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.models.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;


class PodHandlerTest {

    static ApiClient apiClient;
    String podName = "testpod";
    String namespace = "test";
    static V1Pod pod;


    @BeforeAll
    public static void createPod() throws IOException {
        InputStream resourceAsStream = PodHandlerTest.class.getResourceAsStream("/test-data/test-pod.yaml");
        ObjectMapper mapper = ObjectMapperFactory.yamlMapper();
        pod = mapper.readValue(resourceAsStream, V1Pod.class);
        resourceAsStream.close();
    }

    @Test
    void testGetStatus() {
        Assertions.assertEquals("Running", K8sPodUtil.getAggregatedStatusOfContainersForPod(pod));
    }

    @Test
    void testGetMessage() {
        Assertions.assertEquals(null, K8sPodUtil.getPodMessage(pod));
    }

}
