package io.hyscale.generator.services.generator;

import java.util.List;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.models.Manifest;
import io.hyscale.commons.models.ManifestContext;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;

/**
 * Interface to generate kubernetes manifests from the service spec
 * <p>Implementation Notes</p>
 * Implementations to this interface should generate a kubernets manifest
 * @see {@link Manifest}
 *
 */

public interface ManifestGenerator {

    /**
     * Generates the manifest @see {@link Manifest} from the given service spec
     *
     * @param serviceSpec servicespec
     * @param context     consist of parameters which control the manifest generation
     * @throws HyscaleException
     * @return List<Manifest>
     */
    public List<Manifest> generate(ServiceSpec serviceSpec, ManifestContext context) throws HyscaleException;
}
