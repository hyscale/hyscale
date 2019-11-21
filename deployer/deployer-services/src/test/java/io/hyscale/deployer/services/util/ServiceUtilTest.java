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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.hyscale.commons.utils.ObjectMapperFactory;
import io.hyscale.deployer.services.model.ServiceAddress;
import io.kubernetes.client.models.V1LoadBalancerIngress;
import io.kubernetes.client.models.V1Service;

public class ServiceUtilTest {

    private static V1Service v1Service;

    @BeforeAll
    public static void createService() throws IOException {
        InputStream resourceAsStream = PodHandlerTest.class.getResourceAsStream("/test-data/test-service.yaml");
        ObjectMapper mapper = ObjectMapperFactory.yamlMapper();
        v1Service = mapper.readValue(resourceAsStream, V1Service.class);
        resourceAsStream.close();
    }

    @Test
    public void nullService() {
        assertNull(K8sServiceUtil.getServiceAddress(null));
        assertNull(K8sServiceUtil.getPorts(null));
        assertNull(K8sServiceUtil.getLoadBalancer(null));
    }

    @Test
    public void testGetPorts() {
        List<Integer> ports = K8sServiceUtil.getPorts(v1Service);

        assertEquals(getPorts(), ports);
    }
    
    @Test
    public void testServiceAddress() {
        ServiceAddress serviceAddress = K8sServiceUtil.getServiceAddress(v1Service);
        assertEquals(v1Service.getStatus().getLoadBalancer().getIngress().get(0).getIp(), serviceAddress.getServiceIP());
        assertEquals(getPorts(), serviceAddress.getPorts());
        
    }
    
    @Test
    public void testLoadBalancer() {
        V1LoadBalancerIngress loadBalancer = K8sServiceUtil.getLoadBalancer(v1Service);
        assertEquals(v1Service.getStatus().getLoadBalancer().getIngress().get(0), loadBalancer);
    }
    
    private static List<Integer> getPorts(){
        return v1Service.getSpec().getPorts().stream().map(each -> {
            return each.getPort();
        }).collect(Collectors.toList());
    }
}
