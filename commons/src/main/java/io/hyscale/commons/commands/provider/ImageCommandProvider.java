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

import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import io.hyscale.commons.config.SetupConfig;
import io.hyscale.commons.constants.ToolConstants;

@Component
public class ImageCommandProvider {

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
	private static final String LABEL_ARGS = "label";
	private static final String IMAGE_OWNER = "imageowner";
	private static final String HYSCALE = "hyscale";
	private static final String HYPHEN = "-";
	private static final String FORCE_FLAG = "f";
	private static final String QUIET = "q";
	private static final String FILTER = "filter";
	private static final boolean USE_SUDO = false;
	private static final String TARGET = "target";

    public String getBuildImageName(String appName, String serviceName) {
        StringBuilder sb = new StringBuilder();
        sb.append(HYSCALE_IO_URL).append(SLASH).append(appName).append(SLASH).append(serviceName);
        return normalize(sb.toString());
    }

    public String getBuildImageNameWithTag(String appName, String serviceName, String tag) {
        StringBuilder sb = new StringBuilder();
        sb.append(HYSCALE_IO_URL).append(SLASH).append(appName).append(SLASH).append(serviceName);
        if (StringUtils.isNotBlank(tag)) {
            sb.append(ToolConstants.COLON).append(tag);
        }
        return normalize(sb.toString());
    }

    public String dockerBuildCommand(String appName, String serviceName, String tag, String dockerFilePath) {
        return dockerBuildCommand(appName, serviceName, tag, dockerFilePath, null, null);
    }

    // --label “imageowner=hyscale"
    public String dockerBuildCommand(String appName, String serviceName, String tag, String dockerFilePath,String target,
                                     Map<String, String> buildArgs) {
		StringBuilder buildCommand = new StringBuilder();
		buildCommand.append(DOCKER_BUILD);
		if (target != null) {
			buildCommand.append(SPACE).append(HYPHEN).append(HYPHEN).append(TARGET);
		}
		buildCommand.append(SPACE).append(HYPHEN).append(HYPHEN).append(LABEL_ARGS).append(SPACE).append(IMAGE_OWNER)
				.append(EQUALS).append(HYSCALE);

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

    public String dockerVersion() {
        return docker() + VERSION_COMMAND;
    }

    public String dockerImages() {
        return docker() + IMAGES;
    }

    public String dockerPush(String imageFullPath) {
        imageFullPath = normalize(imageFullPath);
        StringBuilder pushCommand = new StringBuilder(docker());
        pushCommand.append(PUSH_COMMAND).append(SPACE).append(imageFullPath);
        return pushCommand.toString();
    }

    public String dockerTag(String sourcePath, String targetPath) {
        sourcePath = normalize(sourcePath);
        targetPath = normalize(targetPath);
        StringBuilder tagCommand = new StringBuilder(docker());
        tagCommand.append(TAG_COMMAND).append(SPACE).append(sourcePath).append(SPACE).append(targetPath);

        return tagCommand.toString();
    }

    public String dockerPull(String imageName) {
        if (StringUtils.isBlank(imageName)) {
            return null;
        }
        imageName = normalize(imageName);
        StringBuilder imagePullCmd = new StringBuilder(docker());
        imagePullCmd.append(PULL_COMMAND).append(SPACE).append(imageName);
        return imagePullCmd.toString();
    }

    public String getImageCleanUpCommand(String appName, String serviceName, String tag) {
        StringBuilder imageCleanCommand = new StringBuilder(docker());
        imageCleanCommand.append(REMOVE_IMAGE).append(SPACE)
                .append(getBuildImageNameWithTag(appName, serviceName, tag));
        return imageCleanCommand.toString();
    }

    public String dockerInspect(String imageFullPath) {
        imageFullPath = normalize(imageFullPath);
        StringBuilder inspectCommand = new StringBuilder(docker());
        inspectCommand.append(INSPECT_COMMAND).append(SPACE).append(imageFullPath);
        return inspectCommand.toString();
    }

    private String docker() {
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


    // docker rmi <id1> <id2> <id3>
    public String removeDockerImages(Set<String> imageIds) {
        if (imageIds == null || imageIds.isEmpty()) {
            return null;
        }
        StringBuilder removeDockerImages = new StringBuilder(docker());
        removeDockerImages.append(REMOVE_IMAGE).append(SPACE).append(HYPHEN).append(FORCE_FLAG);
        for (String imageId : imageIds) {
            removeDockerImages.append(SPACE).append(imageId);
        }
        return removeDockerImages.toString();
    }

    // docker rmi <id1> <id2> <id3>
    public String removeDockerImages(String[] imageIds) {
        if (imageIds == null || imageIds.length == 0) {
            return null;
        }
        StringBuilder removeDockerImages = new StringBuilder(docker());
        removeDockerImages.append(REMOVE_IMAGE).append(SPACE).append(HYPHEN).append(FORCE_FLAG);
        for (String imageId : imageIds) {
            removeDockerImages.append(SPACE).append(imageId);
        }
        return removeDockerImages.toString();
    }

    // docker images --filter label=imageowner=hyscale -q
    public String dockerImagesFilterByImageOwner() {
        StringBuilder sb = new StringBuilder(dockerImages());
        return sb.append(filter(IMAGE_OWNER, HYSCALE)).append(quiet()).toString();
    }

    // docker images <imagename> --filter label=imageowner=hyscale -q
    public String dockerImageByNameFilterByImageOwner(String imageName) {
        StringBuilder sb = new StringBuilder(dockerImages());
        return sb.append(SPACE).append(imageName).append(filter(IMAGE_OWNER, HYSCALE)).append(quiet()).toString();
    }

    // --filter label=key=value
    private StringBuilder filter(String key, String value) {
        return new StringBuilder().append(SPACE).append(HYPHEN).append(HYPHEN).append(FILTER).append(SPACE).append(LABEL_ARGS)
                .append(EQUALS).append(key).append(EQUALS).append(value);
    }

    // -q
    private StringBuilder quiet() {
        return new StringBuilder().append(SPACE).append(HYPHEN).append(QUIET);
    }
}
