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

/** Template of credsStore entity.
 *
 *<p>Example:
 *<pre>{
 * {
 *   "ServerURL": "registry",
 *   "Username": "username",
 *   "Secret": "password"
 * }
 *}</pre>
 * Provides credentials from the credsStore.
 */
public class CredsStoreEntity {
    private String ServerURL;
    private String Username;
    private String Secret;

    public CredsStoreEntity(String serverURL, String username, String secret) {
        ServerURL = serverURL;
        this.Username = username;
        this.Secret = secret;
    }

    public String getServerURL() {
        return ServerURL;
    }

    public void setServerURL(String serverURL) {
        ServerURL = serverURL;
    }

    public String getUsername() {
        return Username;
    }

    public void setUsername(String username) {
        this.Username = username;
    }

    public String getSecret() {
        return Secret;
    }

    public void setSecret(String secret) {
        this.Secret = secret;
    }

}
