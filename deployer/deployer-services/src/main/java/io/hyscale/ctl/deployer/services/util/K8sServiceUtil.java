package io.hyscale.ctl.deployer.services.util;

import java.util.ArrayList;
import java.util.List;

import io.hyscale.ctl.deployer.services.model.ServiceAddress;
import io.kubernetes.client.models.V1LoadBalancerIngress;
import io.kubernetes.client.models.V1Service;
import io.kubernetes.client.models.V1ServicePort;

/**
 * Utility to process information from {@link V1Service}
 *
 */
public class K8sServiceUtil {

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
	    return null;
	}
	List<V1ServicePort> v1ServicePorts = service.getSpec().getPorts();
	if (v1ServicePorts == null || v1ServicePorts.isEmpty()) {
	    return null;
	}
	List<Integer> portsList = new ArrayList<Integer>();

	v1ServicePorts.forEach(each -> {
	    if (each != null && each.getPort() != null) {
		portsList.add(each.getPort());
	    }
	});
	return portsList;
    }
}
