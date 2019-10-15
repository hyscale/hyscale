package io.hyscale.ctl.plugin.framework.handler;

import io.hyscale.ctl.commons.exception.HyscaleException;
import io.hyscale.ctl.commons.models.ManifestContext;
import io.hyscale.ctl.plugin.framework.models.ManifestSnippet;
import io.hyscale.ctl.servicespec.commons.model.service.ServiceSpec;

import java.util.List;

public interface ManifestHandler {

	List<ManifestSnippet> handle(ServiceSpec serviceSpec, ManifestContext manifestContext) throws HyscaleException;
}
