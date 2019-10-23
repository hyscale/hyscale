package io.hyscale.generator.services.generator;

import io.hyscale.commons.utils.NormalizationUtil;
import io.hyscale.generator.services.constants.ManifestGenConstants;

public class K8sResourceNameGenerator {

	public static String getResourceVolumeName(String prefix, String kind) {
		return NormalizationUtil.normalize(prefix + ManifestGenConstants.NAME_DELIMITER + kind);
	}

}
