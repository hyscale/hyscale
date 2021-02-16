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
package io.hyscale.servicespec.commons.builder;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.hyscale.commons.framework.patch.FieldMetaData;
import io.hyscale.commons.framework.patch.FieldMetaDataProvider;
import io.hyscale.commons.framework.patch.PatchStrategy;

/**
 * Provides field data map which contains primary key field for an entity
 * Used to distinguish between object in operations such as merge
 * 
 * @author tushar
 *
 */
public class MapFieldDataProvider implements FieldMetaDataProvider{

    private static final Logger logger = LoggerFactory.getLogger(MapFieldDataProvider.class);
    private static Properties properties = new Properties();

    private static Map<String, String> fieldData = new HashMap<>();
    private static Map<String, String> mergeStrategy = new HashMap<>();
    
    static {
        try {
            properties.load(MapFieldDataProvider.class.getResourceAsStream("/properties/field-data.properties"));
        } catch (Exception e) {
            logger.error("Error while loading field data properties", e);
        }
        if (properties != null) {
            for (Entry<Object, Object> entry : properties.entrySet()) {
                fieldData.put((String) entry.getKey(), (String) entry.getValue());
            }
        }
        try {
            properties.clear();
            properties.load(MapFieldDataProvider.class.getResourceAsStream("/properties/field-data-merge-strategy.properties"));
        } catch (Exception e) {
            logger.error("Error while loading field data properties", e);
        }
        if (properties != null) {
            for (Entry<Object, Object> entry : properties.entrySet()) {
                mergeStrategy.put((String) entry.getKey(), (String) entry.getValue());
            }
        }
    }

    @Override
    public FieldMetaData getMetaData(String field) {
        FieldMetaData fieldMetaData = new FieldMetaData();
        fieldMetaData.setKey(fieldData.get(field));
        fieldMetaData.setPatchStrategy(PatchStrategy.fromString(mergeStrategy.get(field)));
        return fieldMetaData;
    }
}
