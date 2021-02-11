/**
 * Copyright 2019 Pramati Prism, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.hyscale.deployer.services.util;

import io.hyscale.deployer.core.model.CustomResourceKind;
import io.hyscale.deployer.services.client.GenericK8sClient;
import io.hyscale.deployer.services.client.K8sResourceClient;
import io.hyscale.deployer.services.model.CustomObject;
import io.hyscale.deployer.services.model.ServiceAddress;
import io.hyscale.generator.services.model.LBType;
import io.hyscale.generator.services.model.ManifestResource;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.models.V1LoadBalancerIngress;
import io.kubernetes.client.openapi.models.V1Service;
import io.kubernetes.client.openapi.models.V1ServicePort;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Utility to process information from {@link V1Service}
 *
 */
public class K8sServiceUtil {
    
    private K8sServiceUtil() {}

	public static ServiceAddress getServiceAddress(V1Service service) {
		if (service == null) {
			return null;
		}
		ServiceAddress serviceAddress = new ServiceAddress();
		V1LoadBalancerIngress loadBalancerIngress = getLoadBalancer(service);
		if (loadBalancerIngress != null) {
			String host = loadBalancerIngress.getIp() == null ? loadBalancerIngress.getHostname()
					: loadBalancerIngress.getIp();
			serviceAddress.setServiceIP(host);
		}
		List<Integer> ports = getPorts(service);
		serviceAddress.setPorts(ports);

		return serviceAddress;
	}

	public static V1LoadBalancerIngress getLoadBalancer(V1Service lbSvc) {
		V1LoadBalancerIngress loadBalancerIngress = null;
		if (lbSvc == null || lbSvc.getStatus() == null || lbSvc.getStatus().getLoadBalancer() == null) {
			return loadBalancerIngress;
		}
		List<V1LoadBalancerIngress> ingressList = lbSvc.getStatus().getLoadBalancer().getIngress();
		if (ingressList != null && !ingressList.isEmpty()) {
			loadBalancerIngress = ingressList.get(0);
		}
		return loadBalancerIngress;
	}

    public static List<Integer> getPorts(V1Service service) {
	if (service == null || service.getSpec() == null) {
	    return Collections.emptyList();
	}
	List<V1ServicePort> v1ServicePorts = service.getSpec().getPorts();
	if (v1ServicePorts == null || v1ServicePorts.isEmpty()) {
	    return Collections.emptyList();
	}
	List<Integer> portsList = new ArrayList<>();

	v1ServicePorts.forEach(each -> {
	    if (each != null && each.getPort() != null) {
		portsList.add(each.getPort());
	    }
	});
	return portsList;
    }

	public static ServiceAddress getLBServiceAddress(LBType lbType, ApiClient apiClient, String lbSelctor, String namespace) {
		ServiceAddress serviceAddress = new ServiceAddress();
		serviceAddress.setServiceIP(lbType.getServiceAddressPlaceHolder());
		if (lbType.equals(LBType.INGRESS)) {
			CustomResourceKind customResourceKind = new CustomResourceKind(ManifestResource.INGRESS.getKind(), ManifestResource.INGRESS.getApiVersion());
			GenericK8sClient genericK8sClient = new K8sResourceClient(apiClient).withNamespace(namespace)
					.forKind(customResourceKind);
			List<CustomObject> customObjects = genericK8sClient.getBySelector(lbSelctor);
			CustomObject ingressResource = customObjects.get(0);
			//set service address of Ingress resource (if not null).
		}
		return serviceAddress;
	}
}
