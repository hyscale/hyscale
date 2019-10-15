package io.hyscale.ctl.generator.services.generator;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.hyscale.ctl.generator.services.model.ManifestResource;
import io.hyscale.ctl.generator.services.model.MetaDataContext;
import io.hyscale.ctl.plugin.framework.models.ManifestSnippet;
import io.hyscale.ctl.plugin.framework.util.JsonSnippetConvertor;
import io.kubernetes.client.models.V1ObjectMeta;

public class MetadatManifestSnippetGenerator {

	public static ManifestSnippet getMetaData(ManifestResource manifestResource, MetaDataContext metaDataContext)
			throws JsonProcessingException {
		V1ObjectMeta v1ObjectMeta = new V1ObjectMeta();
		v1ObjectMeta.setLabels(manifestResource.getLabels(metaDataContext));
		v1ObjectMeta.setName(manifestResource.getName(metaDataContext));

		ManifestSnippet snippet = new ManifestSnippet();
		snippet.setSnippet(JsonSnippetConvertor.serialize(v1ObjectMeta));
		snippet.setPath("metadata");
		snippet.setKind(manifestResource.getKind());
		return snippet;
	}

	public static ManifestSnippet getApiVersion(ManifestResource manifestResource, MetaDataContext metaDataContext) {
		ManifestSnippet apiVersionSnippet = new ManifestSnippet();
		apiVersionSnippet.setPath("apiVersion");
		apiVersionSnippet.setKind(manifestResource.getKind());
		apiVersionSnippet.setSnippet(manifestResource.getApiVersion());
		return apiVersionSnippet;
	}

	public static ManifestSnippet getKind(ManifestResource manifestResource) {
		ManifestSnippet kindSnippet = new ManifestSnippet();
		kindSnippet.setPath("kind");
		kindSnippet.setKind(manifestResource.getKind());
		kindSnippet.setSnippet(manifestResource.getKind());
		return kindSnippet;
	}

}
