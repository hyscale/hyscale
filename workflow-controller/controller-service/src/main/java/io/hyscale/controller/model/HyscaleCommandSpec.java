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
package io.hyscale.controller.model;

import io.hyscale.deployer.core.model.AppMetadata;

public class HyscaleCommandSpec {

    private AppMetadata appMetadata;
    private PreProcessingArgs preProcessingArgs;
    private PostProcessingArgs postProcessingArgs;

    public AppMetadata getAppMetadata() {
        return appMetadata;
    }

    public void setAppMetadata(AppMetadata appMetadata) {
        this.appMetadata = appMetadata;
    }

    public PreProcessingArgs getPreProcessingArgs() {
        return preProcessingArgs;
    }

    public void setPreProcessingArgs(PreProcessingArgs preProcessingArgs) {
        this.preProcessingArgs = preProcessingArgs;
    }

    public PostProcessingArgs getPostProcessingArgs() {
        return postProcessingArgs;
    }

    public void setPostProcessingArgs(PostProcessingArgs postProcessingArgs) {
        this.postProcessingArgs = postProcessingArgs;
    }

}
