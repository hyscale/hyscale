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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Template of credsStore entity.
 *
 * <p>Example:
 * <pre>{
 * {
 *   "serverURL": "registry",
 *   "username": "username",
 *   "secret": "password"
 * }
 * }</pre>
 * Provides credentials from the credsStore.
 */
public class CredsStoreEntity {
    private String serverURL;
    private String username;
    private String secret;

    @JsonCreator
    public CredsStoreEntity(@JsonProperty("ServerURL") String serverURL, @JsonProperty("Username") String username, @JsonProperty("Secret") String secret) {
        this.serverURL = serverURL;
        this.username = username;
        this.secret = secret;
    }

    public String getServerURL() {
        return serverURL;
    }

    public void setServerURL(String serverURL) {
        this.serverURL = serverURL;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }
}
