package io.hyscale.ctl.generator.services.plugins;

import io.hyscale.ctl.plugin.framework.annotation.ManifestPlugin;
import io.hyscale.ctl.commons.exception.HyscaleException;
import io.hyscale.ctl.commons.models.K8sServiceType;
import io.hyscale.ctl.commons.models.ManifestContext;
import io.hyscale.ctl.generator.services.model.ManifestResource;
import io.hyscale.ctl.plugin.framework.handler.ManifestHandler;
import io.hyscale.ctl.plugin.framework.models.ManifestSnippet;
import io.hyscale.ctl.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.ctl.servicespec.commons.model.service.ServiceSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ManifestPlugin(name = "ServiceTypeHandler")
public class ServiceTypeHandler implements ManifestHandler {
    private static final Logger logger = LoggerFactory.getLogger(ServiceTypeHandler.class);

    @Override
    public List<ManifestSnippet> handle(ServiceSpec serviceSpec, ManifestContext manifestContext)
            throws HyscaleException {
        List<ManifestSnippet> manifestSnippetList = new ArrayList<>();
        if (ManifestResource.SERVICE.getPredicate().test(serviceSpec)) {
            ManifestSnippet serviceTypeSnippet = new ManifestSnippet();
            serviceTypeSnippet.setKind(ManifestResource.SERVICE.getKind());
            serviceTypeSnippet.setPath("spec.type");

            Boolean external = serviceSpec.get(HyscaleSpecFields.external, Boolean.class);
            K8sServiceType serviceType = getServiceType(external == null ? false : external);
            String serviceTypeName = serviceType != null ? serviceType.name() : K8sServiceType.ClusterIP.name();
            logger.debug("Processing serviceType {}.",serviceTypeName);
            serviceTypeSnippet.setSnippet(serviceTypeName);
            manifestSnippetList.add(serviceTypeSnippet);
        }
        return manifestSnippetList;
    }

    private K8sServiceType getServiceType(Boolean external) {

        if (external) {
            if (checkForLoadBalancerType()) {
                return K8sServiceType.LoadBalancer;
            } else {
                return K8sServiceType.NodePort;
            }
        } else {
            return K8sServiceType.ClusterIP;
        }
    }

    private boolean checkForLoadBalancerType() {
        return true;
    }

}
