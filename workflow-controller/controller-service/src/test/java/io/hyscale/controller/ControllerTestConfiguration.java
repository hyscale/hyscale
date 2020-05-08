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
package io.hyscale.controller;

import java.net.URL;

import javax.annotation.PostConstruct;

import org.mockito.Mockito;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;

import io.hyscale.controller.config.ControllerConfig;

@SpringBootConfiguration
@ComponentScan(basePackages = "io.hyscale", 
excludeFilters = @Filter(type = FilterType.ASSIGNABLE_TYPE, classes = CommandLineRunner.class))
@EnableAutoConfiguration
public class ControllerTestConfiguration {
    
    private static final String TEST_DOCKER_CONFIG = "/config/registry.json";

    @MockBean
    public ControllerConfig controllerConfig;

    @PostConstruct
    public void init() {
        Mockito.when(controllerConfig.getDefaultKubeConf()).thenReturn(null);
        URL resourceAsUrl = ControllerTestConfiguration.class.getResource(TEST_DOCKER_CONFIG);
        Mockito.when(controllerConfig.getDefaultRegistryConf()).thenReturn(resourceAsUrl.getPath());
    }
}
