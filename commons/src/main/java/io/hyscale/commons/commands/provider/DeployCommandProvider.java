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
package io.hyscale.commons.commands.provider;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import io.hyscale.commons.exception.CommonErrorCode;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.models.K8sBasicAuth;

@Component
public class DeployCommandProvider {
	public static final String KUBECTL ="kubectl";
	public static final String EXEC="exec";
	public static final String IT="-it";
	public static final String NAMESPACE="-n";
	public static final String BASH="bash";
	public static final String TOKEN="--token";
	public static final String KUBECONFIG="--kubeconfig";
	
	public List<String> getExecCommandByKubeConfig(String replicaName, String namespace, File kubeConfigFile)
			throws HyscaleException {
		if (kubeConfigFile == null || kubeConfigFile.getAbsolutePath() == null) {
			throw new HyscaleException(CommonErrorCode.KUBECONFIG_FILE_NOT_FOUND);
		}
		List<String> commands = getCommand(replicaName, namespace);
		commands.add(KUBECONFIG);
		commands.add(kubeConfigFile.getAbsolutePath());
		return commands;
	}
	
	public List<String> getExecCommandByBasicAuth(String replicaName, String namespace, K8sBasicAuth k8sBasicAuth)
			throws HyscaleException {
		if (k8sBasicAuth.getToken() == null) {
			throw new HyscaleException(CommonErrorCode.K8SBASICAUTH_TOKEN_NOT_FOUND);
		}
		List<String> commands = getCommand(replicaName, namespace);
		commands.add(TOKEN);
		commands.add(k8sBasicAuth.getToken());
		return commands;
	}
	
	private List<String> getCommand(String replicaName, String namespace) {
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
