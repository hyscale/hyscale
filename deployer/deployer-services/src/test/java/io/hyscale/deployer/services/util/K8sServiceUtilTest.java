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

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.models.LoadBalancer;
import io.hyscale.deployer.services.model.CustomObject;
import io.hyscale.deployer.services.model.ServiceAddress;
import io.kubernetes.client.openapi.ApiClient;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.fail;

class K8sServiceUtilTest {

    private static final ApiClient API_CLIENT = new ApiClient();

    private static final String NAME_SPACE = "test";

    private static final String SELECTOR = "selector";

    public static Stream<Arguments> input() throws HyscaleException {
        return Stream.of(Arguments.of(false, prepareMinimalLoadBalancer("istio")),
                Arguments.of(true, prepareMinimalLoadBalancer("istio")));
    }

    @ParameterizedTest
    @MethodSource(value = "input")
    void testGetLBServiceAddress(boolean wait, LoadBalancer loadBalancer) {
        try {
            CustomObject customObject = new CustomObject();
            ServiceAddress serviceAddress = K8sServiceUtil.getLBServiceAddress(wait, loadBalancer, API_CLIENT, SELECTOR, NAME_SPACE);
            Assertions.assertTrue(serviceAddress != null && serviceAddress.getServiceIP() !=null && serviceAddress.getServiceURL() != null);
        } catch (HyscaleException e) {
            fail();
        }
    }

    //Invalid Provider.
    @Test
    public void testGetLBServiceAddress2() {
        try {
            LoadBalancer loadBalancer = prepareMinimalLoadBalancer("invalid");
            ServiceAddress serviceAddress = K8sServiceUtil.getLBServiceAddress(false, loadBalancer, API_CLIENT, SELECTOR, NAME_SPACE);
            Assertions.assertNull(serviceAddress);
        } catch (HyscaleException e) {
            fail();
        }
    }

    private static LoadBalancer prepareMinimalLoadBalancer(String provider) {
        LoadBalancer loadBalancer = new LoadBalancer();
        loadBalancer.setHost("abc.com");
        loadBalancer.setProvider(provider);
        return loadBalancer;
    }
}