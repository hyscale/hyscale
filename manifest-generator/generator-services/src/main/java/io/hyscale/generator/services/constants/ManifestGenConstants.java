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
package io.hyscale.generator.services.constants;

public class ManifestGenConstants {
    
    private ManifestGenConstants() {}

	public static final String NAME_DELIMITER = "-";

	public static final String POD_SPEC_OWNER = "pod-spec-owner";

	public static final String YAML_EXTENSION = ".yaml";

	public static final String IMAGE_SHA_SUM = "IMAGE_SHA_SUM";

	public static final String DEFAULT_CONFIG_PROPS_FILE = "config.props";

	public static final String DEFAULT_SECRETS_FILE = "secret.props";

	public static final String DEFAULT_IMAGE_PULL_POLICY = "Always";

	public static final String IMAGE_PULL_SECRET_NAME = "ImagePullSecretName";
	
	public static final String POD_CHECKSUM = "POD_CHECKSUM";

	//ISTIO loadBalancer fields.

	public static final String LOADBALANCER = "loadBalancer";

	public static final String HOSTS = "hosts";

	public static final String ISTIO = "istio";

	public static final String GATEWAY = "gateway";

	public static final String VIRTUAL_SERVICE = "virtual-service";

	public static final String DESTINATION_RULE = "destination-rule";

	public static final String NETWORKING_API_VERSION = "networking.istio.io/v1beta1";
  
	public static final String NETWORK_POLICY = "networkpolicy";

	public static final String INGRESS= "ingress";
}
