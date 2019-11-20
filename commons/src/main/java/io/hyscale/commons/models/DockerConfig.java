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
package io.hyscale.commons.models;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Model of Docker config file.
 * <p>Example:
 * <pre>{
 *  * {
 *  *   "auths": {
 *  *     "registry": {
 *  *       "auth": "username:password string in base64"
 *  *     },
 *  *     "another registry": {},
 *  *     ...
 *  *   },
 *  *   "credsStore": "credential helper acts a default helper",
 *  *   "credHelpers": {
 *  *     "registry": "credential helper name",
 *  *     "anotherregistry": "another credential helper name",
 *  *     ...
 *  *   }
 *  * }
 *  * }</pre>
 *  <p>Each registry in credHelpers is matched to a credential helper that stores authorization for the registry.
 *     It may take precedence over credsStore if match exists.
 * <p>credsStore is a config that provides default credential helper if it not exists in credHelpers.
 * <p>Auths contains individual Auth for each registry,if specified it is the basic authorization to use for the registry.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DockerConfig {
    private String credsStore;
    private Map<String, String> credHelpers;
    private Map<String, Auth> auths;

    public Map<String, String> getCredHelpers() {
        return this.credHelpers;
    }

    public String getCredsStore() {
        return this.credsStore;
    }

    public void setCredsStore(String credsStore) {
        this.credsStore = credsStore;
    }

    public void setCredHelpers(Map<String, String> credHelpers) {
        this.credHelpers = credHelpers;
    }

    public Map<String, Auth> getAuths() {
        return auths;
    }

    public void setAuths(Map<String, Auth> auths) {
        this.auths = auths;
    }
}
