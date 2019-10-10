package io.hyscale.ctl.generator.services.generator.impl;

import java.util.List;

import io.hyscale.ctl.generator.services.generator.ManifestGenerator;
import io.hyscale.ctl.generator.services.processor.PluginProcessor;
import io.hyscale.ctl.servicespec.commons.model.service.ServiceSpec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.hyscale.ctl.commons.exception.HyscaleException;
import io.hyscale.ctl.commons.models.Manifest;
import io.hyscale.ctl.commons.models.ManifestContext;

@Component
public class K8sManifestGeneratorImpl implements ManifestGenerator {

	@Autowired
	private PluginProcessor pluginProcessor;

	@Override
	public List<Manifest> generate(ServiceSpec serviceSpec, ManifestContext context) throws HyscaleException {

		return pluginProcessor.getManifests(serviceSpec, context);
	}
}
