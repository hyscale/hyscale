import io.hyscale.plugin.framework.handler.ManifestHandler;

module manifestGenerator {
	exports io.hyscale.generator.services.config;
	exports io.hyscale.generator.services.constants;
	exports io.hyscale.generator.services.exception;
	exports io.hyscale.generator.services.generator;
	exports io.hyscale.generator.services.json;
	exports io.hyscale.generator.services.listener;
	exports io.hyscale.generator.services.model;
	exports io.hyscale.generator.services.predicates;
	exports io.hyscale.generator.services.plugins;
	exports io.hyscale.generator.services.processor;
	exports io.hyscale.generator.services.provider;
	exports io.hyscale.generator.services.utils;

	uses ManifestHandler;

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