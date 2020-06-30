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
package io.hyscale.deployer.core.model;

import org.apache.commons.lang3.StringUtils;

/**
 * status information of a service after deploying it.
 */
public class ServiceStatus {
    private Integer exitCode;
    private String message;
    private String name;
    private String k8sError;

    public Integer getExitCode() {
        return exitCode;
    }

    public void setExitCode(Integer exitCode) {
        this.exitCode = exitCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getK8sError() {
        return k8sError;
    }

    public void setK8sError(String k8sError) {
        this.k8sError = k8sError;
    }

    @Override
    public String toString() {
        if (StringUtils.isEmpty(name)) {
            return "";
        }
        StringBuilder sb = new StringBuilder("[");
        sb.append("name: ").append(name);
        if (!StringUtils.isEmpty(message)) {
            sb.append(',').append("message: ").append(message);
        }
        if (!StringUtils.isEmpty(k8sError)) {
            sb.append(',').append("k8sError: ").append(k8sError);
        }
        if (exitCode != null) {
            sb.append(',').append("exitCode: ").append(exitCode);
        }
        sb.append(']');
        return sb.toString();
    }
}
