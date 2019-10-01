module builderservices {
	exports io.hyscale.ctl.builder.services.util;
	exports io.hyscale.ctl.builder.services.constants;
	exports io.hyscale.ctl.builder.services.config;
	exports io.hyscale.ctl.builder.services.exception;
	exports io.hyscale.ctl.builder.services.impl;
	exports io.hyscale.ctl.builder.services.command;

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
