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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import io.hyscale.deployer.services.config.DeployerConfig;
import io.hyscale.deployer.services.exception.DeployerErrorCodes;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.models.AuthConfig;
import io.hyscale.commons.utils.LogProcessor;
import io.hyscale.deployer.services.deployer.Deployer;

/**
 * Utility to handle deployment related logs
 *	
 */
@Component
public class DeployerLogUtil {

	private static final Logger logger = LoggerFactory.getLogger(DeployerLogUtil.class);

	@Autowired
	private DeployerConfig deployerConfig;

	@Autowired
	private Deployer deployer;

	@Autowired
	private LogProcessor logProcessor;

	public void processLogs(AuthConfig authConfig, String appName, String serviceName, 
	        String podName, String namespace, Integer readLines, boolean isTail) throws HyscaleException {

        if (isTail) {
			tailLogs(authConfig, serviceName, podName, namespace, readLines);
		} else {
			readLogs(authConfig, appName, serviceName, podName, namespace, readLines);
		}

	}

	/**
	 * Gets logs from cluster and write them to log file
     * Reads log file from directory to System out
     * Ensures latest logs are present in the directory
	 * @param authConfig
	 * @param namespace
	 * @param appName
	 * @param serviceName
	 * @param podName
	 * @param readLines
	 * @throws HyscaleException
	 */
	private void readLogs(AuthConfig authConfig, String appName, String serviceName, 
	        String podName, String namespace, Integer readLines) throws HyscaleException {
		String logFile = deployerConfig.getServiceLogDir(appName, serviceName);
        try (InputStream is = deployer.logs(authConfig, serviceName, namespace, 
		        podName, serviceName, readLines, false)) {
			logProcessor.writeLogFile(is, logFile);
			logProcessor.readLogFile(new File(logFile), System.out);
		} catch (IOException e) {
			logger.error("Failed to get deploy logs {}", serviceName, e);
			HyscaleException ex = new HyscaleException(e, DeployerErrorCodes.FAILED_TO_GET_LOGS);
			throw ex;
		} catch (HyscaleException ex) {
			throw ex;
		}
	}

	/**
	 * Channels cluster logs to System out
	 * @param authConfig
	 * @param namespace
	 * @param serviceName
	 * @param podName
	 * @param readLines
	 * @throws HyscaleException
	 */
	private void tailLogs(AuthConfig authConfig, String serviceName, String podName, 
	        String namespace, Integer readLines) throws HyscaleException {
		try (InputStream is = deployer.logs(authConfig, serviceName, namespace, 
                podName, serviceName, readLines, true)) {
			IOUtils.copy(is, System.out);
		} catch (IOException e) {
			HyscaleException ex = new HyscaleException(e, DeployerErrorCodes.FAILED_TO_GET_LOGS);
			throw ex;
		} catch (HyscaleException ex) {
			throw ex;
		}
	}
}
