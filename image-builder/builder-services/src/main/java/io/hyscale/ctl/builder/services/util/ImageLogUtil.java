package io.hyscale.ctl.builder.services.util;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.hyscale.ctl.builder.services.config.ImageBuilderConfig;
import io.hyscale.ctl.builder.core.models.BuildContext;
import io.hyscale.ctl.builder.core.models.ImageBuilderActivity;
import io.hyscale.ctl.commons.logger.WorkflowLogger;
import io.hyscale.ctl.builder.services.handler.BuildLogHandler;
import io.hyscale.ctl.commons.utils.LogProcessor;
import io.hyscale.ctl.builder.services.handler.PushLogHandler;
import io.hyscale.ctl.commons.utils.TailLogFile;
import io.hyscale.ctl.builder.services.constants.DockerImageConstants;

@Component
public class ImageLogUtil {

	@Autowired
	private LogProcessor processLogFile;

	private static final Logger logger = LoggerFactory.getLogger(ImageLogUtil.class);


	@Autowired
	private ImageBuilderConfig imageBuilderConfig;

	public void handleLogs(BuildContext context) {
		if (context.isTail()) {
			tailLogs(context);
		} else {
			readLogs(context);
		}
	}

	public void tailLogs(BuildContext context) {
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
	public void readLogs(BuildContext context) {
		String appName = context.getAppName();
		String serviceName = context.getServiceName();

		// build logs
		readBuildLogs(appName, serviceName);
		// push logs
		readPushLogs(appName, serviceName);
	}

	public void readBuildLogs(String appName, String serviceName) {
		File buildLogFile = new File(imageBuilderConfig.getDockerBuildlog(appName, serviceName));
		boolean fileExists = buildLogFile.exists();
		if (fileExists) {
			WorkflowLogger.header(ImageBuilderActivity.BUILD_LOGS);
			processLogFile.readLogFile(buildLogFile, System.out);
			logger.debug("Reading Build logs for app {} and service {}",appName,serviceName);
		}
	}

	public void readPushLogs(String appName, String serviceName) {
		File pushLogFile = new File(imageBuilderConfig.getDockerPushLogDir(appName, serviceName));
		boolean pushLogExists = pushLogFile.exists();
		if (pushLogExists) {
			WorkflowLogger.header(ImageBuilderActivity.IMAGE_PUSH_LOG);
			processLogFile.readLogFile(pushLogFile, System.out);
			logger.debug("Reading push logs for app {} and service {}",appName,serviceName);
		}
	}

	public TailLogFile tailBuildLogs(String appName, String serviceName) {
		BuildLogHandler buildLogHandler = new BuildLogHandler();
		File logFile = new File(imageBuilderConfig.getDockerBuildlog(appName, serviceName));
		boolean fileExists = logFile.exists();
		if (fileExists) {
			WorkflowLogger.header(ImageBuilderActivity.BUILD_LOGS);
		}
		return processLogFile.tailLogFile(logFile, buildLogHandler);
	}

	public TailLogFile tailPushLogs(String appName, String serviceName) {
		PushLogHandler pushLogHandler = new PushLogHandler();
		File logFile = new File(imageBuilderConfig.getDockerPushLogDir(appName, serviceName));
		boolean fileExists = logFile.exists();
		if (fileExists) {
			WorkflowLogger.header(ImageBuilderActivity.IMAGE_PUSH_LOG);
		}
		return processLogFile.tailLogFile(logFile, pushLogHandler);
	}
}
