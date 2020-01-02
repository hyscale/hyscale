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
module commons {
	exports io.hyscale.commons.constants;
	exports io.hyscale.commons.annotations;
	exports io.hyscale.commons.models;
	exports io.hyscale.commons.utils;
	exports io.hyscale.commons.logger;
	exports io.hyscale.commons.commands;
	exports io.hyscale.commons.config;
	exports io.hyscale.commons.component;
	exports io.hyscale.commons.exception;

	requires client.java.proto;
	requires com.fasterxml.jackson.annotation;
	requires protobuf.java;
	requires client.java.api;
	requires gson;
	requires com.fasterxml.jackson.core;
	requires com.fasterxml.jackson.databind;
	requires slf4j.api;
	requires spring.context;
	requires org.apache.commons.lang3;
	requires spring.beans;
	requires com.fasterxml.jackson.dataformat.yaml;
	requires java.validation;
	requires org.apache.commons.compress;
	requires org.apache.commons.io;
	requires com.github.mustachejava;
	requires java.annotation;
	requires org.aspectj.weaver;
}
