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
package io.hyscale.controller.hooks;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.hyscale.builder.services.docker.HyscaleDockerClient;
import io.hyscale.commons.component.InvokerHook;
import io.hyscale.commons.config.SetupConfig;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.io.HyscaleFilesUtil;
import io.hyscale.controller.model.WorkflowContext;

/**
 * Hook to clean up temporary config files used by hyscale
 * @author tushar
 *
 */
@Component
public class ConfigCleanUpHook implements InvokerHook<WorkflowContext> {
    
    @Autowired
    private HyscaleDockerClient hyscaleDockerClient;

    @Override
    public void preHook(WorkflowContext context) throws HyscaleException {
        // No operation
    }

    @Override
    public void postHook(WorkflowContext context) throws HyscaleException {
        if (hyscaleDockerClient.cleanUp()) {
            // Clean up temp config
            HyscaleFilesUtil.deleteDirectory(SetupConfig.getTemporaryDockerConfigDir());
        }
    }
    
    @Override
    public void onError(WorkflowContext context, Throwable th) {
        // No operation
    }

}
