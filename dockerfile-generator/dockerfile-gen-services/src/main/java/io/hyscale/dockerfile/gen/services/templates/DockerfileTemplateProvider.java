package io.hyscale.dockerfile.gen.services.templates;

import com.google.common.collect.Maps;
import io.hyscale.commons.models.ConfigTemplate;
import io.hyscale.dockerfile.gen.core.models.ImageType;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;

@Component
public class DockerfileTemplateProvider {

	private Map<ImageType, ConfigTemplate> imageVsTemplateMap;

	protected static final String TEMPLATES_PATH = "templates/";
	private static final String ARTIFACT_IMAGE_TPL = "artifact-image.tpl";
	private static final String SERVICE_IMAGE_TPL = "service-image.tpl";

	@PostConstruct
	public void init() {
		this.imageVsTemplateMap = Maps.newHashMap();

		ConfigTemplate artifactImgTpl = new ConfigTemplate();
		artifactImgTpl.setRootPath(TEMPLATES_PATH);
		artifactImgTpl.setTemplateName(ARTIFACT_IMAGE_TPL);

		imageVsTemplateMap.put(ImageType.ARTIFACT, artifactImgTpl);

		ConfigTemplate serviceImgTpl = new ConfigTemplate();
		serviceImgTpl.setRootPath(TEMPLATES_PATH);
		serviceImgTpl.setTemplateName(SERVICE_IMAGE_TPL);

		imageVsTemplateMap.put(ImageType.SERVICE, serviceImgTpl);

	}

	public ConfigTemplate getTemplateFor(ImageType imageType) {
		return imageVsTemplateMap.get(imageType);
	}
}
