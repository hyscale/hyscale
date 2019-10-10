package io.hyscale.ctl.generator.services.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.hyscale.ctl.commons.config.SetupConfig;

@Component
public class ManifestConfig {

	@Autowired
	private SetupConfig setupConfig;

	private static final String manifestDir = "manifests";

	public String getManifestDir(String appName, String serviceName) {
		StringBuilder sb = new StringBuilder();
		sb.append(setupConfig.getGeneratedFilesDir(appName, serviceName)).append(manifestDir)
				.append(SetupConfig.FILE_SEPARATOR);
		return sb.toString();
	}

}
