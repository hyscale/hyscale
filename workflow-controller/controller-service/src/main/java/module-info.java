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
module controller.service {
    exports io.hyscale.controller.activity;
    exports io.hyscale.controller.builder;
    exports io.hyscale.controller.commands;
    exports io.hyscale.controller.config;
    exports io.hyscale.controller.constants;
    exports io.hyscale.controller.directive;
    exports io.hyscale.controller.exception;
    exports io.hyscale.controller.hooks;
    exports io.hyscale.controller.initializer;
    exports io.hyscale.controller.invoker;
    exports io.hyscale.controller.manager;
    exports io.hyscale.controller.model;
    exports io.hyscale.controller.profile;
    exports io.hyscale.controller.provider;
    exports io.hyscale.controller.service;
    exports io.hyscale.controller.util;
    exports io.hyscale.controller.validator;

    requires org.apache.commons.lang3;
    requires spring.context;
    requires slf4j.api;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.core;
    requires info.picocli;
    requires json.schema.core;
    requires gson;
    requires prettytime;
    requires client.java.api;
    requires client.java;
    requires org.joda.time;

    requires commons;
    requires builderCore;
    requires manifestGenerator;
    requires dockerfilegenservices;
    requires troubleshooting.integration;
    requires schema.validator;
    requires spring.boot;
    requires spring.beans;
    requires spring.boot.autoconfigure;
    requires spring.core;
    requires deployerModel;
    requires builderservices;
    requires deployer.services;
    requires java.annotation;
    requires org.apache.commons.collections4;
    requires jdk.unsupported;
}
