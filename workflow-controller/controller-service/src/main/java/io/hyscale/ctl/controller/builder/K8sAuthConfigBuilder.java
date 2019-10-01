package io.hyscale.ctl.controller.builder;

import java.io.File;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import io.hyscale.ctl.commons.models.AuthConfig;
import io.hyscale.ctl.commons.models.K8sConfigFileAuth;
import io.hyscale.ctl.controller.config.ControllerConfig;

@Component
public class K8sAuthConfigBuilder {

    @Autowired
    private ControllerConfig controllerConfig;

    public AuthConfig getAuthConfig() {
        K8sConfigFileAuth k8sAuth = new K8sConfigFileAuth();
        k8sAuth.setK8sConfigFile(new File(controllerConfig.getDefaultKubeConf()));
        return k8sAuth;
    }
}
