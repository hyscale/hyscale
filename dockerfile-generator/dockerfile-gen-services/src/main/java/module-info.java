module dockerfilegenservices {
	exports io.hyscale.ctl.dockerfile.gen.services.generator;
	exports io.hyscale.ctl.dockerfile.gen.services.util;
	exports io.hyscale.ctl.dockerfile.gen.services.model;
	exports io.hyscale.ctl.dockerfile.gen.services.constants;
	exports io.hyscale.ctl.dockerfile.gen.services.persist;
	exports io.hyscale.ctl.dockerfile.gen.services.templates;
	exports io.hyscale.ctl.dockerfile.gen.services.predicates;
	exports io.hyscale.ctl.dockerfile.gen.services.exception;
	exports io.hyscale.ctl.dockerfile.gen.services.config;

	requires service_spec_commons;
	requires dockerfilegencore;
	requires commons;
	requires org.apache.commons.io;
	requires spring.beans;
	requires spring.context;
	requires java.annotation;
	requires spring.boot;
	requires spring.core;
	requires slf4j.api;
	requires com.google.common;
	requires org.apache.commons.lang3;
	requires com.fasterxml.jackson.core;
}