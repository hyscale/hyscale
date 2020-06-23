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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.hyscale.commons.config.SetupConfig;
import io.hyscale.commons.constants.ToolConstants;
import io.hyscale.commons.utils.ImageMetadataProvider;
import io.hyscale.commons.utils.NormalizationUtil;

@Component
public class ImageCommandProvider {

    public static final String IMAGE_OWNER = "imageowner";
    public static final String HYSCALE = "hyscale";
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
	private static final String HYPHEN = "-";
	private static final String FORCE_FLAG = "f";
	private static final String QUIET = "q";
	private static final String FILTER = "--filter";
	private static final boolean USE_SUDO = false;
	private static final String TARGET = "target";
	
	@Autowired
	private ImageMetadataProvider imageMetadataProvider;

    public String dockerBuildCommand(String appName, String serviceName, String tag, String dockerFilePath) {
        return dockerBuildCommand(appName, serviceName, tag, dockerFilePath, null, null);
    }

    // --label “imageowner=hyscale"
    public String dockerBuildCommand(String appName, String serviceName, String tag, String dockerFilePath,String target,
                                     Map<String, String> buildArgs) {
		StringBuilder buildCommand = new StringBuilder();
		buildCommand.append(DOCKER_BUILD);
		if (target != null) {
			buildCommand.append(SPACE).append(HYPHEN).append(HYPHEN).append(TARGET).append(SPACE).append(target);
		}
		buildCommand.append(SPACE).append(HYPHEN).append(HYPHEN).append(LABEL_ARGS).append(SPACE).append(IMAGE_OWNER)
				.append(EQUALS).append(HYSCALE);

        if (buildArgs != null && !buildArgs.isEmpty()) {
            buildCommand.append(getBuildArgs(buildArgs));
        }
        buildCommand.append(TAG_ARG);
        buildCommand.append(imageMetadataProvider.getBuildImageNameWithTag(appName, serviceName, tag));
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
        imageFullPath = NormalizationUtil.normalizeImageName(imageFullPath);
        StringBuilder pushCommand = new StringBuilder(docker());
        pushCommand.append(PUSH_COMMAND).append(SPACE).append(imageFullPath);
        return pushCommand.toString();
    }

    public String dockerTag(String sourcePath, String targetPath) {
        sourcePath = NormalizationUtil.normalizeImageName(sourcePath);
        targetPath = NormalizationUtil.normalizeImageName(targetPath);
        StringBuilder tagCommand = new StringBuilder(docker());
        tagCommand.append(TAG_COMMAND).append(SPACE).append(sourcePath).append(SPACE).append(targetPath);

        return tagCommand.toString();
    }

    public String dockerPull(String imageName) {
        if (StringUtils.isBlank(imageName)) {
            return null;
        }
        imageName = NormalizationUtil.normalizeImageName(imageName);
        StringBuilder imagePullCmd = new StringBuilder(docker());
        imagePullCmd.append(PULL_COMMAND).append(SPACE).append(imageName);
        return imagePullCmd.toString();
    }

    public String getImageCleanUpCommand(String appName, String serviceName, String tag) {
        StringBuilder imageCleanCommand = new StringBuilder(docker());
        imageCleanCommand.append(REMOVE_IMAGE).append(SPACE)
                .append(imageMetadataProvider.getBuildImageNameWithTag(appName, serviceName, tag));
        return imageCleanCommand.toString();
    }

    public String dockerInspect(String imageFullPath) {
        imageFullPath = NormalizationUtil.normalizeImageName(imageFullPath);
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

    // docker rmi <id1> <id2> <id3>
    public String removeDockerImages(Set<String> imageIds, boolean force) {
        if (imageIds == null || imageIds.isEmpty()) {
            return null;
        }
        StringBuilder removeDockerImages = new StringBuilder(docker());
        removeDockerImages.append(REMOVE_IMAGE);
        if (force) {
            removeDockerImages.append(SPACE).append(HYPHEN).append(FORCE_FLAG);
        }
        imageIds.stream().forEach( imageId -> {
            removeDockerImages.append(SPACE).append(imageId);
        });
        return removeDockerImages.toString();
    }

    // docker rmi <id1> <id2> <id3>
    public String removeDockerImages(String[] imageIds, boolean force) {
        if (imageIds == null || imageIds.length == 0) {
            return null;
        }
        return removeDockerImages(new HashSet<>(Arrays.asList(imageIds)), force);
    }

    // docker images --filter label=imageowner=hyscale -q
    public String dockerImagesFilterByImageOwner() {
        return dockerImageIds(null, imageMetadataProvider.getImageOwnerLabel());
    }

    // docker images <imagename> --filter label=imageowner=hyscale -q
    public String dockerImageByNameFilterByImageOwner(String imageName) {
        return dockerImageIds(imageName, imageMetadataProvider.getImageOwnerLabel());
    }
    
    public String dockerImageIds(String imageName, Map<String, String> labels) {
        StringBuilder sb = new StringBuilder(dockerImages());
        if (imageName != null) {
            sb.append(SPACE).append(imageName);
        }
        if (labels != null && !labels.isEmpty()) {
            sb.append(labelFilter(labels));
        }
        return sb.append(quiet()).toString();
    }
    
    // --filter label=key1=value1 --filter label=key2=value2 etc
    private String labelFilter(Map<String, String> labels) {
        if (labels == null || labels.isEmpty()) {
            return null;
        }
        StringBuilder labelFilter = new StringBuilder();
        labels.entrySet().stream().forEach(each -> {
            labelFilter.append(SPACE).append(FILTER).append(SPACE).append(LABEL_ARGS)
            .append(EQUALS).append(each.getKey()).append(EQUALS).append(each.getValue());
        });
        return labelFilter.toString();
    }

    // -q
    private StringBuilder quiet() {
        return new StringBuilder().append(SPACE).append(HYPHEN).append(QUIET);
    }
}
