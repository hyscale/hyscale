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
package io.hyscale.deployer.services.model;

import java.util.HashMap;
import java.util.Map;

public enum PodExitStatus {
    
    SIGKILL(137, "SIGKILL"), SIGTERM(143, "SIGTERM"), FAILURE(1, "FAILURE");
    
    private static final Map<Integer, PodExitStatus> statusCodeVsMessage = new HashMap<>();

    static {
        for (PodExitStatus each : PodExitStatus.values()) {
            statusCodeVsMessage.put(each.getCode(), each);
        }
    }

    private Integer code = null;
    private String signal;

    private PodExitStatus(Integer code, String signal) {
        this.code = code;
        this.signal = signal;
    }

    public String getSignal() {
        return signal;
    }

    public Integer getCode() {
        return code;
    }

    public Integer getStatusCode() {
        return this.code;
    }

    public static PodExitStatus fromCode(Integer code) {
        return statusCodeVsMessage.get(code);
    }
}
