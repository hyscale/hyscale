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
package io.hyscale.dockerfile.gen.services.persist;

import java.io.File;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.commons.models.Status;
import io.hyscale.commons.utils.HyscaleFilesUtil;
import io.hyscale.dockerfile.gen.services.model.DockerfileGenContext;
import io.hyscale.dockerfile.gen.core.models.DockerfileActivity;
import io.hyscale.dockerfile.gen.core.models.DockerfileContent;
import io.hyscale.commons.models.FileSpec;
import io.hyscale.commons.models.SupportingFile;
import io.hyscale.dockerfile.gen.services.config.DockerfileGenConfig;

@Component
public class LocalPersistenceService extends DockerfilePersistenceService {

	private static final Logger logger = LoggerFactory.getLogger(LocalPersistenceService.class);

	@Autowired
	private DockerfileGenConfig dockerfileGenConfig;

	@Autowired
	private HyscaleFilesUtil filesUtil;

	@Override
	protected boolean copySupportingFiles(List<SupportingFile> supportingFiles, DockerfileGenContext context) {
		if (supportingFiles == null || supportingFiles.isEmpty()) {
			return true;
		}
		WorkflowLogger.startActivity(DockerfileActivity.SUPPORT_FILES_COPY);
		String appName = context.getAppName();
		String serviceName = context.getServiceName();
		String parentDir = dockerfileGenConfig.getDockerFileParentDir(appName, serviceName);
		boolean isSuccess = supportingFiles.stream().allMatch(each -> {
			String dir = parentDir.concat(each.getRelativePath() != null ? each.getRelativePath() : "");
			File file = each.getFile();
			if (file == null) {
				// Create File in dir
				FileSpec fileSpec = each.getFileSpec();
				if (fileSpec == null) {
					return false;
				}
				try {
					filesUtil.createFile(dir + fileSpec.getName(), fileSpec.getContent());
				} catch (HyscaleException e) {
					logger.error("Failed to create support file {} in directory {}", fileSpec.getName(), dir);
					return false;
				}
				return true;
			}
			// Copy file to dir
			try {
				filesUtil.copyFileToDir(file, new File(dir));
			} catch (HyscaleException e) {
				logger.error("Failed to copy support file {} to directory {}, error {}", file.getName(), dir,
						e.toString());
				return false;
			}
			return true;
		});
		if (isSuccess) {
			WorkflowLogger.endActivity(Status.DONE);
		} else {
			WorkflowLogger.endActivity(Status.FAILED);
		}
		return isSuccess;
	}

	@Override
	protected boolean persist(DockerfileContent dockerfileContent, DockerfileGenContext context) {
		WorkflowLogger.startActivity(DockerfileActivity.DOCKERFILE_GENERATION);
		String appName = context.getAppName();
		String serviceName = context.getServiceName();
		String filename = dockerfileGenConfig.getDockerFileDir(appName, serviceName);

		try {
			filesUtil.createFile(filename, dockerfileContent.getContent());
		} catch (HyscaleException e) {
			logger.error("Failed to persist dockerfile, error {}", e.toString());
			WorkflowLogger.endActivity(Status.FAILED);
			return false;
		}
		WorkflowLogger.endActivity(Status.DONE);
		return true;
	}

}
