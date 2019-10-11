package io.hyscale.ctl.dockerfile.gen.services.predicates;

import io.hyscale.ctl.commons.exception.HyscaleException;
import io.hyscale.ctl.servicespec.commons.model.service.BuildSpec;
import io.hyscale.ctl.servicespec.commons.model.service.Dockerfile;
import io.hyscale.ctl.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.ctl.servicespec.commons.model.service.ServiceSpec;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.function.Predicate;

@Component
public class DockerfileGenPredicates {

	private static final Logger logger = LoggerFactory.getLogger(DockerfileGenPredicates.class);

	public Predicate<ServiceSpec> skipDockerfileGen() {
		return serviceSpec -> {
			if (serviceSpec == null) {
				return false;
			}
			Dockerfile userDockerfile = null;
			BuildSpec buildSpec = null;
			try {
				userDockerfile = serviceSpec.get(
						HyscaleSpecFields.getPath(HyscaleSpecFields.image, HyscaleSpecFields.dockerfile),
						Dockerfile.class);
				buildSpec = serviceSpec.get(
						HyscaleSpecFields.getPath(HyscaleSpecFields.image, HyscaleSpecFields.buildSpec),
						BuildSpec.class);
			} catch (HyscaleException e) {
				logger.error("Error while fetching dockerfile from  image {}", e);
			}
			if (userDockerfile == null && buildSpec == null) {
				return true;
			}

			if (userDockerfile != null) {
				return true;
			}

			if (stackAsServiceImage().test(buildSpec)) {
				return true;
			}

			return false;
		};
	}

	public Predicate<Dockerfile> haveDockerfile() {
		return userDockerfile -> {
			if (userDockerfile == null) {
				return true;
			}
			return false;
		};
	}

	public Predicate<BuildSpec> stackAsServiceImage() {
		return buildSpec -> {
			if (buildSpec == null) {
				return false;
			}
			if (!haveArtifacts().test(buildSpec) && !haveConfigCommands().test(buildSpec)
					&& !haveConfigScript().test(buildSpec) && !haveRunScript().test(buildSpec)
					&& !haveRunCommands().test(buildSpec)) {
				return true;
			}
			return false;
		};
	}

	/**
	 * @return true if artifacts exist in buildspec
	 */

	public Predicate<BuildSpec> haveArtifacts() {
		return buildSpec -> {
			if (buildSpec == null) {
				return false;
			}
			return buildSpec.getArtifacts() != null && !(buildSpec.getArtifacts().isEmpty());
		};
	}

	public Predicate<BuildSpec> haveConfigCommands() {
		return buildSpec -> {
			if (buildSpec == null) {
				return false;
			}
			return !StringUtils.isBlank(buildSpec.getConfigCommands());
		};
	}

	public Predicate<BuildSpec> haveRunCommands() {
		return buildSpec -> {
			if (buildSpec == null) {
				return false;
			}
			return !StringUtils.isBlank(buildSpec.getRunCommands());
		};
	}

	public Predicate<BuildSpec> haveConfigScript() {
		return buildSpec -> {
			if (buildSpec == null) {
				return false;
			}
			return !StringUtils.isBlank(buildSpec.getConfigCommandsScript());
		};
	}

	public Predicate<BuildSpec> haveRunScript() {
		return buildSpec -> {
			if (buildSpec == null) {
				return false;
			}
			return !StringUtils.isBlank(buildSpec.getRunCommandsScript());
		};
	}
}
