package io.hyscale.ctl.deployer.services.builder;

import io.hyscale.ctl.deployer.core.model.ResourceKind;
import io.kubernetes.client.models.V1Namespace;
import io.kubernetes.client.models.V1ObjectMeta;

public class NamespaceBuilder {

	public static V1Namespace build(String namespace) {
		V1Namespace v1Namespace = new V1Namespace();
		v1Namespace.setApiVersion("v1");
		v1Namespace.setKind(ResourceKind.NAMESPACE.getKind());

		V1ObjectMeta v1ObjectMeta = new V1ObjectMeta();
		v1ObjectMeta.setName(namespace);
		v1Namespace.setMetadata(v1ObjectMeta);
		return v1Namespace;
	}
}
