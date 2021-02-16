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
package io.hyscale.servicespec.commons.fields;

/**
 * Defines fields for hyscale service spec
 */
@SuppressWarnings("java:S115")
public class HyscaleSpecFields {

	public static final String name = "name";
	public static final String depends = "depends";
	public static final String image = "image";
	public static final String registry = "registry";
	public static final String environment = "environment";
	public static final String overrides = "overrides";
	public static final String tag = "tag";
	public static final String path = "path";
	public static final String dockerfile = "dockerfile";
	public static final String target = "target";
	public static final String args = "args";
	public static final String buildSpec = "buildSpec";
	public static final String stackImage = "stackImage";
	public static final String artifacts = "artifacts";
	public static final String provider = "provider";
	public static final String source = "source";
	public static final String destination = "destination";
	public static final String configCommands = "configCommands";
	public static final String configCommandsScript = "configCommandsScript";
	public static final String runCommands = "runCommands";
	public static final String runCommandsScript = "runCommandsScript";
	public static final String startCommand = "startCommand";
	public static final String k8sSnippets = "k8sSnippets";
	public static final String replicas = "replicas";
	public static final String memory = "memory";
	public static final String cpu = "cpu";
	public static final String ports = "ports";
	public static final String port = "port";
	public static final String healthCheck = "healthCheck";
	public static final String httpPath = "httpPath";
	public static final String external = "external";
	public static final String lbMappings = "lbMappings";
	public static final String host = "host";
	public static final String volumes = "volumes";
	public static final String size = "size";
	public static final String props = "props";
	public static final String secrets = "secrets";
	public static final String agents = "agents";
	public static final String hyscaleBuild = "hyscaleBuild";
	public static final String kaniko = "kaniko";
	public static final String jib = "jib";
	public static final String tagPolicy = "tagPolicy";
	public static final String context = "context";
	public static final String stack = "stack";
	public static final String custom = "custom";
	public static final String jfrog = "jfrog";
	public static final String jenkins = "jenkins";
	public static final String buildContext = "buildContext";
	public static final String buildCommand = "buildCommand";
	public static final String artifactLocation = "artifactLocation";
	public static final String store = "store";
	public static final String baseRepo = "baseRepo";
	public static final String docker = "docker";
	public static final String useBuildKit = "useBuildKit";
	public static final String buildArgs = "args";
	public static final String cache = "cache";
	public static final String flags = "flags";
	public static final String module = "module";
	public static final String profile = "profile";
	public static final String memoryLimitsInMB = "memoryLimitsInMB";
	public static final String cpuLimits = "cpuLimits";
	public static final String dataPaths = "dataPaths";
	public static final String sidecars = "sidecars";
	public static final String propsVolumePath = "propsVolumePath";
	public static final String secretsVolumePath = "secretsVolumePath";
	public static final String storageClass = "storageClass";
	public static final String loadBalancer = "loadBalancer";

	//Replicas
	public static final String min="min";
	public static final String max="max";
	public static final String cpuThreshold ="cpuThreshold";

	//Network Policies
	public static final String allowTraffic="allowTraffic";

	// JSON fields helper
	public static final String arrayLeftBrace = "[";
	public static final String arrayRightBrace = "]";
	public static final String WILDCARD = "*";
	public static final String ROOT = "$";
	public static final String SLICE_OPERATOR = ":";
	public static final String DOT = ".";

	public static String getArrayIndex(int i) {
		return arrayLeftBrace + i + arrayRightBrace;
	}

	public static String getArrayFromTo(int from, int to) {
		return arrayLeftBrace + from + SLICE_OPERATOR + to + arrayRightBrace;
	}

	public static String getArrayAllValues() {
		return arrayLeftBrace + WILDCARD + arrayRightBrace;
	}
	
	
	public static String getPath(String... fields) {
		if (fields == null) {
			return null;
		}
		StringBuilder path = new StringBuilder(fields[0]);
		for (int i = 1; i < fields.length; i++) {
			path.append(DOT).append(fields[i]);
		}
		return path.toString();
	}

}
