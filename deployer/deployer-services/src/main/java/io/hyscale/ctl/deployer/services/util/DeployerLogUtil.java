package io.hyscale.ctl.deployer.services.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.hyscale.ctl.commons.exception.HyscaleException;
import io.hyscale.ctl.commons.models.DeploymentContext;
import io.hyscale.ctl.commons.utils.LogProcessor;
import io.hyscale.ctl.deployer.services.deployer.Deployer;
import io.hyscale.ctl.deployer.services.exception.DeployerErrorCodes;
import io.hyscale.ctl.deployer.services.config.DeployerConfig;

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

	public void processLogs(DeploymentContext context) throws HyscaleException {

		if (context.isTailLogs()) {
			tailLogs(context);
		} else {
			readLogs(context);
		}

	}

	/**
	 * Gets logs from cluster and write them to log file
	 * Reads log file from directory to System out
	 * Ensures latest logs are present in the directory
	 * @param context
	 * @throws HyscaleException
	 */
	private void readLogs(DeploymentContext context) throws HyscaleException {
		String appName = context.getAppName();
		String serviceName = context.getServiceName();
		String logFile = deployerConfig.getServiceLogDir(appName, serviceName);
		context.setTailLogs(false);
		try (InputStream is = deployer.logs(context)) {
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
	 * @param context
	 * @throws HyscaleException
	 */
	private void tailLogs(DeploymentContext context) throws HyscaleException {
		try (InputStream is = deployer.logs(context)) {
			IOUtils.copy(is, System.out);
		} catch (IOException e) {
			HyscaleException ex = new HyscaleException(e, DeployerErrorCodes.FAILED_TO_GET_LOGS);
			throw ex;
		} catch (HyscaleException ex) {
			throw ex;
		}
	}
}
