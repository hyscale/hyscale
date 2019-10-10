package io.hyscale.ctl.dockerfile.gen.services.manager;

import java.util.List;

import io.hyscale.ctl.commons.config.SetupConfig;
import io.hyscale.ctl.commons.exception.HyscaleException;
import io.hyscale.ctl.dockerfile.gen.services.model.DockerfileGenContext;
import io.hyscale.ctl.commons.models.SupportingFile;
import io.hyscale.ctl.servicespec.commons.model.service.ServiceSpec;
import org.apache.commons.lang3.StringUtils;

public interface DockerfileEntityManager {

	public List<SupportingFile> getSupportingFiles(ServiceSpec serviceSpec, DockerfileGenContext context)
			throws HyscaleException;

}
