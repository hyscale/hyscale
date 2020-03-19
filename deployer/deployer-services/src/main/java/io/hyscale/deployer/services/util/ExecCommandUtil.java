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

import java.util.ArrayList;
import java.util.List;

import io.hyscale.commons.models.K8sBasicAuth;

public class ExecCommandUtil {
	public static final String KUBECTL ="kubectl";
	public static final String EXEC="exec";
	public static final String IT="-it";
	public static final String NAMESPACE="-n";
	public static final String BASH="bash";
	public static final String TOKEN="--token";
	
	public static List<String> getExecCommand(String replicaName, String namespace, String accessToken){
		List<String> commands=getCommand(replicaName, namespace);
        commands.add(TOKEN);
        commands.add(accessToken);
        return commands;
	}
	
	public static List<String> getExecCommand(String replicaName, String namespace,K8sBasicAuth k8sBasicAuth ){
		List<String> commands=getCommand(replicaName, namespace);
        return commands;
	}
	
	private static List<String> getCommand(String replicaName, String namespace) {
		List<String> commands = new ArrayList<>();
		commands.add(KUBECTL);
        commands.add(EXEC);
        commands.add(IT);
        commands.add(replicaName);
        commands.add(NAMESPACE);
        commands.add(namespace);
        commands.add(BASH);
        return commands;
	}
}
