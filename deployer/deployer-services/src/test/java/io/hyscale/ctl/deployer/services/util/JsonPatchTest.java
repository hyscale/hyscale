package io.hyscale.ctl.deployer.services.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.hyscale.ctl.commons.exception.HyscaleException;
import io.hyscale.ctl.commons.utils.ObjectMapperFactory;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.io.InputStream;
import java.util.stream.Stream;

public class JsonPatchTest {

    public static Stream<Arguments> input() {
        return Stream.of(Arguments.of("/source.json", "/target.json",
                "[{\"op\":\"add\",\"path\":\"/spec/containers/0/ports/1\",\"value\":{\"name\":\"secure\",\"containerPort\":443,\"protocol\":\"TCP\"}},{\"op\":\"replace\",\"path\":\"/spec/containers/0/image\",\"value\":\"nginx\"}]")
        );
    }

    @ParameterizedTest
    @MethodSource(value = "input")
    @Disabled
    public void testJsonPatch(String source, String target, String expectedResult) throws HyscaleException, IOException {
        InputStream resourceAsStream = JsonPatchTest.class.getResourceAsStream(source);
        source = IOUtils.toString(resourceAsStream, "UTF-8");
        resourceAsStream.close();
        InputStream targetStream = JsonPatchTest.class.getResourceAsStream(target);
        target = IOUtils.toString(targetStream, "UTF-8");
        targetStream.close();
        Object patch = K8sResourcePatchUtil.getJsonPatch(source, target, String.class);
        Assertions.assertNotNull(patch);
    }
}
