package io.hyscale.controller.builder;

import java.io.File;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import io.hyscale.commons.models.AuthConfig;
import io.hyscale.commons.models.K8sConfigFileAuth;
import io.hyscale.controller.config.ControllerConfig;

/**
 *
 *  Prepares the authorisation config @see {@link AuthConfig }
 *  for kubernetes cluster.
 *
 */

@Component
public class K8sAuthConfigBuilder {

    @Autowired
    private ControllerConfig controllerConfig;

    /**
     * gets {@link K8sConfigFileAuth} from {@link ControllerConfig} default config
     * @return {@link K8sConfigFileAuth}
     */
    public AuthConfig getAuthConfig() {
        K8sConfigFileAuth k8sAuth = new K8sConfigFileAuth();
        k8sAuth.setK8sConfigFile(new File(controllerConfig.getDefaultKubeConf()));
        return k8sAuth;
    }
}
