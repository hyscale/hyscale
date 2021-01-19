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

import io.kubernetes.client.custom.IntOrString;
import io.kubernetes.client.openapi.models.NetworkingV1beta1HTTPIngressPath;
import io.kubernetes.client.openapi.models.NetworkingV1beta1HTTPIngressRuleValue;
import io.kubernetes.client.openapi.models.NetworkingV1beta1IngressBackend;
import io.kubernetes.client.openapi.models.NetworkingV1beta1IngressRule;

import java.util.List;

public class IngressRule extends NetworkingV1beta1IngressRule {

    public void setRule(String serviceName, String port, List<String> paths){

        if(paths!=null && !paths.isEmpty()){
            this.http(new NetworkingV1beta1HTTPIngressRuleValue());
            paths.forEach((path)->{
                NetworkingV1beta1IngressBackend v1beta1IngressBackend = new NetworkingV1beta1IngressBackend();
                v1beta1IngressBackend.setServiceName(serviceName);
                v1beta1IngressBackend.setServicePort(new IntOrString(port));
                NetworkingV1beta1HTTPIngressPath v1beta1HTTPIngressPath = new NetworkingV1beta1HTTPIngressPath().backend(v1beta1IngressBackend);
                v1beta1HTTPIngressPath.setPath(path);
                this.getHttp().addPathsItem(v1beta1HTTPIngressPath);
            });
        }
    }

}
