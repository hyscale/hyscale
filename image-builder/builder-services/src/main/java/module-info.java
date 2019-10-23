module builderservices {
	exports io.hyscale.builder.services.util;
	exports io.hyscale.builder.services.constants;
	exports io.hyscale.builder.services.config;
	exports io.hyscale.builder.services.exception;
	exports io.hyscale.builder.services.impl;
	exports io.hyscale.builder.services.command;

	requires transitive commons;
	requires transitive service_spec_commons;
	requires transitive builderCore;
	requires org.apache.commons.lang3;

	requires spring.beans;
	requires spring.context;
	requires java.annotation;
	requires spring.boot;
	requires spring.core;
	requires slf4j.api;
	requires com.fasterxml.jackson.databind;
	requires com.fasterxml.jackson.core;
}
