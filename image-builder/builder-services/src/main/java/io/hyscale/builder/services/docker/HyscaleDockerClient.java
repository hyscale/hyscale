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
package io.hyscale.builder.services.docker;

import java.util.List;
import java.util.Map;

import io.hyscale.builder.core.models.DockerImage;
import io.hyscale.commons.exception.HyscaleException;
import io.hyscale.commons.models.ImageRegistry;
import io.hyscale.servicespec.commons.model.service.Dockerfile;
import io.hyscale.servicespec.commons.model.service.Image;

/**
 * Interface to provide docker related functionality
 * Implementation of this interface provides different
 * docker clients such as command based, REST client among others
 *
 */
public interface HyscaleDockerClient {
    
    /**
     * @return true if manual login is required for the client
     */
    boolean isLoginRequired();
    
    /**
     * @return true if clean up temporary files are required
     */
    default boolean isCleanUpRequired(){
        return false;
    }
    /**
     * @return true if docker is running, else false
     */
    boolean isDockerRunning();

    /**
     * @return true if docker is installed, else false
     */
    boolean checkForDocker();
    
    /**
     * Returns an ordered list of image Ids based of image name and label.
     * In case image name is not provided, it returns all the image Ids available.
     * @param imageName
     * @param label
     * @return list of imageIds
     * @throws HyscaleException
     */
    List<String> getImageIds(String imageName, Map<String, String> label) throws HyscaleException;
    
    /**
     * Delete passed image Ids
     * Images that cannot be deleted are ignored
     * @param imageIds
     * @param force Use force flag in docker
     * @throws HyscaleException
     */
    void deleteImages(List<String> imageIds, boolean force) throws HyscaleException;
    
    /**
     * Delete passed image Id, ignore if image cannot be deleted
     * @param imageId
     * @param force Use force flag in docker
     * @throws HyscaleException
     */
    void deleteImage(String imageId, boolean force) throws HyscaleException;

    /**
     * Build image based on dockerfile
     * @param dockerfile
     * @param imageName
     * @param tag
     * @param registryMap
     * @param logfile
     * @param isVerbose
     * @return {@link DockerImage} built image details
     * @throws HyscaleException
     */
    DockerImage build(Dockerfile dockerfile, String imageName, String tag, Map<String, ImageRegistry> registryMap,
            String logfile, boolean isVerbose) throws HyscaleException;

    /**
     * Push image to registry
     * @param image
     * @param imageRegistry
     * @param logfile
     * @param isVerbose
     * @return ShaSum of pushed image
     * @throws HyscaleException
     */
    String push(Image image, ImageRegistry imageRegistry, String logfile, boolean isVerbose) throws HyscaleException;

    /**
     * Pull image from registry
     * @param image
     * @param imageRegistry
     * @throws HyscaleException
     */
    void pull(String image, ImageRegistry imageRegistry) throws HyscaleException;

    /**
     * Tag image based on destination
     * @param source
     * @param dest
     * @throws HyscaleException
     */
    void tag(String source, Image dest) throws HyscaleException;
    
    /**
     * Login to registry
     * @param registry
     * @throws HyscaleException
     */
    default void login(ImageRegistry registry) throws HyscaleException {}
}
