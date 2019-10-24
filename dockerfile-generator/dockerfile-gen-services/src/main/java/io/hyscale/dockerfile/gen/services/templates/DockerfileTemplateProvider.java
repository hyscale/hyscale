/**
 * Copyright 2019 Pramati Prism, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
