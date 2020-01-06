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
import io.hyscale.commons.annotations.Normalize;
import io.hyscale.commons.exception.CommonErrorCode;
import io.hyscale.commons.exception.HyscaleException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class ContextNormalizer {
    public static Object getNormalizedContext(Object context) throws HyscaleException{
        if(context == null){
            return context;
        }
        Class componentClass = context.getClass();
        while (componentClass != Object.class) {
            Field[] fields = componentClass.getDeclaredFields();
            for (Field field : fields) {
                if (Modifier.isPrivate(field.getModifiers())) {
                    field.setAccessible(true);
                }
                Normalize annotation = field.getDeclaredAnnotation(Normalize.class);
                if (annotation == null){
                    continue;
                }
                try {
                    Object fieldValue = field.get(context);
                    if (field.getType().getClass().isInstance(String.class) && fieldValue != null) {
                        field.set(context, annotation.entity().normalize(fieldValue.toString()));
                    }
                } catch (IllegalAccessException e) {
                    throw new HyscaleException(e, CommonErrorCode.ILLEGAL_ACCESS, field.getName());
                }
            }
            componentClass = componentClass.getSuperclass();
        }
        return context;
    }
}
