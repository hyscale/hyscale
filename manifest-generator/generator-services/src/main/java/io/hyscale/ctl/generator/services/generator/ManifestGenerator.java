package io.hyscale.ctl.generator.services.generator;

import java.util.List;

import io.hyscale.ctl.commons.exception.HyscaleException;
import io.hyscale.ctl.commons.models.Manifest;
import io.hyscale.ctl.commons.models.ManifestContext;
import io.hyscale.ctl.servicespec.commons.model.service.ServiceSpec;

public interface ManifestGenerator {

	public List<Manifest> generate(ServiceSpec serviceSpec, ManifestContext context) throws HyscaleException;
}
