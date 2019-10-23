package io.hyscale.deployer.services.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.hyscale.commons.utils.ObjectMapperFactory;
import io.kubernetes.client.ApiClient;
import io.kubernetes.client.models.*;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;


public class PodHandlerTest {

    static ApiClient apiClient;
    String podName = "testpod";
    String namespace = "test";
    static V1Pod pod;


    @BeforeAll
    public static void createPod() throws IOException {
        InputStream resourceAsStream = JsonPatchTest.class.getResourceAsStream("/test-data/test-pod.yaml");
        String podData = IOUtils.toString(resourceAsStream, "UTF-8");
        ObjectMapper mapper = ObjectMapperFactory.yamlMapper();
        pod = mapper.readValue(podData, V1Pod.class);
        resourceAsStream.close();
    }

    @Test
    public void testGetStatus() {
        Assertions.assertEquals(K8sPodUtil.getAggregatedStatusOfContainersForPod(pod), "Running");
    }

    @Test
    public void testGetMessage() {
        Assertions.assertEquals(K8sPodUtil.getPodMessage(pod), null);
    }

}
