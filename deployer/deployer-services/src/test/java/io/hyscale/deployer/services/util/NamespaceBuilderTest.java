package io.hyscale.deployer.services.util;

import io.hyscale.deployer.services.builder.NamespaceBuilder;
import io.kubernetes.client.models.V1Namespace;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class NamespaceBuilderTest {

    @Test
    public void testNamespaceBuilder() {
        V1Namespace v1Namespace = NamespaceBuilder.build("mynamespace");
        Assertions.assertNotNull(v1Namespace);
        Assertions.assertEquals(v1Namespace.getKind(), "Namespace");
        Assertions.assertNotNull(v1Namespace.getMetadata());
        Assertions.assertEquals(v1Namespace.getMetadata().getName(), "mynamespace");
    }
}
