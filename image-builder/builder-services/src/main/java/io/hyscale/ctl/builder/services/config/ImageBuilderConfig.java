package io.hyscale.ctl.builder.services.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.hyscale.ctl.commons.config.SetupConfig;

@Component
public class ImageBuilderConfig {

	public static final String IMAGE_BUILDER_PROP = "hyscalctl.image.builder";
	private static final String PUSH_LOG = "push.log";
	private static final String BUILD_LOG = "build.log";
	@Autowired
	private SetupConfig setupConfig;

	public String getDockerBuildlog(String appName, String serviceName) {
		StringBuilder sb = new StringBuilder(setupConfig.getLogsDir(appName, serviceName));
		sb.append(BUILD_LOG);
		return sb.toString();
	}

	public String getDockerPushLogDir(String appName, String serviceName) {
		StringBuilder sb = new StringBuilder(setupConfig.getLogsDir(appName, serviceName));
		sb.append(PUSH_LOG);
		return sb.toString();
	}

}
