package io.hyscale.deployer.services.client;

import io.hyscale.deployer.services.model.CustomObject;
import io.kubernetes.client.openapi.ApiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class K8sResourceClient extends GenericK8sClient {
    private static final Logger logger = LoggerFactory.getLogger(K8sResourceClient.class);

    public K8sResourceClient(ApiClient apiClient) {
        super(apiClient);
    }

    @Override
    public void create(CustomObject resource) {

    }

    @Override
    public void update(CustomObject resource) {

    }

    @Override
    public void delete(CustomObject resource) {

    }

    @Override
    public CustomObject get(CustomObject resource) {
        return null;
    }
}
