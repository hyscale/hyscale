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
package io.hyscale.builder.services.util;

import java.io.File;
import java.io.OutputStream;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.io.LogProcessor;
import io.hyscale.commons.io.TailLogFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.hyscale.builder.services.config.ImageBuilderConfig;
import io.hyscale.builder.core.models.BuildContext;
import io.hyscale.builder.core.models.ImageBuilderActivity;
import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.commons.models.Activity;
import io.hyscale.builder.services.handler.BuildLogHandler;
import io.hyscale.builder.services.handler.PushLogHandler;
import io.hyscale.builder.services.constants.DockerImageConstants;

@Component
public class ImageLogUtil {

    private static final Logger logger = LoggerFactory.getLogger(ImageLogUtil.class);
    
    @SuppressWarnings("java:S106")
    private static final OutputStream SYSTEM_OUTPUT_STREAM = System.out;
    
	@Autowired
	private LogProcessor processLogFile;

	@Autowired
	private ImageBuilderConfig imageBuilderConfig;

	public void handleLogs(BuildContext context) throws HyscaleException{
		if (context.isTail()) {
			tailLogs(context);
		} else {
			readLogs(context);
		}
	}

	public void tailLogs(BuildContext context){
		String appName = context.getAppName();
		String serviceName = context.getServiceName();

		long startTime = System.currentTimeMillis();

		// build logs
		TailLogFile tailBuildLogs = tailBuildLogs(appName, serviceName);
		if (tailBuildLogs != null) {
			while (tailBuildLogs.isRunning()
					&& (System.currentTimeMillis() - startTime) < DockerImageConstants.TAIL_LOG_MAX_WAIT_TIME) {
				// waiting on build logs
			}
			logger.debug("Tailing Build logs for app {} and service {}",appName,serviceName);
			tailBuildLogs.stopRunning();
		}
		startTime = System.currentTimeMillis();
		// push logs
		TailLogFile tailPushLogs = tailPushLogs(appName, serviceName);
		if (tailPushLogs != null) {
			while (tailPushLogs.isRunning()
					&& (System.currentTimeMillis() - startTime) < DockerImageConstants.TAIL_LOG_MAX_WAIT_TIME) {
				// waiting on push logs
			}
			logger.debug("Tailing push logs for app {} and service {}",appName,serviceName);
			tailPushLogs.stopRunning();
		}
	}

	// Read build and push logs from file
	public void readLogs(BuildContext context) throws HyscaleException {
		String appName = context.getAppName();
		String serviceName = context.getServiceName();
		
		// build logs
		String logFilePath = imageBuilderConfig.getDockerBuildlog(appName, serviceName);
		readLogs(logFilePath, ImageBuilderActivity.BUILD_LOGS);
		
		// push logs
		logFilePath = imageBuilderConfig.getDockerPushLogDir(appName, serviceName);
        readLogs(logFilePath, ImageBuilderActivity.IMAGE_PUSH_LOG);
	}

    public void readLogs(String logFilePath, Activity activity) throws HyscaleException {
        if (logFilePath == null) {
            return;
        }
        File logFile = new File(logFilePath);
        if (logFile.exists() && logFile.isFile()) {
            WorkflowLogger.header(activity);
            logger.debug("Reading logs file {}", logFilePath);
            processLogFile.readLogFile(logFile, SYSTEM_OUTPUT_STREAM);
        }
    }
    
	public TailLogFile tailBuildLogs(String appName, String serviceName){
		BuildLogHandler buildLogHandler = new BuildLogHandler();
		File logFile = new File(imageBuilderConfig.getDockerBuildlog(appName, serviceName));
		boolean fileExists = logFile.exists();
		if (fileExists) {
			WorkflowLogger.header(ImageBuilderActivity.BUILD_LOGS);
		}
			return processLogFile.tailLogFile(logFile, buildLogHandler);
	}

	public TailLogFile tailPushLogs(String appName, String serviceName){
		PushLogHandler pushLogHandler = new PushLogHandler();
		File logFile = new File(imageBuilderConfig.getDockerPushLogDir(appName, serviceName));
		boolean fileExists = logFile.exists();
		if (fileExists) {
			WorkflowLogger.header(ImageBuilderActivity.IMAGE_PUSH_LOG);
		}
		return processLogFile.tailLogFile(logFile, pushLogHandler);
	}
}
