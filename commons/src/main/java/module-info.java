module commons {
	exports io.hyscale.ctl.commons.constants;
	exports io.hyscale.ctl.commons.models;
	exports io.hyscale.ctl.commons.utils;
	exports io.hyscale.ctl.commons.logger;
	exports io.hyscale.ctl.commons.commands;
	exports io.hyscale.ctl.commons.config;
	exports io.hyscale.ctl.commons.component;
	exports io.hyscale.ctl.commons.exception;

	requires client.java.proto;
	requires jackson.annotations;
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
}
