package io.hyscale.ctl.controller.manager;

import io.hyscale.ctl.commons.exception.HyscaleException;
import io.hyscale.ctl.commons.models.ImageRegistry;

/**
 * Manage docker registry
 *
 */
public interface RegistryManager {

	public ImageRegistry getImageRegistry(String registry) throws HyscaleException;

}
