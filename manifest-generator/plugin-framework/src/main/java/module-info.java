module pluginframework {
	exports io.hyscale.plugin.framework.models;
	exports io.hyscale.plugin.framework.util;
	exports io.hyscale.plugin.framework.annotation;
	exports io.hyscale.plugin.framework.handler;

	requires service_spec_commons;
	requires commons;
	requires java.validation;
	requires com.fasterxml.jackson.databind;
	requires com.fasterxml.jackson.dataformat.yaml;
	requires com.fasterxml.jackson.core;
	requires com.fasterxml.jackson.annotation;
	requires slf4j.api;
	requires json.path;
	requires org.apache.commons.lang3;
	requires gson;
}