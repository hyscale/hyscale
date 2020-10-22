package io.hyscale.deployer.services.model;

import io.kubernetes.client.common.KubernetesObject;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 *  CustomObject is an implementation of KubernetesObject carrying
 *  context of any given k8s resource kind in a HashMap.
 */

public class CustomObject extends HashMap<String, Object> implements KubernetesObject {

    private static final Logger logger = LoggerFactory.getLogger(CustomObject.class);

    @Override
    public V1ObjectMeta getMetadata() {
        Map<String,Object> metaMap = (Map) get("metadata");
        //TODO build metadata using map data
        return new V1ObjectMeta();
    }

    @Override
    public String getApiVersion() {
        return (String) get("apiVersion");
    }

    @Override
    public String getKind() {
        return (String) get("kind");
    }

}
