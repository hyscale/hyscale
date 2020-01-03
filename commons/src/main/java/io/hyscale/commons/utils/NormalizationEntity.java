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

import org.apache.commons.lang3.StringUtils;

public enum NormalizationEntity implements Normalizable {

    APP_NAME {
        @Override
        public String normalize(String input) {
            return doNormalization(input);
        }
    },
    SVC_NAME {
        @Override
        public String normalize(String input) {
            return doNormalization(input);
        }
    },
    ENV_NAME {
        @Override
        public String normalize(String input) {
            return StringUtils.isNotBlank(input) ? input.trim() : input;
        }
    },
    VOLUME_NAME {
        @Override
        public String normalize(String input) {
            return doNormalization(input);
        }
    },
    NAMESPACE {
        @Override
        public String normalize(String input){
            return StringUtils.isNotBlank(input) ? input.trim().toLowerCase():input;
        }
    },
    SIDECAR_NAME {
        @Override
        public String normalize(String input) {
            return doNormalization(input);
        }
    },
    LABEL_SELECTOR{
        @Override
        public String normalize(String input) {
            Integer MAX_LABEL_VALUE_SIZE = 63;
            if (StringUtils.isEmpty(input)) {
                    return input;
                }
                String normalized = input.trim().replaceAll("[\\.]+", "-").replaceAll("[ ]+", "-")
                        .replaceAll("[^a-zA-Z0-9-_]", "");
                return normalized.substring(0, Integer.min(MAX_LABEL_VALUE_SIZE - 1, normalized.length()));
        }
    };

    private static String doNormalization(String string) {
        return StringUtils.isNotBlank(string) ? string.trim().toLowerCase().replaceAll("[^a-zA-Z0-9-]", "") : string;
    }
}

interface Normalizable {
    String normalize(String input);
}
