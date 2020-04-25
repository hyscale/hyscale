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
package io.hyscale.controller.profile;

import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.controller.commands.args.ProfileLocator;
import io.hyscale.controller.commands.input.ProfileArg;
import io.hyscale.controller.model.EffectiveServiceSpec;
import io.hyscale.controller.provider.EffectiveServiceSpecProvider;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

/*
   This class will process the service spec files based on profile to
   build EffectiveServiceSpec
 */
@Component
public class ServiceSpecProcessor {

    @Autowired
    private ProfileLocator profileLocator;

    @Autowired
    private ProfileSpecProcessor profileSpecProcessor;

    @Autowired
    private EffectiveServiceSpecProvider effectiveServiceSpecProvider;

    public List<EffectiveServiceSpec> process(ProfileArg profileArg, List<File> serviceSpecsFiles) throws HyscaleException {
        List<EffectiveServiceSpec> effectiveServiceSpecs = null;
        if (profileArg != null) {

            effectiveServiceSpecs = effectiveServiceSpecProvider.getEffectiveServiceSpec(
                    profileSpecProcessor.process(profileArg, serviceSpecsFiles));
        } else {
            effectiveServiceSpecs = effectiveServiceSpecProvider.getEffectiveServiceSpec(serviceSpecsFiles, null);
        }
        return effectiveServiceSpecs;
    }
}
