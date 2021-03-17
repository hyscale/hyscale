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

import io.hyscale.commons.utils.HyscaleContextUtil;
import io.hyscale.generator.services.builder.IngressMetaDataBuilder;
import io.hyscale.generator.services.builder.NginxMetaDataBuilder;
import io.hyscale.generator.services.builder.TraefikMetaDataBuilder;
import org.apache.commons.lang3.StringUtils;

/**
 * This class provides the appropriate Ingress Manifests Builder based on the provider mentioned in hspec.
 */
public enum IngressBuilderProvider {
    NGINX("nginx"){
        @Override
        public IngressMetaDataBuilder getMetadataBuilder() {
            return HyscaleContextUtil.getSpringBean(NginxMetaDataBuilder.class);
        }
    },
    TRAEFIK("traefik") {
        @Override
        public IngressMetaDataBuilder getMetadataBuilder() {
            return HyscaleContextUtil.getSpringBean(TraefikMetaDataBuilder.class);
        }
    };

    private String provider;

    IngressBuilderProvider(String provider){
        this.provider = provider;
    }

    public String getProvider(){ return this.provider; }

    public static IngressBuilderProvider fromString(String provider) {
        if (StringUtils.isBlank(provider)) {
            return null;
        }
        for (IngressBuilderProvider ingressBuilderProvider : IngressBuilderProvider.values()) {
            if(ingressBuilderProvider.getProvider().equalsIgnoreCase(provider)){
                return ingressBuilderProvider;
            }
        }
        return null;
    }

    public abstract IngressMetaDataBuilder getMetadataBuilder();

}
