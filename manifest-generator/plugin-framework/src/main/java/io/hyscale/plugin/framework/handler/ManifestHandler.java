package io.hyscale.plugin.framework.handler;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.models.ManifestContext;
import io.hyscale.plugin.framework.models.ManifestSnippet;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;

import java.util.List;

public interface ManifestHandler {

	List<ManifestSnippet> handle(ServiceSpec serviceSpec, ManifestContext manifestContext) throws HyscaleException;
}
