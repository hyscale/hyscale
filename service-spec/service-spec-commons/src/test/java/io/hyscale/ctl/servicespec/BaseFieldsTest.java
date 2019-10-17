package io.hyscale.ctl.servicespec;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.hyscale.ctl.commons.exception.HyscaleException;
import io.hyscale.ctl.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.ctl.servicespec.commons.model.service.*;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;


import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class BaseFieldsTest {

    static ServiceSpec serviceSpec;

    @BeforeAll
    public static void beforeClass() throws IOException {
        ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
        String sampleSpec = "/servicespecs/myservice.hspec.yaml";
        InputStream resourceAsStream = BaseFieldsTest.class.getResourceAsStream(sampleSpec);
        String testData = IOUtils.toString(resourceAsStream, "UTF-8");
        ObjectNode rootNode = (ObjectNode) objectMapper.readTree(testData);
        serviceSpec = new ServiceSpec(rootNode);
    }

    @Test
    public void testName() throws HyscaleException {
        String name = serviceSpec.get(HyscaleSpecFields.name, String.class);
        Assertions.assertEquals("myservice", name);
    }

    @Test
    public void testImage() throws HyscaleException {
        String image = serviceSpec.get(HyscaleSpecFields.getPath(HyscaleSpecFields.image, HyscaleSpecFields.name), String.class);
        Assertions.assertNotNull(image);
        Assertions.assertEquals("myServiceImage", image);
        String registry = serviceSpec.get(HyscaleSpecFields.getPath(HyscaleSpecFields.image, HyscaleSpecFields.registry), String.class);
        Assertions.assertNotNull(registry);
        Assertions.assertEquals("x.y.z", registry);
        String tag = serviceSpec.get(HyscaleSpecFields.getPath(HyscaleSpecFields.image, HyscaleSpecFields.tag), String.class);
        Assertions.assertNotNull(tag);
        Assertions.assertEquals("1.2.3", tag);
    }


    @Test
    public void testBuildSpec() throws HyscaleException {
        BuildSpec buildSpec = serviceSpec.get(HyscaleSpecFields.getPath(HyscaleSpecFields.image, HyscaleSpecFields.buildSpec), BuildSpec.class);
        Assertions.assertNotNull(buildSpec);
        Assertions.assertEquals("abc/pqr:1.0", buildSpec.getStackImage());
        Assertions.assertNotNull(buildSpec.getArtifacts());
        Artifact artifact = buildSpec.getArtifacts().get(0);
        Assertions.assertNotNull(artifact);
        Assertions.assertEquals("sample", artifact.getName());
        Assertions.assertEquals("sample.war", artifact.getSource());
        Assertions.assertEquals("/abc/def/ghi/sample.txt", artifact.getDestination());
        Assertions.assertNotNull(buildSpec.getConfigCommands());
        Assertions.assertEquals("echo \"Hello, I'm configure commands\"", buildSpec.getConfigCommands());
        Assertions.assertNotNull(buildSpec.getRunCommands());
        Assertions.assertEquals("echo \"Hello, I'm run commands\"", buildSpec.getRunCommands());
        Assertions.assertNotNull(buildSpec.getConfigCommandsScript());
        Assertions.assertEquals("/abc/def/configure.sh", buildSpec.getConfigCommandsScript());
        Assertions.assertNotNull(buildSpec.getRunCommandsScript());
        Assertions.assertEquals("/abc/def/run.sh", buildSpec.getRunCommandsScript());
    }

    @Test
    public void testDockerfile() throws HyscaleException {
        Dockerfile dockerfile = serviceSpec.get(HyscaleSpecFields.getPath(HyscaleSpecFields.image, HyscaleSpecFields.dockerfile), Dockerfile.class);
        Assertions.assertNotNull(dockerfile);
        Assertions.assertNotNull(dockerfile.getPath());
        Assertions.assertEquals("/abc", dockerfile.getPath());
        Assertions.assertNotNull(dockerfile.getDockerfilePath());
        Assertions.assertEquals("/Dockerfile", dockerfile.getDockerfilePath());
        Assertions.assertNotNull(dockerfile.getArgs());
        Assertions.assertEquals("value1", dockerfile.getArgs().get("key1"));
    }


    @Test
    public void testVolumes() throws HyscaleException {
        TypeReference<List<Volume>> volumesTypeReference = new TypeReference<List<Volume>>() {
        };
        List<Volume> volumeList = serviceSpec.get(HyscaleSpecFields.volumes, volumesTypeReference);
        Assertions.assertNotNull(volumeList);
        Assertions.assertNotNull(volumeList.get(0));
        Volume volume = volumeList.get(0);
        Assertions.assertEquals("vol-name", volume.getName());
        Assertions.assertEquals("/volume/mount/path", volume.getPath());
        Assertions.assertEquals("1Gi", volume.getSize());
        Assertions.assertEquals("storageClass", volume.getStorageClass());
    }

    @Test
    public void testExternal() throws HyscaleException {
        Boolean external = serviceSpec.get(HyscaleSpecFields.external, Boolean.class);
        Assertions.assertEquals(Boolean.TRUE, external);
    }

    @Test
    public void testHttpHealthCheckPorts() throws HyscaleException {
        TypeReference<List<Port>> portsTypeReference = new TypeReference<List<Port>>() {
        };
        List<Port> ports = serviceSpec.get(HyscaleSpecFields.ports, portsTypeReference);
        Assertions.assertNotNull(ports);
        Assertions.assertNotNull(ports.get(0));
        Port port = ports.get(0);
        Assertions.assertEquals("8080/tcp", port.getPort());
        Assertions.assertNotNull(port.getHealthCheck());
        Assertions.assertEquals("/sample", port.getHealthCheck().getHttpPath());
    }

    @Test
    public void testProps() throws HyscaleException {
        TypeReference<Map<String, String>> propsTypeReference = new TypeReference<Map<String, String>>() {
        };
        Map<String, String> props = serviceSpec.get(HyscaleSpecFields.props, propsTypeReference);
        Assertions.assertNotNull(props);
        Assertions.assertNotNull(props.get("key1"));
        String value = props.get("key2");
        Assertions.assertNotNull(value);
        Assertions.assertEquals("value2", value);
    }

    @Test
    public void testSecrets() throws HyscaleException {
        TypeReference<List<String>> secretsTypeRef = new TypeReference<List<String>>() {
        };
        List<String> secrets = serviceSpec.get(HyscaleSpecFields.secrets, secretsTypeRef);
        Assertions.assertNotNull(secrets);
        Assertions.assertEquals("skey1", secrets.get(0));
        Assertions.assertEquals("skey2", secrets.get(1));
    }

    @Test
    public void testSecretsPath() throws HyscaleException {
        String secretsMountPath = serviceSpec.get(HyscaleSpecFields.secretsVolumePath, String.class);
        String propsMountPath = serviceSpec.get(HyscaleSpecFields.propsVolumePath, String.class);
        Assertions.assertNotNull(secretsMountPath);
        Assertions.assertNotNull(propsMountPath);
        Assertions.assertEquals("/abc/def/secrets.txt", secretsMountPath);
        Assertions.assertEquals("/abc/def/props.txt", propsMountPath);
    }

    @Test
    public void testStartCommands() throws HyscaleException {
        String startCommands = serviceSpec.get(HyscaleSpecFields.startCommand, String.class);
        Assertions.assertNotNull(startCommands);
        Assertions.assertEquals("a,bc,def", startCommands);
    }

    @Test
    public void testReplicas() throws HyscaleException {
        String replicas = serviceSpec.get(HyscaleSpecFields.replicas, String.class);
        Assertions.assertNotNull(replicas);
        Assertions.assertEquals("2", replicas);
    }

    @Test
    public void testMemory() throws HyscaleException {
        String memory = serviceSpec.get(HyscaleSpecFields.memory, String.class);
        Assertions.assertNotNull(memory);
        Assertions.assertEquals("123Mi-456Mi", memory);
    }

    @Test
    public void testCpu() throws HyscaleException {
        String cpu = serviceSpec.get(HyscaleSpecFields.cpu, String.class);
        Assertions.assertNotNull(cpu);
        Assertions.assertEquals("123m-456m", cpu);
    }

}
