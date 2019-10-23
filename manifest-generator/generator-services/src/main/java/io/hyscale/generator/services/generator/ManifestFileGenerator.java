package io.hyscale.generator.services.generator;

import java.io.File;

import io.hyscale.commons.models.YAMLManifest;
import io.hyscale.commons.utils.NormalizationUtil;
import io.hyscale.generator.services.config.ManifestConfig;
import io.hyscale.generator.services.constants.ManifestGenConstants;
import io.hyscale.generator.services.exception.ManifestErrorCodes;
import io.hyscale.plugin.framework.models.ManifestMeta;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.utils.HyscaleFilesUtil;

/**
 * Generate Manifest File with yaml
 *
 * @author tushart
 */
@Component
public class ManifestFileGenerator {

	@Autowired
	private HyscaleFilesUtil filesUtil;

	@Autowired
	private ManifestConfig manifestConfig;

	public YAMLManifest getYamlManifest(String manifestDir, String yaml, ManifestMeta manifestMeta)
			throws HyscaleException {
		if (StringUtils.isBlank(yaml) || StringUtils.isBlank(manifestDir)) {
			throw new HyscaleException(ManifestErrorCodes.ERROR_WHILE_WRITING_MANIFEST_TO_FILE, manifestDir);
		}
		StringBuilder sb = new StringBuilder(manifestDir);
		sb.append(getManifestFileName(manifestMeta));
		sb.append(ManifestGenConstants.YAML_EXTENSION);
		File manifestFile = filesUtil.createFile(sb.toString(), yaml);
		YAMLManifest yamlManifest = new YAMLManifest();
		yamlManifest.setYamlManifest(manifestFile);
		return yamlManifest;
	}

	private String getManifestFileName(ManifestMeta manifestMeta) {
		StringBuilder sb = new StringBuilder();
		if (!StringUtils.isBlank(manifestMeta.getIdentifier())) {
			sb.append(NormalizationUtil.normalize(manifestMeta.getIdentifier()));
			sb.append(ManifestGenConstants.NAME_DELIMITER);
		}
		sb.append(NormalizationUtil.normalize(manifestMeta.getKind()));
		return sb.toString();
	}

}
