package io.hyscale.ctl.deployer.service.model;


import io.hyscale.ctl.commons.models.K8sAuthType;
import io.hyscale.ctl.commons.models.K8sAuthorisation;
import io.kubernetes.client.util.KubeConfig;

public class K8sKubeConfigAuth implements K8sAuthorisation {

    private KubeConfig kubeConfig;

    public KubeConfig getKubeConfig() { return kubeConfig; }

    public void setKubeConfig(KubeConfig kubeConfig) { this.kubeConfig = kubeConfig; }

    @Override
    public K8sAuthType getK8sAuthType() {
        return K8sAuthType.KUBE_CONFIG_OBJECT;
    }

}
