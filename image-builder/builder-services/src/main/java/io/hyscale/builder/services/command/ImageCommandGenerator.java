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
package io.hyscale.builder.services.command;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import io.hyscale.commons.config.SetupConfig;
import io.hyscale.commons.constants.ToolConstants;

@Component
public class ImageCommandGenerator {

	private static final String HYSCALE_IO_URL = "hyscale.io";
	private static final String SLASH = "/";
	private static final String EQUALS = "=";
	private static final String DOCKER_COMMAND = "docker";
	private static final String VERSION_COMMAND = "-v";
	private static final String IMAGES = "images";
	private static final String SUDO_COMMAND = "sudo";
	private static final String PUSH_COMMAND = "push";
	private static final String INSPECT_COMMAND = "inspect";
	private static final String TAG_COMMAND = "tag";
	private static final String SPACE = " ";
	private static final String DOCKER_BUILD = "docker build";
	private static final String TAG_ARG = " -t ";
	private static final String BUILD_ARGS = " --build-arg ";
	private static final String REMOVE_IMAGE = "rmi";
	private static final String PULL_COMMAND = "pull";

	private static final boolean USE_SUDO = false;

	public String getBuildImageName(String appName, String serviceName) {
		StringBuilder sb = new StringBuilder();
		sb.append(HYSCALE_IO_URL).append(SLASH).append(appName).append(SLASH).append(serviceName);
		return normalize(sb.toString());
	}

	public String getBuildImageNameWithTag(String appName, String serviceName, String tag) {
		StringBuilder sb = new StringBuilder();
		sb.append(HYSCALE_IO_URL).append(SLASH).append(appName).append(SLASH).append(serviceName);
		if(StringUtils.isNotBlank(tag)) {
			sb.append(ToolConstants.COLON).append(tag);
		}
		return normalize(sb.toString());
	}

	public String dockerBuildCommand(String appName, String serviceName, String tag, String dockerFilePath) {
		return dockerBuildCommand(appName, serviceName, tag, dockerFilePath, null);
	}

	public String dockerBuildCommand(String appName, String serviceName, String tag, String dockerFilePath,
			Map<String, String> buildArgs) {
		StringBuilder buildCommand = new StringBuilder();
		buildCommand.append(DOCKER_BUILD);
		if (buildArgs != null && !buildArgs.isEmpty()) {
			buildCommand.append(getBuildArgs(buildArgs));
		}
		buildCommand.append(TAG_ARG);
		buildCommand.append(getBuildImageNameWithTag(appName, serviceName, tag));
		dockerFilePath = StringUtils.isNotBlank(dockerFilePath) ? dockerFilePath : SetupConfig.getAbsolutePath(".");
		buildCommand.append(SPACE).append(dockerFilePath).append(ToolConstants.FILE_SEPARATOR);
		return buildCommand.toString();
	}

	private String getBuildArgs(Map<String, String> buildArgs) {
		StringBuilder buildArgsCmd = new StringBuilder();
		buildArgs.entrySet().stream().forEach(each -> {
			buildArgsCmd.append(BUILD_ARGS).append(each.getKey() + EQUALS + each.getValue());
		});
		return buildArgsCmd.toString();
	}

	public String getDockerInstalledCommand() {
		String command = getDockerCommand() + VERSION_COMMAND;
		return command;
	}

	public String getDockerDaemonRunningCommand() {
		String command = getDockerCommand() + IMAGES;
		return command;
	}

	public String getImagePushCommand(String imageFullPath) {
		imageFullPath = normalize(imageFullPath);
		StringBuilder pushCommand = new StringBuilder(getDockerCommand());
		pushCommand.append(PUSH_COMMAND).append(SPACE).append(imageFullPath);
		return pushCommand.toString();
	}

	public String getImageTagCommand(String sourcePath, String targetPath) {
		sourcePath = normalize(sourcePath);
		targetPath = normalize(targetPath);
		StringBuilder tagCommand = new StringBuilder(getDockerCommand());
		tagCommand.append(TAG_COMMAND).append(SPACE).append(sourcePath).append(SPACE).append(targetPath);

		return tagCommand.toString();
	}

	public String getImagePullCommand(String imageName) {
		if (StringUtils.isBlank(imageName)) {
			return null;
		}
		imageName = normalize(imageName);
		StringBuilder imagePullCmd = new StringBuilder(getDockerCommand());
		imagePullCmd.append(PULL_COMMAND).append(SPACE).append(imageName);
		return imagePullCmd.toString();
	}

	public String getImageCleanUpCommand(String appName, String serviceName, String tag) {
		StringBuilder imageCleanCommand = new StringBuilder(getDockerCommand());
		imageCleanCommand.append(REMOVE_IMAGE).append(SPACE)
				.append(getBuildImageNameWithTag(appName, serviceName, tag));
		return imageCleanCommand.toString();
	}

	public String getImageInspectCommand(String imageFullPath) {
		imageFullPath = normalize(imageFullPath);
		StringBuilder inspectCommand = new StringBuilder(getDockerCommand());
		inspectCommand.append(INSPECT_COMMAND).append(SPACE).append(imageFullPath);
		return inspectCommand.toString();
	}

	private String getDockerCommand() {
		if (USE_SUDO) {
			return SUDO_COMMAND + SPACE + DOCKER_COMMAND + SPACE;
		}
		return DOCKER_COMMAND + SPACE;
	}

	private String normalize(String input) {
		input = input.replaceAll(" ", "");
		input = input.toLowerCase();
		return input;
	}
}
