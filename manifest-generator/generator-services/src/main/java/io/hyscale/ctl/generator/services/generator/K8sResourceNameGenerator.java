package io.hyscale.ctl.generator.services.generator;

import io.hyscale.ctl.commons.utils.NormalizationUtil;
import io.hyscale.ctl.generator.services.constants.ManifestGenConstants;

public class K8sResourceNameGenerator {

	public static String getResourceVolumeName(String prefix, String kind) {
		return NormalizationUtil.normalize(prefix + ManifestGenConstants.NAME_DELIMITER + kind);
	}

}
