package io.hyscale.plugin.framework.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ManifestPlugin {

	/**
	 * @return
	 */
	String name();

	/**
	 * @return
	 */
	// String dependencies() default "";

}
