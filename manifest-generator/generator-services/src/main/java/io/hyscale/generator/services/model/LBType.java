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

import io.hyscale.generator.services.builder.IstioManifestBuilder;
import io.hyscale.generator.services.builder.LoadBalancerBuilder;
import io.hyscale.generator.services.builder.NginxManifestBuilder;
import io.hyscale.generator.services.builder.TraefikManifestBuilder;
import org.apache.commons.lang3.StringUtils;

public enum LBType {

    NGINX("nginx"){
        @Override
        public LoadBalancerBuilder getImplementation() {
            return new NginxManifestBuilder();
        }
    },
    TRAEFIK("traefik"){
        @Override
        public LoadBalancerBuilder getImplementation() {
            return new TraefikManifestBuilder();
        }
    },
    ISTIO("istio"){
        @Override
        public LoadBalancerBuilder getImplementation() {
            return new IstioManifestBuilder();
        }
    };

    private String type;

    LBType(String type){
        this.type = type;
    }

    public String getType(){ return this.type; }

    public static LBType fromString(String type) {
        if (StringUtils.isBlank(type)) {
            return null;
        }
        for (LBType lbType : LBType.values()) {
            if(lbType.getType().equalsIgnoreCase(type)){
                return lbType;
            }
        }
        return null;
    }

    public abstract LoadBalancerBuilder getImplementation();

}


