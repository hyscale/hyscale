module manifestGenerator {
	exports io.hyscale.ctl.generator.services.config;
	exports io.hyscale.ctl.generator.services.constants;
	exports io.hyscale.ctl.generator.services.exception;
	exports io.hyscale.ctl.generator.services.generator;
	exports io.hyscale.ctl.generator.services.json;
	exports io.hyscale.ctl.generator.services.listener;
	exports io.hyscale.ctl.generator.services.model;
	exports io.hyscale.ctl.generator.services.predicates;
	exports io.hyscale.ctl.generator.services.plugins;
	exports io.hyscale.ctl.generator.services.processor;
	exports io.hyscale.ctl.generator.services.provider;
	exports io.hyscale.ctl.generator.services.utils;

	uses io.hyscale.ctl.plugin.ManifestHandler;

	requires slf4j.api;
	requires com.github.mustachejava;
	requires com.fasterxml.jackson.databind;
	requires com.fasterxml.jackson.annotation;
	requires org.apache.commons.codec;
	requires transitive client.java.api;
	requires com.fasterxml.jackson.core;
	requires org.apache.commons.lang3;
	requires com.google.common;
	requires spring.beans;
	requires spring.context;
	requires java.annotation;
	requires spring.boot;
	requires spring.core;
	requires transitive commons;
	requires org.apache.commons.io;
	requires transitive pluginframework;
	requires transitive service_spec_commons;
	requires com.fasterxml.jackson.dataformat.yaml;
	requires json.path;
}