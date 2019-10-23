package io.hyscale.builder.services.config;

import io.hyscale.builder.core.models.ImageBuilder;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

public class LocalImageBuildCondition implements Condition {
	@Override
	public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
		String imageBuilder = context.getEnvironment().getProperty(ImageBuilderConfig.IMAGE_BUILDER_PROP);
		return Objects.nonNull(imageBuilder) ? (ImageBuilder.valueOf(imageBuilder) == ImageBuilder.LOCAL ? true : false)
				: true;
	}
}
