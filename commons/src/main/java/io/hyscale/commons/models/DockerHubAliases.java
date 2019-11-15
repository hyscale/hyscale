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


import java.util.Collections;
import java.util.List;

/** Provides aliases for docker hub registry */
public class DockerHubAliases {
    private static final List<String> DOCKER_REGISTRY_ALIASES = Collections.unmodifiableList(List.of("index.docker.io", "registry-1.docker.io", "docker.io"));
    private static final List<String> REGISTRY_HOST_LIST = Collections.unmodifiableList(List.of("index.docker.io/v1/"));

    /**
     * gets docker registry aliases if private docker hub registry given,else returns registry itself.
     *
     * @param registry
     * @return list of registry aliases
     */
    public static List<String> getDockerRegistryAliases(String registry) {
        if (DOCKER_REGISTRY_ALIASES.contains(registry)) {
            return REGISTRY_HOST_LIST;
        } else
            return List.of(registry);
    }

}
