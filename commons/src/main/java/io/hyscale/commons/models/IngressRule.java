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
import io.kubernetes.client.openapi.models.ExtensionsV1beta1HTTPIngressPath;
import io.kubernetes.client.openapi.models.ExtensionsV1beta1HTTPIngressRuleValue;
import io.kubernetes.client.openapi.models.ExtensionsV1beta1IngressBackend;
import io.kubernetes.client.openapi.models.ExtensionsV1beta1IngressRule;

import java.util.List;

public class IngressRule extends ExtensionsV1beta1IngressRule {

    public void setRule(String serviceName, String port, List<String> paths){
        if(paths!=null && !paths.isEmpty()){
            this.http(new ExtensionsV1beta1HTTPIngressRuleValue());
            paths.forEach((path)->{
                ExtensionsV1beta1IngressBackend backend = new ExtensionsV1beta1IngressBackend();
                backend.setServiceName(serviceName);
                backend.setServicePort(new IntOrString(port));
                ExtensionsV1beta1HTTPIngressPath extensionsV1beta1HTTPIngressPath = new ExtensionsV1beta1HTTPIngressPath().backend(backend);
                extensionsV1beta1HTTPIngressPath.setPath(path);
                this.getHttp().addPathsItem(extensionsV1beta1HTTPIngressPath);
            });
        }
    }

}
