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
package io.hyscale.generator.services.model;

import io.hyscale.commons.models.LBType;
import io.hyscale.commons.utils.HyscaleContextUtil;
import io.hyscale.generator.services.builder.IngressManifestBuilder;
import io.hyscale.generator.services.builder.IstioManifestBuilder;
import io.hyscale.generator.services.builder.LoadBalancerBuilder;

/**
 * This class provides the appropriate LoadBalancer manifests builder based on the provider(type of load balancer) mentioned in hspec.
 */
public enum LBBuilderType {

    INGRESS {
        @Override
        public LoadBalancerBuilder getBuilder() {
            return HyscaleContextUtil.getSpringBean(IngressManifestBuilder.class);
        }
    },
    ISTIO {
        @Override
        public LoadBalancerBuilder getBuilder() {
            return HyscaleContextUtil.getSpringBean(IstioManifestBuilder.class);
        }
    };

    public static LBBuilderType getByType(LBType lbType) {
        if (lbType == null) {
            return null;
        }
        return LBBuilderType.valueOf(lbType.name());
    }

    public abstract LoadBalancerBuilder getBuilder();

}


