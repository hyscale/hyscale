module service_spec_commons {

	requires commons;
	requires com.fasterxml.jackson.databind;
	requires com.fasterxml.jackson.dataformat.yaml;
	requires slf4j.api;
	requires com.fasterxml.jackson.core;
	requires json.path;
	requires org.apache.commons.lang3;

	exports io.hyscale.ctl.servicespec.commons.model;
	exports io.hyscale.ctl.servicespec.commons.exception;
	exports io.hyscale.ctl.servicespec.commons.fields;
	exports io.hyscale.ctl.servicespec.commons.model.profile;
	exports io.hyscale.ctl.servicespec.commons.model.service;
	exports io.hyscale.ctl.servicespec.json.parser;
	exports io.hyscale.ctl.servicespec.json.parser.constants;
	exports io.hyscale.ctl.servicespec.json.config;
	exports io.hyscale.ctl.servicespec.annotations;
	exports io.hyscale.ctl.servicespec.commons.util;

}