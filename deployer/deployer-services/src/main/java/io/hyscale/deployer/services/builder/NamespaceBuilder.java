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
package io.hyscale.deployer.services.builder;

import org.apache.commons.lang3.StringUtils;

import io.hyscale.deployer.core.model.ResourceKind;
import io.kubernetes.client.models.V1Namespace;
import io.kubernetes.client.models.V1ObjectMeta;

/**
 * Create V1Namespace from name
 *
 */
public class NamespaceBuilder {

	public static V1Namespace build(String namespace) {
	    if (StringUtils.isBlank(namespace)) {
	        return null;
	    }
		V1Namespace v1Namespace = new V1Namespace();
		v1Namespace.setApiVersion("v1");
		v1Namespace.setKind(ResourceKind.NAMESPACE.getKind());

		V1ObjectMeta v1ObjectMeta = new V1ObjectMeta();
		v1ObjectMeta.setName(namespace);
		v1Namespace.setMetadata(v1ObjectMeta);
		return v1Namespace;
	}
}
