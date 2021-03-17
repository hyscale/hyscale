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
package io.hyscale.commons.models;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author nagachandrai
 * Type of load balancer supported by hyscale.
 */
public enum LBType {

    INGRESS("nginx", "traefik") {
        @Override
        public String getServiceAddressPlaceHolder() {
            return "<External IP of Ingress controller service>";
        }
    },
    ISTIO("istio") {
        @Override
        public String getServiceAddressPlaceHolder() {
            return "<External IP of istio-ingress-gateway>";
        }
    };

    private List<String> lbProviders;

    LBType(String... providers) {
        this.lbProviders = new ArrayList<>();
        this.lbProviders.addAll(Arrays.asList(providers));
    }

    public List<String> getProviders() {
        return this.lbProviders;
    }

    public static LBType getByProvider(String provider) {
        if (StringUtils.isBlank(provider)) {
            return null;
        }
        for (LBType lbType : LBType.values()) {
            List<String> lbProviders = lbType.getProviders();
            for (String lbProvider : lbProviders) {
                if (lbProvider.equalsIgnoreCase(provider)) {
                    return lbType;
                }
            }
        }
        return null;
    }

    public abstract String getServiceAddressPlaceHolder();

}
