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
module service_spec_commons {

	requires commons;
	requires com.fasterxml.jackson.databind;
	requires com.fasterxml.jackson.dataformat.yaml;
	requires slf4j.api;
	requires com.fasterxml.jackson.core;
	requires json.path;
	requires org.apache.commons.lang3;
	requires com.fasterxml.jackson.annotation;
	requires org.apache.commons.io;

	exports io.hyscale.servicespec.commons.model;
	exports io.hyscale.servicespec.commons.exception;
	exports io.hyscale.servicespec.commons.fields;
	exports io.hyscale.servicespec.commons.model.service;
	exports io.hyscale.servicespec.commons.json.parser;
	exports io.hyscale.servicespec.commons.json.parser.constants;
	exports io.hyscale.servicespec.commons.json.config;
	exports io.hyscale.servicespec.commons.util;
	exports io.hyscale.servicespec.commons.builder;
	exports io.hyscale.servicespec.commons.activity;
	exports io.hyscale.servicespec.commons.predicates;

}
