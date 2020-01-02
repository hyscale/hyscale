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
	requires org.aspectj.weaver;
}