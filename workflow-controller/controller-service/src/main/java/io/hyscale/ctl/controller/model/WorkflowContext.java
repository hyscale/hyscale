package io.hyscale.ctl.controller.model;

import io.hyscale.ctl.commons.component.ComponentInvokerContext;
import io.hyscale.ctl.servicespec.commons.model.service.ServiceSpec;

import java.util.HashMap;
import java.util.Map;

/**
 * Context information for workflow controller
 *
 */
public class WorkflowContext extends ComponentInvokerContext {

    private ServiceSpec serviceSpec;
    private String namespace;
    private String appName;
    private String serviceName;
    private String envName;

    private Map<String, Object> attributes;

    public WorkflowContext() {
        attributes = new HashMap<>();
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    public void addAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getEnvName() {
        return envName;
    }

    public void setEnvName(String envName) {
        this.envName = envName;
    }

    public ServiceSpec getServiceSpec() {
        return serviceSpec;
    }

    public void setServiceSpec(ServiceSpec serviceSpec) {
        this.serviceSpec = serviceSpec;
    }


}
