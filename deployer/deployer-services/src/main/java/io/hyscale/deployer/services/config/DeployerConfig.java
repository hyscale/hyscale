package io.hyscale.deployer.services.config;

import io.hyscale.commons.config.SetupConfig;
import io.hyscale.deployer.services.model.ResourceUpdatePolicy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * Deployment related config properties
 *
 */
@Component
@PropertySource("classpath:config/deployer-config.props")
public class DeployerConfig {

	private static final String DEPLOY_LOG = "deploy.log";

	private static final String SERVICE_LOG = "service.log";

	@Value(("${hyscale.ctl.k8s.resource.update.policy:PATCH}"))
	private String updatePolicyAsString;

	@Value(("${hyscale.ctl.k8s.pod.log.tail.lines:100}"))
	private int defaultTailLines;

	@Autowired
	private SetupConfig setupConfig;

	private ResourceUpdatePolicy updatePolicy;

	@PostConstruct
	public void init() {
		updatePolicy = ResourceUpdatePolicy.valueOf(updatePolicyAsString);
	}

	public ResourceUpdatePolicy getUpdatePolicy() {
		return updatePolicy;
	}

	public int getDefaultTailLines() {
		return defaultTailLines;
	}

	/**
	 * @param appName
	 * @param serviceName
	 * @return deploy logs directory
	 */
	public String getDeployLogDir(String appName, String serviceName) {
		StringBuilder sb = new StringBuilder(setupConfig.getLogsDir(appName, serviceName));
		sb.append(DEPLOY_LOG);
		return sb.toString();
	}

	/**
	 * 
	 * @param appName
	 * @param serviceName
	 * @return service logs directory
	 */
	public String getServiceLogDir(String appName, String serviceName) {
		StringBuilder sb = new StringBuilder(setupConfig.getLogsDir(appName, serviceName));
		sb.append(SERVICE_LOG);
		return sb.toString();
	}

}
