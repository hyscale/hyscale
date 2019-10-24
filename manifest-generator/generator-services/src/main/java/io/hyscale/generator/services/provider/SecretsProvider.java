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
package io.hyscale.generator.services.provider;

import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.Secrets;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;

public class SecretsProvider {

    private static final Logger logger = LoggerFactory.getLogger(SecretsProvider.class);

    public static Secrets getSecrets(ServiceSpec serviceSpec) throws HyscaleException {
        Secrets secrets = new Secrets();
        try {
            TypeReference<Map<String, String>> mapTypeReference = new TypeReference<Map<String, String>>() {
            };
            Map<String, String> secretsMap = serviceSpec.get(HyscaleSpecFields.secrets, mapTypeReference);

            if (secretsMap != null && !secretsMap.isEmpty()) {
                secrets.setSecretsMap(secretsMap);
            }
        } catch (HyscaleException e) {
            logger.error("Error while fetching list secrets ", e);
        } catch (Exception e) {
            logger.error("Error while fetching map secrets ", e);
        }
        try {
            TypeReference<Set<String>> setTypeReference = new TypeReference<Set<String>>() {
            };
            Set<String> secretsList = serviceSpec.get(HyscaleSpecFields.secrets, setTypeReference);
            if (secretsList != null && !secretsList.isEmpty()) {
                secrets.setSecretKeys(secretsList);
            }

        } catch (HyscaleException e) {
            logger.error("Error while fetching list secrets ", e);
        } catch (Exception e) {
            logger.error("Error while fetching list secrets ", e);
        }
        return secrets;
    }
}
