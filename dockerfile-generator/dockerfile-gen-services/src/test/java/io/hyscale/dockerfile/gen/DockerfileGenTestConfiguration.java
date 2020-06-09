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
package io.hyscale.dockerfile.gen;

import javax.annotation.PostConstruct;

import org.mockito.Mockito;

import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyMap;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;

import io.hyscale.commons.config.SetupConfig;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.utils.MustacheTemplateResolver;

@SpringBootConfiguration
@ComponentScan(basePackages = "io.hyscale.dockerfile")
@EnableAutoConfiguration
public class DockerfileGenTestConfiguration {

    @MockBean
    private SetupConfig setupConfig;

    @MockBean
    private MustacheTemplateResolver mustacheTemplateResolver;

    @PostConstruct
    public void init() throws HyscaleException {
        Mockito.when(mustacheTemplateResolver.resolveTemplate(anyString(), anyMap())).then(returnsFirstArg());

    }
}
