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
module dockerfilegenservices {
	exports io.hyscale.dockerfile.gen.services.generator;
	exports io.hyscale.dockerfile.gen.services.model;
	exports io.hyscale.dockerfile.gen.services.constants;
	exports io.hyscale.dockerfile.gen.services.persist;
	exports io.hyscale.dockerfile.gen.services.templates;
	exports io.hyscale.dockerfile.gen.services.predicates;
	exports io.hyscale.dockerfile.gen.services.exception;
	exports io.hyscale.dockerfile.gen.services.config;

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
	requires org.aspectj.weaver;
}