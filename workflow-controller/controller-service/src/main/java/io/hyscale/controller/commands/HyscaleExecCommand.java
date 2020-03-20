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
package io.hyscale.controller.commands;

import java.util.concurrent.Callable;

import javax.validation.constraints.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.hyscale.commons.constants.ToolConstants;
import io.hyscale.commons.constants.ValidationConstants;
import io.hyscale.commons.models.K8sConfigFileAuth;
import io.hyscale.controller.builder.K8sAuthConfigBuilder;
import io.hyscale.controller.util.CommandUtil;
import io.hyscale.deployer.services.deployer.Deployer;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "exec", description = { "Exec into  specified container" })
@Component
public class HyscaleExecCommand implements Callable<Integer> {
	
	@Pattern(regexp = ValidationConstants.SERVICE_NAME_REGEX, message = ValidationConstants.INVALID_SERVICE_NAME_MSG)
	@Option(names = { "-s", "--service" }, required = true, description = "Service name")
	private String serviceName;
	
	@Pattern(regexp = ValidationConstants.APP_NAME_REGEX, message = ValidationConstants.INVALID_APP_NAME_MSG)
	@Option(names = { "-a", "--app" }, required = true, description = "Application name")
	private String appName;
	
	@Pattern(regexp = ValidationConstants.REPLICA_NAME_REGEX, message = ValidationConstants.INVALID_REPLICA_NAME_MSG)
	@Option(names = { "-r", "--replica" }, required = true, description = "Replica name")
	private String replicaName;
	
	@Pattern(regexp = ValidationConstants.NAMESPACE_REGEX, message = ValidationConstants.INVALID_NAMESPACE_MSG)
	@Option(names = { "-n", "--namespace", "-ns" }, required = true, description = "Namespace of the service")
	private String namespace;
	
	@Autowired
	private Deployer deployer;
	 @Autowired
	 private K8sAuthConfigBuilder authConfigBuilder;

	@Override
	public Integer call() throws Exception {
		if (!CommandUtil.isInputValid(this)) {
			return ToolConstants.INVALID_INPUT_ERROR_CODE;
		}
		K8sConfigFileAuth k8sConfigFileAuth=(K8sConfigFileAuth) authConfigBuilder.getAuthConfig();
		return deployer.exec(k8sConfigFileAuth,serviceName, appName, namespace, replicaName);
	}

}
