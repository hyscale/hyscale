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
package io.hyscale.controller.builder;

import io.hyscale.commons.constants.ToolConstants;
import io.hyscale.commons.models.Auth;
import io.hyscale.commons.models.CredsStoreEntity;
import io.hyscale.commons.models.DockerCredHelper;
import io.hyscale.commons.models.ImageRegistry;
import org.apache.commons.lang3.StringUtils;

import java.util.Base64;
import java.util.Map;

/**Provides ImageRegistry details*/
public class ImageRegistryBuilder {

    private String registry;

    public ImageRegistryBuilder(String registry) {
        this.registry = registry;
    }

    /**returns ImageRegistry from docker credHelper from local docker config if present,else returns null.
     *
     * @param dockerCredHelper
     * @return ImageRegistry
     */
    public ImageRegistry from(DockerCredHelper dockerCredHelper) {
        if (dockerCredHelper == null) {
            return null;
        }
        return from(dockerCredHelper.getCredentials(registry));
    }

    private ImageRegistry from(CredsStoreEntity credsStoreEntity) {
        if (credsStoreEntity == null) {
            return null;
        }
        return new ImageRegistry(credsStoreEntity.getUsername(), credsStoreEntity.getSecret(), credsStoreEntity.getServerURL());
    }

    /**
     * returns ImageRegistry from Auths of local docker config if found else returns null.
     *
     * @param auths
     * @return ImageRegistry
     */
    public ImageRegistry from(Map<String, Auth> auths) {
        Auth auth = auths != null && auths.containsKey(registry) ? auths.get(registry) : null;
        if (auth == null) {
            return null;
        }
        String encodedAuth = auth.getAuth();
        if (StringUtils.isBlank(encodedAuth)) {
            return null;
        }

        String decodedAuth = new String(Base64.getDecoder().decode(encodedAuth));
        String[] authArray = decodedAuth.split(ToolConstants.COLON);
        ImageRegistry imageRegistry = new ImageRegistry();
        imageRegistry.setUrl(registry);
        imageRegistry.setUserName(authArray[0]);
        imageRegistry.setPassword(authArray[1]);
        return imageRegistry;
    }

}
