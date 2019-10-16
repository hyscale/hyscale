package io.hyscale.ctl.deployer.services.util;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import io.hyscale.ctl.commons.models.KubernetesResource;
import io.hyscale.ctl.commons.models.Manifest;
import io.hyscale.ctl.commons.models.YAMLManifest;
import io.kubernetes.client.models.V1ObjectMeta;
import io.kubernetes.client.util.Yaml;

public class KubernetesResourceUtil {

    private static final String GET_KIND = "getKind";
    private static final String GET_METADATA = "getMetadata";

    public static KubernetesResource getKubernetesResource(Manifest manifest, String namespace)
            throws NoSuchMethodException, SecurityException, IOException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException {
        if (manifest == null) {
            return null;
        }

        KubernetesResource resource = new KubernetesResource();
        YAMLManifest yamlManifest = (YAMLManifest) manifest;
        Object obj = Yaml.load(yamlManifest.getYamlManifest());
        Method kindMethod = obj.getClass().getMethod(GET_KIND);
        String kind = (String) kindMethod.invoke(obj);

        V1ObjectMeta v1ObjectMeta = getObjectMeta(obj);
        if (v1ObjectMeta != null) {
            v1ObjectMeta.setNamespace(namespace);
        }
        resource.setV1ObjectMeta(v1ObjectMeta);
        resource.setKind(kind);
        resource.setResource(obj);
        return resource;

    }

    public static V1ObjectMeta getObjectMeta(Object object) throws NoSuchMethodException, SecurityException,
            IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        if (object == null) {
            return null;
        }
        Method metadataMethod = object.getClass().getMethod(GET_METADATA);

        V1ObjectMeta v1ObjectMeta = (V1ObjectMeta) metadataMethod.invoke(object);

        return v1ObjectMeta;
    }

}
