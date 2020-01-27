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
package io.hyscale.commons.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

public class HyscaleContextHelper {
    
    private static final Logger logger = LoggerFactory.getLogger(HyscaleContextHelper.class);

    private static ApplicationContext applicationContext;

    protected static void setContext(ApplicationContext appContext) {
        applicationContext = appContext;
    }

    public static <T> T getSpringBean(Class<T> className) {
        try {
            if (applicationContext != null) {
                return (T) applicationContext.getBean(className);
            }
            return null;
        } catch (Exception e) {
            logger.debug("Error while getting bean for class {}.", className, e.getMessage());
            return null;
        }
    }

}
