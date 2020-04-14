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
package io.hyscale.servicespec.commons.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.hyscale.commons.constants.ToolConstants;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.servicespec.commons.fields.HyscaleSpecFields;
import io.hyscale.servicespec.commons.model.service.BuildSpecImage;
import io.hyscale.servicespec.commons.model.service.DockerBuildImage;
import io.hyscale.servicespec.commons.model.service.Image;
import io.hyscale.servicespec.commons.model.service.ServiceSpec;


public class ImageUtil {
	private static final Logger logger = LoggerFactory.getLogger(ImageUtil.class);


    private static final String DELIMITER = "/";

    /**
     * Get complete image name from service spec
     * @param serviceSpec
     * @return image complete name
     * registry url + "/" + imageName:tag
     * 1. registry url is null - imageName:tag
     * 2. registry url and tag not present - imageName
     * @throws HyscaleException
     */
    public static String getImage(ServiceSpec serviceSpec) throws HyscaleException {
        if (serviceSpec == null) {
            return null;
        }
        Image image = serviceSpec.get(HyscaleSpecFields.image, Image.class);
        if (image == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(getImageWithoutTag(image));
        String tag = image.getTag();
        if (StringUtils.isNotBlank(tag)) {
            sb.append(ToolConstants.COLON);
            sb.append(tag);
        }
        return sb.toString();
    }

    public static String getImageWithoutTag(ServiceSpec serviceSpec) throws HyscaleException {
        if (serviceSpec == null) {
            return null;
        }
        Image image = serviceSpec.get(HyscaleSpecFields.image, Image.class);
        if (image == null) {
            return null;
        }
        return getImageWithoutTag(image);
    }


    private static String getImageWithoutTag(Image image) {
        String registryUrl = image.getRegistry();
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotBlank(registryUrl)) {
            sb.append(registryUrl);
            sb.append(DELIMITER);
        }
        String imageName = image.getName();
        if (StringUtils.isNotBlank(imageName)) {
            sb.append(imageName);
        }
        return sb.toString();
    }

    /**
     * Gets complete image name which includes registry url, image name and digest.
     *
     * @param serviceSpec
     * @param digest latest digest of the image in service spec
     * @return digest image tagged with digest
     * 1.digest is null - registry url + "/" + imageName
     * 2.digest present -registry url + "/" + imageName+"@"+digest
     * @throws HyscaleException
     */

    public static String getImageWithDigest(ServiceSpec serviceSpec, String digest) throws HyscaleException {
        if (serviceSpec == null) {
            return null;
        }
        Image image = serviceSpec.get(HyscaleSpecFields.image, Image.class);
        if (image == null) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getImageWithoutTag(image));
        if (StringUtils.isNotBlank(digest)) {
            stringBuilder.append(ToolConstants.AT_SIGN).append(digest);
        }
        return stringBuilder.toString();
    }

	public static boolean isImageBuildPushRequired(ServiceSpec serviceSpec) {
		Image image = null;
		try {
			image = serviceSpec.get(HyscaleSpecFields.image, Image.class);
		} catch (HyscaleException e) {
			logger.info("Error while fetching buildSpec and registryUrl from serviceSpec ");
		}
		if (image != null && (image instanceof BuildSpecImage) || (image instanceof DockerBuildImage)) {
			return true;
		} else {
			return false;
		}
	}
}
