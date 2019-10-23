module service_spec_generator {
	requires jackson.annotations;
	requires org.glassfish.java.json;
	requires json.path;
	requires service_spec_commons;
	requires com.fasterxml.jackson.databind;
	requires commons;
	requires java.sql;
	requires com.fasterxml.jackson.core;
	requires org.apache.commons.lang3;

	exports io.hyscale.servicespec.generator;
	exports io.hyscale.servicespec.model;
}