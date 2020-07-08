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
package io.hyscale.generator.services.model;

import java.util.HashMap;
import java.util.Map;

/**
 * The class holds pod checksum related information
 * The data is stored after config map and secret are processed for manifest
 * As such they are not in their original form
 * example: prop data will contain serialized data and binary data as in k8s resource
 *
 */
public class PodChecksum {

    private Prop prop;
    private String secret;
    private Map<String, Prop> agentProps;
    private Map<String, String> agentSecrets;

    public Prop getProp() {
        return prop;
    }

    public void setProp(Prop prop) {
        this.prop = prop;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public Map<String, Prop> getAgentProps() {
        return agentProps;
    }

    public void setAgentProps(Map<String, Prop> agentProps) {
        this.agentProps = agentProps;
    }

    public void addAgentProp(String agent, Prop prop) {
        if (this.agentProps == null) {
            this.agentProps = new HashMap<String, PodChecksum.Prop>();
        }
        agentProps.put(agent, prop);
    }

    public Map<String, String> getAgentSecrets() {
        return agentSecrets;
    }

    public void setAgentSecrets(Map<String, String> agentSecrets) {
        this.agentSecrets = agentSecrets;
    }

    public void addAgentSecret(String agent, String secret) {
        if (this.agentSecrets == null) {
            this.agentSecrets = new HashMap<String, String>();
        }
        agentSecrets.put(agent, secret);
    }

    
    @Override
    public String toString() {
        return "PodChecksum [" + (prop != null ? "prop=" + prop + ", " : "")
                + (secret != null ? "secret=" + secret + ", " : "")
                + (agentProps != null && !agentProps.isEmpty()? "agentProps=" + agentProps + ", " : "")
                + (agentSecrets != null && !agentSecrets.isEmpty()? "agentSecrets=" + agentSecrets : "") + "]";
    }


    public class Prop {
        private String data;
        private String binaryData;

        public String getData() {
            return data;
        }

        public void setData(String data) {
            this.data = data;
        }

        public String getBinaryData() {
            return binaryData;
        }

        public void setBinaryData(String binaryData) {
            this.binaryData = binaryData;
        }
        
        @Override
        public String toString() {
            return "Prop [" + (data != null ? "data=" + data + ", " : "")
                    + (binaryData != null ? "binaryData=" + binaryData : "") + "]";
        }
    }
}
