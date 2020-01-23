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
package io.hyscale.generator.services.utils;

import io.kubernetes.client.models.V1VolumeMount;

public class VolumeMountsUtil {

    public static V1VolumeMount buildForProps(String propsVolumePath, String name) {
        V1VolumeMount volumeMount = new V1VolumeMount();
        volumeMount.setName(name);
        volumeMount.setMountPath(propsVolumePath);
        volumeMount.setReadOnly(true);
        return volumeMount;
    }

    public static V1VolumeMount buildForSecrets(String secretsVolumePath, String name) {
        V1VolumeMount volumeMount = new V1VolumeMount();
        volumeMount.setName(name);
        volumeMount.setMountPath(secretsVolumePath);
        volumeMount.setReadOnly(true);
        return volumeMount;
    }
}
