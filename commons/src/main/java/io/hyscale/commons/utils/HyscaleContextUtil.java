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
package io.hyscale.commons.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

import java.util.Locale;

public class HyscaleContextUtil {

    private static ApplicationContext applicationContext;

    private HyscaleContextUtil() {}

    public static synchronized void setContext(ApplicationContext appContext) {
        if (applicationContext == null) {
            applicationContext = appContext;
        }
    }

    public static <T> T getSpringBean(String beanId) {
        return (T) applicationContext.getBean(beanId);
    }

    public static <T> T getSpringBean(String beanId, Object... args) {
        return (T) applicationContext.getBean(beanId, args);
    }

    public static <T> T getSpringBean(Class<T> className) {
        return applicationContext.getBean(className);
    }

    public static <T> T getSpringBean(Class<T> className, Object... args) {
        return applicationContext.getBean(className, args);
    }

    public static <T> T getSpringBeanNullIfNotExists(Class<T> className) {
        try {
            if (applicationContext != null) {
                return applicationContext.getBean(className);
            } else {
                return null;
            }
        } catch (BeansException e) {
            return null;
        }
    }

    public static String getSpringMessage(String key, Locale locale, String defaultMessage, Object... args) {
        if (defaultMessage == null || defaultMessage.isEmpty()) {
            defaultMessage = applicationContext.getMessage(key, args, defaultMessage, Locale.getDefault());
        }
        return applicationContext.getMessage(key, args, defaultMessage, locale);
    }

    public static String getSpringMessage(String key, Locale locale, Object... args) {
        return applicationContext.getMessage(key, args, locale);
    }

    public static String getSpringMessage(String key, Locale locale, String defaultMessage) {
        if (defaultMessage == null || defaultMessage.isEmpty()) {
            defaultMessage = applicationContext.getMessage(key, null, defaultMessage, Locale.getDefault());
        }
        return applicationContext.getMessage(key, null, defaultMessage, locale);
    }
    
    public static String[] getBeanNames(Class<?> className) {
        return applicationContext.getBeanNamesForType(className);
    }

}
