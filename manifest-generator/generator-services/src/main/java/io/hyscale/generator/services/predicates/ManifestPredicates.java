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
package io.hyscale.generator.services.predicates;

import com.fasterxml.jackson.core.type.TypeReference;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.generator.services.provider.PropsProvider;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.*;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.function.Predicate;

public class ManifestPredicates {

    public static Predicate<ServiceSpec> getVolumesPredicate() {
        return serviceSpec -> {
            TypeReference<List<Volume>> volumesList = new TypeReference<List<Volume>>() {
            };
            List<Volume> volumes = null;
            try {
                volumes = serviceSpec.get("volumes", volumesList);
            } catch (HyscaleException e) {
                return false;
            }
            if (volumes != null && !volumes.isEmpty()) {
                return true;
            }
            return false;
        };
    }

    public static Predicate<ServiceSpec> getAgentsPredicate() {
        return serviceSpec -> {
            TypeReference<List<Agent>> agentsList = new TypeReference<List<Agent>>() {
            };
            List<Agent> agents = null;
            try {
                agents = serviceSpec.get(HyscaleSpecFields.agents, agentsList);
            } catch (HyscaleException e) {
                return false;
            }
            if (agents != null && !agents.isEmpty()) {
                return true;
            }
            return false;
        };
    }

    public static Predicate<ServiceSpec> getPortsPredicate() {
        return serviceSpec -> {
            TypeReference<List<Port>> portsList = new TypeReference<List<Port>>() {
            };
            List<Port> portList = null;
            try {
                portList = serviceSpec.get(HyscaleSpecFields.ports, portsList);
            } catch (HyscaleException e) {
                return false;
            }
            if (portList != null && !portList.isEmpty()) {
                return true;
            }
            return false;
        };
    }

    public static Predicate<ServiceSpec> getPropsPredicate() {
        return serviceSpec -> {
            Props props = null;
            try {
                props = PropsProvider.getProps(serviceSpec);
            } catch (HyscaleException e) {
                return false;
            }
            if (props != null && props.getProps() != null && !props.getProps().isEmpty()) {
                return true;
            }
            return false;
        };
    }

    public static Predicate<ServiceSpec> getSecretsPredicate() {
        return serviceSpec -> {
            //	Secrets secrets = null;
            Secrets secrets = null;
            try {
                secrets = serviceSpec.get(HyscaleSpecFields.secrets, Secrets.class);
            } catch (HyscaleException e) {
                return false;
            }
            if (secrets == null) {
                return false;
            }
            if (secrets.getType() == SecretType.MAP) {
                MapBasedSecrets mapBasedSecrets = (MapBasedSecrets) secrets;
                if (mapBasedSecrets != null && !mapBasedSecrets.isEmpty()) {
                    return true;
                }
            }
            if (secrets.getType() == SecretType.SET) {
                SetBasedSecrets setBasedSecrets = (SetBasedSecrets) secrets;
                if (setBasedSecrets != null && !setBasedSecrets.isEmpty()) {
                    return false;
                }
            }
            return false;
        };
    }

    public static Predicate<ServiceSpec> getSecretsEnvPredicate() {
        return serviceSpec -> {
            Secrets secrets = null;
            try {
                secrets = serviceSpec.get(HyscaleSpecFields.secrets, Secrets.class);
            } catch (HyscaleException e) {
                return false;
            }
            if (secrets == null) {
                return false;
            }
            if (secrets.getType() == SecretType.MAP) {
                MapBasedSecrets mapBasedSecrets = (MapBasedSecrets) secrets;
                if (mapBasedSecrets != null && !mapBasedSecrets.isEmpty()) {
                    return true;
                }
            }
            if (secrets.getType() == SecretType.SET) {
                SetBasedSecrets setBasedSecrets = (SetBasedSecrets) secrets;
                if (setBasedSecrets != null && !setBasedSecrets.isEmpty()) {
                    return true;
                }
            }
            return false;
        };
    }

    public static Predicate<ServiceSpec> haveConfigmapVolume() {
        return serviceSpec -> {
            String propsVolumePath = null;
            try {
                propsVolumePath = serviceSpec.get(HyscaleSpecFields.propsVolumePath, String.class);
            } catch (HyscaleException e) {
                return false;
            }
            if (!StringUtils.isBlank(propsVolumePath)) {
                return true;
            }
            return false;
        };
    }

    public static Predicate<ServiceSpec> haveSecretsVolume() {
        return serviceSpec -> {
            String secretsVolumePath = null;
            try {
                secretsVolumePath = serviceSpec.get(HyscaleSpecFields.secretsVolumePath, String.class);
            } catch (HyscaleException e) {
                return false;
            }
            if (!StringUtils.isBlank(secretsVolumePath)) {
                return true;
            }
            return false;
        };
    }

    public static Predicate<ServiceSpec> isAutoScalingEnabled() {
        return serviceSpec -> {
            Replicas replicas = null;
            try {
                replicas = serviceSpec.get(HyscaleSpecFields.replicas, Replicas.class);
            } catch (HyscaleException e) {
                return false;
            }
            if (replicas != null && replicas.getMax() > 0 && StringUtils.isNotBlank(replicas.getCpuThreshold())) {
                return true;
            }
            return false;
        };
    }

}
