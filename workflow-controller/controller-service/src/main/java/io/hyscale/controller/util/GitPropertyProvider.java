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
package io.hyscale.controller.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Component
public class GitPropertyProvider {

    private static final String GIT_PROPERTIES_FILE = "git.properties";
    private static final Logger logger = LoggerFactory.getLogger(GitPropertyProvider.class);
    private Properties properties;

    @PostConstruct
    public void init() {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(GIT_PROPERTIES_FILE);) {
            if (is != null) {
                properties = new Properties();
                properties.load(is);
            }
        } catch (IOException e) {
            logger.debug("Failed to load {} file ", GIT_PROPERTIES_FILE);
        }
    }

    public String getGitProperty(String property) {
        if (properties != null) {
            return (String) properties.get(property);
        }
        return null;
    }
}
