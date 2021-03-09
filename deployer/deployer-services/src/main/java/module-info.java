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
module deployer.services {
    exports io.hyscale.deployer.services.deployer;
    exports io.hyscale.deployer.services.broker;
    exports io.hyscale.deployer.services.builder;
    exports io.hyscale.deployer.services.config;
    exports io.hyscale.deployer.services.constants;
    exports io.hyscale.deployer.services.exception;
    exports io.hyscale.deployer.services.factory;
    exports io.hyscale.deployer.services.handler;
    exports io.hyscale.deployer.services.handler.impl;
    exports io.hyscale.deployer.services.listener;
    exports io.hyscale.deployer.services.manager;
    exports io.hyscale.deployer.services.model;
    exports io.hyscale.deployer.services.predicates;
    exports io.hyscale.deployer.services.processor;
    exports io.hyscale.deployer.services.progress;
    exports io.hyscale.deployer.services.provider;
    exports io.hyscale.deployer.services.util;
    exports io.hyscale.deployer.services.client;

    requires deployerModel;
    requires commons;
    requires slf4j.api;
    requires com.google.common;
    requires gson;
    requires com.fasterxml.jackson.databind;
    requires okhttp3;
    requires client.java;
    requires client.java.api;
    requires org.apache.commons.lang3;
    requires joda.time;
    requires spring.beans;
    requires spring.core;
    requires zjsonpatch;
}
