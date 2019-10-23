package io.hyscale.controller.manager;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.models.ImageRegistry;

/**
 * Manage docker registry
 *
 */
public interface RegistryManager {

	public ImageRegistry getImageRegistry(String registry) throws HyscaleException;

}
