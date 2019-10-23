module service_spec_commons {

	requires commons;
	requires com.fasterxml.jackson.databind;
	requires com.fasterxml.jackson.dataformat.yaml;
	requires slf4j.api;
	requires com.fasterxml.jackson.core;
	requires json.path;
	requires org.apache.commons.lang3;

	exports io.hyscale.servicespec.commons.model;
	exports io.hyscale.servicespec.commons.exception;
	exports io.hyscale.servicespec.commons.fields;
	exports io.hyscale.servicespec.commons.model.profile;
	exports io.hyscale.servicespec.commons.model.service;
	exports io.hyscale.servicespec.json.parser;
	exports io.hyscale.servicespec.json.parser.constants;
	exports io.hyscale.servicespec.json.config;
	exports io.hyscale.servicespec.annotations;
	exports io.hyscale.servicespec.commons.util;

}