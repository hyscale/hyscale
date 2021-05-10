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

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.logger.ActivityContext;
import io.hyscale.commons.logger.WorkflowLogger;
import io.hyscale.commons.models.LBType;
import io.hyscale.commons.models.LoadBalancer;
import io.hyscale.commons.models.Status;
import io.hyscale.deployer.core.model.CustomResourceKind;
import io.hyscale.deployer.core.model.ResourceKind;
import io.hyscale.deployer.services.client.GenericK8sClient;
import io.hyscale.deployer.services.client.K8sResourceClient;
import io.hyscale.deployer.services.config.DeployerEnvConfig;
import io.hyscale.deployer.services.exception.DeployerErrorCodes;
import io.hyscale.deployer.services.model.CustomObject;
import io.hyscale.deployer.services.model.DeployerActivity;
import io.hyscale.deployer.services.model.ServiceAddress;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.models.V1LoadBalancerIngress;
import io.kubernetes.client.openapi.models.V1Service;
import io.kubernetes.client.openapi.models.V1ServicePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Utility to process information from {@link V1Service}
 *
 */
public class K8sServiceUtil {

	private static final Logger logger = LoggerFactory.getLogger(K8sServiceUtil.class);

	private static final String LOAD_BALANCER = "loadBalancer";

	private static final long LB_READY_STATE_TIME = DeployerEnvConfig.getLBReadyTimeout();
	private static final long MAX_LB_WAIT_TIME = 2000;

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


	public static ServiceAddress getLBServiceAddress(boolean wait, LoadBalancer loadBalancer, ApiClient apiClient, String lbSelector, String namespace) throws HyscaleException {
		if (!wait) {
			return getLBServiceAddress(loadBalancer, apiClient, lbSelector, namespace, true);
		}
		ServiceAddress serviceAddress = null;
		long startTime = System.currentTimeMillis();
		ActivityContext serviceIPContext = new ActivityContext(DeployerActivity.WAITING_FOR_SERVICE_IP);
		WorkflowLogger.startActivity(serviceIPContext);
		try {
			while (System.currentTimeMillis() - startTime < LB_READY_STATE_TIME) {
				WorkflowLogger.continueActivity(serviceIPContext);
				serviceAddress = getLBServiceAddress(loadBalancer, apiClient, lbSelector, namespace, false);
				if (serviceAddress != null && serviceAddress.getServiceIP() != null) {
					break;
				}
				Thread.sleep(MAX_LB_WAIT_TIME);
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			logger.error("Error while retrieving Load Balancer IP address for selector {} in namespace {}. Reason :", lbSelector, namespace, e);
		} catch (Exception e) {
			WorkflowLogger.endActivity(serviceIPContext, Status.FAILED);
			throw new HyscaleException(DeployerErrorCodes.FAILED_TO_GET_SERVICE_ADDRESS);
		}
		if (serviceAddress == null) {
			WorkflowLogger.endActivity(serviceIPContext, Status.FAILED);
			throw new HyscaleException(DeployerErrorCodes.FAILED_TO_GET_SERVICE_ADDRESS);
		}
		if (serviceAddress.getServiceIP() == null) {
			serviceAddress = getLBServiceAddress(loadBalancer, apiClient, lbSelector, namespace, true);
		}
		WorkflowLogger.endActivity(serviceIPContext, Status.DONE);
		return serviceAddress;
	}

	/**
	 *  Service Address details of the load balancer.
	 */
	public static ServiceAddress getLBServiceAddress(LoadBalancer loadBalancer, ApiClient apiClient, String lbSelector, String namespace, boolean setDefault) {
		LBType lbType = LBType.getByProvider(loadBalancer.getProvider());
		if (lbType != null) {
			ServiceAddress serviceAddress = new ServiceAddress();
			serviceAddress.setServiceIP(lbType.getServiceAddressPlaceHolder());
			serviceAddress.setServiceURL(loadBalancer.getHost());
			if (lbType.equals(LBType.INGRESS)) {
				return getIngressServiceAddress(loadBalancer, apiClient, lbSelector, namespace, setDefault);
			}
			return serviceAddress;
		}
		return null;
	}

	/**
	 *
	 * @param loadBalancer Load Balancer details.
	 * @param apiClient apiClient to access the cluster.
	 * @param ingressSelector Selector required to get the Ingress resource from the cluster.
	 * @param namespace namespace of the Ingress
	 * @param setDefaultIp if setDefaultIp is true Service IP will be the place holder, else it will  be the IP of Ingress resource in the cluster.
	 * @return ServiceAddress.
	 */
	public static ServiceAddress getIngressServiceAddress(LoadBalancer loadBalancer, ApiClient apiClient, String ingressSelector, String namespace, boolean setDefaultIp) {
		try {
			ServiceAddress serviceAddress = new ServiceAddress();
			serviceAddress.setServiceURL(loadBalancer.getHost());
			CustomResourceKind customResourceKind = new CustomResourceKind(ResourceKind.INGRESS.getKind(), ResourceKind.INGRESS.getApiVersion());
			GenericK8sClient genericK8sClient = new K8sResourceClient(apiClient).withNamespace(namespace).forKind(customResourceKind);
			List<CustomObject> customObjects = genericK8sClient.getBySelector(ingressSelector);
			CustomObject ingressResource = customObjects.get(0);
			if (ingressResource.get("status") != null) {
				JsonParser jsonParser = new JsonParser();
				JsonObject status = (JsonObject) jsonParser.parse(String.valueOf(ingressResource.get("status")));
				if (status.get(LOAD_BALANCER) != null && status.getAsJsonObject(LOAD_BALANCER).get("ingress") != null) {
					JsonObject ingress = status.getAsJsonObject(LOAD_BALANCER).getAsJsonArray("ingress").get(0).getAsJsonObject();
					serviceAddress.setServiceIP(ingress.get("ip").getAsString());
				}
			}
			if (serviceAddress.getServiceIP() == null && setDefaultIp) {
				LBType lbType = LBType.getByProvider(loadBalancer.getProvider());
				serviceAddress.setServiceIP(Objects.requireNonNull(lbType).getServiceAddressPlaceHolder());
			}
			return serviceAddress;
		} catch (Exception e) {
			logger.error("Error while retrieving Ingress IP address for selector {} in namespace {}. Reason :", ingressSelector, namespace, e);
		}
		return null;
	}

}
