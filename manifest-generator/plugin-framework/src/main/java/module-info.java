module pluginframework {
	exports io.hyscale.ctl.plugin;
	exports io.hyscale.ctl.util;
	exports io.hyscale.ctl.annotation;
	exports io.hyscale.ctl.models;

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